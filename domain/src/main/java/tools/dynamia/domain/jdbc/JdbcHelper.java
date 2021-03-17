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
package tools.dynamia.domain.jdbc;

import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import tools.dynamia.commons.BeanUtils;
import tools.dynamia.commons.logger.LoggingService;
import tools.dynamia.commons.logger.SLF4JLoggingService;
import tools.dynamia.commons.reflect.PropertyInfo;
import tools.dynamia.commons.reflect.ReflectionException;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * The Class JdbcHelper.
 *
 * @author Mario A. Serrano Leones
 */
public class JdbcHelper {

	private final LoggingService logger = new SLF4JLoggingService(JdbcHelper.class);

	/**
	 * The connection.
	 */
	private Connection connection;

	/**
	 * The data source.
	 */
	private DataSource dataSource;

	private boolean showSQL = true;

	private Statement batch;

	private boolean batchSupported;

	private boolean inTransaction;

	/**
	 * Instantiates a new jdbc helper.
	 *
	 * @param connection
	 *            the connection
	 */
	public JdbcHelper(Connection connection) {
		this.connection = connection;
	}

	/**
	 * Instantiates a new jdbc helper.
	 *
	 * @param dataSource
	 *            the data source
	 */
	public JdbcHelper(DataSource dataSource) {
		this.dataSource = dataSource;
	}

	/**
	 * Query.
	 *
	 * @param sql
	 *            the sql
	 * @return the jdbc data set
	 */
	public JdbcDataSet query(String sql) {
		try {
			showSQL(sql);
			Statement stm = getConnection().createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			ResultSet rs = stm.executeQuery(sql);
			return new JdbcDataSet(stm, rs);
		} catch (Exception ex) {
			throw new JdbcException("Exception executing query: " + sql + "  " + ex.getMessage(), ex);
		}
	}

	/**
	 * Query.
	 *
	 * @param sql
	 *            the sql
	 * @param params
	 *            the params
	 * @return the jdbc data set
	 */
	public JdbcDataSet query(String sql, Object... params) {
		try {
			showSQL(sql);
			PreparedStatement pstm = getConnection().prepareStatement(sql, ResultSet.TYPE_SCROLL_INSENSITIVE,
					ResultSet.CONCUR_READ_ONLY);
			if (sql.contains("?") && params != null && params.length > 0) {
				applyStatementParams(pstm, params);
			}
			ResultSet rs = pstm.executeQuery();
			return new JdbcDataSet(pstm, rs);
		} catch (Exception ex) {
			throw new JdbcException("Exception executing query with params: " + sql + "  " + ex.getMessage(), ex);
		}
	}

	/**
	 * Query.
	 *
	 * @param sql
	 *            the sql
	 * @param params
	 *            the params
	 * @return the jdbc data set
	 */
	public JdbcDataSet query(String sql, Map<String, ?> params) {
		try {
			showSQL(sql);
			NamedParameterJdbcTemplate tmp = new NamedParameterJdbcTemplate(dataSource);
			List<Map<String, Object>> result = tmp.queryForList(sql, params);
			return new JdbcDataSet(result);

		} catch (Exception ex) {
			throw new JdbcException("Exception executing query with params: " + sql + "  " + ex.getMessage(), ex);
		}
	}

