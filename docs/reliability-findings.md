# Workflow Analytics Reliability Findings

## System Overview
This system ingests workflow step events, processes them asynchronously, and exposes aggregated golden-signal metrics through an analytics API and dashboard.

The goal of this exercise was to understand how load, latency, errors, and saturation interact in a simplified analytics pipeline similar to Camunda Optimize.

---

## Golden Signals Tracked

**Latency**
- p95 and p99 processing latency derived from event duration

**Throughput**
- Ingest throughput: events accepted per second
- Processed throughput: events fully handled per second

**Errors**
- Percentage of failed workflow steps

**Saturation**
- Queue depth used as a proxy for system saturation and backpressure

---

## Experiments