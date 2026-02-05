package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Event published when a charge is applied during calculation.
 * Provides detailed information about the charge application for auditing.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * @EventListener
 * public void onChargeApplied(ChargeAppliedEvent event) {
 *     log.debug("Charge applied: {} = {}",
 *               event.getCharge().getCode(),
 *               event.getCalculatedAmount());
 * }
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class ChargeAppliedEvent implements Serializable {

    private final Charge charge;
    private final Money baseAmount;
    private final Money calculatedAmount;
    private final Object context;
    private final LocalDateTime timestamp;

    /**
     * Creates a new charge applied event.
     *
     * @param charge the charge that was applied
     * @param baseAmount the base amount used for calculation
     * @param calculatedAmount the resulting charge amount
     * @param context the calculation context (document or line)
     */
    public ChargeAppliedEvent(Charge charge, Money baseAmount, Money calculatedAmount, Object context) {
        this.charge = Objects.requireNonNull(charge, "Charge cannot be null");
        this.baseAmount = Objects.requireNonNull(baseAmount, "Base amount cannot be null");
        this.calculatedAmount = Objects.requireNonNull(calculatedAmount, "Calculated amount cannot be null");
        this.context = context;
        this.timestamp = LocalDateTime.now();
    }

    public Charge getCharge() {
        return charge;
    }

    public Money getBaseAmount() {
        return baseAmount;
    }

    public Money getCalculatedAmount() {
        return calculatedAmount;
    }

    public Object getContext() {
        return context;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "ChargeAppliedEvent{" +
               "charge=" + charge.getCode() +
               ", base=" + baseAmount +
               ", calculated=" + calculatedAmount +
               ", timestamp=" + timestamp +
               '}';
    }
}
