package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a party (customer, supplier, etc.) involved in a financial document.
 * Contains identification, contact information, and financial behavior settings.
 *
 * <p>This class includes:</p>
 * <ul>
 *   <li>Basic identification (id, name, tax id)</li>
 *   <li>Contact information (address, email, phone)</li>
 *   <li>Financial settings (global discounts, tax exemptions, auto-withholdings)</li>
 * </ul>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * DocumentParty customer = DocumentParty.of("12345", "ACME Corp", "TAX-123456")
 *     .email("contact@acme.com")
 *     .phone("+1-555-0100")
 *     .address("123 Main St, New York, NY 10001")
 *     .addGlobalDiscount(new BigDecimal("5.0")) // 5% discount on all purchases
 *     .withTaxExemption("VAT19")
 *     .autoWithholder(true)
 *     .withAutoWithholding("RET_IVA");
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 26.1
 */
public class DocumentParty implements Serializable {

    private String id;
    private String name;
    private String taxId;
    private PartyType type;
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private String email;
    private String phone;
    private String mobile;
    private String website;
    private String contactPerson;

    // Financial behavior settings
    private BigDecimal globalDiscountPercentage;
    private List<String> taxExemptions;
    private boolean autoWithholder;
    private List<String> autoWithholdingCodes;
    private BigDecimal creditLimit;
    private Integer paymentTermDays;
    private String preferredCurrency;
    private String notes;

    /**
     * Default constructor.
     */
    public DocumentParty() {
        this.taxExemptions = new ArrayList<>();
        this.autoWithholdingCodes = new ArrayList<>();
        this.autoWithholder = false;
    }

    /**
     * Creates a document party with basic identification.
     *
     * @param id the party unique identifier
     * @param name the party name
     * @param taxId the tax identification number
     * @return a new DocumentParty instance
     */
    public static DocumentParty of(String id, String name, String taxId) {
        DocumentParty party = new DocumentParty();
        party.setId(id);
        party.setName(name);
        party.setTaxId(taxId);
        return party;
    }

    /**
     * Creates a document party with name only.
     *
     * @param name the party name
     * @return a new DocumentParty instance
     */
    public static DocumentParty of(String name) {
        DocumentParty party = new DocumentParty();
        party.setName(name);
        return party;
    }

    /**
     * Adds a tax exemption code to this party.
     * Tax exemptions indicate which taxes should NOT be applied to this party.
     *
     * @param taxCode the tax code to exempt (e.g., "VAT19", "IVA")
     */
    public void addTaxExemption(String taxCode) {
        Objects.requireNonNull(taxCode, "Tax code cannot be null");
        if (this.taxExemptions == null) {
            this.taxExemptions = new ArrayList<>();
        }
        if (!this.taxExemptions.contains(taxCode)) {
            this.taxExemptions.add(taxCode);
        }
    }

    /**
     * Removes a tax exemption code from this party.
     *
     * @param taxCode the tax code to remove
     */
    public void removeTaxExemption(String taxCode) {
        if (this.taxExemptions != null) {
            this.taxExemptions.remove(taxCode);
        }
    }

    /**
     * Checks if this party is exempt from a specific tax.
     *
     * @param taxCode the tax code to check
     * @return true if the party is exempt from this tax
     */
    public boolean isExemptFromTax(String taxCode) {
        return taxExemptions != null && taxExemptions.contains(taxCode);
    }

    /**
     * Adds an auto-withholding code to this party.
     * Auto-withholding codes indicate which withholdings should be automatically applied.
     *
     * @param withholdingCode the withholding code (e.g., "RET_IVA", "RET_RENTA")
     */
    public void addAutoWithholdingCode(String withholdingCode) {
        Objects.requireNonNull(withholdingCode, "Withholding code cannot be null");
        if (this.autoWithholdingCodes == null) {
            this.autoWithholdingCodes = new ArrayList<>();
        }
        if (!this.autoWithholdingCodes.contains(withholdingCode)) {
            this.autoWithholdingCodes.add(withholdingCode);
        }
    }

    /**
     * Removes an auto-withholding code from this party.
     *
     * @param withholdingCode the withholding code to remove
     */
    public void removeAutoWithholdingCode(String withholdingCode) {
        if (this.autoWithholdingCodes != null) {
            this.autoWithholdingCodes.remove(withholdingCode);
        }
    }

    /**
     * Checks if this party has auto-withholding enabled.
     *
     * @return true if auto-withholding is enabled
     */
    public boolean hasAutoWithholding() {
        return autoWithholder && autoWithholdingCodes != null && !autoWithholdingCodes.isEmpty();
    }

