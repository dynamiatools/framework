package tools.dynamia.modules.finances.api;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Root entity representing a financial document with economic impact.
 * This is the main domain entity of the framework.
 *
 * <p>The document does NOT calculate itself. Calculation is delegated to
 * an external {@link FinancialCalculator} to maintain separation of concerns
 * and enable different calculation strategies.</p>
 *
 * <p>Example usage:</p>
 * <pre>{@code
 * FinancialDocument invoice = new FinancialDocument();
 * invoice.setType(DocumentType.SALE);
 * invoice.setStatus(DocumentStatus.DRAFT);
 * invoice.setCurrency("USD");
 * invoice.setIssueDate(LocalDate.now());
 *
 * DocumentLine line = DocumentLine.of("Product A",
 *                                     new BigDecimal("10"),
 *                                     Money.of("100", "USD"));
 * invoice.addLine(line);
 *
 * FinancialCalculator calculator = new DefaultFinancialCalculator();
 * calculator.calculateDocument(invoice);
 * }</pre>
 *
 * @author Dynamia Finance Framework
 * @since 1.0.0
 */
public class FinancialDocument implements Serializable {

    private String id;
    private DocumentType type;
    private DocumentStatus status;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private String party;
    private String currency;
    private ExchangeRate exchangeRate;
    private List<DocumentLine> lines;
    private List<Charge> charges;
    private DocumentTotals totals;
    private String documentNumber;
    private String reference;
    private String notes;

    /**
     * Default constructor.
     */
    public FinancialDocument() {
        this.lines = new ArrayList<>();
        this.charges = new ArrayList<>();
        this.status = DocumentStatus.DRAFT;
        this.issueDate = LocalDate.now();
    }

    /**
     * Creates a financial document with the specified type.
     *
     * @param type the document type
     * @param currency the currency code
     * @return a new FinancialDocument instance
     */
    public static FinancialDocument of(DocumentType type, String currency) {
        FinancialDocument document = new FinancialDocument();
        document.setType(type);
        document.setCurrency(currency);
        return document;
    }

    /**
     * Adds a line to this document.
     * Sets the bidirectional relationship.
     *
     * @param line the line to add
     */
    public void addLine(DocumentLine line) {
        Objects.requireNonNull(line, "Line cannot be null");
        if (this.lines == null) {
            this.lines = new ArrayList<>();
        }
        this.lines.add(line);
        line.setDocument(this);
        line.setLineNumber(this.lines.size());
    }

    /**
     * Removes a line from this document.
     *
     * @param line the line to remove
     */
    public void removeLine(DocumentLine line) {
        if (this.lines != null) {
            this.lines.remove(line);
            line.setDocument(null);
            // Renumber lines
            for (int i = 0; i < this.lines.size(); i++) {
                this.lines.get(i).setLineNumber(i + 1);
            }
        }
    }

    /**
     * Adds a charge to this document.
     * Document-level charges can apply to the entire document or be inherited by lines.
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
     * Removes a charge from this document.
     *
     * @param charge the charge to remove
     */
    public void removeCharge(Charge charge) {
        if (this.charges != null) {
            this.charges.remove(charge);
        }
    }

    /**
     * Validates that this document has all required fields.
     *
     * @throws IllegalStateException if validation fails
     */
    public void validate() {
        Objects.requireNonNull(type, "Document type is required");
        Objects.requireNonNull(status, "Document status is required");
        Objects.requireNonNull(currency, "Document currency is required");
        Objects.requireNonNull(issueDate, "Document issue date is required");

        if (lines == null || lines.isEmpty()) {
            throw new IllegalStateException("Document must have at least one line");
        }

        // Validate all lines
        lines.forEach(DocumentLine::validate);

        // Validate all charges
        if (charges != null) {
            charges.forEach(Charge::validate);
        }
    }

    /**
     * Checks if this document is in DRAFT status.
     *
     * @return true if status is DRAFT
     */
    public boolean isDraft() {
        return status == DocumentStatus.DRAFT;
    }

    /**
     * Checks if this document is POSTED.
     *
     * @return true if status is POSTED
     */
    public boolean isPosted() {
        return status == DocumentStatus.POSTED;
    }

    /**
     * Checks if this document is CANCELLED.
     *
     * @return true if status is CANCELLED
     */
    public boolean isCancelled() {
        return status == DocumentStatus.CANCELLED;
    }

    /**
     * Posts this document, freezing calculations.
     * A posted document cannot be modified or recalculated.
     *
     * @throws InvalidDocumentStateException if document is not in DRAFT status
     */
    public void post() {
        if (!isDraft()) {
            throw new InvalidDocumentStateException(
                "Only DRAFT documents can be posted. Current status: " + status
            );
        }
        this.status = DocumentStatus.POSTED;
    }

