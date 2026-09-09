const express = require('express');
const admin = require('firebase-admin');
const axios = require('axios');
const cors = require('cors');
const fs = require('fs');
const path = require('path');
const PptxGenJS = require('pptxgenjs');

// Initialize Firebase Admin (optional — PPT creation works without it)
let db = null;
try {
  const serviceAccount = require("./serviceAccountKey.json");
  admin.initializeApp({
    credential: admin.credential.cert(serviceAccount),
    databaseURL: "https://your-project-id.firebaseio.com"
  });
  db = admin.firestore();
  console.log("Firebase Admin initialized successfully.");
} catch (err) {
  console.warn("Firebase Admin not initialized (serviceAccountKey.json missing). Auth-protected routes disabled.");
}

const app = express();
app.use(express.json({ limit: '10mb' }));
app.use(cors());

// AI API Keys (Stored safely on server)
const GEMINI_PRIMARY_KEY = "YOUR_PRIMARY_GEMINI_KEY";
const GEMINI_FALLBACK_KEY = "YOUR_FALLBACK_GEMINI_KEY"; // Different key or provider

// --- MIDDLEWARE: AUTH & RATE LIMITING ---
const authenticateAndRateLimit = async (req, res, next) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).send({ error: "Unauthorized" });
  }

  const idToken = authHeader.split('Bearer ')[1];
  try {
    const decodedToken = await admin.auth().verifyIdToken(idToken);
    const userId = decodedToken.uid;
    req.userId = userId;

    const userRef = db.collection('user_limits').doc(userId);
    const doc = await userRef.get();
    const now = Date.now();
    const oneDay = 24 * 60 * 60 * 1000;

    let userData = {
      requests_today: 0,
      image_requests_today: 0,
      last_reset_time: now
    };

    if (doc.exists) {
      userData = doc.data();
      if (now - userData.last_reset_time > oneDay) {
        userData.requests_today = 0;
        userData.image_requests_today = 0;
        userData.last_reset_time = now;
      }
    }

    const isImageRoute = req.path.includes('image-generate');
    if (isImageRoute) {
        if (userData.image_requests_today >= 5) {
            return res.status(429).send({ error: "Daily image limit reached. Try again tomorrow." });
        }
        userData.image_requests_today += 1;
    } else {
        if (userData.requests_today >= 20) {
            return res.status(429).send({ error: "Daily AI limit reached. Try again tomorrow." });
        }
        userData.requests_today += 1;
    }

    await userRef.set(userData);
    next();
  } catch (error) {
    res.status(401).send({ error: "Invalid Token" });
  }
};

