package com.workflow.analytics.store;

import com.workflow.analytics.model.WorkflowEvent;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Component
public class EventStore {
    private final List<WorkflowEvent> receivedEvents = new CopyOnWriteArrayList<>();
    private final List<WorkflowEvent> processedEvents = new CopyOnWriteArrayList<>();

    public void addReceived(WorkflowEvent event) {
        receivedEvents.add(event);
    }

    public void addProcessed(WorkflowEvent event) {
        processedEvents.add(event);
    }
}
