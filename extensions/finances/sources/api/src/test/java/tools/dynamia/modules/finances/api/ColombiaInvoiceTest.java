package tools.dynamia.modules.finances.api;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests para facturación estilo colombiano donde los precios se muestran con IVA incluido
 * pero al facturar se discrimina la base gravable y los impuestos.
 *
 * En Colombia es común que los precios al público incluyan el IVA (19% actualmente),
 * pero en la factura se debe separar:
 * - Base gravable (precio sin IVA)
 * - IVA (19%)
 * - Total (precio con IVA incluido)
 *
 * @author Dynamia Finance Framework
 */
@DisplayName("Colombia Invoice Tests - Precios con IVA Incluido")
class ColombiaInvoiceTest {

    private FinancialCalculator calculator;
    private static final BigDecimal IVA_19 = new BigDecimal("19");
    private static final String CURRENCY_COP = "COP";

    @BeforeEach
    void setUp() {
        calculator = new DefaultFinancialCalculator();
    }

    @Test
    @DisplayName("Caso 1: Almuerzo - Precio con IVA incluido $20.000 COP")
    void testAlmuerzoConIVAIncluido() {
        // Precio al público con IVA incluido
        Money precioConIVA = Money.of("20000", CURRENCY_COP);

        // Extraer base gravable y IVA
        Money baseGravable = precioConIVA.extractBase(IVA_19);
        Money ivaAmount = precioConIVA.extractTax(IVA_19);

        // Verificar cálculos
        System.out.println("\n=== Almuerzo $20.000 COP ===");
        System.out.println("Precio con IVA: " + precioConIVA);
        System.out.println("Base gravable:  " + baseGravable.round(2));
        System.out.println("IVA 19%:        " + ivaAmount.round(2));

        // Base gravable debería ser aproximadamente 16,806.72
        assertEquals(0, new BigDecimal("16806.72").compareTo(baseGravable.round(2).getAmount()));

        // IVA debería ser aproximadamente 3,193.28
        assertEquals(0, new BigDecimal("3193.28").compareTo(ivaAmount.round(2).getAmount()));

        // La suma debe dar el precio original
        Money suma = baseGravable.add(ivaAmount);
        assertEquals(0, precioConIVA.round(2).compareTo(suma.round(2)));
    }

    @Test
    @DisplayName("Caso 2: Factura de restaurante con múltiples items (precios con IVA)")
    void testFacturaRestauranteConIVAIncluido() {
        // Crear factura
        FinancialDocument factura = FinancialDocument.of(DocumentType.SALE, CURRENCY_COP);
        factura.setDocumentNumber("FACT-001");
        factura.setIssueDate(LocalDate.now());
        factura.setParty("Cliente: Juan Pérez");

        // Precios al público (CON IVA incluido)
        Money precioAlmuerzo = Money.of("20000", CURRENCY_COP);
        Money precioBebida = Money.of("5000", CURRENCY_COP);
        Money precioPostre = Money.of("8000", CURRENCY_COP);

        // Extraer base gravable de cada precio
        Money baseAlmuerzo = precioAlmuerzo.extractBase(IVA_19);
        Money baseBebida = precioBebida.extractBase(IVA_19);
        Money basePostre = precioPostre.extractBase(IVA_19);

        // Agregar líneas con la base gravable (sin IVA)
        factura.addLine(DocumentLine.of("Almuerzo Ejecutivo", BigDecimal.ONE, baseAlmuerzo));
        factura.addLine(DocumentLine.of("Bebida Natural", new BigDecimal("2"), baseBebida));
        factura.addLine(DocumentLine.of("Postre del Día", BigDecimal.ONE, basePostre));

        // Configurar IVA 19% que se aplicará sobre la base
        Charge iva = Charge.percentage("IVA19", "IVA 19%", ChargeType.TAX, IVA_19, 20);
        iva.setAppliesTo(ChargeAppliesTo.LINE);
        iva.setBase(ChargeBase.NET);
        factura.addCharge(iva);

        // Calcular
        calculator.calculateDocument(factura);

        // Imprimir factura
        System.out.println("\n=== FACTURA DE VENTA ===");
        System.out.println("No. " + factura.getDocumentNumber());
        System.out.println("Cliente: " + factura.getParty());
        System.out.println("Fecha: " + factura.getIssueDate());
        System.out.println("\nDETALLE:");

        for (DocumentLine line : factura.getLines()) {
            System.out.println(String.format("%-25s  Cant: %s  P.Unit: %s  Subtotal: %s",
                line.getDescription(),
                line.getQuantity(),
                line.getUnitPrice().round(2),
                line.getBaseAmount().round(2)
            ));
        }

        DocumentTotals totals = factura.getTotals();
        System.out.println("\n--- TOTALES ---");
        System.out.println("Subtotal (Base gravable): " + totals.getSubTotal().round(2));
        System.out.println("IVA 19%:                  " + totals.getTaxTotal().round(2));
        System.out.println("TOTAL A PAGAR:            " + totals.getGrandTotal().round(2));

        // Verificar totales
        // Subtotal = 16,806.72 + (2 × 4,201.68) + 6,722.69 = 31,932.77
        Money expectedSubtotal = baseAlmuerzo
            .add(baseBebida.multiply(2))
            .add(basePostre);

        assertEquals(0, expectedSubtotal.round(2).compareTo(totals.getSubTotal().round(2)));

        // Total debe ser 33,000 (20,000 + 10,000 + 8,000 - los precios originales con IVA)
        Money totalEsperado = Money.of("38000", CURRENCY_COP);
        assertEquals(0, totalEsperado.compareTo(totals.getGrandTotal().round(0)));
    }

