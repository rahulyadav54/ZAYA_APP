const axios = require('axios');

/**
 * AI Gateway for AI ZAYA
 * Handles OpenRouter (NVIDIA Nemotron 3 Ultra as primary) with automatic fallback
 * to Groq / Gemini when configured.
 */

const DEFAULT_NEMOTRON_MODEL = process.env.NEMOTRON_MODEL_ID || 'nvidia/nemotron-3-ultra-550b-a55b';
const FALLBACK_FREE_MODEL = process.env.NEMOTRON_FALLBACK_MODEL_ID || 'nvidia/nemotron-3-ultra-550b-a55b:free';

/**
 * Available models catalog
 */
const AVAILABLE_MODELS = [
    {
        id: DEFAULT_NEMOTRON_MODEL,
        name: 'NVIDIA Nemotron 3 Ultra',
        description: 'Primary frontier reasoning model (550B MoE, 1M context)',
        provider: 'OpenRouter',
        isPrimary: true
    },
    {
        id: FALLBACK_FREE_MODEL,
        name: 'NVIDIA Nemotron 3 Ultra (Free)',
        description: 'Trial endpoint for Nemotron 3 Ultra',
        provider: 'OpenRouter',
        isPrimary: false
    },
    {
        id: 'llama-3.3-70b-versatile',
        name: 'Groq Llama 3.3 70B (Fast)',
        description: 'Ultra-fast low-latency fallback model',
        provider: 'Groq',
        isPrimary: false
    }
];

/**
 * Sanitize text to remove reasoning tokens (<think>...</think>)
 */
function stripReasoningTokens(text) {
    if (!text) return '';
    return text.replace(/<think>[\s\S]*?<\/think>/gi, '').trim();
}

/**
 * Map HTTP error codes to clear user-friendly error messages
 */
function mapErrorToUserMessage(error, provider = 'OpenRouter') {
    const status = error.response ? error.response.status : null;
    const errorData = error.response ? error.response.data : null;
    const rawMsg = (errorData && errorData.error && errorData.error.message) 
        ? errorData.error.message 
        : (error.message || 'Unknown error');

    switch (status) {
        case 400:
            return 'Invalid request: The model could not process this prompt format. Please rephrase.';
        case 401:
            return 'Authentication failure: Server API key is invalid or expired. Please contact support.';
        case 402:
            return 'API credit limit reached: OpenRouter balance exhausted. Please recharge credits.';
        case 403:
            return 'Access denied: Model or resource is restricted or unavailable.';
        case 408:
            return 'Request timed out: Nemotron 3 Ultra is taking too long to respond. Please try again.';
        case 429:
            return 'Rate limit reached: Too many requests at the moment. Please wait a few seconds.';
        case 500:
        case 502:
        case 503:
        case 504:
            return `${provider} server error (${status}): Service temporarily unavailable. Please retry.`;
        default:
            if (error.code === 'ECONNABORTED' || error.message.includes('timeout')) {
                return 'Connection timeout: The AI server took too long to respond.';
            }
            if (error.code === 'ENOTFOUND' || error.message.includes('getaddrinfo')) {
                return 'Network error: Backend server cannot reach OpenRouter. Check internet connectivity.';
            }
            return `AI Service Error: ${rawMsg}`;
    }
}

/**
 * Execute non-streaming chat with primary model and automatic fallbacks
 */
