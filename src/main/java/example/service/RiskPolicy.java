package example.service;

import example.domain.PaymentEvent;
import example.domain.RiskDecision;
import java.math.BigDecimal;

public final class RiskPolicy {
    private static final BigDecimal REVIEW_LIMIT = new BigDecimal("10000");

    public RiskDecision decide(PaymentEvent event) {
        if (event.amount().compareTo(REVIEW_LIMIT) > 0) return new RiskDecision(false, "manual-review-limit");
        if (event.international() && event.amount().compareTo(new BigDecimal("5000")) > 0)
            return new RiskDecision(false, "international-review");
        return new RiskDecision(true, "approved");
    }
}