    @Test
    @DisplayName("Caso 3: Tienda con productos gravados y exentos")
    void testTiendaProductosGravadosYExentos() {
        FinancialDocument factura = FinancialDocument.of(DocumentType.SALE, CURRENCY_COP);
        factura.setDocumentNumber("POS-001");
        factura.setIssueDate(LocalDate.now());

        // Productos gravados (con IVA 19% incluido)
        Money precioJabon = Money.of("12000", CURRENCY_COP);
        Money baseJabon = precioJabon.extractBase(IVA_19);

        // Productos exentos (sin IVA) - el precio es la base directamente
        Money precioLeche = Money.of("5000", CURRENCY_COP);
        Money precioPan = Money.of("3000", CURRENCY_COP);

        // Líneas con IVA
        DocumentLine lineaJabon = DocumentLine.of("Jabón", new BigDecimal("2"), baseJabon);
        Charge ivaJabon = Charge.percentage("IVA19", "IVA 19%", ChargeType.TAX, IVA_19, 20);
        ivaJabon.setAppliesTo(ChargeAppliesTo.LINE);
        lineaJabon.addCharge(ivaJabon);
        factura.addLine(lineaJabon);

        // Líneas sin IVA (productos exentos)
        factura.addLine(DocumentLine.of("Leche (exento)", BigDecimal.ONE, precioLeche));
        factura.addLine(DocumentLine.of("Pan (exento)", new BigDecimal("2"), precioPan));

        // Calcular
        calculator.calculateDocument(factura);

        // Imprimir
        System.out.println("\n=== FACTURA TIENDA ===");
        System.out.println("No. " + factura.getDocumentNumber());
        System.out.println("\nPRODUCTOS:");

        for (DocumentLine line : factura.getLines()) {
            String iva = line.getTotals().getTaxTotal().isZero() ? "EXENTO" : "IVA 19%";
            System.out.println(String.format("%-20s %s  Base: %s  IVA: %s  Total: %s",
                line.getDescription(),
                iva,
                line.getTotals().getBaseAmount().round(2),
                line.getTotals().getTaxTotal().round(2),
                line.getTotals().getNetTotal().round(2)
            ));
        }

        DocumentTotals totals = factura.getTotals();
        System.out.println("\n--- RESUMEN ---");
        System.out.println("Base gravable:   " + totals.getSubTotal().round(2));
        System.out.println("IVA:             " + totals.getTaxTotal().round(2));
        System.out.println("TOTAL:           " + totals.getGrandTotal().round(2));

        // Verificar
        assertNotNull(totals);
        assertTrue(totals.getGrandTotal().isPositive());

        // Total debe ser: 24,000 (jabones con IVA) + 5,000 + 6,000 = 35,000
        Money totalEsperado = Money.of("35000", CURRENCY_COP);
        assertEquals(0, totalEsperado.compareTo(totals.getGrandTotal().round(0)));
    }

