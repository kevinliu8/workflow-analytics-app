package com.workflow.analytics.queue;

import com.workflow.analytics.model.WorkflowEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.concurrent.ArrayBlockingQueue;

@Component
public class EventQueue {
    private final ArrayBlockingQueue<WorkflowEvent> queue;

    public EventQueue(@Value("${QUEUE_CAPACITY:1000}") int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
    }

    public boolean offer(WorkflowEvent event) {
        return queue.offer(event);
    }

    public WorkflowEvent take() throws InterruptedException {
        return queue.take();
    }

    public int depth() {
        return queue.size();
    }
}
