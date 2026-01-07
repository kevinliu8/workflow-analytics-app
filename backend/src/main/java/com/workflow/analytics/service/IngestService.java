package com.workflow.analytics.service;

import com.workflow.analytics.metrics.MetricsTracker;
import com.workflow.analytics.model.WorkflowEvent;
import com.workflow.analytics.queue.EventQueue;
import com.workflow.analytics.store.EventStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class IngestService {
    private static final Logger LOGGER = LoggerFactory.getLogger(IngestService.class);

    private final EventQueue eventQueue;
    private final MetricsTracker metricsTracker;
    private final EventStore eventStore;

    public IngestService(EventQueue eventQueue, MetricsTracker metricsTracker, EventStore eventStore) {
        this.eventQueue = eventQueue;
        this.metricsTracker = metricsTracker;
        this.eventStore = eventStore;
    }

    public void ingest(List<WorkflowEvent> events) {
        long now = System.currentTimeMillis();
        for (WorkflowEvent event : events) {
            metricsTracker.recordReceived(now);
            eventStore.addReceived(event);
            boolean enqueued = eventQueue.offer(event);
            if (!enqueued) {
                LOGGER.warn("Queue full, dropping event for processInstanceId={}", event.getProcessInstanceId());
            }
        }
    }
}
