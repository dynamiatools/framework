/*
 * Copyright (C) 2023 Dynamia Soluciones IT S.A.S - NIT 900302344-1
 * Colombia / South America
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package tools.dynamia.modules.saas.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.BatchSize;
import tools.dynamia.domain.jpa.SimpleEntity;
import tools.dynamia.domain.contraints.NotEmpty;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.saas.api.AccountServiceQuantityCalculator;
import tools.dynamia.modules.saas.domain.enums.AutoQuantityOperation;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;

@Entity
@Table(name = "saas_add_services")
@BatchSize(size = 20)
public class AccountAdditionalService extends SimpleEntity {

    @ManyToOne
    @JsonIgnore
    private Account account;
    private String reference;
    @NotEmpty
    private String name;
    @Column(length = 1000)
    private String description;
    private BigDecimal price = BigDecimal.ZERO;
    private int quantity = 1;
    private BigDecimal total = BigDecimal.ZERO;

    private boolean autoQuantity;
    private String quantityCalculator;
    private AutoQuantityOperation autoQtyOp;


    public void compute() {
        if (price != null) {
            BigDecimal old = total;
            total = price.multiply(BigDecimal.valueOf(quantity));
            notifyChange("total", old, total);
        } else {
            total = BigDecimal.ZERO;
        }
    }

    public Account getAccount() {
        return account;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public BigDecimal getTotal() {
        if (total == null) {
            total = BigDecimal.ZERO;
        }
        return total;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
        compute();
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        compute();
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public boolean isAutoQuantity() {
        return autoQuantity;
    }

    public void setAutoQuantity(boolean autoQuantity) {
        this.autoQuantity = autoQuantity;
    }

    public String getQuantityCalculator() {
        return quantityCalculator;
    }

    public void setQuantityCalculator(String quantityCalculator) {
        this.quantityCalculator = quantityCalculator;
    }

    public void updateQuantity() {
        if (autoQuantity) {
            var calculator = Containers.get().findObjects(AccountServiceQuantityCalculator.class)
                    .stream().filter(s -> s.getId().equals(quantityCalculator))
                    .findFirst().orElse(null);

            if (calculator != null && account != null && account.getId() != null) {
                var qty = (int) calculator.calculate(account.getId());
                if (qty >= 0) {
                    var op = autoQtyOp == null ? AutoQuantityOperation.REPLACE : autoQtyOp;

                    switch (op) {
                        case ADD -> setQuantity(this.quantity + qty);
                        case REPLACE -> setQuantity(qty);
                        case SUBSTRACT -> setQuantity(Math.abs(this.quantity - qty));
                    }

                }
            }
        }
    }

    public AutoQuantityOperation getAutoQtyOp() {
        return autoQtyOp;
    }

    public void setAutoQtyOp(AutoQuantityOperation autoQtyOp) {
        this.autoQtyOp = autoQtyOp;
    }
}