async function chatCompletion({ messages, model = DEFAULT_NEMOTRON_MODEL, temperature = 0.7, max_tokens = 4096 }) {
    const openRouterKey = process.env.OPENROUTER_API_KEY;
    const groqKey = process.env.GROQ_API_KEY;
    const geminiKey = process.env.GEMINI_API_KEY;

    let targetModel = model || DEFAULT_NEMOTRON_MODEL;

    // 1. Try Primary: OpenRouter Nemotron 3 Ultra
    if (openRouterKey && openRouterKey !== 'your_openrouter_api_key_here') {
        try {
            console.log(`[AI Gateway] Routing to OpenRouter: ${targetModel}`);
            const response = await axios.post(
                'https://openrouter.ai/api/v1/chat/completions',
                {
                    model: targetModel,
                    messages: messages,
                    temperature: Number(temperature) || 0.7,
                    max_tokens: Number(max_tokens) || 4096
                },
                {
                    headers: {
                        'Authorization': `Bearer ${openRouterKey}`,
                        'HTTP-Referer': 'https://aizaya.app',
                        'X-Title': 'AI ZAYA',
                        'Content-Type': 'application/json'
                    },
                    timeout: 120000
                }
            );

            const choice = response.data?.choices?.[0];
            const rawContent = choice?.message?.content || '';
            const cleanedContent = stripReasoningTokens(rawContent);

            return {
                response: cleanedContent || rawContent,
                model: targetModel,
                provider: 'OpenRouter',
                usage: response.data?.usage || {}
            };
        } catch (error) {
            console.error(`[AI Gateway] OpenRouter primary error (${targetModel}):`, error.response?.status || error.message);

            // If error is temporary or quota exhausted, try free variant if we weren't already using it
            if (targetModel !== FALLBACK_FREE_MODEL && (error.response?.status === 402 || error.response?.status === 429 || error.response?.status >= 500)) {
                try {
                    console.log(`[AI Gateway] Attempting fallback to Nemotron Free tier (${FALLBACK_FREE_MODEL})...`);
                    const freeResponse = await axios.post(
                        'https://openrouter.ai/api/v1/chat/completions',
                        {
                            model: FALLBACK_FREE_MODEL,
                            messages: messages,
                            temperature: Number(temperature) || 0.7,
                            max_tokens: Number(max_tokens) || 4096
                        },
                        {
                            headers: {
                                'Authorization': `Bearer ${openRouterKey}`,
                                'HTTP-Referer': 'https://aizaya.app',
                                'X-Title': 'AI ZAYA',
                                'Content-Type': 'application/json'
                            },
                            timeout: 120000
                        }
                    );
                    const choice = freeResponse.data?.choices?.[0];
                    const rawContent = choice?.message?.content || '';
                    return {
                        response: stripReasoningTokens(rawContent) || rawContent,
                        model: FALLBACK_FREE_MODEL,
                        provider: 'OpenRouter (Free)',
                        usage: freeResponse.data?.usage || {}
                    };
                } catch (freeErr) {
                    console.error('[AI Gateway] Free variant fallback failed:', freeErr.message);
                }
            }
        }
    }

    // 2. Try Fallback: Groq
    if (groqKey && groqKey !== 'your_groq_api_key_here') {
        try {
            console.log('[AI Gateway] Falling back to Groq (llama-3.3-70b-versatile)...');
            const groqResponse = await axios.post(
                'https://api.groq.com/openai/v1/chat/completions',
                {
                    model: 'llama-3.3-70b-versatile',
                    messages: messages,
                    temperature: Number(temperature) || 0.7,
                    max_tokens: Math.min(Number(max_tokens) || 2048, 4096)
                },
                {
                    headers: {
                        'Authorization': `Bearer ${groqKey}`,
                        'Content-Type': 'application/json'
                    },
                    timeout: 60000
                }
            );
            const content = groqResponse.data?.choices?.[0]?.message?.content || '';
            return {
                response: content,
                model: 'llama-3.3-70b-versatile',
                provider: 'Groq',
                usage: groqResponse.data?.usage || {}
            };
        } catch (groqErr) {
            console.error('[AI Gateway] Groq fallback failed:', groqErr.message);
        }
    }

    // 3. Try Fallback: Gemini
    if (geminiKey && geminiKey !== 'YOUR_PRIMARY_GEMINI_KEY') {
        try {
            console.log('[AI Gateway] Falling back to Google Gemini...');
            const lastUserMsg = [...messages].reverse().find(m => m.role === 'user')?.content || '';
            const geminiUrl = `https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=${geminiKey}`;
            const geminiRes = await axios.post(geminiUrl, {
                contents: [{ parts: [{ text: lastUserMsg }] }]
            });
            const content = geminiRes.data?.candidates?.[0]?.content?.parts?.[0]?.text || '';
            return {
                response: content,
                model: 'gemini-1.5-flash',
                provider: 'Google Gemini',
                usage: {}
            };
        } catch (geminiErr) {
            console.error('[AI Gateway] Gemini fallback failed:', geminiErr.message);
        }
    }

    throw new Error('All AI providers are currently unavailable or unconfigured. Please configure OPENROUTER_API_KEY in the backend environment.');
}

/**
 * Handle Streaming completion using Server-Sent Events (SSE)
 */
