require('dotenv').config();
const express = require('express');
const admin = require('firebase-admin');
const axios = require('axios');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const PptxGenJS = require('pptxgenjs');
const {
    DEFAULT_NEMOTRON_MODEL,
    AVAILABLE_MODELS,
    chatCompletion,
    streamCompletion,
    mapErrorToUserMessage
} = require('./aiGateway');

// ─── Initialize Firebase Admin (Optional for local dev) ───
let db = null;
try {
  const serviceAccount = require("./serviceAccountKey.json");
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://techeagle-5f574-default-rtdb.asia-southeast1.firebasedatabase.app"
  });
  db = admin.firestore();
  console.log("[Firebase] Admin initialized successfully with Firestore.");
} catch (err) {
  console.warn("[Firebase] Admin not initialized (serviceAccountKey.json missing). Running in local mode with memory rate limiter.");
}

const app = express();
app.use(express.json({ limit: '10mb' }));
app.use(cors());

// Serve Events Hub Organizer Web Portal
app.use('/events-admin', express.static(path.join(__dirname, '../events-admin')));

// In-memory rate limiting fallback
const memoryUserLimits = new Map();

// ─── MIDDLEWARE: AUTH & RATE LIMITING ───
const authenticateAndRateLimit = async (req, res, next) => {
  const authHeader = req.headers.authorization;
  const clientUserId = req.headers['x-user-id'] || req.ip || 'anonymous';
  let userId = clientUserId;

  if (authHeader && authHeader.startsWith('Bearer ')) {
    const idToken = authHeader.split('Bearer ')[1];
    if (db && admin.apps.length > 0) {
      try {
        const decodedToken = await admin.auth().verifyIdToken(idToken);
        userId = decodedToken.uid;
      } catch (authErr) {
        // In local mode if verification fails, fallback to client-provided ID or continue
        console.warn('[Auth] Token verification failed:', authErr.message);
      }
    }
  }

  req.userId = userId;

  // Rate limiting check
  const now = Date.now();
  const oneDay = 24 * 60 * 60 * 1000;

  try {
    if (db) {
      const userRef = db.collection('user_limits').doc(userId);
      const doc = await userRef.get();
      let userData = {
        requests_today: 0,
        last_reset_time: now
      };

      if (doc.exists) {
        userData = doc.data();
        if (now - (userData.last_reset_time || 0) > oneDay) {
          userData.requests_today = 0;
          userData.last_reset_time = now;
        }
      }

      const dailyLimit = Number(process.env.RATE_LIMIT_DAILY) || 80;
      if (userData.requests_today >= dailyLimit) {
        return res.status(429).json({
          error: `Daily AI request limit reached (${dailyLimit}/day). Please try again tomorrow.`
        });
      }

      userData.requests_today += 1;
      await userRef.set(userData, { merge: true });
    } else {
      // Memory rate limiter
      let limitRecord = memoryUserLimits.get(userId);
      if (!limitRecord || now - limitRecord.lastReset > oneDay) {
        limitRecord = { count: 0, lastReset: now };
      }
      const dailyLimit = Number(process.env.RATE_LIMIT_DAILY) || 80;
      if (limitRecord.count >= dailyLimit) {
        return res.status(429).json({
          error: `Daily AI request limit reached (${dailyLimit}/day). Please try again tomorrow.`
        });
      }
      limitRecord.count += 1;
      memoryUserLimits.set(userId, limitRecord);
    }
    next();
  } catch (rateErr) {
    console.error('[RateLimit Error]:', rateErr.message);
    next(); // don't block user on rate-limiter db glitch
  }
};

// ─── HEALTH & METRICS ENDPOINT ───
app.get('/api/health', (req, res) => {
  const hasOpenRouter = Boolean(process.env.OPENROUTER_API_KEY && process.env.OPENROUTER_API_KEY !== 'your_openrouter_api_key_here');
  const hasGroq = Boolean(process.env.GROQ_API_KEY && process.env.GROQ_API_KEY !== 'your_groq_api_key_here');
  const hasGemini = Boolean(process.env.GEMINI_API_KEY && process.env.GEMINI_API_KEY !== 'YOUR_PRIMARY_GEMINI_KEY');

  res.json({
    status: 'ok',
    version: '2.0.0',
    primaryModel: DEFAULT_NEMOTRON_MODEL,
    providers: {
      openrouter: hasOpenRouter ? 'configured' : 'missing_key',
      groq_fallback: hasGroq ? 'configured' : 'disabled',
      gemini_fallback: hasGemini ? 'configured' : 'disabled'
    },
    uptime: Math.floor(process.uptime()),
    timestamp: new Date().toISOString()
  });
});

// ─── AVAILABLE MODELS ENDPOINT ───
app.get('/api/models', (req, res) => {
  res.json({
    primary: DEFAULT_NEMOTRON_MODEL,
    models: AVAILABLE_MODELS
  });
});

