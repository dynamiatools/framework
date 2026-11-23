package mybookstore.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import tools.dynamia.domain.jpa.BaseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoices")
public class Invoice extends BaseEntity {


    @ManyToOne
    @JoinColumn(name = "customer_id")
    @NotNull
    private Customer customer;
    private String number;
    private String email;
    private BigDecimal total;
    @OneToMany(mappedBy = "invoice", orphanRemoval = true, cascade = CascadeType.ALL)
    private List<InvoiceDetail> details = new ArrayList<>();

    public void calcTotal() {
        total = details.stream().map(InvoiceDetail::calcSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<InvoiceDetail> getDetails() {
        return details;
    }

    public void setDetails(List<InvoiceDetail> details) {
        this.details = details;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }


}
