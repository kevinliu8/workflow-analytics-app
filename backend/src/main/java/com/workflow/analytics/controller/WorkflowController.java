package com.workflow.analytics.controller;

import com.workflow.analytics.metrics.MetricsSummary;
import com.workflow.analytics.metrics.MetricsTracker;
import com.workflow.analytics.model.WorkflowEvent;
import com.workflow.analytics.queue.EventQueue;
import com.workflow.analytics.service.IngestService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class WorkflowController {
    private final IngestService ingestService;
    private final MetricsTracker metricsTracker;
    private final EventQueue eventQueue;

    public WorkflowController(IngestService ingestService,
                              MetricsTracker metricsTracker,
                              EventQueue eventQueue) {
        this.ingestService = ingestService;
        this.metricsTracker = metricsTracker;
        this.eventQueue = eventQueue;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(@RequestBody List<WorkflowEvent> events) {
        ingestService.ingest(events);
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "ok");
    }

    @GetMapping("/metrics/summary")
    public MetricsSummary summary() {
        return metricsTracker.snapshot(eventQueue.depth());
    }
}