async function streamCompletion({ messages, model = DEFAULT_NEMOTRON_MODEL, temperature = 0.7, max_tokens = 4096, res, clientReq }) {
    const openRouterKey = process.env.OPENROUTER_API_KEY;

    if (!openRouterKey || openRouterKey === 'your_openrouter_api_key_here') {
        res.write(`data: ${JSON.stringify({ error: 'Server configuration error: OPENROUTER_API_KEY not set.' })}\n\n`);
        res.write('data: [DONE]\n\n');
        return res.end();
    }

    const targetModel = model || DEFAULT_NEMOTRON_MODEL;

    // Set up SSE headers
    res.setHeader('Content-Type', 'text/event-stream; charset=utf-8');
    res.setHeader('Cache-Control', 'no-cache, no-transform');
    res.setHeader('Connection', 'keep-alive');
    res.setHeader('X-Accel-Buffering', 'no');
    res.flushHeaders?.();

    let isAborted = false;
    const cancelTokenSource = axios.CancelToken.source();

    clientReq.on('close', () => {
        isAborted = true;
        cancelTokenSource.cancel('Client disconnected.');
    });

    try {
        console.log(`[AI Gateway Stream] Connecting to OpenRouter for: ${targetModel}`);
        const streamResponse = await axios.post(
            'https://openrouter.ai/api/v1/chat/completions',
            {
                model: targetModel,
                messages: messages,
                temperature: Number(temperature) || 0.7,
                max_tokens: Number(max_tokens) || 4096,
                stream: true
            },
            {
                headers: {
                    'Authorization': `Bearer ${openRouterKey}`,
                    'HTTP-Referer': 'https://aizaya.app',
                    'X-Title': 'AI ZAYA',
                    'Content-Type': 'application/json'
                },
                responseType: 'stream',
                cancelToken: cancelTokenSource.token,
                timeout: 120000
            }
        );

        let buffer = '';
        let insideThinkTag = false;

        streamResponse.data.on('data', (chunk) => {
            if (isAborted) return;
            buffer += chunk.toString();

            const lines = buffer.split('\n');
            buffer = lines.pop(); // keep incomplete line

            for (const line of lines) {
                const trimmed = line.trim();
                if (!trimmed || trimmed.startsWith(':')) continue;

                if (trimmed === 'data: [DONE]') {
                    res.write('data: [DONE]\n\n');
                    continue;
                }

                if (trimmed.startsWith('data: ')) {
                    const jsonStr = trimmed.slice(6);
                    try {
                        const parsed = JSON.parse(jsonStr);
                        const delta = parsed.choices?.[0]?.delta;
                        if (!delta) continue;

                        // Check for reasoning content
                        if (delta.reasoning) {
                            // Notify UI of thinking without dumping raw reasoning tokens
                            res.write(`data: ${JSON.stringify({ type: 'thinking', status: true })}\n\n`);
                            continue;
                        }

                        let text = delta.content || '';
                        if (!text) continue;

                        // Handle <think> tags in content
                        if (text.includes('<think>')) {
                            insideThinkTag = true;
                            res.write(`data: ${JSON.stringify({ type: 'thinking', status: true })}\n\n`);
                            text = text.replace(/<think>[\s\S]*?<\/think>/g, '');
                            if (text.includes('<think>')) {
                                text = text.substring(0, text.indexOf('<think>'));
                            }
                        } else if (insideThinkTag) {
                            if (text.includes('</think>')) {
                                insideThinkTag = false;
                                text = text.substring(text.indexOf('</think>') + 8);
                                res.write(`data: ${JSON.stringify({ type: 'thinking', status: false })}\n\n`);
                            } else {
                                // Skip internal thinking content
                                continue;
                            }
                        }

                        if (text.length > 0) {
                            res.write(`data: ${JSON.stringify({ type: 'content', delta: text, model: targetModel })}\n\n`);
                        }
                    } catch (parseErr) {
                        // ignore malformed SSE chunk
                    }
                }
            }
        });

        streamResponse.data.on('end', () => {
            if (!isAborted) {
                res.write('data: [DONE]\n\n');
                res.end();
            }
        });

        streamResponse.data.on('error', (streamErr) => {
            if (!isAborted) {
                console.error('[AI Gateway Stream] Stream error:', streamErr.message);
                const userMsg = mapErrorToUserMessage(streamErr, 'OpenRouter');
                res.write(`data: ${JSON.stringify({ error: userMsg })}\n\n`);
                res.write('data: [DONE]\n\n');
                res.end();
            }
        });

    } catch (err) {
        if (!isAborted) {
            console.error('[AI Gateway Stream] Upstream request failed:', err.response?.status || err.message);
            const userMsg = mapErrorToUserMessage(err, 'OpenRouter');
            res.write(`data: ${JSON.stringify({ error: userMsg })}\n\n`);
            res.write('data: [DONE]\n\n');
            res.end();
        }
    }
}

module.exports = {
    DEFAULT_NEMOTRON_MODEL,
    AVAILABLE_MODELS,
    chatCompletion,
    streamCompletion,
    mapErrorToUserMessage,
    stripReasoningTokens
};