// --- AI ROUTER WITH FALLBACK ---
async function callGemini(message, apiKey) {
    const url = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${apiKey}`;
    const response = await axios.post(url, {
        contents: [{ parts: [{ text: message }] }]
    });
    return response.data.candidates[0].content.parts[0].text;
}

const getAiResponseWithFallback = async (prompt) => {
    try {
        // Try Primary
        console.log("Calling Primary AI...");
        return await callGemini(prompt, GEMINI_PRIMARY_KEY);
    } catch (error) {
        console.error("Primary AI Failed, trying Fallback...");
        try {
            // Try Fallback
            return await callGemini(prompt, GEMINI_FALLBACK_KEY);
        } catch (fallbackError) {
            console.error("All AI providers failed.");
            throw new Error("AI services are currently unavailable. Please try again later.");
        }
    }
};

// --- ENDPOINTS ---

app.post('/api/chat', authenticateAndRateLimit, async (req, res) => {
  const { message } = req.body;
  try {
    const aiText = await getAiResponseWithFallback(message);
    res.send({ response: aiText });
  } catch (error) {
    res.status(500).send({ error: error.message });
  }
});

app.post('/api/text-generate', authenticateAndRateLimit, async (req, res) => {
    const { prompt } = req.body;
    try {
      const aiText = await getAiResponseWithFallback(`Act as a professional writer: ${prompt}`);
      res.send({ text: aiText });
    } catch (error) {
      res.status(500).send({ error: error.message });
    }
});

app.post('/api/code-generate', authenticateAndRateLimit, async (req, res) => {
    const { prompt } = req.body;
    try {
      const aiText = await getAiResponseWithFallback(`Generate code for: ${prompt}. Provide only the code block.`);
      res.send({ code: aiText });
    } catch (error) {
      res.status(500).send({ error: error.message });
    }
});

app.post('/api/quiz-generate', authenticateAndRateLimit, async (req, res) => {
    const { topic } = req.body;
    try {
      const aiText = await getAiResponseWithFallback(`Create a 5-question multiple choice quiz about: ${topic}.`);
      res.send({ quiz: aiText });
    } catch (error) {
      res.status(500).send({ error: error.message });
    }
});

app.post('/api/image-generate', authenticateAndRateLimit, async (req, res) => {
    const { prompt } = req.body;
    try {
        // Placeholder for Stable Diffusion / DALL-E
        // If primary image gen fails, you could route to a secondary service here
        res.send({ imageUrl: "https://via.placeholder.com/512?text=AI+Generated+Image" });
    } catch (error) {
        res.status(500).send({ error: "Image generation service is down." });
    }
});

// --- PPT Generation Endpoint ---
// Ensure output directory exists
const pptOutputDir = path.join(__dirname, 'generated_ppts');
if (!fs.existsSync(pptOutputDir)) {
    fs.mkdirSync(pptOutputDir, { recursive: true });
}

// Serve generated PPT files as static downloads
app.use('/downloads', express.static(pptOutputDir));

app.post('/api/ppt-generate', authenticateAndRateLimit, async (req, res) => {
    const { topic, outline } = req.body;
    if (!topic) {
        return res.status(400).send({ error: "Topic is required." });
    }

    try {
        // Generate structured slide content via AI
        const aiPrompt = `Create a professional PowerPoint presentation about: "${topic}".
Return ONLY a valid JSON array of slide objects. No markdown, no explanation, no code fences.
Each object must have: "title" (string) and "bullets" (array of strings, 3-5 items each).
Include 8-12 slides: a title slide (bullets = subtitle text as single item), content slides, and a "Thank You" slide.
Example format: [{"title":"Introduction","bullets":["Point 1","Point 2","Point 3"]}]`;

        const aiResponse = await getAiResponseWithFallback(aiPrompt);

        // Parse the AI response - extract JSON
        let slides;
        try {
            // Try to find JSON array in the response
            const jsonMatch = aiResponse.match(/\[[\s\S]*\]/);
            if (jsonMatch) {
                slides = JSON.parse(jsonMatch[0]);
            } else {
                slides = JSON.parse(aiResponse);
            }
        } catch (parseErr) {
            // Fallback: create basic slides from outline
            slides = [
                { title: topic, bullets: ["AI-Generated Presentation"] },
                { title: "Overview", bullets: ["Generated with AI ZAYA", "Topic: " + topic] },
                { title: "Thank You", bullets: ["Questions?"] }
            ];
        }

        // Create the PowerPoint using PptxGenJS
        const pptx = new PptxGenJS();
        pptx.author = 'AI ZAYA';
        pptx.title = topic;
        pptx.subject = topic;

        slides.forEach((slideData, index) => {
            const slide = pptx.addSlide();

            if (index === 0) {
                // Title Slide
                slide.addText(slideData.title || topic, {
                    x: 0.5, y: 1.5, w: '90%', h: 1.5,
                    fontSize: 36, bold: true, color: '003366',
                    align: 'center', fontFace: 'Arial'
                });
                if (slideData.bullets && slideData.bullets.length > 0) {
                    slide.addText(slideData.bullets.join('\n'), {
                        x: 0.5, y: 3.5, w: '90%', h: 1,
                        fontSize: 18, color: '666666',
                        align: 'center', fontFace: 'Arial'
                    });
                }
            } else {
                // Content Slide
                slide.addText(slideData.title || `Slide ${index + 1}`, {
                    x: 0.5, y: 0.3, w: '90%', h: 0.8,
                    fontSize: 28, bold: true, color: '003366',
                    fontFace: 'Arial'
                });
                // Add a divider line
                slide.addShape(pptx.ShapeType.line, {
                    x: 0.5, y: 1.2, w: 9, h: 0,
                    line: { color: '0088CC', width: 2 }
                });
                // Bullet points
                if (slideData.bullets && slideData.bullets.length > 0) {
                    const bulletText = slideData.bullets.map(b => ({
                        text: b,
                        options: { fontSize: 16, color: '333333', bullet: true, breakLine: true }
                    }));
                    slide.addText(bulletText, {
                        x: 0.7, y: 1.5, w: 8.5, h: 3.5,
                        fontFace: 'Arial', valign: 'top'
                    });
                }
            }
        });

        // Generate unique filename
        const timestamp = Date.now();
        const fileName = `presentation_${timestamp}.pptx`;
        const filePath = path.join(pptOutputDir, fileName);

        // Write to file
        await pptx.writeFile({ fileName: filePath });

        // Return download URL
        const baseUrl = `${req.protocol}://${req.get('host')}`;
        const fileUrl = `${baseUrl}/downloads/${fileName}`;

        res.send({ fileUrl, fileName });

    } catch (error) {
        console.error("PPT generation error:", error);
        res.status(500).send({ error: "Failed to generate presentation: " + error.message });
    }
});

