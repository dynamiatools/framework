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
package tools.dynamia.zk.crud.ui;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zul.Label;
import tools.dynamia.commons.Messages;
import tools.dynamia.domain.EntityReference;
import tools.dynamia.domain.EntityReferenceRepository;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.web.util.SessionCache;
import tools.dynamia.zk.BindingComponentIndex;
import tools.dynamia.zk.ComponentAliasIndex;
import tools.dynamia.zk.ui.LoadableOnly;

import java.io.Serializable;

public class EntityReferenceLabel extends Label  implements LoadableOnly {

    /**
     *
     */
    private static final long serialVersionUID = -2409612688524316053L;

    static {
        BindingComponentIndex.getInstance().put("referenceId", EntityReferenceLabel.class);
        ComponentAliasIndex.getInstance().put("entityreflabel", EntityReferenceLabel.class);
    }

    private EntityReference reference;
    private Serializable referenceId;
    private String entityClassName;
    private String entityAlias;
    private EntityReferenceRepository repository;

    public Serializable getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(Serializable referenceId) {

        if (this.referenceId == null || !this.referenceId.equals(referenceId)) {
            this.referenceId = referenceId;
            initRepository();
            initValue();
        } else if (this.referenceId == null) {
            reference = null;
            setValue("");
            setTooltiptext("");
        }
    }

    private void initValue() {
        if (repository != null && referenceId != null) {

            reference = (EntityReference) SessionCache.getInstance()
                    .getOrLoad("entityRef" + repository.getAlias() + "" + referenceId, s -> repository.load(referenceId));

            if (reference != null) {
                setValue(reference.getName());
                setTooltiptext(reference.getDescription());
            }
        } else {
            setValue("");
            setTooltiptext("");
        }

    }

    public String getEntityClassName() {
        return entityClassName;
    }

    public void setEntityClassName(String entityClassName) {
        this.entityClassName = entityClassName;
        initRepository();
    }

    public String getEntityAlias() {
        return entityAlias;
    }

    public void setEntityAlias(String entityAlias) {
        this.entityAlias = entityAlias;
        initRepository();
    }

    private void initRepository() {
        if (repository == null) {
            this.repository = DomainUtils.getEntityReferenceRepository(entityClassName);

            if (repository == null) {
                repository = DomainUtils.getEntityReferenceRepositoryByAlias(entityAlias);
            }

            setTooltiptext(repository == null ? Messages.get(EntityReferencePickerBox.class, "noservice") : "");
        }
    }

}
