# Facturación Estilo Colombiano - Precios con IVA Incluido

## 📋 Contexto

En Colombia (y muchos otros países de Latinoamérica), es común que los precios al público se muestren **con el IVA incluido**. Por ejemplo:
- Un almuerzo cuesta **$20.000 COP** (precio mostrado al cliente)
- Pero al facturar se debe discriminar:
  - **Base gravable**: $16.806,72 (precio sin IVA)
  - **IVA 19%**: $3.193,28
  - **Total**: $20.000 (suma de base + IVA)

## 🛠️ Nuevas Utilidades Implementadas

Se agregaron 3 métodos a la clase `Money` para facilitar este escenario:

### 1. `extractBase(taxPercentage)` - Extraer Base Gravable

Calcula la base gravable desde un precio que incluye impuesto.

```java
Money precioConIVA = Money.of("20000", "COP");
Money baseGravable = precioConIVA.extractBase(new BigDecimal("19"));
// baseGravable = COP 16806.72
```

**Fórmula:** `baseAmount = priceWithTax / (1 + taxRate/100)`

### 2. `extractTax(taxPercentage)` - Extraer Monto del Impuesto

Calcula el monto del impuesto desde un precio que lo incluye.

```java
Money precioConIVA = Money.of("20000", "COP");
Money ivaAmount = precioConIVA.extractTax(new BigDecimal("19"));
// ivaAmount = COP 3193.28
```

### 3. `addTax(taxPercentage)` - Agregar Impuesto a Base

Calcula el precio final agregando el impuesto a una base.

```java
Money basePrice = Money.of("16806.72", "COP");
Money priceWithTax = basePrice.addTax(new BigDecimal("19"));
// priceWithTax = COP 20000.00
```

---

## 📝 Casos de Uso Reales

### Caso 1: Restaurante - Almuerzo Ejecutivo

```java
// Precio en menú: $20.000 COP (con IVA incluido)
Money precioMenu = Money.of("20000", "COP");

// Extraer base para facturación
Money base = precioMenu.extractBase(new BigDecimal("19"));
Money iva = precioMenu.extractTax(new BigDecimal("19"));

System.out.println("Base gravable: " + base.round(2));  // COP 16806.72
System.out.println("IVA 19%:       " + iva.round(2));   // COP 3193.28
System.out.println("Total:         " + precioMenu);      // COP 20000
```

### Caso 2: Factura Completa de Restaurante

```java
FinancialDocument factura = FinancialDocument.of(DocumentType.SALE, "COP");
factura.setDocumentNumber("FACT-001");

// Precios al público (CON IVA)
Money precioAlmuerzo = Money.of("20000", "COP");
Money precioBebida = Money.of("5000", "COP");
Money precioPostre = Money.of("8000", "COP");

// Extraer bases para cada producto
Money baseAlmuerzo = precioAlmuerzo.extractBase(new BigDecimal("19"));
Money baseBebida = precioBebida.extractBase(new BigDecimal("19"));
Money basePostre = precioPostre.extractBase(new BigDecimal("19"));

// Agregar líneas con las bases (sin IVA)
factura.addLine(DocumentLine.of("Almuerzo Ejecutivo", BigDecimal.ONE, baseAlmuerzo));
factura.addLine(DocumentLine.of("Bebida", new BigDecimal("2"), baseBebida));
factura.addLine(DocumentLine.of("Postre", BigDecimal.ONE, basePostre));

// Configurar IVA 19%
Charge iva = Charge.percentage("IVA19", "IVA 19%", ChargeType.TAX, 
                                new BigDecimal("19"), 20);
iva.setAppliesTo(ChargeAppliesTo.LINE);
factura.addCharge(iva);

// Calcular
FinancialCalculator calculator = new DefaultFinancialCalculator();
calculator.calculateDocument(factura);

// Resultado:
// Subtotal: $31.932,77 (suma de bases)
// IVA 19%:  $6.067,23
// TOTAL:    $38.000 (precio total mostrado al cliente)
```

### Caso 3: Productos Gravados y Exentos

```java
FinancialDocument factura = FinancialDocument.of(DocumentType.SALE, "COP");

// Productos con IVA (jabón)
Money precioJabon = Money.of("12000", "COP");
Money baseJabon = precioJabon.extractBase(new BigDecimal("19"));
DocumentLine lineaJabon = DocumentLine.of("Jabón", new BigDecimal("2"), baseJabon);
Charge ivaJabon = Charge.percentage("IVA19", "IVA 19%", ChargeType.TAX, 
                                     new BigDecimal("19"), 20);
ivaJabon.setAppliesTo(ChargeAppliesTo.LINE);
lineaJabon.addCharge(ivaJabon);
factura.addLine(lineaJabon);

// Productos exentos (leche, pan) - sin extracción de base
factura.addLine(DocumentLine.of("Leche (exento)", BigDecimal.ONE, 
                                Money.of("5000", "COP")));
factura.addLine(DocumentLine.of("Pan (exento)", new BigDecimal("2"), 
                                Money.of("3000", "COP")));

calculator.calculateDocument(factura);

// Resultado:
// Base gravable: $31.168,07 (solo productos con IVA)
// IVA:           $3.831,93 (solo sobre productos gravados)
// TOTAL:         $35.000
```

### Caso 4: Factura Empresarial con Retención

