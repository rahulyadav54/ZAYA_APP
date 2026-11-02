import urllib.request
import json
import sys

BASE = 'https://aizaya-api.zayacodehub-aizaya.workers.dev'

def test_endpoint(name, url, method='GET', data=None, expected_status=200):
    headers = {'User-Agent': 'okhttp/4.12.0'}
    if data:
        headers['Content-Type'] = 'application/json; charset=utf-8'
        payload = json.dumps(data, ensure_ascii=False).encode('utf-8')
    else:
        payload = None
    req = urllib.request.Request(url, data=payload, headers=headers, method=method)
    try:
        with urllib.request.urlopen(req, timeout=30) as resp:
            body = resp.read().decode('utf-8')
            ct = resp.headers.get('Content-Type')
            passed = (resp.status == expected_status)
            tag = "PASS" if passed else "FAIL"
            print(f'[{tag}] {name} -> Status: {resp.status}, Content-Type: {ct}')
            return True, body
    except urllib.error.HTTPError as e:
        passed = (e.code == expected_status)
        tag = "PASS" if passed else "FAIL"
        print(f'[{tag}] {name} -> Expected {expected_status}, Got {e.code}')
        return passed, e.read().decode('utf-8', errors='replace')
    except Exception as e:
        print(f'[FAIL] {name} -> Error: {e}')
        return False, str(e)

print('=== 1. Health Endpoint ===')
test_endpoint('Health Check', f'{BASE}/api/health', expected_status=200)

print('\n=== 2. Key Status Endpoint ===')
test_endpoint('Key Status', f'{BASE}/api/key-status', expected_status=200)

print('\n=== 3. Models Catalog ===')
test_endpoint('Models Catalog', f'{BASE}/api/models', expected_status=200)

print('\n=== 4. Request Validation Tests ===')
test_endpoint('Missing Messages (Expect 400)', f'{BASE}/api/chat', method='POST', data={}, expected_status=400)
test_endpoint('Empty Messages Array (Expect 400)', f'{BASE}/api/chat', method='POST', data={'messages': []}, expected_status=400)

print('\n=== 5. Multilingual UTF-8 Round-Trip Tests ===')
multilingual = [
    ('English', 'Hello, how are you?'),
    ('Hindi', 'नमस्ते, आप कैसे हैं?'),
    ('Nepali', 'नमस्ते, तपाईंलाई कस्तो छ?'),
    ('Tamil', 'வணக்கம், நீங்கள் எப்படி இருக்கிறீர்கள்?'),
    ('Emoji', 'Hello 👋 AI ZAYA 🚀')
]

for lang, prompt in multilingual:
    ok, body = test_endpoint(f'Chat: {lang}', f'{BASE}/api/chat', method='POST', data={
        'messages': [{'role': 'user', 'content': f'Respond briefly in the same language/script to: {prompt}'}]
    })
    if ok:
        try:
            parsed = json.loads(body)
            res = parsed.get("response")
            out_str = f"   Response: {res}\n"
            sys.stdout.buffer.write(out_str.encode('utf-8'))
            sys.stdout.buffer.flush()
        except Exception as e:
            print('   Parse error:', e)

print('\n=== 6. Streaming SSE Test ===')
try:
    req = urllib.request.Request(
        f'{BASE}/api/chat/stream',
        data=json.dumps({'messages': [{'role': 'user', 'content': 'Count 1 to 3.'}]}).encode('utf-8'),
        headers={'Content-Type': 'application/json; charset=utf-8', 'User-Agent': 'okhttp/4.12.0'},
        method='POST'
    )
    with urllib.request.urlopen(req, timeout=30) as resp:
        ct = resp.headers.get('Content-Type')
        print(f'[PASS] SSE Stream -> Status: {resp.status}, Content-Type: {ct}')
        lines = [line.decode('utf-8').strip() for line in resp if line.strip()]
        print(f'   Received {len(lines)} SSE events, sample: {lines[:3]}')
except Exception as e:
    print(f'[FAIL] SSE Stream -> Error: {e}')

