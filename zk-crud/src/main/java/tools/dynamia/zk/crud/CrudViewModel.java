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

import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.domain.ValidationError;
import tools.dynamia.domain.query.QueryParameters;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.navigation.NavigationManager;
import tools.dynamia.ui.MessageType;
import tools.dynamia.ui.UIMessages;
import tools.dynamia.zk.AbstractViewModel;
import tools.dynamia.zk.util.ZKBindingUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Very simple MVVM ZK crud view model, just create, read (all), update and delete. Extend this class if you want more commands
 */
public class CrudViewModel<T> extends AbstractViewModel<T> {


    public static final String MODEL_CLASS = "modelClass";

    private final CrudService crudService;
    private Class<T> modelClass;
    private Object parent;
    private String parentName;
    private T selected;
    private List<T> result = new ArrayList<>();
    private QueryParameters params = new QueryParameters();

    public CrudViewModel() {
        this.crudService = crudService();
    }

    public CrudViewModel(Class<T> modelClass) {
        this.crudService = crudService();
        this.modelClass = modelClass;

    }

    public CrudViewModel(CrudService crudService, Class<T> modelClass) {
        this.crudService = crudService;
        this.modelClass = modelClass;
    }

    @Init
    protected void init() {
        if (modelClass == null) {
            Class mc = null;
            try {
                //trying to get model class from current pages attributes
                mc = (Class) NavigationManager.getCurrent().getCurrentPage().getAttribute(MODEL_CLASS);
            } catch (Exception e) {
                //ignore
            }

            if (mc == null) {
                try {
                    //trying to get model class from execution parameters
                    mc = (Class) Executions.getCurrent().getAttribute(MODEL_CLASS);
                } catch (Exception e) {
                    //ignore
                }
            }

            if (mc != null) {
                this.modelClass = mc;
            }
        }
    }

    @Command
    @NotifyChange("result")
    public void query() {
        if (params != null) {
            this.result = crudService.find(modelClass, params);
        } else {
            this.result = crudService.findAll(modelClass);
        }
        this.result = new ArrayList<>(this.result);
    }

    @Command
    @NotifyChange("result")
    public void create() {
        T model = BeanUtils.newInstance(modelClass);
        this.result.add(model);
    }


    @Command
    @NotifyChange("result")
    public void save(@BindingParam("entity") T entity) {
        try {
            createRelationship(entity);
            T savedEntity = crudService.save(entity);
            if (result != null) {
                int index = result.indexOf(entity);
                result.set(index, savedEntity);
            }
            UIMessages.showMessage("OK");
        } catch (ValidationError e) {
            UIMessages.showMessage(e.getMessage(), MessageType.WARNING);
        } catch (Exception e) {
            UIMessages.showMessage("Error " + e.getMessage(), MessageType.ERROR);
        }
    }

    @Command
    public void delete(@BindingParam("entity") T entity) {
        if (entity != null) {
            UIMessages.showQuestion("Esta seguro de borrar " + entity + "?", () -> {
                try {
                    crudService.executeWithinTransaction(() -> crudService.delete(entity));
                    UIMessages.showMessage("Borrado OK");
                    ZKBindingUtil.postNotifyChange(this, "result");
                } catch (ValidationError e) {
                    UIMessages.showMessage(e.getMessage(), MessageType.WARNING);
                } catch (Exception e) {
                    UIMessages.showMessage("ERROR: " + e.getMessage(), MessageType.ERROR);
                }
            });
        }
    }

    private void createRelationship(T model) {
        if (model != null && parent != null && parentName != null && !parentName.isEmpty()) {
            BeanUtils.setFieldValue(parentName, model, parent);
        }

    }

    public Class<T> getModelClass() {
        return modelClass;
    }

    public QueryParameters getParams() {
        return params;
    }

    public void setParams(QueryParameters params) {
        this.params = params;
    }
}
