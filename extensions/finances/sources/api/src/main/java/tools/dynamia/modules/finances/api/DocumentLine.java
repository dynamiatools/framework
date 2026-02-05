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
 * <p>Basic example usage:</p>
 * <pre>{@code
 * DocumentLine line = new DocumentLine();
 * line.setDescription("Product A");
 * line.setQuantity(new BigDecimal("10"));
 * line.setUnitPrice(Money.of("100", "USD"));
 *
 * // Simplified methods for charges
 * line.addTax("VAT19", "VAT 19%", new BigDecimal("19"));
 * line.addDiscount("DISC5", "Line Discount", new BigDecimal("5"));
 * line.addFee("HANDLING", "Handling Fee", new BigDecimal("10.00"));
 *
 * // Using fluent API for method chaining
 * DocumentLine line2 = DocumentLine.of("Product B", new BigDecimal("5"), Money.of("200", "USD"))
 *     .tax("VAT19", "VAT 19%", new BigDecimal("19"))
 *     .discount("DISC10", "Volume Discount", new BigDecimal("10"))
 *     .itemCode("PROD-B");
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
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

    // Convenience methods for specific charge types

    /**
     * Adds a tax charge to this line with simplified parameters.
     *
     * @param code the tax code (e.g., "VAT19")
     * @param name the tax name (e.g., "Value Added Tax 19%")
     * @param percentage the tax percentage
     * @return the created Charge instance
     */
    public Charge addTax(String code, String name, BigDecimal percentage) {
        Charge tax = Charge.percentage(code, name, ChargeType.TAX, percentage, 20);
        addCharge(tax);
        return tax;
    }

    /**
     * Adds a tax charge with custom priority.
     *
     * @param code the tax code
     * @param name the tax name
     * @param percentage the tax percentage
     * @param priority the application priority
     * @return the created Charge instance
     */
    public Charge addTax(String code, String name, BigDecimal percentage, int priority) {
        Charge tax = Charge.percentage(code, name, ChargeType.TAX, percentage, priority);
        addCharge(tax);
        return tax;
    }

    /**
     * Gets all tax charges from this line (line-specific only, not document-level).
     *
     * @return list of tax charges
     */
    public List<Charge> getTaxes() {
        if (charges == null) {
            return Collections.emptyList();
        }
        return charges.stream()
                .filter(c -> c.getType() == ChargeType.TAX)
                .toList();
    }

    /**
     * Adds a discount charge to this line with simplified parameters.
     *
     * @param code the discount code (e.g., "DISC10")
     * @param name the discount name (e.g., "10% Volume Discount")
     * @param percentage the discount percentage
     * @return the created Charge instance
     */
    public Charge addDiscount(String code, String name, BigDecimal percentage) {
        Charge discount = Charge.percentage(code, name, ChargeType.DISCOUNT, percentage, 10);
        addCharge(discount);
        return discount;
    }

    /**
     * Adds a discount charge with custom priority.
     *
     * @param code the discount code
     * @param name the discount name
     * @param percentage the discount percentage
     * @param priority the application priority
     * @return the created Charge instance
     */
    public Charge addDiscount(String code, String name, BigDecimal percentage, int priority) {
        Charge discount = Charge.percentage(code, name, ChargeType.DISCOUNT, percentage, priority);
        addCharge(discount);
        return discount;
    }

    /**
     * Adds a fixed amount discount.
     *
     * @param code the discount code
     * @param name the discount name
     * @param amount the fixed discount amount
     * @return the created Charge instance
     */
    public Charge addFixedDiscount(String code, String name, BigDecimal amount) {
        Charge discount = Charge.fixed(code, name, ChargeType.DISCOUNT, amount, 10);
        addCharge(discount);
        return discount;
    }

    /**
     * Gets all discount charges from this line (line-specific only, not document-level).
     *
     * @return list of discount charges
     */
    public List<Charge> getDiscounts() {
        if (charges == null) {
            return Collections.emptyList();
        }
        return charges.stream()
                .filter(c -> c.getType() == ChargeType.DISCOUNT)
                .toList();
    }

    /**
     * Adds a withholding charge to this line with simplified parameters.
     *
     * @param code the withholding code (e.g., "RET_IVA")
     * @param name the withholding name (e.g., "IVA Withholding")
     * @param percentage the withholding percentage
     * @return the created Charge instance
     */
    public Charge addWithholding(String code, String name, BigDecimal percentage) {
        Charge withholding = Charge.percentage(code, name, ChargeType.WITHHOLDING, percentage, 30);
        addCharge(withholding);
        return withholding;
    }

    /**
     * Adds a withholding charge with custom priority.
     *
     * @param code the withholding code
     * @param name the withholding name
     * @param percentage the withholding percentage
     * @param priority the application priority
     * @return the created Charge instance
     */
    public Charge addWithholding(String code, String name, BigDecimal percentage, int priority) {
        Charge withholding = Charge.percentage(code, name, ChargeType.WITHHOLDING, percentage, priority);
        addCharge(withholding);
        return withholding;
    }

    /**
     * Gets all withholding charges from this line (line-specific only, not document-level).
     *
     * @return list of withholding charges
     */
    public List<Charge> getWithholdings() {
        if (charges == null) {
            return Collections.emptyList();
        }
        return charges.stream()
                .filter(c -> c.getType() == ChargeType.WITHHOLDING)
                .toList();
    }

    /**
     * Adds a fee charge to this line with simplified parameters.
     *
     * @param code the fee code (e.g., "SHIP")
     * @param name the fee name (e.g., "Shipping Fee")
     * @param amount the fixed fee amount
     * @return the created Charge instance
     */
    public Charge addFee(String code, String name, BigDecimal amount) {
        Charge fee = Charge.fixed(code, name, ChargeType.FEE, amount, 5);
        addCharge(fee);
        return fee;
    }

    /**
     * Adds a percentage-based fee charge.
     *
     * @param code the fee code
     * @param name the fee name
     * @param percentage the fee percentage
     * @return the created Charge instance
     */
    public Charge addPercentageFee(String code, String name, BigDecimal percentage) {
        Charge fee = Charge.percentage(code, name, ChargeType.FEE, percentage, 5);
        addCharge(fee);
        return fee;
    }

    /**
     * Gets all fee charges from this line (line-specific only, not document-level).
     *
     * @return list of fee charges
     */
    public List<Charge> getFees() {
        if (charges == null) {
            return Collections.emptyList();
        }
        return charges.stream()
                .filter(c -> c.getType() == ChargeType.FEE)
                .toList();
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

    // Fluent API methods for method chaining

    /**
     * Sets the line description and returns this instance for chaining.
     *
     * @param description the line description
     * @return this DocumentLine instance
     */
    public DocumentLine description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the quantity and returns this instance for chaining.
     *
     * @param quantity the quantity
     * @return this DocumentLine instance
     */
    public DocumentLine quantity(BigDecimal quantity) {
        this.quantity = quantity;
        return this;
    }

    /**
     * Sets the unit price and returns this instance for chaining.
     *
     * @param unitPrice the unit price
     * @return this DocumentLine instance
     */
    public DocumentLine unitPrice(Money unitPrice) {
        this.unitPrice = unitPrice;
        return this;
    }

    /**
     * Sets the item code and returns this instance for chaining.
     *
     * @param itemCode the item code
     * @return this DocumentLine instance
     */
    public DocumentLine itemCode(String itemCode) {
        this.itemCode = itemCode;
        return this;
    }

    /**
     * Sets the item name and returns this instance for chaining.
     *
     * @param itemName the item name
     * @return this DocumentLine instance
     */
    public DocumentLine itemName(String itemName) {
        this.itemName = itemName;
        return this;
    }

    /**
     * Sets the line number and returns this instance for chaining.
     *
     * @param lineNumber the line number
     * @return this DocumentLine instance
     */
    public DocumentLine lineNumber(Integer lineNumber) {
        this.lineNumber = lineNumber;
        return this;
    }

    /**
     * Adds a charge and returns this instance for chaining.
     *
     * @param charge the charge to add
     * @return this DocumentLine instance
     */
    public DocumentLine charge(Charge charge) {
        addCharge(charge);
        return this;
    }

    /**
     * Adds a tax and returns this instance for chaining.
     *
     * @param code the tax code
     * @param name the tax name
     * @param percentage the tax percentage
     * @return this DocumentLine instance
     */
    public DocumentLine tax(String code, String name, BigDecimal percentage) {
        addTax(code, name, percentage);
        return this;
    }

    /**
     * Adds a discount and returns this instance for chaining.
     *
     * @param code the discount code
     * @param name the discount name
     * @param percentage the discount percentage
     * @return this DocumentLine instance
     */
    public DocumentLine discount(String code, String name, BigDecimal percentage) {
        addDiscount(code, name, percentage);
        return this;
    }

    /**
     * Adds a withholding and returns this instance for chaining.
     *
     * @param code the withholding code
     * @param name the withholding name
     * @param percentage the withholding percentage
     * @return this DocumentLine instance
     */
    public DocumentLine withholding(String code, String name, BigDecimal percentage) {
        addWithholding(code, name, percentage);
        return this;
    }

    /**
     * Adds a fee and returns this instance for chaining.
     *
     * @param code the fee code
     * @param name the fee name
     * @param amount the fee amount
     * @return this DocumentLine instance
     */
    public DocumentLine fee(String code, String name, BigDecimal amount) {
        addFee(code, name, amount);
        return this;
    }

    /**
     * Sets the id and returns this instance for chaining.
     *
     * @param id the line id
     * @return this DocumentLine instance
     */
    public DocumentLine id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Creates a deep copy of this line.
     * The copy will have:
     * - A new null ID (must be set by the caller)
     * - All basic properties copied
     * - All charges copied (deep copy)
     * - Totals reset to null (must be recalculated)
     * - Document reference set to null (must be set when adding to document)
     *
     * @return a new DocumentLine instance with copied data
     */
    public DocumentLine copy() {
        DocumentLine copy = new DocumentLine();

        // Copy basic properties
        copy.description = this.description;
        copy.quantity = this.quantity;
        copy.unitPrice = this.unitPrice != null ? this.unitPrice.copy() : null;
        copy.itemCode = this.itemCode;
        copy.itemName = this.itemName;
        copy.lineNumber = this.lineNumber;

        // Deep copy charges
        if (this.charges != null) {
            copy.charges = new ArrayList<>();
            for (Charge charge : this.charges) {
                copy.charges.add(charge.copy());
            }
        }

        // Reset references
        copy.id = null;
        copy.document = null;
        copy.totals = null;

        return copy;
    }

    @Override
    public String toString() {
        return description + " (" + quantity + " × " + unitPrice + ")";
    }
}