    @Test
    @DisplayName("Caso 4: Usar método addTax para calcular precio con IVA")
    void testCalcularPrecioConIVA() {
        // Base: $16,806.72
        Money basePrice = Money.of("16806.72", CURRENCY_COP);

        // Agregar IVA 19%
        Money priceWithTax = basePrice.addTax(IVA_19);

        System.out.println("\n=== Calcular Precio con IVA ===");
        System.out.println("Base:           " + basePrice);
        System.out.println("+ IVA 19%:      " + basePrice.percentage(IVA_19).round(2));
        System.out.println("= Precio final: " + priceWithTax.round(2));

        // Debe dar aproximadamente $20,000
        assertEquals(0, new BigDecimal("20000.00").compareTo(priceWithTax.round(2).getAmount()));
    }

    @Test
    @DisplayName("Caso 5: Factura con descuento sobre precio con IVA incluido")
    void testFacturaConDescuento() {
        FinancialDocument factura = FinancialDocument.of(DocumentType.SALE, CURRENCY_COP);
        factura.setDocumentNumber("FACT-DESC-001");
        factura.setIssueDate(LocalDate.now());

        // Precio al público con IVA: $100,000
        Money precioConIVA = Money.of("100000", CURRENCY_COP);
        Money base = precioConIVA.extractBase(IVA_19);

        // Agregar línea con base
        factura.addLine(DocumentLine.of("Producto Premium", BigDecimal.ONE, base));

        // Descuento 10% sobre la base
        Charge descuento = Charge.percentage("DESC10", "Descuento 10%",
                                             ChargeType.DISCOUNT, new BigDecimal("10"), 10);
        descuento.setAppliesTo(ChargeAppliesTo.LINE);
        descuento.setBase(ChargeBase.NET);
        factura.addCharge(descuento);

        // IVA 19% sobre base después del descuento
        Charge iva = Charge.percentage("IVA19", "IVA 19%", ChargeType.TAX, IVA_19, 20);
        iva.setAppliesTo(ChargeAppliesTo.LINE);
        iva.setBase(ChargeBase.PREVIOUS_TOTAL);
        factura.addCharge(iva);

        // Calcular
        calculator.calculateDocument(factura);

        DocumentTotals totals = factura.getTotals();

        System.out.println("\n=== Factura con Descuento ===");
        System.out.println("Precio original con IVA: " + precioConIVA);
        System.out.println("Base gravable:           " + base.round(2));
        System.out.println("- Descuento 10%:         " + totals.getDiscountTotal().round(2));
        System.out.println("= Base después desc.:    " + base.subtract(totals.getDiscountTotal()).round(2));
        System.out.println("+ IVA 19%:               " + totals.getTaxTotal().round(2));
        System.out.println("= TOTAL A PAGAR:         " + totals.getGrandTotal().round(2));

        // Verificar
        assertNotNull(totals);
        assertTrue(totals.getDiscountTotal().isPositive());
        assertTrue(totals.getTaxTotal().isPositive());

        // Total debe ser menor que 100,000 por el descuento
        assertTrue(totals.getGrandTotal().compareTo(precioConIVA) < 0);
    }