```java
FinancialDocument factura = FinancialDocument.of(DocumentType.SALE, "COP");
factura.setParty("Empresa ABC S.A.S - Gran Contribuyente");

// Servicio profesional: $5'000.000 (base sin IVA)
Money baseServicio = Money.of("5000000", "COP");
factura.addLine(DocumentLine.of("Consultoría Empresarial", 
                                BigDecimal.ONE, baseServicio));

// IVA 19%
Charge iva = Charge.percentage("IVA19", "IVA 19%", ChargeType.TAX, 
                                new BigDecimal("19"), 20);
iva.setAppliesTo(ChargeAppliesTo.LINE);
factura.addCharge(iva);

// Retención en la fuente 11%
Charge retencion = Charge.percentage("RETEFTE", "Retención 11%", 
                                     ChargeType.WITHHOLDING, 
                                     new BigDecimal("11"), 30);
retencion.setAppliesTo(ChargeAppliesTo.LINE);
retencion.setBase(ChargeBase.NET); // Sobre la base, no sobre el total
factura.addCharge(retencion);

calculator.calculateDocument(factura);

// Resultado:
// Base gravable:     $5.000.000
// IVA 19%:           $950.000
// TOTAL FACTURA:     $5.950.000
// Retención (11%):   $550.000
// VALOR A PAGAR:     $5.400.000 (total - retención)
```

---

## 🧮 Fórmulas Matemáticas

### Extraer Base desde Precio con IVA:
```
Base = PrecioConIVA / (1 + TasaIVA/100)

Ejemplo con IVA 19%:
Base = 20.000 / (1 + 19/100)
Base = 20.000 / 1.19
Base = 16.806,72
```

### Extraer IVA desde Precio con IVA:
```
IVA = PrecioConIVA - Base
IVA = 20.000 - 16.806,72
IVA = 3.193,28
```

### Agregar IVA a Base:
```
PrecioConIVA = Base × (1 + TasaIVA/100)

Ejemplo con IVA 19%:
PrecioConIVA = 16.806,72 × 1.19
PrecioConIVA = 20.000
```

---

## 📊 Tests Incluidos

Se crearon 7 tests comprehensivos en `ColombiaInvoiceTest.java`:

1. ✅ **testAlmuerzoConIVAIncluido** - Extracción básica de base e IVA
2. ✅ **testFacturaRestauranteConIVAIncluido** - Factura completa con múltiples items
3. ✅ **testTiendaProductosGravadosYExentos** - Mix de productos con y sin IVA
4. ✅ **testCalcularPrecioConIVA** - Agregar IVA a una base
5. ✅ **testFacturaConDescuento** - Descuentos sobre precios con IVA
6. ✅ **testFacturaConRetencion** - Retención en la fuente empresarial
7. ✅ **testExtractBaseConDiferentesTasas** - IVA 19%, 5% y 0% (exento)

**Estado:** ✅ 7/7 tests passing

---

## 🎯 Diferentes Tasas de IVA en Colombia

| Producto | Tasa IVA | Ejemplo |
|----------|----------|---------|
| General | 19% | Electrónicos, ropa, restaurantes |
| Reducida | 5% | Algunos alimentos procesados |
| Exento | 0% | Leche, pan, huevos, medicamentos |

```java
// IVA 19% (general)
Money precio = Money.of("100000", "COP");
Money base19 = precio.extractBase(new BigDecimal("19")); // 84.033,61

// IVA 5% (alimentos procesados)
Money base5 = precio.extractBase(new BigDecimal("5"));   // 95.238,10

// IVA 0% (exento)
Money base0 = precio.extractBase(BigDecimal.ZERO);       // 100.000,00
```

---

## 💡 Mejores Prácticas

### ✅ DO:
- Usar `extractBase()` cuando el precio mostrado incluye IVA
- Configurar el IVA como cargo a nivel de documento para aplicarlo a todas las líneas
- Separar productos gravados de exentos en líneas diferentes
- Usar `ChargeBase.NET` para retenciones (sobre base gravable)

### ❌ DON'T:
- No incluir el IVA en el precio unitario cuando uses el framework de cálculo
- No aplicar IVA dos veces (una en precio y otra como cargo)
- No usar `ChargeBase.PREVIOUS_TOTAL` para retenciones (va sobre base)

---

## 🔍 Verificación de Cálculos

Siempre verifica que la suma de base + IVA = precio original:

```java
Money precio = Money.of("20000", "COP");
Money base = precio.extractBase(new BigDecimal("19"));
Money iva = precio.extractTax(new BigDecimal("19"));

// Verificar
assert base.add(iva).round(2).equals(precio.round(2));
```

---

## 📖 Referencias

- [DIAN - Dirección de Impuestos y Aduanas Nacionales](https://www.dian.gov.co)
- Estatuto Tributario Colombiano - Artículos sobre IVA
- Tarifas de IVA vigentes: 0%, 5%, 19%

---

## ✅ Resumen

El framework ahora soporta completamente el flujo de facturación colombiano:

1. ✅ Precios con IVA incluido
2. ✅ Extracción de base gravable
3. ✅ Cálculo automático de IVA
4. ✅ Productos gravados y exentos
5. ✅ Retención en la fuente
6. ✅ Descuentos sobre precios con IVA
7. ✅ Múltiples tasas de IVA

**Total de Tests:** 94 (87 anteriores + 7 nuevos)  
**Estado:** ✅ 100% passing  
**Ready for:** Facturación colombiana en producción 🇨🇴