	/**
	 * Execute.
	 *
	 * @param sql
	 *            the sql
	 * @return the int
	 */
	public int execute(String sql) {
		Statement stm = null;
		try {
			showSQL(sql);
			stm = getConnection().createStatement();
			return stm.executeUpdate(sql);
		} catch (Exception ex) {
			if (isInTransaction()) {
				rollback();
			}
			throw new JdbcException("Exception executing SQL: " + sql + "  " + ex.getMessage(), ex);
		} finally {
			try {
				stm.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Execute.
	 *
	 * @param sql
	 *            the sql
	 * @param params
	 *            the params
	 * @return the int
	 */
	public int execute(String sql, Object... params) {
		PreparedStatement pstm = null;
		try {
			showSQL(sql);
			pstm = getConnection().prepareStatement(sql);
			if (sql.contains("?") && params != null && params.length > 0) {
				applyStatementParams(pstm, params);
			}
			return pstm.executeUpdate();

		} catch (Exception ex) {
			if (isInTransaction()) {
				rollback();
			}
			throw new JdbcException("Exception executing SQL with params: " + sql + "  " + ex.getMessage(), ex);
		} finally {
			try {
				pstm.close();
			} catch (Exception ex) {
			}
		}
	}

	/**
	 * Call procedure.
	 *
	 * @param name
	 *            the name
	 * @return the callable statement
	 * @throws SQLException
	 *             the SQL exception
	 */
	public CallableStatement callProcedure(String name) throws SQLException {
		String methodName = name.trim();
		if (!methodName.startsWith("{") && !methodName.endsWith("}")) {
			methodName = "{call " + methodName + " }";
		}
		showSQL("Calling procedure: " + methodName);
		return getConnection().prepareCall(methodName);
	}

	/**
	 * Count.
	 *
	 * @param table
	 *            the table
	 * @param where
	 *            the where
	 * @param params
	 *            the params
	 * @return the long
	 */
	public long count(String table, String where, Object... params) {
		long count = 0;
		String sql = "select count(*) as RESULT from " + table + " where " + where;
		try {
			JdbcDataSet qr = query(sql, params);
			if (qr.next()) {
				count = Long.parseLong(qr.getResultSet().getString("RESULT"));
				qr.close();
			}
		} catch (Exception e) {
			throw new JdbcException("Exception Counting " + table, e);
		}
		return count;
	}

	/**
	 * Find all.
	 *
	 * @param table
	 *            the table
	 * @param where
	 *            the where
	 * @param params
	 *            the params
	 * @return the jdbc data set
	 */
	public JdbcDataSet findAll(String table, String where, Object... params) {
		String sql = "select * from " + table + " where " + where;
		return query(sql, params);
	}

	/**
	 * Creates the in parameters.
	 *
	 * @param size
	 *            the size
	 * @return the string
	 */
	public static String createInParameters(int size) {
		StringBuilder sb = new StringBuilder();
		sb.append("(");

		for (int i = 0; i < size; i++) {
			sb.append("?");
			if (i + 1 < size) {
				sb.append(",");
			}
		}
		sb.append(")");
		return sb.toString();
	}

	/**
	 * Apply statement params.
	 *
	 * @param stm
	 *            the stm
	 * @param params
	 *            the params
	 * @throws Exception
	 *             the exception
	 */
	public static void applyStatementParams(PreparedStatement stm, Object[] params) throws Exception {
		int index = 1;
		for (Object param : params) {
			if (param instanceof Collection) {
				Collection col = (Collection) param;
				for (Object elem : col) {
					applyStatementParam(stm, elem, index);
					index++;
				}
			} else {
				applyStatementParam(stm, param, index);
				index++;
			}
		}
	}

	/**
	 * Apply statement param.
	 *
	 * @param s
	 *            the s
	 * @param o
	 *            the o
	 * @param index
	 *            the index
	 * @throws Exception
	 *             the exception
	 */
	private static void applyStatementParam(PreparedStatement s, Object o, int index) throws Exception {

		if (o instanceof Integer) {
			s.setInt(index, (Integer) o);
		} else if (o instanceof Long) {
			s.setLong(index, (Long) o);
		} else if (o instanceof Float) {
			s.setFloat(index, (Float) o);
		} else if (o instanceof BigDecimal) {
			s.setBigDecimal(index, (BigDecimal) o);
		} else if (o instanceof String) {
			s.setString(index, (String) o);
		} else if (o instanceof java.sql.Date) {
			s.setDate(index, (java.sql.Date) o);
		} else if (o instanceof Date) {
			Date d = (Date) o;
			s.setDate(index, new java.sql.Date(d.getTime()));
		} else if (o instanceof Boolean) {
			s.setBoolean(index, (Boolean) o);
		}
	}

	/**
	 * Gets the connection.
	 *
	 * @return the connection
	 */
	public Connection getConnection() {
		if (dataSource != null) {
			try {
				if (connection == null || connection.isClosed()) {
					connection = dataSource.getConnection();
				}
			} catch (SQLException e) {
				throw new JdbcException("Exception getting connection from datasource " + dataSource, e);
			}
		}
		return connection;
	}

	/**
	 * Call commit on current connection. If is in transaction call rollback()
	 * when exception
	 */

	public void commit() {
		try {
			getConnection().commit();

		} catch (SQLException e) {
			if (isInTransaction()) {
				rollback();
			}

			throw new JdbcException("Exception commiting connection", e);
		} finally {
			if (isInTransaction()) {
				try {
					getConnection().setAutoCommit(true);
				} catch (Exception e2) {
					// ignore
				}
			}

			inTransaction = false;

		}
	}

	public void rollback() {
		try {
			getConnection().rollback();
		} catch (SQLException e) {
			// ignore
		}
	}

	public void startTransaction() {

		try {
			getConnection().setAutoCommit(false);
			inTransaction = true;
		} catch (SQLException e) {
			throw new JdbcException("Error starting transaction: " + e.getMessage(), e);
		}
	}

	public boolean isInTransaction() {
		return inTransaction;
	}

	public void closeConnection() {
		try {
			if (!getConnection().isClosed()) {
				getConnection().close();
			}
		} catch (SQLException e) {
			throw new JdbcException("Exception closing connection", e);
		}
	}

	public <T> T mapRow(Class<T> classType, ResultSet rs) {
		return mapRow(classType, rs, null);
	}

	public <T> T mapRow(Class<T> classType, ResultSet rs, ObjectMapper<T> mapper) {
		T object = BeanUtils.newInstance(classType);
		List<PropertyInfo> properties = BeanUtils.getPropertiesInfo(classType);

		for (PropertyInfo property : properties) {
			if (!property.isCollection()) {
				Object value = columnValue(rs, property.getName());
				if (value != null) {
					if (property.is(Boolean.class) || property.is(boolean.class)) {
						value = mapBoolean(value);
					} else if (value instanceof Number) {
						Number number = (Number) value;

						if ((property.is(Long.class) || property.is(long.class))) {
							value = number.longValue();
						} else if (property.is(Double.class) || property.is(double.class)) {
							value = number.doubleValue();
						} else if (property.is(Integer.class) || property.is(int.class)) {
							value = number.intValue();
						} else if (property.is(Float.class) || property.is(float.class)) {
							value = number.floatValue();
						} else if (property.is(BigDecimal.class) && value instanceof Number) {
							value = new BigDecimal(number.toString());
						}
					}

					try {
						if (property.is(String.class) && !(value instanceof String)) {
							value = value.toString();
						}

						BeanUtils.setFieldValue(property, object, value);
					} catch (ReflectionException e) {
						// ignore
					}
				} else if (mapper != null) {
					mapper.map(object, property, rs);
				}
			}
		}

		return object;
	}

	public <T> List<T> mapDataSet(Class<T> classType, JdbcDataSet recordset) {
		return mapDataSet(classType, recordset, null);
	}

	public <T> List<T> mapDataSet(Class<T> classType, JdbcDataSet recordset, ObjectMapper<T> mapper) {
		List<T> list = new ArrayList<>();
		recordset.forEach((rowIndex, rs) -> list.add(mapRow(classType, rs, mapper)));
		return list;
	}

	private Object mapBoolean(Object value) {
		if (value instanceof Boolean) {
			return value;
		} else if (value instanceof String) {
			return value.equals("Y") || value.equals("true");
		} else if (value instanceof Number) {
			return ((Number) value).intValue() == 1;
		} else {
			return value;
		}
	}

	/**
	 * Get column value by name, if column not found, return null.
	 * 
	 * @param rs
	 * @param name
	 * @return
	 */
	public Object columnValue(ResultSet rs, String name) {

		try {
			return rs.getObject(name);
		} catch (SQLException e) {
			return null;
		}

	}

	/**
	 * Get column value by index, if column not found, return null.
	 * 
	 * @param rs
	 * @param index
	 * @return
	 */
	public Object columnValue(ResultSet rs, int index) {
		try {
			return rs.getObject(index);
		} catch (SQLException e) {
			return null;
		}
	}

	private void showSQL(String sql) {
		if (isShowSQL()) {
			logger.info(sql);
		}

	}

	public boolean isShowSQL() {
		return showSQL;
	}

	public void setShowSQL(boolean showSQL) {
		this.showSQL = showSQL;
	}

	/**
	 * Create a new batch statement, use addBatch(sql) later and call
	 * executeBatch(). This method disable autocommit from connection
	 */
	public void createBatch() {
		try {
			Connection con = getConnection();
			con.setAutoCommit(false);
			this.batch = con.createStatement();
			this.batchSupported = con.getMetaData().supportsBatchUpdates();
			logger.info("Creating new batch statement");
			if (!batchSupported) {
				logger.warn("Current database driver do not support Batch updates, using normal executeUpdate");
			} else {
				logger.info("Batch ready");
			}
		} catch (SQLException e) {
			throw new JdbcException("Exception creating batch", e);
		}
	}

	/**
	 * Add new sql to current batch statement or executeUpdate if drive dont
	 * support Batch updates
	 * 
	 * @param sql
	 */
	public void addBatch(String sql) {
		if (batch != null) {
			try {
				sql = sql.trim();
				showSQL("BATCH: " + sql);
				if (batchSupported) {
					batch.addBatch(sql);
				} else {
					batch.executeUpdate(sql);
				}
			} catch (SQLException e) {
				throw new JdbcException("Exception adding to batch: " + e.getMessage(), e);
			}
		} else {
			throw new JdbcException("No batch created, call newBatch() method first");
		}
	}

	/**
	 * Execute and commit batch. If batch statement if not supported just
	 * execute commit and return an empty array of int[]. Finally it clear batch
	 * and set auto commit true.
	 * 
	 * @return
	 */
	public int[] executeBatch() {
		int[] result = new int[0];
		if (batch != null) {
			try {
				if (batchSupported) {
					logger.info("Executing batch");
					result = batch.executeBatch();
				}
				commit();
				logger.info("Batch completed");
			} catch (SQLException e) {
				throw new JdbcException("Exception executing batch: " + e.getMessage(), e);
			} finally {
				try {
					batch.clearBatch();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				try {
					batch.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				batch = null;
				batchSupported = false;

				try {
					getConnection().setAutoCommit(true);
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

		} else {
			throw new JdbcException("No batch created, call newBatch() method first");
		}
		return result;
	}

}