    /**
     * Checks if this party has a global discount configured.
     *
     * @return true if global discount percentage is greater than zero
     */
    public boolean hasGlobalDiscount() {
        return globalDiscountPercentage != null && globalDiscountPercentage.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Gets the full address as a single string.
     *
     * @return formatted address
     */
    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (address != null && !address.isEmpty()) {
            sb.append(address);
        }
        if (city != null && !city.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(city);
        }
        if (state != null && !state.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(state);
        }
        if (postalCode != null && !postalCode.isEmpty()) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(postalCode);
        }
        if (country != null && !country.isEmpty()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(country);
        }
        return sb.toString();
    }

    /**
     * Validates that this party has all required fields.
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        Objects.requireNonNull(name, "Party name is required");

        if (globalDiscountPercentage != null) {
            if (globalDiscountPercentage.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalStateException("Global discount cannot be negative");
            }
            if (globalDiscountPercentage.compareTo(new BigDecimal("100")) > 0) {
                throw new IllegalStateException("Global discount cannot exceed 100%");
            }
        }

        if (creditLimit != null && creditLimit.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalStateException("Credit limit cannot be negative");
        }

        if (paymentTermDays != null && paymentTermDays < 0) {
            throw new IllegalStateException("Payment term days cannot be negative");
        }
    }

    /**
     * Creates a deep copy of this party.
     *
     * @return a new DocumentParty instance with copied data
     */
    public DocumentParty copy() {
        DocumentParty copy = new DocumentParty();

        copy.id = this.id;
        copy.name = this.name;
        copy.taxId = this.taxId;
        copy.type = this.type;
        copy.address = this.address;
        copy.city = this.city;
        copy.state = this.state;
        copy.country = this.country;
        copy.postalCode = this.postalCode;
        copy.email = this.email;
        copy.phone = this.phone;
        copy.mobile = this.mobile;
        copy.website = this.website;
        copy.contactPerson = this.contactPerson;

        copy.globalDiscountPercentage = this.globalDiscountPercentage;
        copy.autoWithholder = this.autoWithholder;
        copy.creditLimit = this.creditLimit;
        copy.paymentTermDays = this.paymentTermDays;
        copy.preferredCurrency = this.preferredCurrency;
        copy.notes = this.notes;

        if (this.taxExemptions != null) {
            copy.taxExemptions = new ArrayList<>(this.taxExemptions);
        }

        if (this.autoWithholdingCodes != null) {
            copy.autoWithholdingCodes = new ArrayList<>(this.autoWithholdingCodes);
        }

        return copy;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaxId() {
        return taxId;
    }

    public void setTaxId(String taxId) {
        this.taxId = taxId;
    }

    public PartyType getType() {
        return type;
    }

    public void setType(PartyType type) {
        this.type = type;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public BigDecimal getGlobalDiscountPercentage() {
        return globalDiscountPercentage;
    }

    public void setGlobalDiscountPercentage(BigDecimal globalDiscountPercentage) {
        this.globalDiscountPercentage = globalDiscountPercentage;
    }

    public List<String> getTaxExemptions() {
        return taxExemptions != null ? Collections.unmodifiableList(taxExemptions) : Collections.emptyList();
    }

    public void setTaxExemptions(List<String> taxExemptions) {
        this.taxExemptions = taxExemptions != null ? new ArrayList<>(taxExemptions) : new ArrayList<>();
    }

    public boolean isAutoWithholder() {
        return autoWithholder;
    }

    public void setAutoWithholder(boolean autoWithholder) {
        this.autoWithholder = autoWithholder;
    }

    public List<String> getAutoWithholdingCodes() {
        return autoWithholdingCodes != null ? Collections.unmodifiableList(autoWithholdingCodes) : Collections.emptyList();
    }

    public void setAutoWithholdingCodes(List<String> autoWithholdingCodes) {
        this.autoWithholdingCodes = autoWithholdingCodes != null ? new ArrayList<>(autoWithholdingCodes) : new ArrayList<>();
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
    }

    public Integer getPaymentTermDays() {
        return paymentTermDays;
    }

    public void setPaymentTermDays(Integer paymentTermDays) {
        this.paymentTermDays = paymentTermDays;
    }

    public String getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(String preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // Fluent API methods for method chaining

    /**
     * Sets the id and returns this instance for chaining.
     *
     * @param id the party id
     * @return this DocumentParty instance
     */
    public DocumentParty id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the name and returns this instance for chaining.
     *
     * @param name the party name
     * @return this DocumentParty instance
     */
    public DocumentParty name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the tax id and returns this instance for chaining.
     *
     * @param taxId the tax identification number
     * @return this DocumentParty instance
     */
    public DocumentParty taxId(String taxId) {
        this.taxId = taxId;
        return this;
    }

    /**
     * Sets the party type and returns this instance for chaining.
     *
     * @param type the party type
     * @return this DocumentParty instance
     */
    public DocumentParty type(PartyType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the address and returns this instance for chaining.
     *
     * @param address the address
     * @return this DocumentParty instance
     */
    public DocumentParty address(String address) {
        this.address = address;
        return this;
    }

    /**
     * Sets the city and returns this instance for chaining.
     *
     * @param city the city
     * @return this DocumentParty instance
     */
    public DocumentParty city(String city) {
        this.city = city;
        return this;
    }

    /**
     * Sets the state and returns this instance for chaining.
     *
     * @param state the state
     * @return this DocumentParty instance
     */
    public DocumentParty state(String state) {
        this.state = state;
        return this;
    }

    /**
     * Sets the country and returns this instance for chaining.
     *
     * @param country the country
     * @return this DocumentParty instance
     */
    public DocumentParty country(String country) {
        this.country = country;
        return this;
    }

    /**
     * Sets the postal code and returns this instance for chaining.
     *
     * @param postalCode the postal code
     * @return this DocumentParty instance
     */
    public DocumentParty postalCode(String postalCode) {
        this.postalCode = postalCode;
        return this;
    }

    /**
     * Sets the email and returns this instance for chaining.
     *
     * @param email the email address
     * @return this DocumentParty instance
     */
    public DocumentParty email(String email) {
        this.email = email;
        return this;
    }

    /**
     * Sets the phone and returns this instance for chaining.
     *
     * @param phone the phone number
     * @return this DocumentParty instance
     */
    public DocumentParty phone(String phone) {
        this.phone = phone;
        return this;
    }

    /**
     * Sets the mobile and returns this instance for chaining.
     *
     * @param mobile the mobile number
     * @return this DocumentParty instance
     */
    public DocumentParty mobile(String mobile) {
        this.mobile = mobile;
        return this;
    }

    /**
     * Sets the website and returns this instance for chaining.
     *
     * @param website the website URL
     * @return this DocumentParty instance
     */
    public DocumentParty website(String website) {
        this.website = website;
        return this;
    }

    /**
     * Sets the contact person and returns this instance for chaining.
     *
     * @param contactPerson the contact person name
     * @return this DocumentParty instance
     */
    public DocumentParty contactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
        return this;
    }

    /**
     * Sets the global discount percentage and returns this instance for chaining.
     *
     * @param percentage the discount percentage
     * @return this DocumentParty instance
     */
    public DocumentParty addGlobalDiscount(BigDecimal percentage) {
        this.globalDiscountPercentage = percentage;
        return this;
    }

    /**
     * Adds a tax exemption and returns this instance for chaining.
     *
     * @param taxCode the tax code to exempt
     * @return this DocumentParty instance
     */
    public DocumentParty withTaxExemption(String taxCode) {
        addTaxExemption(taxCode);
        return this;
    }

    /**
     * Sets auto-withholding and returns this instance for chaining.
     *
     * @param autoWithholder true to enable auto-withholding
     * @return this DocumentParty instance
     */
    public DocumentParty autoWithholder(boolean autoWithholder) {
        this.autoWithholder = autoWithholder;
        return this;
    }

    /**
     * Adds an auto-withholding code and returns this instance for chaining.
     *
     * @param withholdingCode the withholding code
     * @return this DocumentParty instance
     */
    public DocumentParty withAutoWithholding(String withholdingCode) {
        addAutoWithholdingCode(withholdingCode);
        return this;
    }

    /**
     * Sets the credit limit and returns this instance for chaining.
     *
     * @param creditLimit the credit limit
     * @return this DocumentParty instance
     */
    public DocumentParty creditLimit(BigDecimal creditLimit) {
        this.creditLimit = creditLimit;
        return this;
    }

    /**
     * Sets the payment term days and returns this instance for chaining.
     *
     * @param days the payment term in days
     * @return this DocumentParty instance
     */
    public DocumentParty paymentTermDays(Integer days) {
        this.paymentTermDays = days;
        return this;
    }

    /**
     * Sets the preferred currency and returns this instance for chaining.
     *
     * @param currency the currency code
     * @return this DocumentParty instance
     */
    public DocumentParty preferredCurrency(String currency) {
        this.preferredCurrency = currency;
        return this;
    }

    /**
     * Sets the notes and returns this instance for chaining.
     *
     * @param notes the notes
     * @return this DocumentParty instance
     */
    public DocumentParty notes(String notes) {
        this.notes = notes;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (taxId != null && !taxId.isEmpty()) {
            sb.append(" (").append(taxId).append(")");
        }
        return sb.toString();
    }
}
