# Dynamia Finance Framework - Quick Reference

## 🚀 Quick Start Commands

### Build & Compile
```bash
# Clean and compile
cd /home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/sources/api
mvn clean compile

# Compile only
mvn compile
```

### Run Tests
```bash
# Run all tests
mvn test

# Run all tests (quiet mode)
mvn test -q

# Run specific test class
mvn test -Dtest=MoneyTest
mvn test -Dtest=FinancialCalculatorTest

# Run specific test method
mvn test -Dtest=MoneyTest#testGetAvailableCurrencies

# Run with verbose output
mvn test -X
```

### Run Examples
```bash
# Run main example (4 scenarios)
mvn exec:java -Dexec.mainClass="tools.dynamia.modules.finances.api.examples.FinanceFrameworkExample"

# Run currency utilities example
mvn exec:java -Dexec.mainClass="tools.dynamia.modules.finances.api.examples.MoneyCurrencyExample"

# Run with quiet mode
mvn -q exec:java -Dexec.mainClass="tools.dynamia.modules.finances.api.examples.FinanceFrameworkExample"
```

### Generate Reports
```bash
# Generate test report
mvn surefire-report:report

# Generate Javadoc
mvn javadoc:javadoc

# Open generated Javadoc
xdg-open target/site/apidocs/index.html
```

### Package
```bash
# Create JAR
mvn package

# Create JAR (skip tests)
mvn package -DskipTests

# Install to local repository
mvn install
```

---

## 📁 Important Paths

```
Project Root:
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/

Source Code:
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/sources/api/src/main/java/tools/dynamia/modules/finances/api/

Test Code:
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/sources/api/src/test/java/tools/dynamia/modules/finances/api/

Examples:
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/sources/api/src/main/java/tools/dynamia/modules/finances/api/examples/

Documentation:
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/README.md
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/IMPLEMENTATION.md
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/TEST-SUITE.md
/home/mario/IdeaProjects/DynamiaPlatform/extensions/finances/EXECUTIVE-SUMMARY.md
```

---

## 💡 Common Use Cases

### 1. Create a Simple Invoice
```java
FinancialDocument invoice = FinancialDocument.of(DocumentType.SALE, "USD");
invoice.setDocumentNumber("INV-001");
invoice.addLine(DocumentLine.of("Product", BigDecimal.ONE, Money.of("100", "USD")));

FinancialCalculator calculator = new DefaultFinancialCalculator();
calculator.calculateDocument(invoice);

System.out.println("Total: " + invoice.getTotals().getGrandTotal());
```

### 2. Add VAT to Invoice
```java
Charge vat = Charge.percentage("VAT19", "VAT 19%", ChargeType.TAX, 
                                new BigDecimal("19"), 20);
vat.setAppliesTo(ChargeAppliesTo.LINE);
invoice.addCharge(vat);
calculator.calculateDocument(invoice);
```

### 3. Apply Discount
```java
Charge discount = Charge.percentage("DISC10", "Discount 10%", 
                                     ChargeType.DISCOUNT, 
                                     new BigDecimal("10"), 10);
invoice.addCharge(discount);
```

### 4. Work with Currencies
```java
// Get all available currencies
List<String> currencies = Money.getAvailableCurrencies();

// Get currency for specific locale
String usCurrency = Money.getCurrencyForLocale(Locale.US); // "USD"

// Get current system currency
String current = Money.getCurrentCurrency();

// Create money with locale-based currency
String currency = Money.getCurrencyForLocale(Locale.GERMANY);
Money price = Money.of("100.00", currency);
```

### 5. Handle Exchange Rates
```java
ExchangeRate rate = ExchangeRate.of("USD", "EUR", new BigDecimal("0.85"), LocalDate.now());
Money usd = Money.of("100", "USD");
Money eur = rate.convert(usd); // EUR 85.00
```

---

## 🔍 Debugging

### Check Compilation Errors
```bash
# Show detailed compilation errors
mvn compile -X

# Check specific file
javac -cp target/classes src/main/java/tools/dynamia/modules/finances/api/Money.java
```

### View Test Results
```bash
# Test results location
cat target/surefire-reports/tools.dynamia.modules.finances.api.MoneyTest.txt

# View all test results
ls -la target/surefire-reports/
```

### Check Dependencies
```bash
# Display dependency tree
mvn dependency:tree

# Check for updates
mvn versions:display-dependency-updates
```

---

## 📊 Project Statistics

### Count Files
```bash
# Count production classes
find src/main/java -name "*.java" | wc -l

# Count test classes
find src/test/java -name "*.java" | wc -l

# Count total lines of code
find src -name "*.java" -exec wc -l {} + | tail -1
```

### Test Statistics
```bash
# Run tests and show summary
mvn test | grep -E "Tests run:|BUILD"

# Count test methods
grep -r "@Test" src/test/java | wc -l
```

---

## 🛠️ Development Workflow

### 1. Make Changes
```bash
# Edit code
vim src/main/java/tools/dynamia/modules/finances/api/Money.java
```

### 2. Compile & Test
```bash
# Quick check
mvn clean compile test -q
```

### 3. Run Specific Tests
```bash
# Test only what you changed
mvn test -Dtest=MoneyTest -q
```

### 4. Verify Everything
```bash
# Full build and test
mvn clean verify
```

---

## 📖 Documentation

### View Javadoc Locally
```bash
# Generate and open
mvn javadoc:javadoc && xdg-open target/site/apidocs/index.html
```

### View README Files
```bash
# Main README
less README.md

# Implementation details
less IMPLEMENTATION.md

# Test documentation
less TEST-SUITE.md

# Executive summary
less EXECUTIVE-SUMMARY.md
```

---

## 🔄 Integration

### Add to Another Project
```xml
<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.finances.api</artifactId>
    <version>26.1</version>
</dependency>
```

### Use in Code
```java
import tools.dynamia.modules.finances.api.*;

public class MyInvoiceService {
    private final FinancialCalculator calculator = new DefaultFinancialCalculator();
    
    public DocumentTotals calculateInvoice(FinancialDocument invoice) {
        calculator.calculateDocument(invoice);
        return invoice.getTotals();
    }
}
```

---

## ✅ Verification Checklist

Before deploying:

- [ ] `mvn clean compile` - Compiles successfully
- [ ] `mvn test` - All 87 tests pass
- [ ] `mvn verify` - Build verification successful
- [ ] `mvn javadoc:javadoc` - Javadoc generates without errors
- [ ] Run examples - Both examples execute correctly
- [ ] Check documentation - All MD files are up to date
- [ ] Review changes - Git status is clean

---

## 📞 Support

### Documentation Files:
- `README.md` - Complete framework documentation
- `IMPLEMENTATION.md` - Visual structure and architecture
- `TEST-SUITE.md` - Complete test documentation
- `EXECUTIVE-SUMMARY.md` - Executive summary and metrics
- `QUICK-REFERENCE.md` - This file

### Example Code:
- `FinanceFrameworkExample.java` - 4 complete scenarios
- `MoneyCurrencyExample.java` - Currency utility examples

### Online Resources:
- Dynamia Tools: https://www.dynamia.tools
- Maven Repository: https://central.sonatype.com/artifact/tools.dynamia

---

**Last Updated:** February 5, 2026  
**Version:** 26.1  
**Status:** ✅ Production Ready
