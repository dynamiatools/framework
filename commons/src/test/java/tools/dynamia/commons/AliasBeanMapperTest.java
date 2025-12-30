package tools.dynamia.commons;

import my.company.Product;
import my.company.Producto;
import org.junit.Assert;
import org.junit.Test;

public class AliasBeanMapperTest {

    @Test
    public void shouldMapAnnotatedToUnannotated() {
        Producto producto = new Producto("Laptop", 1500.0, "LPT001");
        Product product = new Product();

        AliasBeanMapper.map(producto, product, null);

        Assert.assertEquals("Laptop", product.getName());
        Assert.assertEquals(1500.0, product.getPrice(), 0.0);
        Assert.assertEquals("LPT001", product.getSku());
    }
}