// ─── CHAT STREAMING ENDPOINT (SSE) ───
app.post('/api/chat/stream', authenticateAndRateLimit, async (req, res) => {
  const { messages, message, model, temperature, max_tokens } = req.body;

  let conversationMessages = messages;
  if (!conversationMessages && message) {
    conversationMessages = [{ role: 'user', content: String(message) }];
  }

  if (!conversationMessages || !Array.isArray(conversationMessages) || conversationMessages.length === 0) {
    return res.status(400).json({ error: 'Request body must include a valid messages array.' });
  }

  await streamCompletion({
    messages: conversationMessages,
    model: model || DEFAULT_NEMOTRON_MODEL,
    temperature: temperature ?? 0.7,
    max_tokens: max_tokens ?? 4096,
    res,
    clientReq: req
  });
});

// ─── CHAT NON-STREAMING ENDPOINT ───
app.post('/api/chat', authenticateAndRateLimit, async (req, res) => {
  const { message, messages, model, temperature, max_tokens } = req.body;

  let conversationMessages = messages;
  if (!conversationMessages && message) {
    conversationMessages = [{ role: 'user', content: String(message) }];
  }

  if (!conversationMessages || !Array.isArray(conversationMessages) || conversationMessages.length === 0) {
    return res.status(400).json({ error: 'Request body must include a valid messages array or message string.' });
  }

  try {
    const result = await chatCompletion({
      messages: conversationMessages,
      model: model || DEFAULT_NEMOTRON_MODEL,
      temperature: temperature ?? 0.7,
      max_tokens: max_tokens ?? 4096
    });

    res.json({
      response: result.response,
      model: result.model,
      provider: result.provider,
      usage: result.usage
    });
  } catch (error) {
    console.error('[API /api/chat Error]:', error.message);
    const userMessage = mapErrorToUserMessage(error, 'OpenRouter');
    res.status(error.response?.status || 500).json({ error: userMessage });
  }
});

