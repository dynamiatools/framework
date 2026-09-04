package tools.dynamia.commons;

import my.company.Product;
import my.company.Producto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class AliasBeanMapperTest {

    @Test
    public void shouldMapAnnotatedToUnannotated() {
        Producto producto = new Producto("Laptop", 1500.0, "LPT001");
        Product product = new Product();

        AliasBeanMapper.map(producto, product, null);

        Assertions.assertEquals("Laptop", product.getName());
        Assertions.assertEquals(1500.0, product.getPrice(), 0.0);
        Assertions.assertEquals("LPT001", product.getSku());
    }
}
