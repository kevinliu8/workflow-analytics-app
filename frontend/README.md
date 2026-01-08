# Workflow Analytics Dashboard

Minimal React (Vite) dashboard for polling workflow analytics metrics.

## Prerequisites
- Backend running at `http://localhost:8080`

## Run locally
- Install deps: `npm install`
- Start dev server: `npm run dev`

## Notes
- Polls `GET /metrics/summary` every 3 seconds
- Vite dev server proxies `/metrics` to `http://localhost:8080`
- Metrics shown: ingest throughput, processed throughput, error rate, queue depth, p95/p99 latency
