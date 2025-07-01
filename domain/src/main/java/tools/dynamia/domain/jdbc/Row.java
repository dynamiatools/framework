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

package tools.dynamia.domain.jdbc;

import tools.dynamia.commons.logger.LoggingService;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Row extends HashMap<String, Object> {

    /**
     *
     */
    private static final long serialVersionUID = 6405893485092211340L;
    public ResultSet resultSet;
    private Map<String, Object> cache;

    public Row(Map<String, Object> data) {
        this.cache = data;
    }


    public Row(ResultSet resultSet) {
        super();
        this.resultSet = resultSet;
    }


    public ResultSet getResultSet() {
        return resultSet;
    }

    public Object getColumnValue(int index) {
        return col(index);
    }

    public Object col(int index) {
        try {
            return resultSet.getObject(index);
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    public Object getColumnValue(String name) {
        return col(name);
    }

    public Object col(String name) {
        try {
            Object value = null;
            if (cache != null) {
                value = cache.get(name);
            }

            if (value == null && cache == null && resultSet != null) {
                value = resultSet.getObject(name);
            }
            return value;
        } catch (SQLException e) {
            throw new JdbcException(e);
        }
    }

    @Override
    public Object put(String key, Object value) {
        throw new UnsupportedOperationException("No usar");
    }

    @Override
    public Object get(Object key) {
        return getColumnValue(key.toString());
    }

    public void loadAll(List<String> columns) {
        cache = new HashMap<>();
        if (resultSet != null) {
            columns.forEach(c -> {
                try {
                    cache.put(c, resultSet.getObject(c));
                } catch (SQLException e) {
                    LoggingService.get(Row.class).error("Error loading data", e);
                }
            });
        }

    }

}
