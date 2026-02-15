package yuphy.outbox.starter.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "outbox")
public class OutboxProperties {

    private String topic = "outbox.events";
    private Publisher publisher = new Publisher();

    public String getTopic() {
        return topic;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public Publisher getPublisher() {
        return publisher;
    }

    public void setPublisher(Publisher publisher) {
        this.publisher = publisher;
    }

    public static class Publisher {
        private boolean enabled = true;
        private int batchSize = 100;
        private long pollIntervalMs = 1000;
        private long sendTimeoutMs = 5000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public int getBatchSize() {
            return batchSize;
        }

        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }

        public long getPollIntervalMs() {
            return pollIntervalMs;
        }

        public void setPollIntervalMs(long pollIntervalMs) {
            this.pollIntervalMs = pollIntervalMs;
        }

        public long getSendTimeoutMs() {
            return sendTimeoutMs;
        }

        public void setSendTimeoutMs(long sendTimeoutMs) {
            this.sendTimeoutMs = sendTimeoutMs;
        }
    }
}
