package tools.dynamia.modules.finances.api;

import java.util.Comparator;

/**
 * Comparator for sorting charges by priority.
 * Lower priority values are applied first.
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * List<Charge> charges = document.getCharges();
 * charges.sort(new ChargeComparator());
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class ChargeComparator implements Comparator<Charge> {

    @Override
    public int compare(Charge c1, Charge c2) {
        // First compare by priority (ascending)
        int priorityCompare = Integer.compare(
            c1.getPriority() != null ? c1.getPriority() : 100,
            c2.getPriority() != null ? c2.getPriority() : 100
        );

        if (priorityCompare != 0) {
            return priorityCompare;
        }

        // If same priority, order by type (TAX before WITHHOLDING)
        if (c1.getType() != null && c2.getType() != null) {
            return c1.getType().compareTo(c2.getType());
        }

        // Finally by code for consistency
        if (c1.getCode() != null && c2.getCode() != null) {
            return c1.getCode().compareTo(c2.getCode());
        }

        return 0;
    }
}
