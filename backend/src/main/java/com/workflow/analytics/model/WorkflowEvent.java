package com.workflow.analytics.model;

public class WorkflowEvent {
    private String processInstanceId;
    private String step;
    private Status status;
    private long eventTime;
    private int durationMs;

    public WorkflowEvent() {
    }

    public WorkflowEvent(String processInstanceId, String step, Status status, long eventTime, int durationMs) {
        this.processInstanceId = processInstanceId;
        this.step = step;
        this.status = status;
        this.eventTime = eventTime;
        this.durationMs = durationMs;
    }

    public String getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public long getEventTime() {
        return eventTime;
    }

    public void setEventTime(long eventTime) {
        this.eventTime = eventTime;
    }

    public int getDurationMs() {
        return durationMs;
    }

    public void setDurationMs(int durationMs) {
        this.durationMs = durationMs;
    }
}
