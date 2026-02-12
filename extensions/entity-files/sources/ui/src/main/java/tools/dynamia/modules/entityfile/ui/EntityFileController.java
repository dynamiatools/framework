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

package tools.dynamia.modules.entityfile.ui;

import tools.dynamia.domain.query.QueryConditions;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.modules.entityfile.EntityFileAccountProvider;
import tools.dynamia.modules.entityfile.domain.EntityFile;
import tools.dynamia.modules.entityfile.enums.EntityFileType;
import tools.dynamia.modules.entityfile.service.EntityFileService;
import tools.dynamia.ui.icons.Icons;
import tools.dynamia.ui.icons.IconsTheme;
import tools.dynamia.zk.crud.TreeCrudController;
import tools.dynamia.zk.crud.ui.EntityTreeNode;
import tools.dynamia.zk.crud.ui.LazyEntityTreeNode;

import java.io.Serial;
import java.util.List;

public class EntityFileController extends TreeCrudController<EntityFile> {

    private final EntityFileService service;
    private Object targetEntity;

    /**
     *
     */
    @Serial
    private static final long serialVersionUID = 7926996145692421296L;

    public EntityFileController() {
        this.service = Containers.get().findObject(EntityFileService.class);
        setParentName("parent");
    }

    @Override
    protected List<EntityFile> loadRoots() {
        return service.getEntityFiles(targetEntity);
    }

    @Override
    protected List<EntityFile> loadChildren(EntityFile parentEntity) {
        return service.getEntityFiles(targetEntity, parentEntity);
    }

    @Override
    protected void afterCreate() {
        if (getSelected() != null && getSelected().getType() == EntityFileType.DIRECTORY) {
            getEntity().setParent(getSelected());
        }
    }

    @Override
    protected EntityTreeNode<EntityFile> newNode(EntityFile entity) {
        EntityTreeNode node = null;

        switch (entity.getType()) {
            case DIRECTORY:
                node = new LazyEntityTreeNode(entity, entity.getName(), this);
                node.setIcon("folder");
                node.setOnOpenListener(this);
                break;
            case FILE:
            case IMAGE:

                node = new EntityTreeNode(entity, entity.getName());

                var icon = IconsTheme.get().getIcon(entity.getExtension() + "-file");
                if (icon != null && icon.getName() != null) {
                    node.setIcon(icon.getName());
                } else {
                    node.setIcon("entityfile");
                }
                break;
            default:
                break;
        }

        return node;
    }

    public void setTargetEntity(Object targetEntity) {
        this.targetEntity = targetEntity;
    }

    public Object getTargetEntity() {
        return targetEntity;
    }

    @Override
    protected void beforeQuery() {
        if (targetEntity != null) {
            setParemeter("targetEntity", QueryConditions.eq(getTargetEntity().getClass().getName()));

            Object id = DomainUtils.findEntityId(getTargetEntity());
            if (id instanceof Long) {
                setParemeter("targetEntityId", QueryConditions.eq(id));
            } else {
                setParemeter("targetEntitySId", QueryConditions.eq(id.toString()));
            }
        }

        EntityFileAccountProvider provider = Containers.get().findObject(EntityFileAccountProvider.class);
        if (provider != null) {
            setParemeter("accountId", provider.getAccountId());
        }
    }
}
