package example.domain;

import java.math.BigDecimal;

public record PaymentEvent(String paymentId, BigDecimal amount, String currency, boolean international) {}