    @Test
    @DisplayName("Caso 6: Retención en la fuente para factura empresarial")
    void testFacturaConRetencion() {
        FinancialDocument factura = FinancialDocument.of(DocumentType.SALE, CURRENCY_COP);
        factura.setDocumentNumber("FACT-EMP-001");
        factura.setIssueDate(LocalDate.now());
        factura.setParty("Empresa ABC S.A.S - Gran Contribuyente");

        // Servicio profesional: $5'000,000 (precio base sin IVA para servicios)
        Money baseServicio = Money.of("5000000", CURRENCY_COP);
        factura.addLine(DocumentLine.of("Consultoría Empresarial", BigDecimal.ONE, baseServicio));

        // IVA 19%
        Charge iva = Charge.percentage("IVA19", "IVA 19%", ChargeType.TAX, IVA_19, 20);
        iva.setAppliesTo(ChargeAppliesTo.LINE);
        factura.addCharge(iva);

        // Retención en la fuente 11% (sobre base gravable)
        Charge retencion = Charge.percentage("RETEFTE", "Retención en la Fuente 11%",
                                            ChargeType.WITHHOLDING, new BigDecimal("11"), 30);
        retencion.setAppliesTo(ChargeAppliesTo.LINE);
        retencion.setBase(ChargeBase.NET); // Sobre la base, no sobre el total con IVA
        factura.addCharge(retencion);

        // Calcular
        calculator.calculateDocument(factura);

        DocumentTotals totals = factura.getTotals();

        System.out.println("\n=== FACTURA EMPRESARIAL CON RETENCIÓN ===");
        System.out.println("Cliente: " + factura.getParty());
        System.out.println("\nBase gravable:           " + totals.getSubTotal());
        System.out.println("+ IVA 19%:               " + totals.getTaxTotal());
        System.out.println("= TOTAL FACTURA:         " + totals.getGrandTotal());
        System.out.println("- Retención Fte (11%):   " + totals.getWithholdingTotal());
        System.out.println("= VALOR A PAGAR:         " + totals.getPayableTotal());

        // Verificaciones
        assertEquals(Money.of("5000000", CURRENCY_COP), totals.getSubTotal());
        assertEquals(Money.of("950000", CURRENCY_COP), totals.getTaxTotal()); // 19% de 5M
        assertEquals(Money.of("5950000", CURRENCY_COP), totals.getGrandTotal()); // 5M + 950K
        assertEquals(Money.of("550000", CURRENCY_COP), totals.getWithholdingTotal()); // 11% de 5M
        assertEquals(Money.of("5400000", CURRENCY_COP), totals.getPayableTotal()); // 5.95M - 550K
    }

    @Test
    @DisplayName("Caso 7: Verificar método extractBase con diferentes tasas de IVA")
    void testExtractBaseConDiferentesTasas() {
        Money precio100k = Money.of("100000", CURRENCY_COP);

        // IVA 19% (tarifa general)
        Money base19 = precio100k.extractBase(new BigDecimal("19"));
        Money iva19 = precio100k.extractTax(new BigDecimal("19"));

        // IVA 5% (algunos alimentos)
        Money base5 = precio100k.extractBase(new BigDecimal("5"));
        Money iva5 = precio100k.extractTax(new BigDecimal("5"));

        // IVA 0% (exento)
        Money base0 = precio100k.extractBase(BigDecimal.ZERO);
        Money iva0 = precio100k.extractTax(BigDecimal.ZERO);

        System.out.println("\n=== Extracción de Base con Diferentes Tasas ===");
        System.out.println("Precio: " + precio100k);
        System.out.println("\nIVA 19%: Base=" + base19.round(2) + " + IVA=" + iva19.round(2));
        System.out.println("IVA  5%: Base=" + base5.round(2) + " + IVA=" + iva5.round(2));
        System.out.println("IVA  0%: Base=" + base0.round(2) + " + IVA=" + iva0.round(2));

        // Verificar que base + IVA = precio original
        assertEquals(precio100k.round(2), base19.add(iva19).round(2));
        assertEquals(precio100k.round(2), base5.add(iva5).round(2));
        assertEquals(precio100k.round(2), base0.add(iva0).round(2));

        // Con IVA 0%, base debe ser igual al precio
        assertEquals(precio100k, base0);
        assertTrue(iva0.isZero());
    }
}
