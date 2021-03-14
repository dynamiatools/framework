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
package tools.dynamia.zk.crud;

import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.ValueWrapper;
import tools.dynamia.commons.reflect.ReflectionException;
import tools.dynamia.crud.CrudControllerException;
import tools.dynamia.crud.SubcrudControllerAPI;
import tools.dynamia.domain.query.DataSet;
import tools.dynamia.domain.query.ListDataSet;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.ValidatorService;
import tools.dynamia.domain.util.CrudServiceListener;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.integration.Containers;
import tools.dynamia.zk.viewers.table.TableView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class SubcrudController<E> extends CrudController<E> implements SubcrudControllerAPI<E> {

    /**
     *
     */
    private static final long serialVersionUID = 2791457285184056200L;
    private String parentName;
    private Object parent;
    private List<E> toBeUpdatedEntities = new ArrayList<>();
    private List<E> toBeCreatedEntities = new ArrayList<>();
    private List<E> toBeDeletedEntities = new ArrayList<>();
    private String childrenName;

    public SubcrudController(Object parent, String parentName, String childrenName) {
        this(null, parent, parentName, childrenName);
    }

    public SubcrudController(Class<E> entityClass, Object parent, String parentName, String childrenName) {
        super(entityClass);
        this.parentName = parentName;
        this.parent = parent;
        this.childrenName = childrenName;
        inspectParentChildrens();
        setSaveWithNewTransaction(false);
    }

    @Override
    public void newEntity() {
        super.newEntity();
        if (parent != null) {
            relateChildParent(getEntity(), parent);
        }
    }

    private void inspectParentChildrens() {
        if (parent != null && DomainUtils.findEntityId(parent) == null && childrenName != null) {
            Collection<E> children = (Collection<E>) BeanUtils.invokeGetMethod(parent, childrenName);
            if (children != null) {
                for (E child : children) {
                    if (DomainUtils.findEntityId(child) == null) {
                        toBeCreatedEntities.add(child);
                    }
                }
                children.clear();
            }
        }
    }

    @Override
    protected void beforeQuery() {
        setParemeter(parentName, parent);
    }

    @Override
    public void query() {
        if (DomainUtils.findEntityId(parent) != null) {
            super.query();
        } else {
            setQueryResult(new ListDataSet(Collections.EMPTY_LIST));
        }
    }

    @Override
    public void save() {
        ValidatorService validatorService = Containers.get().findObject(ValidatorService.class);
        validatorService.validate(getEntity());

        if (DomainUtils.findEntityId(parent) == null) {
            fireCrudListener();

            //add child to parent after validated
            createRelationship(getEntity(), parent);

            if (DomainUtils.findEntityId(getEntity()) == null) {
                if (!toBeCreatedEntities.contains(getEntity())) {
                    toBeCreatedEntities.add(getEntity());
                }
            } else if (!toBeUpdatedEntities.contains(getEntity())) {
                toBeUpdatedEntities.add(getEntity());
            }
        } else {
            if (DomainUtils.findEntityId(getEntity()) == null) {
                createRelationship(getEntity(), parent);
            }
            super.save();
        }

    }

    @Override
    public void delete() {
        Serializable parentId = DomainUtils.findEntityId(parent);
        if (parentId == null) {
            if (DomainUtils.findEntityId(getSelected()) != null) {
                toBeUpdatedEntities.remove(getSelected());
                toBeDeletedEntities.add(getSelected());
            } else {
                toBeCreatedEntities.remove(getSelected());
            }

            if (dataSetView instanceof TableView) {
                TableView tableView = (TableView) dataSetView;
                tableView.getSelectedItem().detach();
            }

        } else {
            super.delete();
        }

    }

    protected void setParentEntity(Object parentEntity) {
        this.parent = parentEntity;
        if (getEntity() != null) {
            relateChildParent(getEntity(), parent);
        }
    }

    private void createRelationship(E newChild, Object parent) {
        relateChildParent(newChild, parent);
        relateParentChild(newChild, parent);
    }

    /**
     * This is like: parent.getChildren().add(child);
     *
     * @param newChild
     * @param parent
     */
    protected void relateParentChild(E newChild, Object parent) {
        try {

            Object object = BeanUtils.invokeGetMethod(parent, childrenName);
            if (object != null && object instanceof Collection) {
                Collection children = (Collection) object;
                children.add(newChild);

            }

        } catch (Exception e) {
            logger.error("Cannot create relationship parent <-> child in SubcrudController " + getEntityClass() + ". Check children name ["
                    + childrenName + "]", e);

        }

    }

    /**
     * This is like: child.setParent(value)
     *
     * @param newChild
     * @param parent
     */
    protected void relateChildParent(E newChild, Object parent) {
        try {
            BeanUtils.invokeSetMethod(newChild, parentName, parent);
        } catch (ReflectionException e) {
            if (e.getCause().getClass() == NoSuchMethodException.class) {
                if (parent instanceof ValueWrapper) {
                    parent = ((ValueWrapper) parent).getValue();
                }

                if (parent.getClass().getSuperclass() != Object.class) {
                    createRelationship(newChild, new ValueWrapper(parent, parent.getClass().getSuperclass()));
                } else {
                    throw new CrudControllerException("Cannot create relationship parent <-> child in SubcrudController "
                            + getEntityClass() + ". Check parent name [" + parentName + "]", e);
                }
            }
        }
    }

    @Override
    public void setQueryResult(DataSet queryResult) {
        addInMemoryResults();
        super.setQueryResult(queryResult);
    }

    private void addInMemoryResults() {
        if (dataSetView instanceof TableView) {
            List<E> defaultValues = new ArrayList<>();
            defaultValues.addAll(toBeCreatedEntities);
            defaultValues.addAll(toBeUpdatedEntities);

            TableView tableView = (TableView) dataSetView;
            tableView.setDefaultValue(defaultValues);
        }

    }

    public void doCreates() {
        for (E entity : toBeCreatedEntities) {
            crudService.create(entity);
        }
        toBeCreatedEntities.clear();

        for (SubcrudController subCrud : getSubcontrollers()) {
            subCrud.doCreates();
        }
    }

    public void doUpdates() {
        for (E entity : toBeUpdatedEntities) {
            crudService.update(entity);
        }
        toBeUpdatedEntities.clear();
        for (SubcrudController subCrud : getSubcontrollers()) {
            subCrud.doUpdates();
        }
    }

    public void doDeletes() {
        for (SubcrudController subCrud : getSubcontrollers()) {
            subCrud.doDeletes();
        }

        for (E entity : toBeDeletedEntities) {
            crudService.delete(entity.getClass(), DomainUtils.findEntityId(entity));
        }
        toBeDeletedEntities.clear();

    }

    private List<Serializable> createIdList(List<E> objects) {
        List<Serializable> ids = new ArrayList<>();

        for (E entity : objects) {
            ids.add(DomainUtils.findEntityId(entity));
        }
        return ids;
    }

    @Override
    public QueryParameters getParams() {
        beforeQuery();
        return super.getParams();
    }

    @Override
    public Object getParentEntity() {
        return parent;
    }

    private void fireCrudListener() {
        for (CrudServiceListener listener : Containers.get().findObjects(CrudServiceListener.class)) {
            try {
                listener.beforeCreate(getEntity());
            } catch (ClassCastException e) {
            }
        }
    }

    @Override
    public List<E> getToBeUpdatedEntities() {
        return toBeUpdatedEntities;
    }

    @Override
    public List<E> getToBeCreatedEntities() {
        return toBeCreatedEntities;
    }

    @Override
    public List<E> getToBeDeletedEntities() {
        return toBeDeletedEntities;
    }

    @Override
    public String getParentName() {
        return parentName;
    }

    @Override
    public String getChildrenName() {
        return childrenName;
    }
}
