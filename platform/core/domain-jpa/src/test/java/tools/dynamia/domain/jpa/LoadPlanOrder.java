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
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import tools.dynamia.domain.InitializeOnLoad;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregate root with a declared load plan, used by {@link LoadPlanTest}.
 */
@Entity
@Table(name = "test_LoadPlanOrder")
@InitializeOnLoad({"lines", "lines.subs"})
public class LoadPlanOrder extends BaseEntity {

    private String name;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<LoadPlanLine> lines = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<LoadPlanNote> notes = new ArrayList<>();

    public LoadPlanOrder() {
    }

    public LoadPlanOrder(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<LoadPlanLine> getLines() {
        return lines;
    }

    public void setLines(List<LoadPlanLine> lines) {
        this.lines = lines;
    }

    public List<LoadPlanNote> getNotes() {
        return notes;
    }

    public void setNotes(List<LoadPlanNote> notes) {
        this.notes = notes;
    }
}
