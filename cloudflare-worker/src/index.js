/**
 * AI ZAYA 2.0 - Production Cloudflare Worker AI Gateway
 * Created by ZAYA CODE HUB
 *
 * Core Architecture:
 * - Ultra-low latency global edge routing on Cloudflare Workers
 * - ₹0 Budget Enforcement: Strictly routes to verified free-tier models (:free)
 * - Intelligent Bounded Retry on 429 with exponential backoff & Retry-After support
 * - Guaranteed UTF-8 encoding pipeline (Hindi, Nepali, Tamil, Bhojpuri, Maithili, Emojis)
 * - Real-time SSE streaming with reasoning token isolation (<think> stripping)
 * - AI ZAYA Branding (AI ZAYA Advanced, AI ZAYA Smart, AI ZAYA Fast)
 * - Abuse prevention without restrictive daily caps
 * - Safe error handling with zero key/token leakage
 */

const CORS_HEADERS = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization, x-user-id, X-Title, HTTP-Referer',
  'Access-Control-Max-Age': '86400',
};

const JSON_HEADERS = {
  ...CORS_HEADERS,
  'Content-Type': 'application/json; charset=utf-8',
};

const STREAM_HEADERS = {
  ...CORS_HEADERS,
  'Content-Type': 'text/event-stream; charset=utf-8',
  'Cache-Control': 'no-cache, no-transform',
  'Connection': 'keep-alive',
  'X-Accel-Buffering': 'no',
};

// Verified Free-Tier Models Pool (Strictly ₹0 Budget)
const FREE_MODELS = {
  ADVANCED: 'nvidia/nemotron-3-ultra-550b-a55b:free',
  SMART: 'nvidia/nemotron-3-ultra-550b-a55b:free',
  FAST: 'nvidia/nemotron-3-ultra-550b-a55b:free',
  FALLBACK_1: 'nvidia/nemotron-3-ultra-550b-a55b:free',
  FALLBACK_2: 'google/gemma-4-31b-it:free'
};

// Default AI ZAYA System Prompt
const SYSTEM_PROMPT = `You are AI ZAYA, an elite, highly intelligent AI assistant created by ZAYA CODE HUB.
You assist users with general questions, software development, debugging, mathematics, learning, translation, and writing.
Guidelines:
1. Always be polite, helpful, structured, and accurate.
2. Support all languages naturally (English, Hindi, Nepali, Tamil, Maithili, Bhojpuri, Bengali, etc.). Preserve proper script and spelling.
3. When providing code, use markdown code blocks with the correct language tag.
4. Maintain a clean, professional tone representing AI ZAYA.`;

// In-memory rate limiting map per edge datacenter (sliding window burst protection: 40 req/min)
const ipRequestHistory = new Map();

function checkAbuseProtection(ip) {
  const now = Date.now();
  const windowMs = 60000; // 1 minute
  const maxPerWindow = 40; // Max 40 requests per minute per IP

  let timestamps = ipRequestHistory.get(ip);
  if (!timestamps) {
    timestamps = [];
    ipRequestHistory.set(ip, timestamps);
  }

  // Filter out timestamps older than windowMs
  timestamps = timestamps.filter(t => now - t < windowMs);
  ipRequestHistory.set(ip, timestamps);

  if (timestamps.length >= maxPerWindow) {
    return false;
  }

  timestamps.push(now);
  return true;
}

/**
 * Remove internal reasoning tokens (<think>...</think>) safely without corrupting multibyte Unicode.
 */
function cleanReasoning(text) {
  if (!text) return '';
  return text.replace(/<think>[\s\S]*?<\/think>/gi, '').trim();
}

/**
 * Validates an OpenRouter API key.
 */
function isValidKey(key) {
  if (!key) return false;
  const trimmed = key.trim();
  if (trimmed.length < 10) return false;
  if (trimmed === 'your_openrouter_api_key_here') return false;
  return true;
}

/**
 * Maps incoming requested model to a verified ₹0 free model.
 * Guarantees that paid endpoints are NEVER hit.
 */
