package example;

import example.config.WorkerConfig;
import example.infra.InfraiQueueClient;
import example.service.RiskPolicy;

public final class QueueWorker {
    public static void main(String[] args) {
        WorkerConfig config = WorkerConfig.fromEnvironment();
        InfraiQueueClient queue = new InfraiQueueClient(config);
        RiskPolicy policy = new RiskPolicy();
        System.out.println("worker ready: " + queue.consume());
        System.out.println("risk policy loaded: " + policy.decide(new example.domain.PaymentEvent("demo", java.math.BigDecimal.valueOf(120), "USD", false)));
    }
}
