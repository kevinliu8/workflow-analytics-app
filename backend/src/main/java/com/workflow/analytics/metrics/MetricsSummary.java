package com.workflow.analytics.metrics;

public class MetricsSummary {
    private double ingestThroughput;
    private double processedThroughput;
    private double errorRate;
    private int queueDepth;
    private int p95Latency;
    private int p99Latency;

    public MetricsSummary() {
    }

    public MetricsSummary(double ingestThroughput, double processedThroughput, double errorRate,
                          int queueDepth, int p95Latency, int p99Latency) {
        this.ingestThroughput = ingestThroughput;
        this.processedThroughput = processedThroughput;
        this.errorRate = errorRate;
        this.queueDepth = queueDepth;
        this.p95Latency = p95Latency;
        this.p99Latency = p99Latency;
    }

    public double getIngestThroughput() {
        return ingestThroughput;
    }

    public void setIngestThroughput(double ingestThroughput) {
        this.ingestThroughput = ingestThroughput;
    }

    public double getProcessedThroughput() {
        return processedThroughput;
    }

    public void setProcessedThroughput(double processedThroughput) {
        this.processedThroughput = processedThroughput;
    }

    public double getErrorRate() {
        return errorRate;
    }

    public void setErrorRate(double errorRate) {
        this.errorRate = errorRate;
    }

    public int getQueueDepth() {
        return queueDepth;
    }

    public void setQueueDepth(int queueDepth) {
        this.queueDepth = queueDepth;
    }

    public int getP95Latency() {
        return p95Latency;
    }

    public void setP95Latency(int p95Latency) {
        this.p95Latency = p95Latency;
    }

    public int getP99Latency() {
        return p99Latency;
    }

    public void setP99Latency(int p99Latency) {
        this.p99Latency = p99Latency;
    }
}
