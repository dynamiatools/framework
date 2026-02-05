package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a charge (tax, discount, withholding, or fee) that can be applied
 * to a financial document or document line.
 *
 * <p>This unified charge system handles all types of financial charges using
 * a single abstraction, making the framework flexible and extensible.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * Charge vat = new Charge();
 * vat.setCode("VAT19");
 * vat.setName("Value Added Tax 19%");
 * vat.setType(ChargeType.TAX);
 * vat.setRateType(RateType.PERCENTAGE);
 * vat.setValue(new BigDecimal("19"));
 * vat.setAppliesTo(ChargeAppliesTo.LINE);
 * vat.setBase(ChargeBase.NET);
 * vat.setPriority(20);
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class Charge implements Serializable {

    private String id;
    private String code;
    private String name;
    private ChargeType type;
    private RateType rateType;
    private BigDecimal value;
    private ChargeAppliesTo appliesTo;
    private ChargeBase base;
    private Boolean refundable;
    private Integer priority;
    private String description;

    /**
     * Default constructor.
     */
    public Charge() {
        this.refundable = false;
        this.priority = 100;
        this.base = ChargeBase.NET;
    }

    /**
     * Creates a percentage-based charge.
     *
     * @param code the charge code
     * @param name the charge name
     * @param type the charge type
     * @param percentage the percentage value
     * @param priority the application priority
     * @return a new Charge instance
     */
    public static Charge percentage(String code, String name, ChargeType type,
                                    BigDecimal percentage, int priority) {
        Charge charge = new Charge();
        charge.setCode(code);
        charge.setName(name);
        charge.setType(type);
        charge.setRateType(RateType.PERCENTAGE);
        charge.setValue(percentage);
        charge.setPriority(priority);
        charge.setAppliesTo(ChargeAppliesTo.LINE);
        return charge;
    }

    /**
     * Creates a fixed amount charge.
     *
     * @param code the charge code
     * @param name the charge name
     * @param type the charge type
     * @param amount the fixed amount
     * @param priority the application priority
     * @return a new Charge instance
     */
    public static Charge fixed(String code, String name, ChargeType type,
                              BigDecimal amount, int priority) {
        Charge charge = new Charge();
        charge.setCode(code);
        charge.setName(name);
        charge.setType(type);
        charge.setRateType(RateType.FIXED);
        charge.setValue(amount);
        charge.setPriority(priority);
        charge.setAppliesTo(ChargeAppliesTo.DOCUMENT);
        return charge;
    }

    /**
     * Validates that this charge has all required fields.
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        Objects.requireNonNull(code, "Charge code is required");
        Objects.requireNonNull(name, "Charge name is required");
        Objects.requireNonNull(type, "Charge type is required");
        Objects.requireNonNull(rateType, "Charge rate type is required");
        Objects.requireNonNull(value, "Charge value is required");
        Objects.requireNonNull(appliesTo, "Charge appliesTo is required");
        Objects.requireNonNull(base, "Charge base is required");
        Objects.requireNonNull(priority, "Charge priority is required");
    }

    /**
     * Checks if this charge is a tax.
     *
     * @return true if type is TAX
     */
    public boolean isTax() {
        return type == ChargeType.TAX;
    }

    /**
     * Checks if this charge is a discount.
     *
     * @return true if type is DISCOUNT
     */
    public boolean isDiscount() {
        return type == ChargeType.DISCOUNT;
    }

    /**
     * Checks if this charge is a withholding.
     *
     * @return true if type is WITHHOLDING
     */
    public boolean isWithholding() {
        return type == ChargeType.WITHHOLDING;
    }

    /**
     * Checks if this charge is a fee.
     *
     * @return true if type is FEE
     */
    public boolean isFee() {
        return type == ChargeType.FEE;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ChargeType getType() {
        return type;
    }

    public void setType(ChargeType type) {
        this.type = type;
    }

    public RateType getRateType() {
        return rateType;
    }

    public void setRateType(RateType rateType) {
        this.rateType = rateType;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public ChargeAppliesTo getAppliesTo() {
        return appliesTo;
    }

    public void setAppliesTo(ChargeAppliesTo appliesTo) {
        this.appliesTo = appliesTo;
    }

    public ChargeBase getBase() {
        return base;
    }

    public void setBase(ChargeBase base) {
        this.base = base;
    }

    public Boolean getRefundable() {
        return refundable;
    }

    public void setRefundable(Boolean refundable) {
        this.refundable = refundable;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    // Fluent API methods for method chaining

    /**
     * Sets the id and returns this instance for chaining.
     *
     * @param id the charge id
     * @return this Charge instance
     */
    public Charge id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the code and returns this instance for chaining.
     *
     * @param code the charge code
     * @return this Charge instance
     */
    public Charge code(String code) {
        this.code = code;
        return this;
    }

    /**
     * Sets the name and returns this instance for chaining.
     *
     * @param name the charge name
     * @return this Charge instance
     */
    public Charge name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the type and returns this instance for chaining.
     *
     * @param type the charge type
     * @return this Charge instance
     */
    public Charge type(ChargeType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the rate type and returns this instance for chaining.
     *
     * @param rateType the rate type
     * @return this Charge instance
     */
    public Charge rateType(RateType rateType) {
        this.rateType = rateType;
        return this;
    }

    /**
     * Sets the value and returns this instance for chaining.
     *
     * @param value the charge value
     * @return this Charge instance
     */
    public Charge value(BigDecimal value) {
        this.value = value;
        return this;
    }

    /**
     * Sets the appliesTo and returns this instance for chaining.
     *
     * @param appliesTo where the charge applies to
     * @return this Charge instance
     */
    public Charge appliesTo(ChargeAppliesTo appliesTo) {
        this.appliesTo = appliesTo;
        return this;
    }

    /**
     * Sets the base and returns this instance for chaining.
     *
     * @param base the charge base
     * @return this Charge instance
     */
    public Charge base(ChargeBase base) {
        this.base = base;
        return this;
    }

    /**
     * Sets the refundable flag and returns this instance for chaining.
     *
     * @param refundable whether the charge is refundable
     * @return this Charge instance
     */
    public Charge refundable(Boolean refundable) {
        this.refundable = refundable;
        return this;
    }

    /**
     * Sets the priority and returns this instance for chaining.
     *
     * @param priority the application priority
     * @return this Charge instance
     */
    public Charge priority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Sets the description and returns this instance for chaining.
     *
     * @param description the charge description
     * @return this Charge instance
     */
    public Charge description(String description) {
        this.description = description;
        return this;
    }

    /**
     * Creates a copy of this charge.
     * All properties are copied to a new instance.
     *
     * @return a new Charge instance with copied data
     */
    public Charge copy() {
        Charge copy = new Charge();
        copy.id = null; // Reset ID for new charge
        copy.code = this.code;
        copy.name = this.name;
        copy.type = this.type;
        copy.rateType = this.rateType;
        copy.value = this.value;
        copy.appliesTo = this.appliesTo;
        copy.base = this.base;
        copy.refundable = this.refundable;
        copy.priority = this.priority;
        copy.description = this.description;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Charge charge = (Charge) o;
        return Objects.equals(code, charge.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return name + " (" + code + "): " + value + " [" + type + "/" + rateType + "]";
    }
}
