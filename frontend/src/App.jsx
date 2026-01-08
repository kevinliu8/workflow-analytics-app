import { useEffect, useState } from "react";

const POLL_INTERVAL_MS = 3000;
const ENDPOINT = "/metrics/summary";

const formatNumber = (value) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return "-";
  }
  return new Intl.NumberFormat("en-US", {
    maximumFractionDigits: 2,
  }).format(value);
};

const formatPercent = (value) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return "-";
  }
  return `${formatNumber(value)}%`;
};

export default function App() {
  const [metrics, setMetrics] = useState(null);
  const [status, setStatus] = useState("idle");
  const [error, setError] = useState(null);

  useEffect(() => {
    let isMounted = true;
    let intervalId;

    const fetchMetrics = async () => {
      setStatus((prev) => (prev === "idle" ? "loading" : prev));
      setError(null);

      try {
        const response = await fetch(ENDPOINT);
        if (!response.ok) {
          throw new Error(`Request failed with ${response.status}`);
        }
        const data = await response.json();
        if (isMounted) {
          setMetrics(data);
          setStatus("ready");
        }
      } catch (fetchError) {
        if (isMounted) {
          setStatus("error");
          setError(fetchError);
        }
      }
    };

    fetchMetrics();
    intervalId = window.setInterval(fetchMetrics, POLL_INTERVAL_MS);

    return () => {
      isMounted = false;
      if (intervalId) {
        window.clearInterval(intervalId);
      }
    };
  }, []);

  return (
    <div className="app">
      <header className="header">
        <div>
          <h1>Workflow Analytics</h1>
          <p>Realtime summary from the ingestion pipeline.</p>
        </div>
        <div className="status">
          <span className={`status-dot ${status}`} />
          <span className="status-label">{status}</span>
        </div>
      </header>

      {error ? (
        <div className="error">{error.message}</div>
      ) : null}

      <section className="grid">
        <div className="card">
          <h2>Ingest throughput</h2>
          <p className="value">{formatNumber(metrics?.ingestThroughput)}</p>
          <p className="unit">events/sec</p>
        </div>
        <div className="card">
          <h2>Processed throughput</h2>
          <p className="value">{formatNumber(metrics?.processedThroughput)}</p>
          <p className="unit">events/sec</p>
        </div>
        <div className="card">
          <h2>Error rate</h2>
          <p className="value">{formatPercent(metrics?.errorRate)}</p>
          <p className="unit">of events</p>
        </div>
        <div className="card">
          <h2>Queue depth (saturation)</h2>
          <p className="value">{formatNumber(metrics?.queueDepth)}</p>
          <p className="unit">messages</p>
        </div>
        <div className="card">
          <h2>P95 latency</h2>
          <p className="value">{formatNumber(metrics?.p95Latency)}</p>
          <p className="unit">ms</p>
        </div>
        <div className="card">
          <h2>P99 latency</h2>
          <p className="value">{formatNumber(metrics?.p99Latency)}</p>
          <p className="unit">ms</p>
        </div>
      </section>
    </div>
  );
}
