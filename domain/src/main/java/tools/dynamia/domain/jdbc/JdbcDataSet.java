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

import tools.dynamia.domain.query.DataSet;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The Class JdbcDataSet.
 *
 * @author Mario A. Serrano Leones
 */
public class JdbcDataSet extends DataSet<ResultSet> implements Iterable<Row> {

    /**
     * The statement.
     */
    private Statement statement;
    private String name;
    private List<Row> rows;
    private List<String> cols;

    /**
     * Instantiates a new jdbc data set.
     *
     * @param rs the rs
     */
    public JdbcDataSet(ResultSet rs) {
        super(rs);
    }

    /**
     * Instantiates a new jdbc data set.
     *
     * @param statement the statement
     * @param rs        the rs
     */
    public JdbcDataSet(Statement statement, ResultSet rs) {
        super(rs);
        this.statement = statement;
    }

    public JdbcDataSet(List<Map<String, Object>> result) {
        super(null);
        cols = new ArrayList<>();
        rows = new ArrayList<>();
        if (result != null && !result.isEmpty()) {
            rows = new ArrayList<>();
            result.forEach(map -> {

                if (cols == null || cols.isEmpty()) {
                    cols = new ArrayList<>(map.keySet());
                }

                rows.add(new Row(map));
            });
        }
    }

    /**
     * Gets the result set.
     *
     * @return the result set
     */
    public ResultSet getResultSet() {
        return getData();
    }

    /**
     * Close.
     */
    public void close() {
        try {
            getResultSet().close();
        } catch (Exception ignored) {
        }
        try {
            statement.close();
        } catch (Exception ignored) {
        }
    }

    /**
     * Next.
     *
     * @return true, if successful
     */
    public boolean next() {
        try {
            return getResultSet().next();
        } catch (SQLException sQLException) {
            throw new JdbcException(sQLException);
        }
    }

    /**
     * Previous.
     *
     * @return true, if successful
     */
    public boolean previous() {
        try {
            return getResultSet().previous();
        } catch (SQLException sQLException) {
            throw new JdbcException(sQLException);
        }
    }

    /**
     * For each.
     *
     * @param row the row
     */
    public void forEach(JdbcRow row) {
        try {
            reset();
            while (next()) {
                row.process(getResultSet().getRow(), getResultSet());
            }
        } catch (Exception e) {
            throw new JdbcException("Error in forEach", e);
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.dynamia.tools.domain.query.DataSet#getSize()
     */
    @Override
    public long getSize() {
        if (rows != null) {
            return rows.size();
        }
        return -1;
    }

    @Override
    public Iterator<Row> iterator() {
        reset();
        return new Iterator<>() {

            @Override
            public boolean hasNext() {
                return JdbcDataSet.this.next();
            }

            @Override
            public Row next() {
                return new Row(getResultSet());
            }
        };
    }

    public void reset() {
        try {
            if (getData() != null && getData().getType() != ResultSet.TYPE_FORWARD_ONLY) {
                getData().beforeFirst();

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getColumnsLabels() {


        if ((cols == null || cols.isEmpty()) && getData() != null) {
            try {
                this.cols = new ArrayList<>();
                ResultSetMetaData metaData = getData().getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String column = metaData.getColumnLabel(i);
                    cols.add(column);
                }
            } catch (SQLException e) {
                throw new JdbcException("Error geting columns labels:" + e.getMessage(), e);
            }
        }

        return cols;

    }

    public Map<String, Object> getDataMap() {
        return new HashMap<>() {

            @Override
            public Object get(Object key) {
                try {
                    return getData().getObject((String) key);
                } catch (SQLException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                    return null;
                }
            }
        };
    }

    public List<Row> getRows() {
        if (rows == null) {
            rows = new ArrayList<>();
            List<String> columns = getColumnsLabels();

            for (Row row : this) {

                row.loadAll(columns);
                rows.add(row);
            }

        }

        return rows;
    }
}