// --- PPT Create Endpoint (no auth, receives pre-generated slides from app) ---
app.post('/api/ppt-create', async (req, res) => {
    const { topic, slides } = req.body;
    if (!topic || !slides || !Array.isArray(slides) || slides.length === 0) {
        return res.status(400).send({ error: "Topic and slides array are required." });
    }

    try {
        const pptx = new PptxGenJS();
        pptx.author = 'AI ZAYA';
        pptx.title = topic;
        pptx.subject = topic;
        pptx.layout = 'LAYOUT_WIDE'; // 13.33 x 7.5 inches

        // ─── Professional Color Palette ───
        const COLORS = {
            darkNavy:    '0D1B2A',
            navy:        '1B2838',
            mediumBlue:  '1565C0',
            accentBlue:  '2196F3',
            lightBlue:   'E3F2FD',
            white:       'FFFFFF',
            offWhite:    'F8FAFC',
            lightGray:   'ECEFF1',
            darkGray:    '37474F',
            mediumGray:  '607080',
            textDark:    '212121',
            textMedium:  '455A64',
            accentGold:  'FFB300',
            accentCyan:  '00BCD4',
            gradientTop: '1A237E',
            gradientBot: '0D47A1',
        };

        const totalSlides = slides.length;

        slides.forEach((slideData, index) => {
            const slide = pptx.addSlide();
            const sTitle = (slideData.title || '').trim();
            const sBullets = (slideData.bullets || []).map(b => (b || '').trim()).filter(b => b.length > 0);
            const isFirst = index === 0;
            const isLast = index === totalSlides - 1;
            const isTOC = index === 1 && sTitle.toLowerCase().includes('table of contents') || sTitle.toLowerCase().includes('agenda') || sTitle.toLowerCase().includes('overview');

            if (isFirst) {
                // ═══════════════ TITLE SLIDE ═══════════════
                slide.background = { fill: COLORS.darkNavy };

                // Decorative top accent bar
                slide.addShape(pptx.ShapeType.rect, {
                    x: 0, y: 0, w: '100%', h: 0.08,
                    fill: { color: COLORS.accentBlue }
                });

                // Decorative side accent
                slide.addShape(pptx.ShapeType.rect, {
                    x: 0, y: 0, w: 0.06, h: '100%',
                    fill: { color: COLORS.accentCyan }
                });

                // Main title
                slide.addText(sTitle || topic, {
                    x: 1.0, y: 1.8, w: 11.3, h: 2.0,
                    fontSize: 40, bold: true, color: COLORS.white,
                    align: 'center', fontFace: 'Calibri',
                    lineSpacingMultiple: 1.2
                });

                // Horizontal accent line
                slide.addShape(pptx.ShapeType.rect, {
                    x: 4.5, y: 3.9, w: 4.3, h: 0.04,
                    fill: { color: COLORS.accentGold }
                });

                // Subtitle
                if (sBullets.length > 0) {
                    slide.addText(sBullets.join(' | '), {
                        x: 1.5, y: 4.3, w: 10.3, h: 1.0,
                        fontSize: 18, color: COLORS.accentCyan,
                        align: 'center', fontFace: 'Calibri',
                        italic: true
                    });
                }

                // Bottom branding
                slide.addText('Generated by AI ZAYA', {
                    x: 0.5, y: 6.8, w: 12.3, h: 0.4,
                    fontSize: 10, color: COLORS.mediumGray,
                    align: 'center', fontFace: 'Calibri'
                });

            } else if (isLast && (sTitle.toLowerCase().includes('thank') || sTitle.toLowerCase().includes('q&a') || sTitle.toLowerCase().includes('questions'))) {
                // ═══════════════ THANK YOU SLIDE ═══════════════
                slide.background = { fill: COLORS.darkNavy };

                slide.addShape(pptx.ShapeType.rect, {
                    x: 0, y: 0, w: '100%', h: 0.08,
                    fill: { color: COLORS.accentBlue }
                });

                slide.addText(sTitle, {
                    x: 1.0, y: 2.0, w: 11.3, h: 2.0,
                    fontSize: 44, bold: true, color: COLORS.white,
                    align: 'center', fontFace: 'Calibri'
                });

                slide.addShape(pptx.ShapeType.rect, {
                    x: 5.0, y: 4.1, w: 3.3, h: 0.04,
                    fill: { color: COLORS.accentGold }
                });

                if (sBullets.length > 0) {
                    slide.addText(sBullets.join(' • '), {
                        x: 2.0, y: 4.5, w: 9.3, h: 1.0,
                        fontSize: 20, color: COLORS.accentCyan,
                        align: 'center', fontFace: 'Calibri'
                    });
                }

                slide.addText('Presented with AI ZAYA', {
                    x: 0.5, y: 6.8, w: 12.3, h: 0.4,
                    fontSize: 10, color: COLORS.mediumGray,
                    align: 'center', fontFace: 'Calibri'
                });

            } else {
                // ═══════════════ CONTENT SLIDE ═══════════════
                slide.background = { fill: COLORS.offWhite };

                // Top accent bar
                slide.addShape(pptx.ShapeType.rect, {
                    x: 0, y: 0, w: '100%', h: 0.06,
                    fill: { color: COLORS.accentBlue }
                });

                // Left accent strip
                slide.addShape(pptx.ShapeType.rect, {
                    x: 0, y: 0, w: 0.05, h: '100%',
                    fill: { color: COLORS.mediumBlue }
                });

                // Title background box
                slide.addShape(pptx.ShapeType.rect, {
                    x: 0.05, y: 0.06, w: '100%', h: 1.1,
                    fill: { color: COLORS.white }
                });

                // Slide title
                slide.addText(sTitle, {
                    x: 0.8, y: 0.2, w: 11.0, h: 0.8,
                    fontSize: 26, bold: true, color: COLORS.darkNavy,
                    fontFace: 'Calibri', valign: 'middle'
                });

                // Title underline accent
                slide.addShape(pptx.ShapeType.rect, {
                    x: 0.8, y: 1.1, w: 2.5, h: 0.04,
                    fill: { color: COLORS.accentBlue }
                });

                // Bullet points
                if (sBullets.length > 0) {
                    const bulletRows = sBullets.map(bulletText => ({
                        text: bulletText,
                        options: {
                            fontSize: 17,
                            color: COLORS.textDark,
                            fontFace: 'Calibri',
                            bullet: { type: 'bullet', style: '●' },
                            paraSpaceAfter: 10,
                            indentLevel: 0,
                            breakLine: true
                        }
                    }));

                    slide.addText(bulletRows, {
                        x: 1.0, y: 1.5, w: 10.5, h: 4.8,
                        valign: 'top',
                        lineSpacingMultiple: 1.5
                    });
                }

                // Slide number footer
                slide.addText(`${index + 1} / ${totalSlides}`, {
                    x: 11.5, y: 7.0, w: 1.5, h: 0.35,
                    fontSize: 9, color: COLORS.mediumGray,
                    align: 'right', fontFace: 'Calibri'
                });

                // Bottom line
                slide.addShape(pptx.ShapeType.rect, {
                    x: 0, y: 7.35, w: '100%', h: 0.03,
                    fill: { color: COLORS.lightGray }
                });
            }
        });

        const timestamp = Date.now();
        const fileName = `presentation_${timestamp}.pptx`;
        const filePath = path.join(pptOutputDir, fileName);

        await pptx.writeFile({ fileName: filePath });

        const baseUrl = `${req.protocol}://${req.get('host')}`;
        const fileUrl = `${baseUrl}/downloads/${fileName}`;

        console.log(`PPT created: ${fileName} (${slides.length} slides)`);
        res.send({ fileUrl, fileName });

    } catch (error) {
        console.error("PPT create error:", error);
        res.status(500).send({ error: "Failed to create presentation: " + error.message });
    }
});

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
    console.log(`Backend running on port ${PORT}`);
    console.log(`Local access:  http://localhost:${PORT}`);
    console.log(`Device access: http://${localIp}:${PORT}`);
});
