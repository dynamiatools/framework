[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.finances.api)](https://search.maven.org/search?q=tools.dynamia.modules.importer)
![Java Version Required](https://img.shields.io/badge/java-25-blue)

# Dynamia Finance Framework
Base framework for calculating classic financial documents

## Overview

The **Dynamia Finance Framework** is a lightweight, domain-driven calculation engine designed to handle the complex mathematics behind financial documents. It provides a robust foundation for building financial systems without imposing constraints on presentation, persistence, or business rules.

This framework focuses exclusively on the **calculation logic** that powers invoices, credit notes, purchase orders, and other financial documents. It handles the intricate details of applying taxes, discounts, withholdings, and fees in the correct order, managing multiple currencies, and producing accurate, auditable totals.

## Objective

Provide a reusable, tested, and production-ready core for all common financial calculations in documents such as:

- **Sales invoices** - Customer billing with taxes and discounts
- **Purchase orders** - Vendor procurement with applicable charges
- **Credit / Debit notes** - Adjustments to existing documents
- **Adjustments** - Corrections and modifications
- **Quotes** - Pre-sale estimates and proposals
- **Charges and credits** - Standalone financial transactions

The framework is built around a flexible **charge system** that unifies the treatment of:
- **Taxes** (VAT, sales tax, excise duties)
- **Discounts** (commercial, promotional, volume-based)
- **Withholdings** (income tax retention, social security)
- **Fees** (shipping, handling, environmental charges)

## Design Philosophy

### 1. **Separation of Concerns**
The framework strictly separates calculation logic from:
- **Persistence** - No JPA, no database dependencies
- **Presentation** - No UI components or web dependencies
- **Business Rules** - No workflow or approval logic
- **Legislation** - No country-specific tax rules

This makes it a pure **domain model** that can be integrated into any system.

### 2. **Deterministic Calculations**
Given the same inputs, the framework will always produce the same outputs. Calculations are:
- **Repeatable** - Running twice yields identical results
- **Auditable** - Every step can be traced and verified
- **Testable** - Unit tests can verify correctness

### 3. **No Side Effects**
The calculation engine:
- Does not modify external state
- Does not query external services
- Does not persist data
- Does not publish events (events are returned for optional handling)

### 4. **Extensibility First**
The framework is designed to be extended, not modified:
- New charge types can be added without changing core code
- Custom calculation strategies can be plugged in
- Regional rules can be implemented as separate modules
- Business-specific logic lives outside the framework

### 5. **Currency-Aware**
All monetary amounts are strongly typed with currency information:
- Prevents accidental operations between different currencies
- Supports exchange rates at document level
- Handles rounding according to currency precision

## What the Framework DOES

✅ **Calculate line totals** - Base amount, quantity × unit price  
✅ **Apply charges in order** - Respects priority for correct calculation  
✅ **Handle percentage and fixed charges** - Including formula-based charges  
✅ **Support line-level and document-level charges** - Flexible application  
✅ **Manage multiple currencies** - With frozen exchange rates  
✅ **Aggregate totals** - Subtotals, tax totals, grand totals  
✅ **Calculate payable amounts** - After withholdings and credits  
✅ **Handle rounding** - Currency-appropriate rounding rules  
✅ **Validate charge application** - Ensures charges apply to valid bases  
✅ **Provide immutable totals** - Results are frozen and auditable  

## What the Framework DOES NOT Implement

❌ **User Interface** - No views, forms, or UI components  
❌ **Persistence** - No JPA entities, repositories, or database logic  
❌ **Accounting** - No journal entries, ledgers, or double-entry bookkeeping  
❌ **Tax Legislation** - No country-specific tax rules or regulations  
❌ **Electronic Invoicing** - No XML generation, digital signatures, or government integrations  
❌ **Workflow** - No approval processes, status transitions, or business rules  
❌ **Reporting** - No report generation or data visualization  
❌ **Numbering** - No document number sequences or prefixes  

These concerns should be implemented in layers above the framework.

## Use Cases

### For Product Developers
- Build ERP systems with consistent financial calculations
- Create e-commerce platforms with complex pricing rules
- Develop POS systems with tax and discount logic
- Build subscription billing systems

### For System Integrators
- Standardize calculation logic across multiple applications
- Replace inconsistent spreadsheet-based calculations
- Migrate legacy systems to modern architecture
- Integrate with accounting systems

### For Consultants
- Prototype financial logic quickly
- Demonstrate calculation scenarios to clients
- Build country-specific adapters on top of the core
- Create testing frameworks for financial systems

## Key Benefits

🎯 **Consistency** - Same calculation logic everywhere  
🎯 **Reliability** - Thoroughly tested and validated  
🎯 **Flexibility** - Adapt to any business model  
🎯 **Maintainability** - Clear, documented, modular code  
🎯 **Performance** - Lightweight with no external dependencies  
🎯 **Testability** - Easy to unit test and verify  
🎯 **Extensibility** - Plugin architecture for customization  

## Architecture Highlights

- **Pure Java** - No framework dependencies (Spring optional for events)
- **Immutable Value Objects** - Money, ExchangeRate, Totals
- **Strategy Pattern** - Pluggable calculation strategies
- **Domain Events** - Optional event publishing for integration
- **Builder Pattern** - Fluent APIs for object construction
- **Comparator Chain** - Charge ordering by priority
- **No Null Values** - Uses Optional where appropriate

## Integration Points

The framework integrates easily with:
- **Spring Boot** - Use as domain services
- **Jakarta EE** - CDI-compatible components
- **Microservices** - Lightweight calculation service
- **Legacy Systems** - Wrap existing persistence layers
- **Event-Driven Architectures** - Publish calculation events

## Who Should Use This Framework

✅ Developers building financial or e-commerce applications  
✅ Teams needing consistent calculation logic across systems  
✅ Organizations replacing manual calculation processes  
✅ Projects requiring auditable financial calculations  
✅ Systems that need multi-currency support  

## Who Should NOT Use This Framework

❌ Simple applications with only basic arithmetic (use BigDecimal directly)  
❌ Systems that need country-specific tax logic built-in (extend this framework)  
❌ Projects requiring full ERP functionality (this is just the calculation core)  

---

**In summary:** The Dynamia Finance Framework is a calculation engine, not a complete financial system. It provides the mathematical foundation that other systems can build upon, ensuring accuracy, consistency, and maintainability of financial calculations.

---

## 1. Fundamental Concepts

### 1.1 FinancialDocument
Root entity representing any document with economic impact.

Attributes:
- id
- type: SALE | PURCHASE | CREDIT_NOTE | DEBIT_NOTE | ADJUSTMENT | QUOTE
- status: DRAFT | POSTED | CANCELLED
- issueDate
- dueDate
- party (customer, supplier, third party)
- currency
- exchangeRate
- lines: List<DocumentLine>
- charges: List<Charge> (at document level)
- totals: DocumentTotals

Rules:
- The document does NOT calculate.
- The document only contains state and structure.
- Calculation is delegated to an external engine.

---

## 2. Document Lines

### 2.1 DocumentLine
Represents an economic line of the document.

Attributes:
- id
- description
- quantity
- unitPrice
- charges: List<Charge> (at line level)
- totals: LineTotals

Rules:
- Each line is calculated independently.
- Charges can be applied per line or inherited from the document.

---

## 3. Charge System

### 3.1 Charge
Single abstraction for taxes, discounts, withholdings, and fees.

Attributes:
- code
- name
- type: TAX | DISCOUNT | WITHHOLDING | FEE
- rateType: PERCENTAGE | FIXED | FORMULA
- value
- appliesTo: LINE | DOCUMENT
- base: NET | GROSS | PREVIOUS_TOTAL
- refundable: Boolean
- priority: Integer (application order)

Examples:
- VAT 19% → TAX / PERCENTAGE
- Withholding 2.5% → WITHHOLDING / PERCENTAGE
- Commercial discount → DISCOUNT / FIXED
- Environmental fee → FEE / FIXED

Rules:
- Charges are applied in ascending priority order.
- A charge can depend on the result of another.

---

## 4. Calculation Engine

### 4.1 FinancialCalculator
Central service of the framework.

Responsibilities:
- Calculate totals per line
- Apply line charges
- Apply document charges
- Group taxes
- Resolve rounding
- Generate final totals

Suggested methods:
- calculateLine(DocumentLine)
- calculateDocument(FinancialDocument)
- recalculate(FinancialDocument)

Rules:
- Calculation must be deterministic.
- The engine does not persist data.
- The engine can be replaceable by strategy.

---

## 5. Totals

### 5.1 LineTotals
Totals per line.

Fields:
- baseAmount
- discountTotal
- taxTotal
- withholdingTotal
- feeTotal
- netTotal

---

### 5.2 DocumentTotals
Consolidated document totals.

Fields:
- subTotal
- discountTotal
- taxTotal
- withholdingTotal
- feeTotal
- grandTotal
- payableTotal

Notes:
- payableTotal = grandTotal - withholdingTotal
- All totals must be persisted for audit purposes.

---

## 6. Money and Currency

### 6.1 Money
Monetary value object.

Fields:
- amount
- currency

Rules:
- Operations between different currencies are not allowed without a rate.

---

### 6.2 ExchangeRate
Represents an exchange rate.

Fields:
- fromCurrency
- toCurrency
- rate
- date

Rules:
- The document saves the rate used.
- Calculation never queries external rates.

---

## 7. Document Lifecycle

States:
- DRAFT → editable
- POSTED → calculations frozen
- CANCELLED → no financial impact

Rules:
- A POSTED document is not recalculated.
- Modifications require reversal or note.

---

## 8. Domain Events

Suggested events:
- BeforeDocumentCalculate
- AfterDocumentCalculate
- BeforeDocumentPost
- AfterDocumentPost
- OnChargeApplied
- OnTotalsCalculated

Usage:
- Validations
- Integrations
- Automation
- Legal or regional extensions

---

## 9. Extensibility

The framework allows:
- New charge types
- New calculation rules
- Regional rules as plugins
- Charge definition via metadata (YAML)

Example YAML:
```yaml
charge:
  code: VAT19
  type: TAX
  rateType: PERCENTAGE
  value: 19
  appliesTo: LINE
  priority: 20
```

---

# Implementation Work Plan

## 📋 Structure Analysis

Based on the provided schema and Dynamia Tools conventions, the framework will be organized as follows:

### Location
```
DynamiaPlatform/extensions/finances/src/main/java/tools/dynamia/finances/
```

### Proposed Package Structure
```
tools.dynamia.finances/
├── model/                    # Domain entities
│   ├── FinancialDocument
│   ├── DocumentLine
│   ├── Charge
│   ├── Money
│   └── ExchangeRate
├── types/                    # Enumerations and types
│   ├── DocumentType
│   ├── DocumentStatus
│   ├── ChargeType
│   ├── RateType
│   └── ChargeBase
├── totals/                   # Total value objects
│   ├── LineTotals
│   └── DocumentTotals
├── calculator/               # Calculation engine
│   ├── FinancialCalculator (interface)
│   ├── DefaultFinancialCalculator
│   ├── LineCalculator
│   ├── ChargeCalculator
│   └── TotalsAggregator
├── strategy/                 # Calculation strategies
│   ├── ChargeStrategy (interface)
│   ├── PercentageChargeStrategy
│   ├── FixedChargeStrategy
│   └── FormulaChargeStrategy
├── events/                   # Domain events
│   ├── DocumentEvent (abstract)
│   ├── BeforeDocumentCalculateEvent
│   ├── AfterDocumentCalculateEvent
│   ├── BeforeDocumentPostEvent
│   ├── AfterDocumentPostEvent
│   ├── ChargeAppliedEvent
│   └── TotalsCalculatedEvent
├── exceptions/               # Specific exceptions
│   ├── FinancialCalculationException
│   ├── InvalidCurrencyOperationException
│   └── InvalidDocumentStateException
└── util/                     # Utilities
    ├── MoneyCalculator
    ├── RoundingUtil
    └── ChargeComparator
```

---

## 🎯 Implementation Phases

### **Phase 1: Foundations** ✅
**Objective:** Establish base types and value objects

**Files to create:**
1. `types/DocumentType.java` - Enum with document types
2. `types/DocumentStatus.java` - Enum with states
3. `types/ChargeType.java` - Enum with charge types
4. `types/RateType.java` - Enum with rate types
5. `types/ChargeBase.java` - Enum with calculation bases
6. `model/Money.java` - Value object for monetary amounts
7. `model/ExchangeRate.java` - Exchange rate model

**Features:**
- Immutability in value objects
- Complete Javadoc in English
- Utility methods in Money (add, subtract, multiply, etc.)

---

### **Phase 2: Totals** ✅
**Objective:** Implement total containers

**Files to create:**
1. `totals/LineTotals.java` - Totals per line
2. `totals/DocumentTotals.java` - Consolidated totals

**Features:**
- Immutable objects (or with builder pattern)
- Access and aggregation methods
- toString() for debugging

---

### **Phase 3: Core Model** ✅
**Objective:** Implement main entities

**Files to create:**
1. `model/Charge.java` - Generic charge
2. `model/DocumentLine.java` - Document line
3. `model/FinancialDocument.java` - Main document

**Features:**
- No JPA annotations
- Bidirectional relationships where applicable
- Basic validations (NotNull, etc.)
- Business methods (addLine, addCharge, etc.)

---

### **Phase 4: Calculation Strategies** ✅
**Objective:** Implement Strategy pattern for different charge types

**Files to create:**
1. `strategy/ChargeStrategy.java` - Main interface
2. `strategy/PercentageChargeStrategy.java`
3. `strategy/FixedChargeStrategy.java`
4. `strategy/FormulaChargeStrategy.java`

**Features:**
- Pure Strategy pattern
- Each strategy handles a specific RateType
- Extensible for new types

---

### **Phase 5: Utilities** ✅
**Objective:** Auxiliary tools

**Files to create:**
1. `util/MoneyCalculator.java` - Operations with Money
2. `util/RoundingUtil.java` - Rounding handling
3. `util/ChargeComparator.java` - Comparator by priority

**Features:**
- Static methods
- Stateless
- Reusable

---

### **Phase 6: Calculation Engine** ✅
**Objective:** Implement central calculator

**Files to create:**
1. `calculator/FinancialCalculator.java` - Main interface
2. `calculator/ChargeCalculator.java` - Charge applicator
3. `calculator/LineCalculator.java` - Line calculator
4. `calculator/TotalsAggregator.java` - Totals aggregator
5. `calculator/DefaultFinancialCalculator.java` - Main implementation

**Features:**
- Use of strategies
- Deterministic calculation
- No side effects
- Respects charge priorities

---

### **Phase 7: Events** ✅
**Objective:** Domain event system

**Files to create:**
1. `events/DocumentEvent.java` - Base event
2. `events/BeforeDocumentCalculateEvent.java`
3. `events/AfterDocumentCalculateEvent.java`
4. `events/BeforeDocumentPostEvent.java`
5. `events/AfterDocumentPostEvent.java`
6. `events/ChargeAppliedEvent.java`
7. `events/TotalsCalculatedEvent.java`

**Features:**
- Compatible with Spring's ApplicationEventPublisher
- Immutable
- With timestamp

---

### **Phase 8: Exceptions** ✅
**Objective:** Specific error handling

**Files to create:**
1. `exceptions/FinancialCalculationException.java`
2. `exceptions/InvalidCurrencyOperationException.java`
3. `exceptions/InvalidDocumentStateException.java`

**Features:**
- Extends RuntimeException
- Descriptive messages
- Serializable

---

### **Phase 9: Documentation** ✅
**Objective:** README and examples

**Files to create:**
1. Usage examples in Javadoc comments
2. Integration examples

---

## 📐 Design Principles

### ✅ Applied:
- **Domain-Driven Design:** Rich model with business logic
- **Immutability:** Immutable value objects
- **Strategy Pattern:** For different calculation types
- **Event-Driven:** Event system for extensibility
- **No JPA:** Pure POJOs without persistence
- **Single Responsibility:** Each class one responsibility
- **Open/Closed:** Open for extension, closed for modification

### ✅ Dynamia Tools Conventions:
- Complete Javadoc in English
- Clean and modular code
- Examples in documentation
- No coupling with other modules
- Location in extensions (framework independent)

---

## 🔍 Technical Considerations

### Dependencies Needed:
- Java 17+ (records, pattern matching)
- Commons Lang (utilities)
- No Spring dependencies (pure core)
- Optional: Jackson for Money serialization

### Testing Strategy:
- Unit tests for each calculator
- Integration tests for complete flow
- Edge case tests (rounding, multiple currencies)

---

## 📊 Proposed Execution Order

1. **Phase 1** → Base types (no dependencies)
2. **Phase 2** → Totals (depends on Money)
3. **Phase 3** → Model (depends on types and totals)
4. **Phase 5** → Utilities (depends on Money)
5. **Phase 4** → Strategies (depends on Charge and Money)
6. **Phase 6** → Calculator (depends on all previous)
7. **Phase 8** → Exceptions (independent)
8. **Phase 7** → Events (depends on model)
9. **Phase 9** → Documentation

---

## ✅ Final Validation

Before finalizing, we will verify:
- [ ] All files compile without errors
- [ ] Complete and correct Javadoc
- [ ] No dependencies on JPA or Spring
- [ ] README with clear examples
- [ ] Code follows Dynamia Tools conventions

---

## 🚀 Estimation

- **Total classes:** ~30
- **Estimated time:** Complete implementation in one session
- **Complexity:** Medium-High

---

## Usage Examples

### Basic Example: Creating and Calculating a Sales Invoice

```java
// Create a sales document
FinancialDocument invoice = new FinancialDocument();
invoice.setType(DocumentType.SALE);
invoice.setStatus(DocumentStatus.DRAFT);
invoice.setCurrency("USD");
invoice.setIssueDate(LocalDate.now());

// Add a line
DocumentLine line1 = new DocumentLine();
line1.setDescription("Product A");
line1.setQuantity(new BigDecimal("10"));
line1.setUnitPrice(Money.of("100", "USD"));

// Add VAT charge to the line
Charge vat = new Charge();
vat.setCode("VAT19");
vat.setName("Value Added Tax 19%");
vat.setType(ChargeType.TAX);
vat.setRateType(RateType.PERCENTAGE);
vat.setValue(new BigDecimal("19"));
vat.setAppliesTo(ChargeAppliesTo.LINE);
vat.setPriority(20);

line1.addCharge(vat);
invoice.addLine(line1);

// Calculate the document
FinancialCalculator calculator = new DefaultFinancialCalculator();
calculator.calculateDocument(invoice);

// Access totals
System.out.println("Subtotal: " + invoice.getTotals().getSubTotal());
System.out.println("Tax Total: " + invoice.getTotals().getTaxTotal());
System.out.println("Grand Total: " + invoice.getTotals().getGrandTotal());
```

### Example: Multiple Charges with Priority

```java
// Commercial discount (priority 10)
Charge discount = new Charge();
discount.setCode("DISC10");
discount.setType(ChargeType.DISCOUNT);
discount.setRateType(RateType.PERCENTAGE);
discount.setValue(new BigDecimal("10"));
discount.setPriority(10);

// Tax after discount (priority 20)
Charge tax = new Charge();
tax.setCode("VAT19");
tax.setType(ChargeType.TAX);
tax.setRateType(RateType.PERCENTAGE);
tax.setValue(new BigDecimal("19"));
tax.setPriority(20);

// Withholding after tax (priority 30)
Charge withholding = new Charge();
withholding.setCode("WHOLD25");
withholding.setType(ChargeType.WITHHOLDING);
withholding.setRateType(RateType.PERCENTAGE);
withholding.setValue(new BigDecimal("2.5"));
withholding.setPriority(30);

line.addCharge(discount);
line.addCharge(tax);
line.addCharge(withholding);

calculator.calculateLine(line);
```

### Example: Document-Level Charges

```java
// Shipping fee at document level
Charge shipping = new Charge();
shipping.setCode("SHIP");
shipping.setName("Shipping Cost");
shipping.setType(ChargeType.FEE);
shipping.setRateType(RateType.FIXED);
shipping.setValue(new BigDecimal("50"));
shipping.setAppliesTo(ChargeAppliesTo.DOCUMENT);

invoice.addCharge(shipping);
calculator.calculateDocument(invoice);
```

---

## License

This framework is part of Dynamia Tools and follows the same license terms.
