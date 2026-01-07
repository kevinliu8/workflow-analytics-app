package com.workflow.analytics.metrics;

import com.workflow.analytics.model.Status;
import com.workflow.analytics.model.WorkflowEvent;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicLong;

@Component
public class MetricsTracker {
    private static final long THROUGHPUT_WINDOW_MS = 60_000L;

    private final AtomicLong totalReceived = new AtomicLong();
    private final AtomicLong totalProcessed = new AtomicLong();
    private final AtomicLong totalFailed = new AtomicLong();
    private final ConcurrentLinkedQueue<Long> ingestTimestamps = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<Long> processedTimestamps = new ConcurrentLinkedQueue<>();
    private final List<Integer> processedDurations = Collections.synchronizedList(new ArrayList<>());

    public void recordReceived(long timestampMs) {
        totalReceived.incrementAndGet();
        ingestTimestamps.add(timestampMs);
        pruneOld(ingestTimestamps, timestampMs - THROUGHPUT_WINDOW_MS);
    }

    public void recordProcessed(WorkflowEvent event, long timestampMs) {
        totalProcessed.incrementAndGet();
        if (event.getStatus() == Status.FAIL) {
            totalFailed.incrementAndGet();
        }
        processedTimestamps.add(timestampMs);
        processedDurations.add(event.getDurationMs());
        pruneOld(processedTimestamps, timestampMs - THROUGHPUT_WINDOW_MS);
    }

    public MetricsSummary snapshot(int queueDepth) {
        long now = System.currentTimeMillis();
        double ingestThroughput = throughputPerSecond(ingestTimestamps, now);
        double processedThroughput = throughputPerSecond(processedTimestamps, now);
        double errorRate = totalProcessed.get() == 0 ? 0.0
                : (double) totalFailed.get() / (double) totalProcessed.get();
        int p95 = percentile(processedDurations, 95);
        int p99 = percentile(processedDurations, 99);
        return new MetricsSummary(ingestThroughput, processedThroughput, errorRate, queueDepth, p95, p99);
    }

    private double throughputPerSecond(ConcurrentLinkedQueue<Long> timestamps, long now) {
        pruneOld(timestamps, now - THROUGHPUT_WINDOW_MS);
        return (double) timestamps.size() / (THROUGHPUT_WINDOW_MS / 1000.0);
    }

    private void pruneOld(ConcurrentLinkedQueue<Long> timestamps, long cutoff) {
        while (true) {
            Long peek = timestamps.peek();
            if (peek == null || peek >= cutoff) {
                return;
            }
            timestamps.poll();
        }
    }

    private int percentile(List<Integer> values, int percentile) {
        if (values.isEmpty()) {
            return 0;
        }
        List<Integer> snapshot;
        synchronized (values) {
            snapshot = new ArrayList<>(values);
        }
        Collections.sort(snapshot);
        int index = (int) Math.ceil((percentile / 100.0) * snapshot.size()) - 1;
        if (index < 0) {
            index = 0;
        }
        if (index >= snapshot.size()) {
            index = snapshot.size() - 1;
        }
        return snapshot.get(index);
    }
}
