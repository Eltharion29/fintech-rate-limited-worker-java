package example;

import example.domain.PaymentEvent;
import example.service.RiskPolicy;
import java.math.BigDecimal;

public final class RiskPolicyTest {
    public static void main(String[] args) {
        var decision = new RiskPolicy().decide(new PaymentEvent("p-1", new BigDecimal("12000"), "USD", false));
        if (decision.approved() || !decision.reason().equals("manual-review-limit")) throw new AssertionError(decision);
        System.out.println("RiskPolicyTest passed");
    }
}