    /**
     * Cancels this document.
     *
     * @throws InvalidDocumentStateException if document is already CANCELLED
     */
    public void cancel() {
        if (isCancelled()) {
            throw new InvalidDocumentStateException("Document is already cancelled");
        }
        this.status = DocumentStatus.CANCELLED;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DocumentType getType() {
        return type;
    }

    public void setType(DocumentType type) {
        this.type = type;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getParty() {
        return party;
    }

    public void setParty(String party) {
        this.party = party;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public ExchangeRate getExchangeRate() {
        return exchangeRate;
    }

    public void setExchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
    }

    public List<DocumentLine> getLines() {
        return lines != null ? Collections.unmodifiableList(lines) : Collections.emptyList();
    }

    public void setLines(List<DocumentLine> lines) {
        this.lines = lines != null ? new ArrayList<>(lines) : new ArrayList<>();
        // Set bidirectional relationship
        if (this.lines != null) {
            for (int i = 0; i < this.lines.size(); i++) {
                DocumentLine line = this.lines.get(i);
                line.setDocument(this);
                line.setLineNumber(i + 1);
            }
        }
    }

    public List<Charge> getCharges() {
        return charges != null ? Collections.unmodifiableList(charges) : Collections.emptyList();
    }

    public void setCharges(List<Charge> charges) {
        this.charges = charges != null ? new ArrayList<>(charges) : new ArrayList<>();
    }

    public DocumentTotals getTotals() {
        return totals;
    }

    public void setTotals(DocumentTotals totals) {
        this.totals = totals;
    }

    public String getDocumentNumber() {
        return documentNumber;
    }

    public void setDocumentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
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
     * @param id the document id
     * @return this FinancialDocument instance
     */
    public FinancialDocument id(String id) {
        this.id = id;
        return this;
    }

    /**
     * Sets the document type and returns this instance for chaining.
     *
     * @param type the document type
     * @return this FinancialDocument instance
     */
    public FinancialDocument type(DocumentType type) {
        this.type = type;
        return this;
    }

    /**
     * Sets the document status and returns this instance for chaining.
     *
     * @param status the document status
     * @return this FinancialDocument instance
     */
    public FinancialDocument status(DocumentStatus status) {
        this.status = status;
        return this;
    }

    /**
     * Sets the issue date and returns this instance for chaining.
     *
     * @param issueDate the issue date
     * @return this FinancialDocument instance
     */
    public FinancialDocument issueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
        return this;
    }

    /**
     * Sets the due date and returns this instance for chaining.
     *
     * @param dueDate the due date
     * @return this FinancialDocument instance
     */
    public FinancialDocument dueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
        return this;
    }

    /**
     * Sets the party and returns this instance for chaining.
     *
     * @param party the party (customer/supplier)
     * @return this FinancialDocument instance
     */
    public FinancialDocument party(String party) {
        this.party = party;
        return this;
    }

    /**
     * Sets the currency and returns this instance for chaining.
     *
     * @param currency the currency code
     * @return this FinancialDocument instance
     */
    public FinancialDocument currency(String currency) {
        this.currency = currency;
        return this;
    }

    /**
     * Sets the exchange rate and returns this instance for chaining.
     *
     * @param exchangeRate the exchange rate
     * @return this FinancialDocument instance
     */
    public FinancialDocument exchangeRate(ExchangeRate exchangeRate) {
        this.exchangeRate = exchangeRate;
        return this;
    }

    /**
     * Adds a line and returns this instance for chaining.
     *
     * @param line the line to add
     * @return this FinancialDocument instance
     */
    public FinancialDocument line(DocumentLine line) {
        addLine(line);
        return this;
    }

    /**
     * Adds a charge and returns this instance for chaining.
     *
     * @param charge the charge to add
     * @return this FinancialDocument instance
     */
    public FinancialDocument charge(Charge charge) {
        addCharge(charge);
        return this;
    }

    /**
     * Sets the document number and returns this instance for chaining.
     *
     * @param documentNumber the document number
     * @return this FinancialDocument instance
     */
    public FinancialDocument documentNumber(String documentNumber) {
        this.documentNumber = documentNumber;
        return this;
    }

    /**
     * Sets the reference and returns this instance for chaining.
     *
     * @param reference the reference
     * @return this FinancialDocument instance
     */
    public FinancialDocument reference(String reference) {
        this.reference = reference;
        return this;
    }

    /**
     * Sets the notes and returns this instance for chaining.
     *
     * @param notes the notes
     * @return this FinancialDocument instance
     */
    public FinancialDocument notes(String notes) {
        this.notes = notes;
        return this;
    }

    /**
     * Sets the totals and returns this instance for chaining.
     *
     * @param totals the document totals
     * @return this FinancialDocument instance
     */
    public FinancialDocument totals(DocumentTotals totals) {
        this.totals = totals;
        return this;
    }

    @Override
    public String toString() {
        return type + " #" + documentNumber + " (" + status + ") - " + currency;
    }
}
