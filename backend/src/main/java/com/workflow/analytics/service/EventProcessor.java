package com.workflow.analytics.service;

import com.workflow.analytics.metrics.MetricsTracker;
import com.workflow.analytics.model.WorkflowEvent;
import com.workflow.analytics.queue.EventQueue;
import com.workflow.analytics.store.EventStore;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class EventProcessor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EventProcessor.class);

    private final EventQueue eventQueue;
    private final MetricsTracker metricsTracker;
    private final EventStore eventStore;
    private final ExecutorService executor;
    private final int processDelayMs;
    private final int workerCount;

    public EventProcessor(EventQueue eventQueue,
                          MetricsTracker metricsTracker,
                          EventStore eventStore,
                          @Value("${PROCESS_DELAY_MS:0}") int processDelayMs,
                          @Value("${WORKER_COUNT:2}") int workerCount) {
        this.eventQueue = eventQueue;
        this.metricsTracker = metricsTracker;
        this.eventStore = eventStore;
        this.processDelayMs = processDelayMs;
        this.workerCount = workerCount;
        this.executor = Executors.newFixedThreadPool(workerCount, new WorkerThreadFactory());
    }

    @PostConstruct
    public void startWorkers() {
        for (int i = 0; i < workerCount; i++) {
            executor.submit(this::runWorker);
        }
        LOGGER.info("Started {} worker(s) with PROCESS_DELAY_MS={}", workerCount, processDelayMs);
    }

    @PreDestroy
    public void stopWorkers() {
        executor.shutdownNow();
        try {
            if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                LOGGER.warn("Worker shutdown timed out");
            }
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }

    private void runWorker() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                WorkflowEvent event = eventQueue.take();
                if (processDelayMs > 0) {
                    Thread.sleep(processDelayMs);
                }
                eventStore.addProcessed(event);
                metricsTracker.recordProcessed(event, System.currentTimeMillis());
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                LOGGER.error("Worker error", ex);
            }
        }
    }

    private static class WorkerThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();

        @Override
        public Thread newThread(Runnable runnable) {
            Thread thread = new Thread(runnable);
            thread.setName("event-worker-" + counter.incrementAndGet());
            thread.setDaemon(true);
            return thread;
        }
    }
}
