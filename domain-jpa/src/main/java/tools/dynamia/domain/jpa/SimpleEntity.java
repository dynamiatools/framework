/*
 * Copyright (C) 2021 Dynamia Soluciones IT S.A.S - NIT 900302344-1
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

import tools.dynamia.domain.AbstractEntity;

import javax.persistence.*;

/**
 * The Class SimpleEntity.
 */
@MappedSuperclass
public abstract class SimpleEntity extends AbstractEntity<Long> {

    /**
     * The id.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * The version.
     */
    @Version
    private int version;

    /*
	 * (non-Javadoc)
	 * 
	 * @see AbstractEntity#getId()
     */
    @Override
    public Long getId() {
        return id;
    }

    /*
	 * (non-Javadoc)
	 * 
	 * @see AbstractEntity#setId(java.io.Serializable)
     */
    @Override
    public void setId(Long id) {
        Long oldId = this.id;
        this.id = id;
        notifyChange("id", oldId, id);
    }
    
    

}
