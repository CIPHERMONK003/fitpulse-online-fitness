package com.fitness.concurrency;

import java.sql.Timestamp;

/**
 * Task item processed asynchronously by the background notification worker thread.
 */
public class NotificationTask {
    private final String recipient;
    private final String recipientName;
    private final String subject;
    private final String body;
    private final String channel; // IN_APP, EMAIL_SIMULATION, PUSH
    private final Timestamp createdAt;

    public NotificationTask(String recipient, String recipientName, String subject, String body, String channel) {
        this.recipient = recipient;
        this.recipientName = recipientName;
        this.subject = subject;
        this.body = body;
        this.channel = channel;
        this.createdAt = new Timestamp(System.currentTimeMillis());
    }

    public String getRecipient() {
        return recipient;
    }

    public String getRecipientName() {
        return recipientName;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public String getChannel() {
        return channel;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return String.format("[%s] To: %s (%s) | Subject: '%s' | Time: %s",
                channel, recipientName, recipient, subject, createdAt);
    }
}
