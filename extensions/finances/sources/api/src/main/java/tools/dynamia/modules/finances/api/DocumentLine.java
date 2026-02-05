package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single line in a financial document.
 * Each line contains a description, quantity, unit price, and can have charges applied.
 *
 * <p>Lines are calculated independently and their totals are aggregated at the document level.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DocumentLine line = new DocumentLine();
 * line.setDescription("Product A");
 * line.setQuantity(new BigDecimal("10"));
 * line.setUnitPrice(Money.of("100", "USD"));
 *
 * Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX,
 *                                 new BigDecimal("19"), 20);
 * line.addCharge(vat);
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class DocumentLine implements Serializable {

    private String id;
    private String description;
    private BigDecimal quantity;
    private Money unitPrice;
    private List<Charge> charges;
    private LineTotals totals;
    private FinancialDocument document;
    private Integer lineNumber;
    private String itemCode;
    private String itemName;

    /**
     * Default constructor.
     */
    public DocumentLine() {
        this.charges = new ArrayList<>();
        this.quantity = BigDecimal.ONE;
    }

    /**
     * Creates a document line with description, quantity and unit price.
     *
     * @param description the line description
     * @param quantity the quantity
     * @param unitPrice the unit price
     * @return a new DocumentLine instance
     */
    public static DocumentLine of(String description, BigDecimal quantity, Money unitPrice) {
        DocumentLine line = new DocumentLine();
        line.setDescription(description);
        line.setQuantity(quantity);
        line.setUnitPrice(unitPrice);
        return line;
    }

    /**
     * Calculates the base amount (quantity × unit price) without any charges.
     *
     * @return the base amount
     */
    public Money getBaseAmount() {
        if (unitPrice == null || quantity == null) {
            return Money.zero(unitPrice != null ? unitPrice.getCurrencyCode() : "USD");
        }
        return unitPrice.multiply(quantity);
    }

    /**
     * Adds a charge to this line.
     *
     * @param charge the charge to add
     */
    public void addCharge(Charge charge) {
        Objects.requireNonNull(charge, "Charge cannot be null");
        if (this.charges == null) {
            this.charges = new ArrayList<>();
        }
        this.charges.add(charge);
    }

    /**
     * Removes a charge from this line.
     *
     * @param charge the charge to remove
     */
    public void removeCharge(Charge charge) {
        if (this.charges != null) {
            this.charges.remove(charge);
        }
    }

    /**
     * Gets all charges for this line, including inherited document charges.
     *
     * @return unmodifiable list of charges
     */
    public List<Charge> getAllCharges() {
        List<Charge> allCharges = new ArrayList<>();

        if (this.charges != null) {
            allCharges.addAll(this.charges);
        }

        // Add document-level charges that apply to lines
        if (this.document != null && this.document.getCharges() != null) {
            this.document.getCharges().stream()
                .filter(c -> c.getAppliesTo() == ChargeAppliesTo.LINE)
                .forEach(allCharges::add);
        }

        return Collections.unmodifiableList(allCharges);
    }

    /**
     * Validates that this line has all required fields.
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        Objects.requireNonNull(description, "Line description is required");
        Objects.requireNonNull(quantity, "Line quantity is required");
        Objects.requireNonNull(unitPrice, "Line unit price is required");

        if (quantity.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Line quantity cannot be negative");
        }
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public Money getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
    }

    public List<Charge> getCharges() {
        return charges != null ? Collections.unmodifiableList(charges) : Collections.emptyList();
    }

    public void setCharges(List<Charge> charges) {
        this.charges = charges != null ? new ArrayList<>(charges) : new ArrayList<>();
    }

    public LineTotals getTotals() {
        return totals;
    }

    public void setTotals(LineTotals totals) {
        this.totals = totals;
    }

    public FinancialDocument getDocument() {
        return document;
    }

    public void setDocument(FinancialDocument document) {
        this.document = document;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public void setLineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
    }

    public String getItemCode() {
        return itemCode;
    }

    public void setItemCode(String itemCode) {
        this.itemCode = itemCode;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    @Override
    public String toString() {
        return description + " (" + quantity + " × " + unitPrice + ")";
    }
}
