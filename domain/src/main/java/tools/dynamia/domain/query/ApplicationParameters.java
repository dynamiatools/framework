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
package tools.dynamia.domain.query;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.SimpleCache;
import tools.dynamia.domain.services.CrudService;
import tools.dynamia.domain.util.DomainUtils;
import tools.dynamia.domain.util.QueryBuilder;
import tools.dynamia.integration.Containers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class ApplicationParameters.
 *
 * @author Mario Serrano Leones
 */
@Transactional
@Service
public class ApplicationParameters implements Parameters {

    private CrudService crudService;

    /**
     * The cache.
     */
    private SimpleCache<String, Parameter> cache = new SimpleCache<>();

    @Autowired
    public ApplicationParameters(CrudService crudService) {
        this.crudService = crudService;
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getParameters(java.util.List)
     */
    @Override
    public List<Parameter> getParameters(List<String> paramNames) {
        return getParameters(DomainUtils.getDefaultParameterClass(), paramNames);
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getParameters(java.lang.Class, java.util.List)
     */
    @Override
    public List<Parameter> getParameters(Class<? extends Parameter> parameterClass, List<String> paramNames) {
        QueryParameters qp = QueryParameters.with("name", QueryConditions.in(paramNames));
        return (List<Parameter>) crudService.find(parameterClass, qp);

    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#all()
     */
    @Override
    public List<Parameter> all() {
        return (List<Parameter>) crudService.findAll(DomainUtils.getDefaultParameterClass(), "name");
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getParameter(java.lang.String)
     */
    @Override
    public Parameter getParameter(String name) {
        return getParameter(DomainUtils.getDefaultParameterClass(), name);
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getParameter(java.lang.Class, java.lang.String)
     */
    @Override
    public Parameter getParameter(Class<? extends Parameter> parameterClass, String name) {
        String identifier = getIdentifier(parameterClass);

        Parameter param = cache.get(identifier + name);
        if (param == null) {
            param = crudService.findSingle(parameterClass, "name", QueryConditions.eq(name));
            if (param != null && param.isCacheable()) {
                cache.add(identifier + name, param);
            }
        }
        return param;
    }

    private String getIdentifier(Class<? extends Parameter> parameterClass) {
        Parameter pid = BeanUtils.newInstance(parameterClass);
        return pid.identifier();
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#save(com.dynamia.tools.domain.query.Parameter)
     */
    @Override
    public void save(Parameter p) {
        if (p.getId() == null) {
            crudService.create(p);
        } else {
            crudService.update(p);
        }
        cache.remove(p.identifier() + p.getName());
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#save(java.util.Collection)
     */
    @Override
    public void save(Collection<Parameter> params) {
        if (params == null) {
            throw new NullPointerException("Cannot save null list of parameters");
        }

        for (Parameter p : params) {
            save(p);
        }
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getValue(java.lang.String)
     */
    @Override
    public String getValue(String parameter) {
        return getValue(DomainUtils.getDefaultParameterClass(), parameter);
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getValue(java.lang.Class, java.lang.String)
     */
    @Override
    public String getValue(Class<? extends Parameter> parameterClass, String parameter) {
        Parameter param = getParameter(parameterClass, parameter);
        if (param != null) {
            return param.getValue();
        } else {
            return null;
        }
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getValue(java.lang.String, java.lang.String)
     */
    @Override
    public String getValue(String parameter, String defaultValue) {
        return getValue(DomainUtils.getDefaultParameterClass(), parameter, defaultValue);
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#getValue(java.lang.Class, java.lang.String, java.lang.String)
     */
    @Override
    public String getValue(Class<? extends Parameter> parameterClass, String parameter, String defaultValue) {
        Parameter p = getParameter(parameterClass, parameter);
        String value = null;

        if (p != null) {
            value = p.getValue();
        } else {
            p = BeanUtils.newInstance(parameterClass);
            p.setName(parameter);
            p.setValue(defaultValue);
            save(p);
            value = defaultValue;
        }

        return value;
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#setParameter(java.lang.Class, java.lang.String, java.lang.Object)
     */
    @Override
    public void setParameter(Class<? extends Parameter> parameterClass, String name, Object value) {
        Parameter p = getParameter(parameterClass, name);

        if (p == null) {
            p = BeanUtils.newInstance(parameterClass);
            p.setName(name);
            p.setValue(value.toString());
            p.setValueType(value.getClass().getName());

        }
        p.setValue(value.toString());
        save(p);
    }

    /* (non-Javadoc)
     * @see com.dynamia.tools.domain.query.Parameters#setParameter(java.lang.String, java.lang.Object)
     */
    @Override
    public void setParameter(String name, Object value) {
        setParameter(DomainUtils.getDefaultParameterClass(), name, value);
    }

    /**
     * Clear cache.
     */
    public void clearCache() {
        cache.clear();
    }

    //utility metho

    /**
     * Gets the.
     *
     * @return the parameters
     */
    public static Parameters get() {
        return Containers.get().findObject(Parameters.class);
    }

    @Override
    public void increaseCounter(Parameter counterParam) {
        var paramClass = DomainUtils.getDefaultParameterClass();
        if (paramClass == null) {
            throw new RuntimeException("No default parameter class found");
        }
        var sql = "update " + paramClass.getName() + " p set p.value =  coalesce(p.value,0) + 1 where p.id = :id ";
        crudService.execute(sql, QueryParameters.with("id", counterParam.getId()));
    }

    @Override
    @Transactional
    public long findNextCounterValue(Parameter counterParam) {
        increaseCounter(counterParam);

        var paramClass = DomainUtils.getDefaultParameterClass();
        if (paramClass == null) {
            throw new RuntimeException("No default parameter class found");
        }

        String value = crudService.executeProjection(String.class, "select coalesce(p.value,0) from " + paramClass.getName() + " p where p.id = :id",
                QueryParameters.with("id", counterParam.getId()));

        return Long.parseLong(value);
    }
}