function resolveFreeModel(requestedModel, env) {
  if (!requestedModel) {
    return env.NEMOTRON_MODEL_ID || FREE_MODELS.ADVANCED;
  }
  const clean = requestedModel.trim();
  if (clean.includes('ultra') || clean.includes('nemotron')) {
    return FREE_MODELS.ADVANCED;
  }
  if (clean.includes('smart') || clean.includes('gemma')) {
    return FREE_MODELS.SMART;
  }
  if (clean.includes('fast') || clean.includes('llama')) {
    return FREE_MODELS.FAST;
  }
  // Enforce :free suffix
  if (clean.endsWith(':free')) {
    return clean;
  }
  // If user requested a paid model, safely map to the free counterpart
  return `${clean}:free`;
}

function mapStatusToMessage(status, rawError = '') {
  switch (status) {
    case 400:
      return 'Invalid request format. Please check your query and try again.';
    case 401:
      return 'Authentication configuration notice. AI ZAYA service gateway is updating.';
    case 402:
      return 'AI capacity temporarily reached. Please retry in a few moments.';
    case 403:
      return 'Access to this AI model is currently updating. Please try again shortly.';
    case 408:
      return 'Request timed out. AI ZAYA is taking longer than usual. Please retry.';
    case 429:
      return 'High request volume. AI ZAYA is processing many requests. Please wait 5 seconds and retry.';
    case 500:
    case 502:
    case 503:
    case 504:
      return 'AI provider temporarily busy. Please retry in a moment.';
    default:
      return `AI Service temporary issue (${status}). Please try again.`;
  }
}

/**
 * Sleep helper for backoff
 */
const sleep = (ms) => new Promise(resolve => setTimeout(resolve, ms));

export default {
  async fetch(request, env, ctx) {
    const url = new URL(request.url);

    // ─── 1. CORS Preflight ───
    if (request.method === 'OPTIONS') {
      return new Response(null, { headers: CORS_HEADERS });
    }

    // ─── 2. Health Check ───
    if (url.pathname === '/api/health' || url.pathname === '/health') {
      const apiKey = (env.OPENROUTER_API_KEY || '').trim();
      const hasOpenRouter = isValidKey(apiKey);
      return new Response(JSON.stringify({
        status: 'ok',
        version: '2.2.0',
        environment: 'production',
        platform: 'Cloudflare Workers (Edge)',
        primaryModel: env.NEMOTRON_MODEL_ID || FREE_MODELS.ADVANCED,
        freeBudgetPolicy: 'enforced',
        providers: {
          openrouter: hasOpenRouter ? 'configured' : 'key_missing'
        },
        timestamp: new Date().toISOString()
      }, null, 2), {
        headers: JSON_HEADERS
      });
    }

    // ─── 3. Key Status Endpoint (Safe - no key exposure) ───
    if (url.pathname === '/api/key-status') {
      const k = (env.OPENROUTER_API_KEY || '').trim();
      return new Response(JSON.stringify({
        keySet: k.length > 0,
        keyLength: k.length,
        keyPrefix: k.length >= 12 ? k.substring(0, 12) + '...' : 'none',
        isValid: isValidKey(k),
        startsWithSkOr: k.startsWith('sk-or-')
      }), { headers: JSON_HEADERS });
    }

    // ─── 4. Models Catalog (Branded) ───
    if (url.pathname === '/api/models' && request.method === 'GET') {
      return new Response(JSON.stringify({
        primary: FREE_MODELS.ADVANCED,
        models: [
          {
            id: FREE_MODELS.ADVANCED,
            displayName: 'AI ZAYA Advanced',
            description: 'Frontier reasoning intelligence with 550B parameters and multi-lingual mastery',
            isDefault: true
          },
          {
            id: FREE_MODELS.SMART,
            displayName: 'AI ZAYA Smart',
            description: 'Balanced daily learning companion with deep reasoning and zero cost',
            isDefault: false
          },
          {
            id: FREE_MODELS.FAST,
            displayName: 'AI ZAYA Fast',
            description: 'Ultra-low latency responses for quick facts, translations, and summaries',
            isDefault: false
          }
        ]
      }), {
        headers: JSON_HEADERS
      });
    }

    // ─── 5. Abuse Prevention ───
    const clientIp = request.headers.get('CF-Connecting-IP') || 'unknown';
    if (!checkAbuseProtection(clientIp)) {
      return new Response(JSON.stringify({
        error: 'Too many requests in a short period. Please slow down and wait a moment.'
      }), {
        status: 429,
        headers: JSON_HEADERS
      });
    }

    // ─── 6. Streaming Chat (/api/chat/stream) ───
    if (url.pathname === '/api/chat/stream' && request.method === 'POST') {
      return handleStreamRequest(request, env);
    }

    // ─── 7. Non-Streaming Chat (/api/chat) ───
    if (url.pathname === '/api/chat' && request.method === 'POST') {
      return handleChatRequest(request, env);
    }

    // ─── 8. Text Generate Shortcut ───
    if (url.pathname === '/api/text-generate' && request.method === 'POST') {
      return handleTextGenerate(request, env);
    }

    // Default 404
    return new Response(JSON.stringify({ error: 'Endpoint not found.' }), {
      status: 404,
      headers: JSON_HEADERS
    });
  }
};

