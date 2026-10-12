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
package tools.dynamia.domain.jpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "test_LoadPlanLine")
public class LoadPlanLine extends BaseEntity {

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    private LoadPlanOrder order;

    @OneToMany(mappedBy = "line", cascade = CascadeType.ALL)
    private List<LoadPlanSub> subs = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LoadPlanOrder getOrder() {
        return order;
    }

    public void setOrder(LoadPlanOrder order) {
        this.order = order;
    }

    public List<LoadPlanSub> getSubs() {
        return subs;
    }

    public void setSubs(List<LoadPlanSub> subs) {
        this.subs = subs;
    }
}