// ─── SPECIALIZED AI GENERATION ENDPOINTS ───
app.post('/api/text-generate', authenticateAndRateLimit, async (req, res) => {
  const { prompt } = req.body;
  if (!prompt) return res.status(400).json({ error: 'Prompt is required.' });

  try {
    const result = await chatCompletion({
      messages: [
        { role: 'system', content: 'You are an expert professional writer. Produce high-quality, formatted text.' },
        { role: 'user', content: prompt }
      ]
    });
    res.json({ text: result.response });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/code-generate', authenticateAndRateLimit, async (req, res) => {
  const { prompt } = req.body;
  if (!prompt) return res.status(400).json({ error: 'Prompt is required.' });

  try {
    const result = await chatCompletion({
      messages: [
        { role: 'system', content: 'You are an elite software architect and programmer. Output production-ready, clean code using markdown code blocks with language tags. Keep explanations minimal and clear.' },
        { role: 'user', content: `Generate code for: ${prompt}` }
      ]
    });
    res.json({ code: result.response });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/quiz-generate', authenticateAndRateLimit, async (req, res) => {
  const { topic } = req.body;
  if (!topic) return res.status(400).json({ error: 'Topic is required.' });

  try {
    const result = await chatCompletion({
      messages: [
        { role: 'system', content: 'Create an engaging 5-question multiple choice quiz with questions, options (A, B, C, D), and answers with brief explanations.' },
        { role: 'user', content: `Create a quiz about: ${topic}` }
      ]
    });
    res.json({ quiz: result.response });
  } catch (error) {
    res.status(500).json({ error: error.message });
  }
});

app.post('/api/image-generate', authenticateAndRateLimit, async (req, res) => {
  const { prompt } = req.body;
  res.json({ imageUrl: `https://image.pollinations.ai/prompt/${encodeURIComponent(prompt || 'learning companion')}?width=512&height=512&nologo=true` });
});

// ─── PPT GENERATION ENDPOINTS (PRESERVED) ───
const pptOutputDir = path.join(__dirname, 'generated_ppts');
if (!fs.existsSync(pptOutputDir)) {
  fs.mkdirSync(pptOutputDir, { recursive: true });
}

app.use('/downloads', express.static(pptOutputDir));

app.post('/api/ppt-generate', authenticateAndRateLimit, async (req, res) => {
  const { topic } = req.body;
  if (!topic) {
    return res.status(400).json({ error: "Topic is required." });
  }

  try {
    const aiPrompt = `Create a professional PowerPoint presentation outline about: "${topic}".
Return ONLY a valid JSON array of slide objects. No markdown, no explanation, no code fences.
Each object must have: "title" (string) and "bullets" (array of strings, 3-5 items each).
Include 8-12 slides: a title slide, content slides, and a Thank You slide.
Example: [{"title":"Introduction","bullets":["Point 1","Point 2","Point 3"]}]`;

    const aiResult = await chatCompletion({
      messages: [{ role: 'user', content: aiPrompt }],
      temperature: 0.5
    });

    let slides;
    try {
      const jsonMatch = aiResult.response.match(/\[[\s\S]*\]/);
      slides = jsonMatch ? JSON.parse(jsonMatch[0]) : JSON.parse(aiResult.response);
    } catch {
      slides = [
        { title: topic, bullets: ["AI-Generated Presentation by AI ZAYA"] },
        { title: "Overview", bullets: ["Generated with NVIDIA Nemotron 3 Ultra", `Topic: ${topic}`] },
        { title: "Key Highlights", bullets: ["Modern content analysis", "Comprehensive coverage", "Actionable insights"] },
        { title: "Thank You", bullets: ["Questions & Discussion"] }
      ];
    }

    const pptx = new PptxGenJS();
    pptx.author = 'AI ZAYA';
    pptx.title = topic;
    pptx.layout = 'LAYOUT_WIDE';

    slides.forEach((slideData, idx) => {
      const slide = pptx.addSlide();
      slide.addText(slideData.title || `Slide ${idx + 1}`, {
        x: 0.8, y: 0.5, w: 11.5, h: 1.0,
        fontSize: 28, bold: true, color: '0D1B2A'
      });
      if (slideData.bullets && slideData.bullets.length > 0) {
        const bulletText = slideData.bullets.map(b => ({
          text: b,
          options: { fontSize: 16, color: '333333', bullet: true, breakLine: true }
        }));
        slide.addText(bulletText, { x: 1.0, y: 1.8, w: 11.0, h: 4.8 });
      }
    });

    const timestamp = Date.now();
    const fileName = `presentation_${timestamp}.pptx`;
    const filePath = path.join(pptOutputDir, fileName);
    await pptx.writeFile({ fileName: filePath });

    const baseUrl = `${req.protocol}://${req.get('host')}`;
    res.json({ fileUrl: `${baseUrl}/downloads/${fileName}`, fileName });
  } catch (error) {
    console.error("PPT generation error:", error);
    res.status(500).json({ error: "Failed to generate presentation: " + error.message });
  }
});

app.post('/api/ppt-create', async (req, res) => {
  const { topic, slides } = req.body;
  if (!topic || !slides || !Array.isArray(slides) || slides.length === 0) {
    return res.status(400).json({ error: "Topic and slides array are required." });
  }

  try {
    const pptx = new PptxGenJS();
    pptx.author = 'AI ZAYA';
    pptx.title = topic;
    pptx.layout = 'LAYOUT_WIDE';

    const COLORS = {
      darkNavy: '0D1B2A',
      accentBlue: '2196F3',
      accentCyan: '00BCD4',
      accentGold: 'FFB300',
      white: 'FFFFFF',
      offWhite: 'F8FAFC',
      textDark: '212121',
      mediumGray: '607080'
    };

    slides.forEach((slideData, index) => {
      const slide = pptx.addSlide();
      const sTitle = (slideData.title || '').trim();
      const sBullets = (slideData.bullets || []).map(b => (b || '').trim()).filter(b => b.length > 0);
      const isFirst = index === 0;

      if (isFirst) {
        slide.background = { fill: COLORS.darkNavy };
        slide.addText(sTitle || topic, {
          x: 1.0, y: 2.0, w: 11.3, h: 2.0,
          fontSize: 38, bold: true, color: COLORS.white,
          align: 'center'
        });
        if (sBullets.length > 0) {
          slide.addText(sBullets.join(' | '), {
            x: 1.5, y: 4.2, w: 10.3, h: 1.0,
            fontSize: 18, color: COLORS.accentCyan,
            align: 'center', italic: true
          });
        }
      } else {
        slide.background = { fill: COLORS.offWhite };
        slide.addText(sTitle, {
          x: 0.8, y: 0.3, w: 11.0, h: 0.8,
          fontSize: 26, bold: true, color: COLORS.darkNavy
        });
        if (sBullets.length > 0) {
          const bulletRows = sBullets.map(b => ({
            text: b,
            options: { fontSize: 16, color: COLORS.textDark, bullet: true, breakLine: true }
          }));
          slide.addText(bulletRows, { x: 1.0, y: 1.5, w: 11.0, h: 5.0 });
        }
      }
    });

    const timestamp = Date.now();
    const fileName = `presentation_${timestamp}.pptx`;
    const filePath = path.join(pptOutputDir, fileName);
    await pptx.writeFile({ fileName: filePath });

    const baseUrl = `${req.protocol}://${req.get('host')}`;
    res.json({ fileUrl: `${baseUrl}/downloads/${fileName}`, fileName });
  } catch (error) {
    console.error("PPT create error:", error);
    res.status(500).json({ error: "Failed to create presentation: " + error.message });
  }
});

// ─── START SERVER ───
const PORT = process.env.PORT || 3000;
app.listen(PORT, '0.0.0.0', () => {
  const os = require('os');
  const nets = os.networkInterfaces();
  let localIp = 'localhost';
  for (const name of Object.keys(nets)) {
    for (const net of nets[name]) {
      if (net.family === 'IPv4' && !net.internal) {
        localIp = net.address;
        break;
      }
    }
  }
  console.log(`[AI ZAYA Gateway] Running on port ${PORT}`);
  console.log(`[AI ZAYA Gateway] Primary Model: ${DEFAULT_NEMOTRON_MODEL}`);
  console.log(`[AI ZAYA Gateway] Local URL:  http://localhost:${PORT}`);
  console.log(`[AI ZAYA Gateway] Device URL: http://${localIp}:${PORT}`);
});
