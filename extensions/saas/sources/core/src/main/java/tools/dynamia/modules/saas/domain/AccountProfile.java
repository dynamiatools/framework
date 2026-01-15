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

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import org.hibernate.annotations.BatchSize;
import tools.dynamia.domain.jpa.BaseEntity;
import tools.dynamia.domain.contraints.NotEmpty;

@Entity
@Table(name = "saas_profiles")
@BatchSize(size = 10)
public class AccountProfile extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4559514760400696153L;
	@NotEmpty
	private String name;
	@Column(length = 1000)
	private String description;


	@OneToMany(mappedBy = "profile", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
	private List<AccountProfileRestriction> restrictions = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public List<AccountProfileRestriction> getRestrictions() {
		return restrictions;
	}

	public void setRestrictions(List<AccountProfileRestriction> restrictions) {
		this.restrictions = restrictions;
	}

	@Override
	public String toString() {
		return getName();
	}

}
