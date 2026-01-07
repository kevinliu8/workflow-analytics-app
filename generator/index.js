const http = require('http');
const { randomUUID } = require('crypto');

const TARGET_URL = process.env.TARGET_URL || 'http://localhost:8080/ingest';
const GEN_RPS = Number.parseInt(process.env.GEN_RPS || '100', 10);
const BATCH_SIZE = Number.parseInt(process.env.BATCH_SIZE || '50', 10);
const FAIL_RATE = Number.parseFloat(process.env.FAIL_RATE || '0.05');
const LATENCY_MODE = (process.env.LATENCY_MODE || 'normal').toLowerCase();

if (!Number.isFinite(GEN_RPS) || GEN_RPS <= 0) {
  throw new Error('GEN_RPS must be a positive integer');
}
if (!Number.isFinite(BATCH_SIZE) || BATCH_SIZE <= 0) {
  throw new Error('BATCH_SIZE must be a positive integer');
}
if (!Number.isFinite(FAIL_RATE) || FAIL_RATE < 0 || FAIL_RATE > 1) {
  throw new Error('FAIL_RATE must be between 0 and 1');
}
if (!['normal', 'heavy'].includes(LATENCY_MODE)) {
  throw new Error('LATENCY_MODE must be normal or heavy');
}

const STEPS = ['validate', 'payment', 'ship'];

let attemptedThisSec = 0;
let successThisSec = 0;
let failedThisSec = 0;

function randomChoice(arr) {
  return arr[Math.floor(Math.random() * arr.length)];
}

function randn() {
  let u = 0;
  let v = 0;
  while (u === 0) u = Math.random();
  while (v === 0) v = Math.random();
  return Math.sqrt(-2.0 * Math.log(u)) * Math.cos(2.0 * Math.PI * v);
}

function durationMs() {
  if (LATENCY_MODE === 'heavy') {
    const xm = 100;
    const alpha = 1.3;
    const u = 1 - Math.random();
    const pareto = xm / Math.pow(u, 1 / alpha);
    return Math.min(60000, Math.max(10, Math.round(pareto)));
  }

  const mu = Math.log(200);
  const sigma = 0.4;
  const logNormal = Math.exp(mu + sigma * randn());
  return Math.min(10000, Math.max(10, Math.round(logNormal)));
}

function buildEvent() {
  return {
    processInstanceId: randomUUID(),
    step: randomChoice(STEPS),
    status: Math.random() < FAIL_RATE ? 'failed' : 'completed',
    eventTime: new Date().toISOString(),
    durationMs: durationMs(),
  };
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function postJson(url, payload) {
  return new Promise((resolve, reject) => {
    const data = Buffer.from(JSON.stringify(payload));
    const req = http.request(url, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Content-Length': data.length,
      },
    }, (res) => {
      const chunks = [];
      res.on('data', (chunk) => chunks.push(chunk));
      res.on('end', () => {
        const body = Buffer.concat(chunks).toString('utf8');
        resolve({ statusCode: res.statusCode || 0, body });
      });
    });

    req.on('error', reject);
    req.write(data);
    req.end();
  });
}

async function sendBatchWithRetry(batch) {
  const maxRetries = 3;
  for (let attempt = 0; attempt <= maxRetries; attempt += 1) {
    attemptedThisSec += 1;
    try {
      const res = await postJson(TARGET_URL, { events: batch });
      if (res.statusCode >= 200 && res.statusCode < 300) {
        successThisSec += 1;
        return true;
      }

      if (res.statusCode >= 500 && res.statusCode < 600 && attempt < maxRetries) {
        const baseDelay = 200 * Math.pow(2, attempt);
        const jitter = Math.floor(Math.random() * 200);
        await sleep(baseDelay + jitter);
        continue;
      }

      failedThisSec += 1;
      return false;
    } catch (err) {
      if (attempt < maxRetries) {
        const baseDelay = 200 * Math.pow(2, attempt);
        const jitter = Math.floor(Math.random() * 200);
        await sleep(baseDelay + jitter);
        continue;
      }

      failedThisSec += 1;
      return false;
    }
  }

  failedThisSec += 1;
  return false;
}

async function sendTick() {
  const events = Array.from({ length: GEN_RPS }, () => buildEvent());
  const batches = [];
  for (let i = 0; i < events.length; i += BATCH_SIZE) {
    batches.push(events.slice(i, i + BATCH_SIZE));
  }

  const pending = batches.map((batch) => sendBatchWithRetry(batch));
  await Promise.allSettled(pending);
}

setInterval(() => {
  const now = new Date().toISOString();
  console.log(
    `[${now}] attempted sends/sec=${attemptedThisSec} ` +
      `successful sends/sec=${successThisSec} failed sends=${failedThisSec}`
  );
  attemptedThisSec = 0;
  successThisSec = 0;
  failedThisSec = 0;
}, 1000);

setInterval(() => {
  sendTick().catch((err) => {
    const now = new Date().toISOString();
    console.error(`[${now}] tick error`, err);
  });
}, 1000);

console.log(`Sending ${GEN_RPS} events/sec in batches of ${BATCH_SIZE} to ${TARGET_URL}`);
