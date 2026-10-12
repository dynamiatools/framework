package tools.dynamia.modules.reports.core.testentities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "test_customers")
public class TestCustomer {

    @Id
    private Long id;
    private String name;
    private String city;
    private BigDecimal balance;

    public TestCustomer() {
    }

    public TestCustomer(Long id, String name, String city, BigDecimal balance) {
        this.id = id;
        this.name = name;
        this.city = city;
        this.balance = balance;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCity() {
        return city;
    }

    public BigDecimal getBalance() {
        return balance;
    }
}