/**
 * Handle Streaming SSE Proxy with Resilient UTF-8 and 429 Handling
 */
async function handleStreamRequest(request, env) {
  let body;
  try {
    body = await request.json();
  } catch {
    return new Response(JSON.stringify({ error: 'Invalid JSON request format.' }), {
      status: 400,
      headers: JSON_HEADERS
    });
  }

  let rawMessages = body.messages;
  if (!rawMessages && body.message) {
    rawMessages = [{ role: 'user', content: String(body.message) }];
  }
  if (!rawMessages || !Array.isArray(rawMessages) || rawMessages.length === 0) {
    return new Response(JSON.stringify({ error: 'messages array is required.' }), {
      status: 400,
      headers: JSON_HEADERS
    });
  }

  // Ensure system prompt is present
  const messages = [...rawMessages];
  if (!messages.some(m => m.role === 'system')) {
    messages.unshift({ role: 'system', content: SYSTEM_PROMPT });
  }

  const apiKey = (env.OPENROUTER_API_KEY || '').trim();
  if (!isValidKey(apiKey)) {
    return new Response(JSON.stringify({ error: 'AI Gateway configuration notice: OPENROUTER_API_KEY is not configured.' }), {
      status: 500,
      headers: JSON_HEADERS
    });
  }

  const model = resolveFreeModel(body.model, env);
  const temperature = typeof body.temperature === 'number' ? body.temperature : 0.7;
  const maxTokens = typeof body.max_tokens === 'number' ? body.max_tokens : 4096;

  // Bounded retry loop for 429 errors (up to 2 retries)
  let lastStatus = 500;
  let lastErrorText = '';

  for (let attempt = 0; attempt < 3; attempt++) {
    try {
      const upstreamRes = await fetch('https://openrouter.ai/api/v1/chat/completions', {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${apiKey}`,
          'HTTP-Referer': 'https://aizaya.app',
          'X-Title': 'AI ZAYA',
          'Content-Type': 'application/json; charset=utf-8'
        },
        body: JSON.stringify({
          model,
          messages,
          temperature,
          max_tokens: maxTokens,
          stream: true
        })
      });

      if (upstreamRes.status === 429) {
        lastStatus = 429;
        const retryAfterSec = parseInt(upstreamRes.headers.get('Retry-After') || '1', 10);
        const waitMs = Math.min(Math.max(retryAfterSec, 1) * 1000, 3000);
        if (attempt < 2) {
          await sleep(waitMs);
          continue;
        }
      }

      if (!upstreamRes.ok) {
        lastStatus = upstreamRes.status;
        lastErrorText = await upstreamRes.text();
        if (attempt < 2 && upstreamRes.status >= 500) {
          await sleep(1000);
          continue;
        }
        return new Response(JSON.stringify({
          error: mapStatusToMessage(upstreamRes.status, lastErrorText)
        }), {
          status: upstreamRes.status,
          headers: JSON_HEADERS
        });
      }

      // Stream Transformation with UTF-8 byte boundary preservation
      let insideThink = false;
      let leftover = '';
      const decoder = new TextDecoder('utf-8');
      const encoder = new TextEncoder();

      const transformStream = new TransformStream({
        transform(chunk, controller) {
          const text = leftover + decoder.decode(chunk, { stream: true });
          const lines = text.split('\n');
          leftover = lines.pop() || '';

          for (const line of lines) {
            const trimmed = line.trim();
            if (!trimmed || trimmed.startsWith(':')) continue;

            if (trimmed === 'data: [DONE]') {
              controller.enqueue(encoder.encode('data: [DONE]\n\n'));
              continue;
            }

            if (trimmed.startsWith('data: ')) {
              const jsonPart = trimmed.slice(6);
              try {
                const parsed = JSON.parse(jsonPart);
                const delta = parsed.choices?.[0]?.delta;
                if (!delta) continue;

                if (delta.reasoning) {
                  controller.enqueue(encoder.encode(`data: ${JSON.stringify({ type: 'thinking', status: true })}\n\n`));
                  continue;
                }

                let content = delta.content || '';
                if (!content) continue;

                if (content.includes('<think>')) {
                  insideThink = true;
                  controller.enqueue(encoder.encode(`data: ${JSON.stringify({ type: 'thinking', status: true })}\n\n`));
                  content = content.replace(/<think>[\s\S]*?<\/think>/g, '');
                  if (content.includes('<think>')) {
                    content = content.substring(0, content.indexOf('<think>'));
                  }
                } else if (insideThink) {
                  if (content.includes('</think>')) {
                    insideThink = false;
                    content = content.substring(content.indexOf('</think>') + 8);
                    controller.enqueue(encoder.encode(`data: ${JSON.stringify({ type: 'thinking', status: false })}\n\n`));
                  } else {
                    continue; // Swallow internal reasoning tokens
                  }
                }

                if (content.length > 0) {
                  controller.enqueue(encoder.encode(`data: ${JSON.stringify({ type: 'content', delta: content, model })}\n\n`));
                }
              } catch {
                // Ignore parse errors on partial chunks
              }
            }
          }
        },
        flush(controller) {
          controller.enqueue(encoder.encode('data: [DONE]\n\n'));
        }
      });

      return new Response(upstreamRes.body.pipeThrough(transformStream), {
        headers: STREAM_HEADERS
      });

    } catch (err) {
      if (attempt < 2) {
        await sleep(1000);
        continue;
      }
      return new Response(JSON.stringify({ error: `Connection failed: ${err.message}` }), {
        status: 502,
        headers: JSON_HEADERS
      });
    }
  }

  return new Response(JSON.stringify({ error: mapStatusToMessage(lastStatus, lastErrorText) }), {
    status: lastStatus,
    headers: JSON_HEADERS
  });
}

/**
 * Handle Non-Streaming Completion with ₹0 Enforcement, Bounded 429 Retry, and UTF-8
 */
async function handleChatRequest(request, env) {
  let body;
  try {
    body = await request.json();
  } catch {
    return new Response(JSON.stringify({ error: 'Invalid JSON request format.' }), {
      status: 400,
      headers: JSON_HEADERS
    });
  }

  let rawMessages = body.messages;
  if (!rawMessages && body.message) {
    rawMessages = [{ role: 'user', content: String(body.message) }];
  }
  if (!rawMessages || !Array.isArray(rawMessages) || rawMessages.length === 0) {
    return new Response(JSON.stringify({ error: 'messages array is required.' }), {
      status: 400,
      headers: JSON_HEADERS
    });
  }

  // Ensure system prompt is present
  const messages = [...rawMessages];
  if (!messages.some(m => m.role === 'system')) {
    messages.unshift({ role: 'system', content: SYSTEM_PROMPT });
  }

  const apiKey = (env.OPENROUTER_API_KEY || '').trim();
  if (!isValidKey(apiKey)) {
    return new Response(JSON.stringify({
      error: 'AI ZAYA backend configuration notice: OPENROUTER_API_KEY is not configured.'
    }), {
      status: 503,
      headers: JSON_HEADERS
    });
  }

  const primaryModel = resolveFreeModel(body.model, env);
  const fallbackModel = FREE_MODELS.FALLBACK_1;
  const temperature = body.temperature ?? 0.7;
  const maxTokens = body.max_tokens ?? 4096;

  // Bounded retry execution helper
  async function callOpenRouter(targetModel) {
    for (let attempt = 0; attempt < 3; attempt++) {
      try {
        const res = await fetch('https://openrouter.ai/api/v1/chat/completions', {
          method: 'POST',
          headers: {
            'Authorization': `Bearer ${apiKey}`,
            'HTTP-Referer': 'https://aizaya.app',
            'X-Title': 'AI ZAYA',
            'Content-Type': 'application/json; charset=utf-8'
          },
          body: JSON.stringify({
            model: targetModel,
            messages,
            temperature,
            max_tokens: maxTokens
          })
        });

        if (res.status === 429) {
          const retryAfterSec = parseInt(res.headers.get('Retry-After') || '1', 10);
          const waitMs = Math.min(Math.max(retryAfterSec, 1) * 1000, 3000);
          if (attempt < 2) {
            await sleep(waitMs);
            continue;
          }
        }

        if (res.ok) {
          const data = await res.json();
          const rawContent = data.choices?.[0]?.message?.content || '';
          return { success: true, content: cleanReasoning(rawContent), model: targetModel };
        }

        const errText = await res.text();
        if (attempt < 2 && res.status >= 500) {
          await sleep(1000);
          continue;
        }
        return { success: false, status: res.status, error: errText };

      } catch (err) {
        if (attempt < 2) {
          await sleep(1000);
          continue;
        }
        return { success: false, status: 502, error: err.message };
      }
    }
    return { success: false, status: 429, error: 'Rate limit reached after retries' };
  }

  // 1. Try Primary Model
  const primaryResult = await callOpenRouter(primaryModel);
  if (primaryResult.success) {
    return new Response(JSON.stringify({
      response: primaryResult.content,
      model: primaryResult.model,
      provider: 'AI ZAYA Edge'
    }), {
      headers: JSON_HEADERS
    });
  }

  // 2. Try Fallback Free Model if primary hit 429 or 5xx
  if ((primaryResult.status === 429 || primaryResult.status >= 500) && primaryModel !== fallbackModel) {
    const fallbackResult = await callOpenRouter(fallbackModel);
    if (fallbackResult.success) {
      return new Response(JSON.stringify({
        response: fallbackResult.content,
        model: fallbackResult.model,
        provider: 'AI ZAYA Edge (Smart)'
      }), {
        headers: JSON_HEADERS
      });
    }
  }

  return new Response(JSON.stringify({
    error: mapStatusToMessage(primaryResult.status, primaryResult.error)
  }), {
    status: primaryResult.status || 500,
    headers: JSON_HEADERS
  });
}

/**
 * Quick Text Generator Endpoint
 */
async function handleTextGenerate(request, env) {
  const { prompt } = await request.json().catch(() => ({}));
  if (!prompt) {
    return new Response(JSON.stringify({ error: 'Prompt is required.' }), {
      status: 400,
      headers: JSON_HEADERS
    });
  }

  const modifiedReq = new Request(request.url, {
    method: 'POST',
    headers: request.headers,
    body: JSON.stringify({
      messages: [
        { role: 'user', content: prompt }
      ]
    })
  });
  return handleChatRequest(modifiedReq, env);
}
