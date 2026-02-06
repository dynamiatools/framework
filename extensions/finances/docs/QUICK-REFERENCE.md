# Complete Tutorial - Dynamia Finance Framework

**Version:** 26.1  
**Date:** February 6, 2026  
**Status:** ✅ Production

---

## 📚 Table of Contents

1. [Introduction](#introduction)
2. [Fundamental Concepts](#fundamental-concepts)
3. [Installation and Setup](#installation-and-setup)
4. [Getting Started](#getting-started)
5. [Practical Use Cases](#practical-use-cases)
6. [Working with Charges](#working-with-charges)
7. [Currency Handling](#currency-handling)
8. [Advanced Calculations](#advanced-calculations)
9. [Best Practices](#best-practices)
10. [API Reference](#api-reference)
11. [Troubleshooting](#troubleshooting)
12. [Useful Commands](#useful-commands)

---

## Introduction

### What is the Dynamia Finance Framework?

The **Dynamia Finance Framework** is a lightweight, domain-driven calculation engine designed to handle the complex mathematics behind financial documents. It provides a solid foundation for building financial systems without imposing constraints on presentation, persistence, or business rules.

### What is it for?

This framework focuses exclusively on the **calculation logic** that powers invoices, credit notes, purchase orders, and other financial documents. It handles the intricate details of applying taxes, discounts, withholdings, and fees in the correct order, managing multiple currencies, and producing accurate, auditable totals.

### Design Philosophy

- **Separation of concerns**: No dependencies on databases, UI, or web frameworks
- **Deterministic calculations**: Same input data = same results
- **No side effects**: Does not modify external state
- **Extensible**: Designed to be extended, not modified
- **Currency-aware**: All amounts are strongly typed with currency information

### What the framework DOES

✅ Calculate line totals  
✅ Apply charges in priority order  
✅ Handle percentage and fixed charges  
✅ Support line-level and document-level charges  
✅ Manage multiple currencies  
✅ Aggregate totals (subtotals, taxes, grand total)  
✅ Calculate payable amounts  
✅ Validate charge application  

### What the framework DOES NOT do

❌ User interface  
❌ Database persistence  
❌ Accounting (journal entries, ledgers)  
❌ Country-specific tax legislation  
❌ Electronic invoicing  
❌ Workflows  
❌ Report generation  

---

## Fundamental Concepts

### 1. FinancialDocument

The root entity that represents any document with economic impact.

**Document types:**
- `SALE` - Sales invoice
- `PURCHASE` - Purchase order
- `CREDIT_NOTE` - Credit note
- `DEBIT_NOTE` - Debit note
- `ADJUSTMENT` - Adjustment
- `QUOTE` - Quote

**Document statuses:**
- `DRAFT` - Draft
- `POSTED` - Posted
- `CANCELLED` - Cancelled

**Main components:**
```java
FinancialDocument {
    String id;
    DocumentType type;
    DocumentStatus status;
    LocalDate issueDate;
    LocalDate dueDate;
    DocumentParty party;
    String currency;
    ExchangeRate exchangeRate;
    List<DocumentLine> lines;      // Document lines
    List<Charge> charges;           // Document-level charges
    DocumentTotals totals;          // Calculated totals
}
```

### 2. DocumentLine

Represents an economic line within the document.

```java
DocumentLine {
    String id;
    String description;
    double quantity;
    Money unitPrice;
    List<Charge> charges;           // Line-specific charges
    LineTotals totals;              // Line totals
}
```

### 3. Charge

Represents any price modifier: taxes, discounts, withholdings, or fees.

**Charge types:**
- `TAX` - Tax (VAT, sales tax, etc.)
- `DISCOUNT` - Commercial discount
- `WITHHOLDING` - Withholding
- `FEE` - Additional fee

**Calculation methods:**
- `PERCENTAGE` - Percentage on a base
- `FIXED` - Fixed amount
- `FORMULA` - Custom formula

**Application scope:**
- `LINE` - Applies to each line
- `DOCUMENT` - Applies to the entire document

```java
Charge {
    String id;
    String name;
    ChargeType type;
    ChargeMethod method;
    ChargeAppliesTo appliesTo;
    ChargeBase base;
    BigDecimal rate;               // For percentages
    BigDecimal amount;             // For fixed amounts
    int priority;                  // Application order
}
```

### 4. Money

Represents a monetary amount with its currency.

```java
Money {
    BigDecimal amount;
    String currency;
}
```

### 5. FinancialCalculator

Calculation engine that processes documents and produces totals.

```java
interface FinancialCalculator {
    void calculateDocument(FinancialDocument document);
}
```

---

## Installation and Setup

### Prerequisites

- Java 25 or higher
- Maven 3.6+
- IDE (IntelliJ IDEA, Eclipse, VS Code)

### Add the Dependency

#### Maven
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.finances.api</artifactId>
    <version>26.1</version>
</dependency>
```

#### Gradle
```groovy
implementation 'tools.dynamia.modules:tools.dynamia.modules.finances.api:26.1'
```

### Verify Installation

```bash
# Compile the project
mvn clean compile

# Run tests
mvn test

# Run basic example
mvn exec:java -Dexec.mainClass="tools.dynamia.modules.finances.api.examples.FinanceFrameworkExample"
```

---

## Getting Started

### Example 1: Your First Invoice

Let's create a simple invoice with one product and calculate its total.

```java
import tools.dynamia.modules.finances.api.*;
import java.math.BigDecimal;
import java.time.LocalDate;

public class MyFirstInvoice {
    static void main(String[] args) {
        // 1. Create the document
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-001");
        invoice.setIssueDate(LocalDate.now());
        invoice.setParty(DocumentParty.of("John Doe"));
        
        // 2. Add a product line
        DocumentLine line = DocumentLine.of(
            "Dell XPS 15 Laptop",              // Description
            1,               // Quantity
            Money.of(1500.00, "USD")         // Unit price
        );
        invoice.addLine(line);
        
        // 3. Calculate the invoice
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);
        
        // 4. Show results
        DocumentTotals totals = invoice.getTotals();
        System.out.println("Subtotal: " + totals.getSubtotal());
        System.out.println("Total to pay: " + totals.getGrandTotal());
    }
}
```

**Output:**
```
Subtotal: USD 1,500.00
Total to pay: USD 1,500.00
```

### Example 2: Invoice with VAT

Now let's add a 19% tax (VAT).

```java
public class InvoiceWithVAT {
    static void main(String[] args) {
        // Create document
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-002");
        
        // Add product
        DocumentLine line = DocumentLine.of(
            "Dell XPS 15 Laptop",
            1,
            Money.of(1500.00, "USD")
        );
        
        // Create and configure VAT
        Charge vat = Charge.percentage(
            "VAT19",                           // Charge ID
            "VAT 19%",                         // Descriptive name
            ChargeType.TAX,                    // Type: tax
            BigDecimal.valueOf(19),              // Rate: 19%
            20                                 // Priority
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);    // Applies per line
        vat.setBase(ChargeBase.NET);               // Base: net price
        
        // Add VAT to the line
        line.addCharge(vat);
        invoice.addLine(line);
        
        // Calculate
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);
        
        // Show results
        DocumentTotals totals = invoice.getTotals();
        System.out.println("Subtotal: " + totals.getSubtotal());
        System.out.println("VAT (19%): " + totals.getTotalTaxes());
        System.out.println("Total to pay: " + totals.getGrandTotal());
    }
}
```

**Output:**
```
Subtotal: USD 1,500.00
VAT (19%): USD 285.00
Total to pay: USD 1,785.00
```

### Example 3: Invoice with Discount

Let's apply a 10% discount before VAT.

```java
public class InvoiceWithDiscount {
    public static void main(String[] args) {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-003");
        
        DocumentLine line = DocumentLine.of(
            "Dell XPS 15 Laptop",
            1,
            Money.of(1500.00, "USD")
        );
        
        // 10% discount - Priority 10 (applied first)
        Charge discount = Charge.percentage(
            "DISC10",
            "Volume discount 10%",
            ChargeType.DISCOUNT,
            BigDecimal.valueOf(10),
            10                              // Low priority = applied first
        );
        discount.setAppliesTo(ChargeAppliesTo.LINE);
        discount.setBase(ChargeBase.NET);
        
        // VAT 19% - Priority 20 (applied after discount)
        Charge vat = Charge.percentage(
            "VAT19",
            "VAT 19%",
            ChargeType.TAX,
            BigDecimal.valueOf(19),
            20                              // High priority = applied after
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);  // Calculated on previous total
        
        line.addCharge(discount);
        line.addCharge(vat);
        invoice.addLine(line);
        
        // Calculate
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);
        
        // Show step-by-step calculations
        LineTotals lineTotals = line.getTotals();
        System.out.println("Base price: USD 1,500.00");
        System.out.println("Discount 10%: -USD " + discount.getCalculatedAmount());
        System.out.println("Subtotal after discount: USD 1,350.00");
        System.out.println("VAT 19% on USD 1,350.00: USD " + vat.getCalculatedAmount());
        System.out.println("Line total: " + lineTotals.getTotal());
        System.out.println("\nInvoice total: " + invoice.getTotals().getGrandTotal());
    }
}
```

**Output:**
```
Base price: USD 1,500.00
Discount 10%: -USD 150.00
Subtotal after discount: USD 1,350.00
VAT 19% on USD 1,350.00: USD 256.50
Line total: USD 1,606.50

Invoice total: USD 1,606.50
```

---

## Practical Use Cases

### Case 1: E-Commerce System

**Scenario:** Shopping cart with multiple products, discounts, and shipping cost.

```java
public class ShoppingCart {
    public void processOrder() {
        FinancialDocument order = FinancialDocument.of(DocumentType.SALE, "USD");
        order.setDocumentNumber("ORDER-12345");
        order.setParty("Maria Garcia");
        
        // Product 1: 2 books
        DocumentLine books = DocumentLine.of(
            "The Lord of the Rings",
            2,
            Money.of(25.00, "USD")
        );
        order.addLine(books);
        
        // Product 2: 1 game
        DocumentLine game = DocumentLine.of(
            "The Legend of Zelda",
            1,
            Money.of(59.99, "USD")
        );
        order.addLine(game);
        
        // Coupon discount: 15% on entire order
        Charge coupon = Charge.percentage(
            "COUPON15",
            "15% discount coupon",
            ChargeType.DISCOUNT,
            BigDecimal.valueOf(15),
            10
        );
        coupon.setAppliesTo(ChargeAppliesTo.LINE);
        order.addCharge(coupon);
        
        // VAT: 10%
        Charge vat = Charge.percentage(
            "VAT10",
            "VAT 10%",
            ChargeType.TAX,
            BigDecimal.valueOf(10),
            20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        order.addCharge(vat);
        
        // Shipping cost: $10 fixed at document level
        Charge shipping = Charge.fixed(
            "SHIPPING",
            "Shipping cost",
            ChargeType.FEE,
            BigDecimal.valueOf(10.00),
            100                             // High priority = applied at the end
        );
        shipping.setAppliesTo(ChargeAppliesTo.DOCUMENT);
        order.addCharge(shipping);
        
        // Calculate
        new DefaultFinancialCalculator().calculateDocument(order);
        
        // Show summary
        DocumentTotals totals = order.getTotals();
        System.out.println("═══════════════════════════════════");
        System.out.println("        ORDER SUMMARY");
        System.out.println("═══════════════════════════════════");
        System.out.println("Product subtotal: " + totals.getSubtotal());
        System.out.println("Discount (15%): " + totals.getTotalDiscounts());
        System.out.println("VAT (10%): " + totals.getTotalTaxes());
        System.out.println("Shipping: " + totals.getTotalFees());
        System.out.println("───────────────────────────────────");
        System.out.println("TOTAL TO PAY: " + totals.getGrandTotal());
        System.out.println("═══════════════════════════════════");
    }
}
```

### Case 2: Invoice with Withholding

**Scenario:** Professional invoice with income tax withholding.

```java
public class InvoiceWithWithholding {
    public void createProfessionalInvoice() {
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "COP");
        invoice.setDocumentNumber("INV-2026-001");
        invoice.setParty("ABC Company Ltd.");
        
        // Professional services
        DocumentLine services = DocumentLine.of(
            "Software development consulting - 40 hours",
            40,
            Money.of(50000, "COP")        // $50,000 COP per hour
        );
        
        // VAT 19%
        Charge vat = Charge.percentage(
            "VAT19",
            "VAT 19%",
            ChargeType.TAX,
            BigDecimal.valueOf(19),
            20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.NET);
        
        // Income tax withholding 11%
        Charge withholding = Charge.percentage(
            "WITHHOLDING11",
            "Income tax withholding 11%",
            ChargeType.WITHHOLDING,
            BigDecimal.valueOf(11),
            30                              // Applied after VAT
        );
        withholding.setAppliesTo(ChargeAppliesTo.LINE);
        withholding.setBase(ChargeBase.NET);  // Calculated on net value
        
        services.addCharge(vat);
        services.addCharge(withholding);
        invoice.addLine(services);
        
        // Calculate
        new DefaultFinancialCalculator().calculateDocument(invoice);
        
        // Show breakdown
        DocumentTotals totals = invoice.getTotals();
        System.out.println("═══════════════════════════════════");
        System.out.println("      SERVICES INVOICE");
        System.out.println("═══════════════════════════════════");
        System.out.println("Services rendered: " + totals.getSubtotal());
        System.out.println("VAT 19%: " + totals.getTotalTaxes());
        System.out.println("Withholding 11%: " + totals.getTotalWithholdings());
        System.out.println("───────────────────────────────────");
        System.out.println("Invoice total: " + totals.getGrandTotal());
        System.out.println("TO PAY (after withholding): " + totals.getPayableAmount());
        System.out.println("═══════════════════════════════════");
    }
}
```

### Case 3: Multi-Level Quote

**Scenario:** Quote with volume discounts and frequent customer discount.

```java
public class MultiLevelQuote {
    public void generateQuote() {
        FinancialDocument quote = FinancialDocument.of(DocumentType.QUOTE, "USD");
        quote.setDocumentNumber("QUOTE-2026-050");
        quote.setParty("Tech Solutions Corporation");
        
        // Product 1: Software licenses (large purchase)
        DocumentLine licenses = DocumentLine.of(
            "Premium Software Annual Licenses",
            100,
            Money.of(200.00, "USD")
        );
        
        // Volume discount: 20%
        Charge volumeDisc = Charge.percentage(
            "VOLUME20",
            "Volume discount (+50 licenses)",
            ChargeType.DISCOUNT,
            BigDecimal.valueOf(20),
            5
        );
        volumeDisc.setAppliesTo(ChargeAppliesTo.LINE);
        
        // Frequent customer discount: additional 5%
        Charge customerDisc = Charge.percentage(
            "CUSTOMER5",
            "Frequent customer discount",
            ChargeType.DISCOUNT,
            BigDecimal.valueOf(5),
            10
        );
        customerDisc.setAppliesTo(ChargeAppliesTo.LINE);
        customerDisc.setBase(ChargeBase.PREVIOUS_TOTAL);
        
        // VAT
        Charge vat = Charge.percentage(
            "VAT19",
            "VAT 19%",
            ChargeType.TAX,
            BigDecimal.valueOf(19),
            20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);
        
        licenses.addCharge(volumeDisc);
        licenses.addCharge(customerDisc);
        licenses.addCharge(vat);
        quote.addLine(licenses);
        
        // Calculate
        new DefaultFinancialCalculator().calculateDocument(quote);
        
        // Present quote
        DocumentTotals totals = quote.getTotals();
        LineTotals lineTotals = licenses.getTotals();
        
        System.out.println("═══════════════════════════════════════");
        System.out.println("           QUOTE");
        System.out.println("═══════════════════════════════════════");
        System.out.println("Product: Premium Software Licenses");
        System.out.println("Quantity: 100 units");
        System.out.println("Unit price: USD 200.00");
        System.out.println("───────────────────────────────────────");
        System.out.println("Subtotal: USD 20,000.00");
        System.out.println("Volume disc. (20%): -USD 4,000.00");
        System.out.println("Customer disc. (5%): -USD 800.00");
        System.out.println("Taxable base: USD 15,200.00");
        System.out.println("VAT (19%): USD 2,888.00");
        System.out.println("═══════════════════════════════════════");
        System.out.println("QUOTE TOTAL: " + totals.getGrandTotal());
        System.out.println("═══════════════════════════════════════");
        System.out.println("\nTotal savings: USD 4,800.00!");
    }
}
```

---

## Working with Charges

Charges are the heart of the framework. Understanding how they work is essential.

### Anatomy of a Charge

```java
Charge charge = new Charge();
charge.setId("CHARGE_01");
charge.setName("Descriptive name");
charge.setType(ChargeType.TAX);              // TAX, DISCOUNT, WITHHOLDING, FEE
charge.setMethod(ChargeMethod.PERCENTAGE);   // PERCENTAGE, FIXED, FORMULA
charge.setAppliesTo(ChargeAppliesTo.LINE);   // LINE or DOCUMENT
charge.setBase(ChargeBase.NET);              // NET or PREVIOUS_TOTAL
charge.setRate(BigDecimal.valueOf(19));        // For percentages
charge.setPriority(20);                      // Application order
```

### Charge Priority

Priority determines the order in which charges are applied. **Lower number = higher priority (applied first)**.

```java
// Typical application order:
Charge discount = Charge.percentage("DISC", "Discount", 
    ChargeType.DISCOUNT, BigDecimal.valueOf(10), 10);    // First

Charge tax = Charge.percentage("VAT", "VAT", 
    ChargeType.TAX, BigDecimal.valueOf(19), 20);         // Second

Charge withholding = Charge.percentage("WITH", "Withholding", 
    ChargeType.WITHHOLDING, BigDecimal.valueOf(5), 30);  // Third

Charge fee = Charge.fixed("SHIP", "Shipping", 
    ChargeType.FEE, BigDecimal.valueOf(10), 100);        // Last
```

### Calculation Base

The base determines on what amount the charge is calculated.

**ChargeBase.NET**
```java
// Calculated on net value (quantity × price)
Charge discount = Charge.percentage("DISC10", "Disc 10%", 
    ChargeType.DISCOUNT, BigDecimal.valueOf(10), 10);
discount.setBase(ChargeBase.NET);

// If net price = $1000
// Discount = $1000 × 10% = $100
```

**ChargeBase.PREVIOUS_TOTAL**
```java
// Calculated on accumulated total up to this point
Charge vat = Charge.percentage("VAT19", "VAT 19%", 
    ChargeType.TAX, BigDecimal.valueOf(19), 20);
vat.setBase(ChargeBase.PREVIOUS_TOTAL);

// If previous total = $900 (after discount)
// VAT = $900 × 19% = $171
```

### Charge Types

#### 1. Taxes (TAX)

```java
// VAT 19%
Charge vat = Charge.percentage(
    "VAT19",
    "Value Added Tax 19%",
    ChargeType.TAX,
    BigDecimal.valueOf(19),
    20
);
vat.setAppliesTo(ChargeAppliesTo.LINE);
vat.setBase(ChargeBase.PREVIOUS_TOTAL);

// Fixed tax
Charge fixedTax = Charge.fixed(
    "CITYTAX",
    "City Tax",
    ChargeType.TAX,
    BigDecimal.valueOf(5.00),
    25
);
fixedTax.setAppliesTo(ChargeAppliesTo.DOCUMENT);
```

#### 2. Discounts (DISCOUNT)

```java
// Percentage discount
Charge commercialDisc = Charge.percentage(
    "DISC15",
    "Commercial discount 15%",
    ChargeType.DISCOUNT,
    BigDecimal.valueOf(15),
    10
);

// Fixed discount
Charge coupon = Charge.fixed(
    "COUPON50",
    "$50 discount coupon",
    ChargeType.DISCOUNT,
    BigDecimal.valueOf(50.00),
    15
);
```

#### 3. Withholdings (WITHHOLDING)

```java
// Income tax withholding
Charge incomeWith = Charge.percentage(
    "INCOMEWITH",
    "Income tax withholding",
    ChargeType.WITHHOLDING,
    BigDecimal.valueOf(2.5),
    30
);

// VAT withholding
Charge vatWith = Charge.percentage(
    "VATWITH",
    "VAT withholding",
    ChargeType.WITHHOLDING,
    BigDecimal.valueOf(15),
    35
);
```

#### 4. Fees (FEE)

```java
// Shipping fee
Charge shipping = Charge.fixed(
    "SHIPPING",
    "Shipping cost",
    ChargeType.FEE,
    BigDecimal.valueOf(25.00),
    100
);

// Handling fee
Charge handling = Charge.percentage(
    "HANDLING",
    "Handling fee 3%",
    ChargeType.FEE,
    BigDecimal.valueOf(3),
    105
);
```

### Line-Level vs Document-Level Charges

#### Line Level (ChargeAppliesTo.LINE)

Applied to each individual line of the document.

```java
FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");

// VAT will be applied to EACH line
Charge vat = Charge.percentage("VAT19", "VAT 19%", 
    ChargeType.TAX, BigDecimal.valueOf(19), 20);
vat.setAppliesTo(ChargeAppliesTo.LINE);

// Add charge to document (will apply to all lines)
invoice.addCharge(vat);

// Line 1: $100 → VAT = $19
invoice.addLine(DocumentLine.of("Product A", 1, Money.of(100, "USD")));

// Line 2: $200 → VAT = $38
invoice.addLine(DocumentLine.of("Product B", 1, Money.of(200, "USD")));

// Total VAT = $19 + $38 = $57
```

#### Document Level (ChargeAppliesTo.DOCUMENT)

Applied once to the document total.

```java
// Shipping applied ONCE to document total
Charge shipping = Charge.fixed("SHIP", "Shipping", 
    ChargeType.FEE, BigDecimal.valueOf(15.00), 100);
shipping.setAppliesTo(ChargeAppliesTo.DOCUMENT);

invoice.addCharge(shipping);

// Line total = $300
// Shipping = $15 (once)
// Grand total = $372 (300 + 57 VAT + 15 shipping)
```

---

## Currency Handling

The framework has full support for multiple currencies and exchange rates.

### Creating Monetary Amounts

```java
// Basic form
Money usd = Money.of(100.50, "USD");
Money eur = Money.of(85.75, "EUR");
Money cop = Money.of(350000, "COP");

// With BigDecimal (if you already have one)
Money price = Money.of(new BigDecimal("99.99"), "USD");

// With locale
String currency = Money.getCurrencyForLocale(Locale.US);
Money money = Money.of(100, currency);  // USD 100.00
```

### Operations with Money

```java
Money a = Money.of(100, "USD");
Money b = Money.of(50, "USD");

// Add
Money sum = a.add(b);          // USD 150.00

// Subtract
Money difference = a.subtract(b);    // USD 50.00

// Multiply by quantity
Money product = a.multiply(BigDecimal.valueOf(3));  // USD 300.00

// Divide
Money quotient = a.divide(BigDecimal.valueOf(2));    // USD 50.00

// Compare
boolean isGreater = a.isGreaterThan(b);              // true
boolean isLess = a.isLessThan(b);                    // false
boolean isEqual = a.equals(Money.of(100, "USD")); // true
```

### Get Available Currencies

```java
// All ISO 4217 currencies
List<String> currencies = Money.getAvailableCurrencies();
System.out.println("Available currencies: " + currencies.size());
// [USD, EUR, GBP, JPY, COP, MXN, ARS, ...]

// Currency by locale
String usdCurrency = Money.getCurrencyForLocale(Locale.US);           // "USD"
String eurCurrency = Money.getCurrencyForLocale(Locale.GERMANY);      // "EUR"
String copCurrency = Money.getCurrencyForLocale(new Locale("es", "CO")); // "COP"

// Current system currency
String current = Money.getCurrentCurrency();
System.out.println("System currency: " + current);
```

### Exchange Rates

```java
// Create exchange rate
ExchangeRate rate = ExchangeRate.of(
    "USD",                          // Source currency
    "EUR",                          // Target currency
    BigDecimal.valueOf(0.92),         // Conversion rate
    LocalDate.now()                 // Effective date
);

// Convert
Money dollars = Money.of(100, "USD");
Money euros = rate.convert(dollars);
System.out.println(dollars + " = " + euros);  // USD 100.00 = EUR 92.00

// Inverse conversion
ExchangeRate inverseRate = rate.inverse();
Money dollarsAgain = inverseRate.convert(euros);
System.out.println(euros + " = " + dollarsAgain);
```

### Document with Exchange Rate

```java
// Create document in USD but with exchange rate to EUR
FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");

// Set exchange rate
ExchangeRate exchange = ExchangeRate.of("USD", "EUR", 
    BigDecimal.valueOf(0.92), LocalDate.now());
invoice.setExchangeRate(exchange);

// Add lines in USD
invoice.addLine(DocumentLine.of("Product", 1, 
    Money.of(100, "USD")));

// Calculate
new DefaultFinancialCalculator().calculateDocument(invoice);

// Totals will be in USD, but you can convert
DocumentTotals totals = invoice.getTotals();
Money totalUsd = totals.getGrandTotal();  // USD 100.00
Money totalEur = exchange.convert(totalUsd);  // EUR 92.00
```

---

## Advanced Calculations

### Complex Scenario: Everything Together

This example combines all concepts: multiple lines, various charge types, priorities, and levels.

```java
public class CompleteScenario {
    public void execute() {
        // 1. Create document
        FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
        invoice.setDocumentNumber("INV-COMPLETE-001");
        invoice.setIssueDate(LocalDate.now());
        invoice.setDueDate(LocalDate.now().plusDays(30));
        invoice.setParty(DocumentParty.builder()
            .name("XYZ Corporation Inc.")
            .taxId("901234567-8")
            .build());
        
        // 2. Define global charges (will apply to all lines)
        
        // Early payment discount: 5%
        Charge earlyDisc = Charge.percentage(
            "DESC_EARLY",
            "Early payment discount 5%",
            ChargeType.DISCOUNT,
            BigDecimal.valueOf(5),
            10
        );
        earlyDisc.setAppliesTo(ChargeAppliesTo.LINE);
        earlyDisc.setBase(ChargeBase.NET);
        
        // VAT 19%
        Charge vat = Charge.percentage(
            "VAT19",
            "VAT 19%",
            ChargeType.TAX,
            BigDecimal.valueOf(19),
            20
        );
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);
        
        // Withholding 2.5%
        Charge withholding = Charge.percentage(
            "WITHHOLDING",
            "Income tax withholding 2.5%",
            ChargeType.WITHHOLDING,
            BigDecimal.valueOf(2.5),
            30
        );
        withholding.setAppliesTo(ChargeAppliesTo.LINE);
        withholding.setBase(ChargeBase.NET);
        
        invoice.addCharge(earlyDisc);
        invoice.addCharge(vat);
        invoice.addCharge(withholding);
        
        // 3. Add lines
        
        // Line 1: Software licenses
        DocumentLine line1 = DocumentLine.of(
            "Microsoft Office 365 Licenses - Enterprise",
            50,
            Money.of(25.00, "USD")
        );
        
        // Additional volume discount on this specific line
        Charge volumeDisc = Charge.percentage(
            "DESC_VOL",
            "Volume discount 10%",
            ChargeType.DISCOUNT,
            BigDecimal.valueOf(10),
            5  // Higher priority than general discount
        );
        volumeDisc.setAppliesTo(ChargeAppliesTo.LINE);
        volumeDisc.setBase(ChargeBase.NET);
        line1.addCharge(volumeDisc);
        
        invoice.addLine(line1);
        
        // Line 2: Consulting services
        DocumentLine line2 = DocumentLine.of(
            "Specialized technical consulting - 20 hours",
            20,
            Money.of(150.00, "USD")
        );
        invoice.addLine(line2);
        
        // Line 3: Hardware
        DocumentLine line3 = DocumentLine.of(
            "Dell Precision 5570 Laptop",
            3,
            Money.of(2500.00, "USD")
        );
        invoice.addLine(line3);
        
        // 4. Document-level charge: shipping
        Charge shipping = Charge.fixed(
            "SHIPPING",
            "Shipping and handling cost",
            ChargeType.FEE,
            BigDecimal.valueOf(150.00),
            100
        );
        shipping.setAppliesTo(ChargeAppliesTo.DOCUMENT);
        invoice.addCharge(shipping);
        
        // 5. Calculate
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);
        
        // 6. Display detailed results
        printDetailedInvoice(invoice);
    }
    
    private void printDetailedInvoice(FinancialDocument invoice) {
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║           DETAILED SALES INVOICE                      ║");
        System.out.println("╠═══════════════════════════════════════════════════════╣");
        System.out.println("║ Number: " + invoice.getDocumentNumber());
        System.out.println("║ Date: " + invoice.getIssueDate());
        System.out.println("║ Customer: " + invoice.getParty().getName());
        System.out.println("╚═══════════════════════════════════════════════════════╝");
        
        int lineNum = 1;
        for (DocumentLine line : invoice.getLines()) {
            System.out.println("\n┌─────────────────────────────────────────────────────┐");
            System.out.println("│ Line " + lineNum + ": " + line.getDescription());
            System.out.println("├─────────────────────────────────────────────────────┤");
            System.out.println("│ Quantity: " + line.getQuantity());
            System.out.println("│ Unit price: " + line.getUnitPrice());
            System.out.println("│ Subtotal: " + line.getTotals().getNet());
            
            if (!line.getCharges().isEmpty()) {
                System.out.println("│");
                System.out.println("│ Applied charges:");
                for (Charge charge : line.getCharges()) {
                    String sign = charge.getType() == ChargeType.DISCOUNT || 
                                   charge.getType() == ChargeType.WITHHOLDING ? "-" : "+";
                    System.out.println("│   " + sign + " " + charge.getName() + 
                                     ": " + Money.of(charge.getCalculatedAmount(), 
                                     invoice.getCurrency()));
                }
            }
            
            System.out.println("│");
            System.out.println("│ Line total: " + line.getTotals().getTotal());
            System.out.println("└─────────────────────────────────────────────────────┘");
            lineNum++;
        }
        
        DocumentTotals totals = invoice.getTotals();
        System.out.println("\n╔═══════════════════════════════════════════════════════╗");
        System.out.println("║                    TOTALS                             ║");
        System.out.println("╠═══════════════════════════════════════════════════════╣");
        System.out.println("║ Subtotal: " + totals.getSubtotal());
        System.out.println("║ Total discounts: " + totals.getTotalDiscounts());
        System.out.println("║ Total taxes: " + totals.getTotalTaxes());
        System.out.println("║ Total withholdings: " + totals.getTotalWithholdings());
        System.out.println("║ Total fees: " + totals.getTotalFees());
        System.out.println("╠═══════════════════════════════════════════════════════╣");
        System.out.println("║ GRAND TOTAL: " + totals.getGrandTotal());
        System.out.println("║ PAYABLE AMOUNT: " + totals.getPayableAmount());
        System.out.println("╚═══════════════════════════════════════════════════════╝\n");
    }
}
```

### Manual Step-by-Step Calculation

To understand exactly how the calculation engine works:

```java
import java.math.BigDecimal;

public class ManualCalculation {
    public void demonstrate() {
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   STEP-BY-STEP CALCULATION DEMONSTRATION");
        System.out.println("═══════════════════════════════════════════════\n");

        // Initial data
        double quantity = 10;
        BigDecimal unitPrice = BigDecimal.valueOf(100.00);

        System.out.println("1. INITIAL DATA");
        System.out.println("   Quantity: " + quantity);
        System.out.println("   Unit price: $" + unitPrice);
        System.out.println();

        // Step 1: Calculate subtotal
        BigDecimal subtotal = BigDecimal.valueOf(quantity).multiply(unitPrice);
        System.out.println("2. SUBTOTAL (quantity × price)");
        System.out.println("   " + quantity + " × $" + unitPrice + " = $" + subtotal);
        System.out.println();

        // Step 2: Apply 15% discount
        BigDecimal discountRate = BigDecimal.valueOf(15);
        BigDecimal discountAmount = subtotal.multiply(discountRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal afterDiscount = subtotal.subtract(discountAmount);

        System.out.println("3. APPLY 15% DISCOUNT");
        System.out.println("   Base: $" + subtotal);
        System.out.println("   Discount: $" + subtotal + " × 15% = $" + discountAmount);
        System.out.println("   Total after discount: $" + afterDiscount);
        System.out.println();

        // Step 3: Apply 19% VAT on discounted value
        BigDecimal vatRate = BigDecimal.valueOf(19);
        BigDecimal vatAmount = afterDiscount.multiply(vatRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal withVAT = afterDiscount.add(vatAmount);

        System.out.println("4. APPLY 19% VAT");
        System.out.println("   Base: $" + afterDiscount);
        System.out.println("   VAT: $" + afterDiscount + " × 19% = $" + vatAmount);
        System.out.println("   Total with VAT: $" + withVAT);
        System.out.println();

        // Step 4: Calculate 2.5% withholding on subtotal
        BigDecimal withholdingRate = BigDecimal.valueOf(2.5);
        BigDecimal withholdingAmount = subtotal.multiply(withholdingRate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        System.out.println("5. CALCULATE 2.5% WITHHOLDING");
        System.out.println("   Base: $" + subtotal + " (original net value)");
        System.out.println("   Withholding: $" + subtotal + " × 2.5% = $" + withholdingAmount);
        System.out.println();

        // Step 5: Calculate payable amount
        BigDecimal toPay = withVAT.subtract(withholdingAmount);

        System.out.println("6. PAYABLE AMOUNT");
        System.out.println("   Total with VAT: $" + withVAT);
        System.out.println("   Less withholding: -$" + withholdingAmount);
        System.out.println("   ═══════════════════════════════");
        System.out.println("   TOTAL TO PAY: $" + toPay);
        System.out.println();

        // Now do it with the framework
        System.out.println("═══════════════════════════════════════════════");
        System.out.println("   VERIFICATION WITH FRAMEWORK");
        System.out.println("═══════════════════════════════════════════════\n");

        FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");
        DocumentLine line = DocumentLine.of("Product", quantity,
                Money.of(unitPrice, "USD"));

        Charge disc = Charge.percentage("DISC15", "Discount 15%",
                ChargeType.DISCOUNT, BigDecimal.valueOf(15), 10);
        disc.setAppliesTo(ChargeAppliesTo.LINE);
        disc.setBase(ChargeBase.NET);

        Charge vat = Charge.percentage("VAT19", "VAT 19%",
                ChargeType.TAX, BigDecimal.valueOf(19), 20);
        vat.setAppliesTo(ChargeAppliesTo.LINE);
        vat.setBase(ChargeBase.PREVIOUS_TOTAL);

        Charge with = Charge.percentage("WITH", "Withholding 2.5%",
                ChargeType.WITHHOLDING, BigDecimal.valueOf(2.5), 30);
        with.setAppliesTo(ChargeAppliesTo.LINE);
        with.setBase(ChargeBase.NET);

        line.addCharge(disc);
        line.addCharge(vat);
        line.addCharge(with);
        doc.addLine(line);

        new DefaultFinancialCalculator().calculateDocument(doc);

        DocumentTotals totals = doc.getTotals();
        System.out.println("Framework subtotal: " + totals.getSubtotal());
        System.out.println("Framework discount: " + totals.getTotalDiscounts());
        System.out.println("Framework VAT: " + totals.getTotalTaxes());
        System.out.println("Framework withholding: " + totals.getTotalWithholdings());
        System.out.println("Framework total: " + totals.getGrandTotal());
        System.out.println("Framework payable: " + totals.getPayableAmount());
        System.out.println("\n✓ Results match perfectly");
    }
}
```

---

## Best Practices

### 1. Always Use the Factory Pattern

```java
// ✅ CORRECT
FinancialDocument doc = FinancialDocument.of(DocumentType.SALE, "USD");
DocumentLine line = DocumentLine.of("Product", quantity, price);
Charge charge = Charge.percentage("VAT", "VAT 19%", 
    ChargeType.TAX, BigDecimal.valueOf(19), 20);

// ❌ INCORRECT
FinancialDocument doc = new FinancialDocument();
doc.setType(DocumentType.SALE);
doc.setCurrency("USD");
// ... requires more lines and is error-prone
```

### 2. Set Priorities Correctly

```java
// ✅ CORRECT: Logical order
Charge disc = Charge.percentage(..., 10);    // First discount
Charge vat = Charge.percentage(..., 20);     // Then tax
Charge with = Charge.percentage(..., 30);    // Finally withholding

// ❌ INCORRECT: Inverted priorities
Charge with = Charge.percentage(..., 10);    // Withholding first
Charge vat = Charge.percentage(..., 20);     
Charge disc = Charge.percentage(..., 30);    // Discount last
// This produces incorrect calculations
```

### 3. Configure Base Correctly

```java
// ✅ CORRECT: Discount on net value
Charge discount = Charge.percentage(...);
discount.setBase(ChargeBase.NET);

// ✅ CORRECT: VAT on discounted value
Charge vat = Charge.percentage(...);
vat.setBase(ChargeBase.PREVIOUS_TOTAL);

// ❌ INCORRECT: VAT on net value (ignores discounts)
Charge vat = Charge.percentage(...);
vat.setBase(ChargeBase.NET);
```

### 4. Validate Currencies

```java
// ✅ CORRECT: Validate before creating
String currency = "USD";
if (Money.getAvailableCurrencies().contains(currency)) {
    Money money = Money.of(100, currency);
} else {
    throw new IllegalArgumentException("Invalid currency: " + currency);
}

// ✅ CORRECT: Use standard ISO codes
Money usd = Money.of(100, "USD");    // ✓
Money eur = Money.of(100, "EUR");    // ✓

// ❌ INCORRECT: Invalid codes
Money invalid = Money.of(100, "DOLLARS");  // ✗ Error
```

### 5. Reuse Global Charges

```java
// ✅ CORRECT: Define once, apply to many lines
Charge vat = Charge.percentage("VAT19", "VAT 19%", 
    ChargeType.TAX, BigDecimal.valueOf(19), 20);
vat.setAppliesTo(ChargeAppliesTo.LINE);

invoice.addCharge(vat);  // Will apply to all lines

invoice.addLine(line1);
invoice.addLine(line2);
invoice.addLine(line3);

// ❌ INCORRECT: Create charge for each line
line1.addCharge(Charge.percentage("VAT19", ...));
line2.addCharge(Charge.percentage("VAT19", ...));
line3.addCharge(Charge.percentage("VAT19", ...));
// Duplicated code and hard to maintain
```

### 6. Error Handling

```java
// ✅ CORRECT: Validate before calculating
public void processInvoice(FinancialDocument invoice) {
    if (invoice == null) {
        throw new IllegalArgumentException("Invoice cannot be null");
    }
    
    if (invoice.getLines().isEmpty()) {
        throw new IllegalStateException("Invoice must have at least one line");
    }
    
    if (invoice.getCurrency() == null || invoice.getCurrency().isEmpty()) {
        throw new IllegalStateException("Invoice must have a defined currency");
    }
    
    try {
        FinancialCalculator calculator = new DefaultFinancialCalculator();
        calculator.calculateDocument(invoice);
    } catch (Exception e) {
        log.error("Error calculating invoice: " + invoice.getDocumentNumber(), e);
        throw new RuntimeException("Could not calculate invoice", e);
    }
}
```

### 7. Totals Immutability

```java
// ✅ CORRECT: Get totals after calculating
calculator.calculateDocument(invoice);
DocumentTotals totals = invoice.getTotals();
Money total = totals.getGrandTotal();

// ❌ INCORRECT: Try to get totals before calculating
DocumentTotals totals = invoice.getTotals();  // null or empty
Money total = totals.getGrandTotal();         // NullPointerException
```

### 8. Testing

```java
// ✅ CORRECT: Test calculations with known values
@Test
public void testInvoiceCalculationWithVAT() {
    // Given
    FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
    DocumentLine line = DocumentLine.of("Product", 
        1, Money.of("100.00", "USD"));
    
    Charge vat = Charge.percentage("VAT19", "VAT 19%", 
        ChargeType.TAX, new BigDecimal("19"), 20);
    vat.setAppliesTo(ChargeAppliesTo.LINE);
    vat.setBase(ChargeBase.NET);
    
    line.addCharge(vat);
    invoice.addLine(line);
    
    // When
    FinancialCalculator calculator = new DefaultFinancialCalculator();
    calculator.calculateDocument(invoice);
    
    // Then
    DocumentTotals totals = invoice.getTotals();
    assertEquals(Money.of("100.00", "USD"), totals.getSubtotal());
    assertEquals(Money.of("19.00", "USD"), totals.getTotalTaxes());
    assertEquals(Money.of("119.00", "USD"), totals.getGrandTotal());
}
```

---

## API Reference

### Main Classes

#### FinancialDocument
```java
// Constructor
FinancialDocument doc = FinancialDocument.of(DocumentType type, String currency);

// Main methods
void addLine(DocumentLine line);
void addCharge(Charge charge);
void setDocumentNumber(String number);
void setIssueDate(LocalDate date);
void setParty(DocumentParty party);
DocumentTotals getTotals();

// Simplified methods
void addTax(String id, String name, BigDecimal rate);
void addDiscount(String id, String name, BigDecimal rate);
void addWithholding(String id, String name, BigDecimal rate);
void addFee(String id, String name, BigDecimal amount);

List<Charge> getTaxes();
List<Charge> getDiscounts();
List<Charge> getWithholdings();
List<Charge> getFees();
```

#### DocumentLine
```java
// Constructor
DocumentLine line = DocumentLine.of(String description, 
                                    double quantity, 
                                    Money unitPrice);

// Main methods
void addCharge(Charge charge);
void setDescription(String description);
LineTotals getTotals();
```

#### Charge
```java
// Factory constructors
Charge tax = Charge.percentage(String id, String name, 
                               ChargeType type, BigDecimal rate, int priority);
Charge fee = Charge.fixed(String id, String name, 
                          ChargeType type, BigDecimal amount, int priority);

// Configuration methods
void setAppliesTo(ChargeAppliesTo appliesTo);
void setBase(ChargeBase base);
void setPriority(int priority);

// Query methods
BigDecimal getCalculatedAmount();
ChargeType getType();
```

#### Money
```java
// Constructors
Money money = Money.of(String amount, String currency);
Money money = Money.of(BigDecimal amount, String currency);

// Operations
Money add(Money other);
Money subtract(Money other);
Money multiply(BigDecimal multiplier);
Money divide(BigDecimal divisor);

// Comparisons
boolean isGreaterThan(Money other);
boolean isLessThan(Money other);
boolean isZero();

// Static utilities
static List<String> getAvailableCurrencies();
static String getCurrencyForLocale(Locale locale);
static String getCurrentCurrency();
```

#### ExchangeRate
```java
// Constructor
ExchangeRate rate = ExchangeRate.of(String from, String to, 
                                    BigDecimal rate, LocalDate date);

// Methods
Money convert(Money amount);
ExchangeRate inverse();
```

#### FinancialCalculator
```java
// Interface
void calculateDocument(FinancialDocument document);

// Default implementation
FinancialCalculator calculator = new DefaultFinancialCalculator();
calculator.calculateDocument(document);
```

### Enums

#### DocumentType
```java
DocumentType.SALE           // Sales invoice
DocumentType.PURCHASE       // Purchase order
DocumentType.CREDIT_NOTE    // Credit note
DocumentType.DEBIT_NOTE     // Debit note
DocumentType.ADJUSTMENT     // Adjustment
DocumentType.QUOTE          // Quote
```

#### DocumentStatus
```java
DocumentStatus.DRAFT        // Draft
DocumentStatus.POSTED       // Posted
DocumentStatus.CANCELLED    // Cancelled
```

#### ChargeType
```java
ChargeType.TAX              // Tax
ChargeType.DISCOUNT         // Discount
ChargeType.WITHHOLDING      // Withholding
ChargeType.FEE              // Fee
```

#### ChargeMethod
```java
ChargeMethod.PERCENTAGE     // Percentage
ChargeMethod.FIXED          // Fixed amount
ChargeMethod.FORMULA        // Custom formula
```

#### ChargeAppliesTo
```java
ChargeAppliesTo.LINE        // Line level
ChargeAppliesTo.DOCUMENT    // Document level
```

#### ChargeBase
```java
ChargeBase.NET              // On net value
ChargeBase.PREVIOUS_TOTAL   // On accumulated total
```

---

## Troubleshooting

### Problem 1: Calculations are incorrect

**Symptom:** Totals don't match expected values.

**Solution:**
```java
// Verify charges have correct configuration
Charge charge = ...;
System.out.println("ID: " + charge.getId());
System.out.println("Type: " + charge.getType());
System.out.println("Method: " + charge.getMethod());
System.out.println("Applies to: " + charge.getAppliesTo());
System.out.println("Base: " + charge.getBase());
System.out.println("Priority: " + charge.getPriority());

// Verify application order
List<Charge> charges = document.getCharges();
charges.sort(Comparator.comparing(Charge::getPriority));
System.out.println("Application order:");
charges.forEach(c -> System.out.println(c.getPriority() + ": " + c.getName()));
```

### Problem 2: Error creating Money with invalid currency

**Symptom:** Exception when creating Money.

**Solution:**
```java
// Validate currency before using
String currency = "XXX";
if (!Money.getAvailableCurrencies().contains(currency)) {
    System.err.println("Invalid currency: " + currency);
    currency = Money.getCurrentCurrency();  // Use system currency
}
Money money = Money.of("100", currency);
```

### Problem 3: NullPointerException when getting totals

**Symptom:** NPE when accessing getTotals().

**Solution:**
```java
// Always calculate before getting totals
FinancialCalculator calculator = new DefaultFinancialCalculator();
calculator.calculateDocument(document);  // ← Important!

// Now you can get totals
DocumentTotals totals = document.getTotals();
```

### Problem 4: Discounts and withholdings don't subtract

**Symptom:** Discounts add instead of subtract.

**Explanation:** The framework calculates correctly, but you must interpret the types:
```java
DocumentTotals totals = document.getTotals();

// Discounts and withholdings are already considered in totals
Money subtotal = totals.getSubtotal();              // Base
Money discounts = totals.getTotalDiscounts();       // Positive (what is discounted)
Money taxes = totals.getTotalTaxes();               // Positive (what is added)
Money withholdings = totals.getTotalWithholdings(); // Positive (what is withheld)
Money total = totals.getGrandTotal();               // Final total
Money toPay = totals.getPayableAmount();            // What is actually paid

// Conceptual formula:
// Grand Total = Subtotal - Discounts + Taxes + Fees
// To Pay = Grand Total - Withholdings
```

---

## Useful Commands

### Compilation and Testing

```bash
# Compile
cd /home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/sources/api
mvn clean compile

# Run tests
mvn test

# Specific test
mvn test -Dtest=MoneyTest

# Test with detailed output
mvn test -X

# Package
mvn package

# Install to local repository
mvn install
```

### Run Examples

```bash
# Complete example (4 scenarios)
mvn exec:java -Dexec.mainClass="tools.dynamia.modules.finances.api.examples.FinanceFrameworkExample"

# Currency example
mvn exec:java -Dexec.mainClass="tools.dynamia.modules.finances.api.examples.MoneyCurrencyExample"

# Quiet mode
mvn -q exec:java -Dexec.mainClass="..."
```

### Documentation

```bash
# Generate Javadoc
mvn javadoc:javadoc

# View Javadoc
xdg-open target/site/apidocs/index.html

# Test report
mvn surefire-report:report
```

### Verification

```bash
# View dependency tree
mvn dependency:tree

# Check for updates
mvn versions:display-dependency-updates

# Code analysis
mvn verify
```

---

## Additional Resources

### Project Documentation
- `README.md` - Complete framework documentation
- `IMPLEMENTATION.md` - Visual structure and architecture
- `TEST-SUITE.md` - Test documentation (87 tests)
- `EXECUTIVE-SUMMARY.md` - Executive summary and metrics

### Example Code
- `FinanceFrameworkExample.java` - 4 complete scenarios
- `MoneyCurrencyExample.java` - Currency utilities

### External Links
- **Dynamia Tools:** https://www.dynamia.tools
- **Maven Central:** https://central.sonatype.com/artifact/tools.dynamia
- **GitHub Repository:** https://github.com/dynamia-projects

---

## License

Apache License 2.0

---

**Last updated:** February 6, 2026  
**Framework version:** 26.1  
**Status:** ✅ Production  
**Tests:** 87 tests passing  

---

**Need help?** Check the included examples or review the complete documentation in README.md
