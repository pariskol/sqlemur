package gr.kgdev.sqlemur.core;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import gr.kgdev.sqlemur.orm.ORMUtils;

public class SQLemur {

	private static final List<Object> EMPTY_LIST = Collections.emptyList();
	private DataSource dataSource;
	private String driver;
	private String url;
	private String user;
	private String password;
	private boolean isCcMode = false;

	public SQLemur(String driver, String url, String user, String password) {
		this.url = url;
		this.driver = driver;
		this.user = user;
		this.password = password;
		this.dataSource = this.initDatasource();
	}

	protected DataSource initDatasource() {
		return DatasourceFactory.createDbcp2Datasource(getDriver(), getUrl(), getUser(), getPassword());
	}

	/**
	 * Creates a jdbc prepared statement for given query and params.
	 * 
	 * @param conn
	 * @param query
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	private PreparedStatement prepareStatementWithParams(Connection conn, String query, List<Object> params)
			throws SQLException {
		var statement = conn.prepareStatement(query);
		var i = 1;
		for (var param : params) {
			if (param == null || param.toString().equals(""))
				statement.setNull(i++, Types.VARCHAR);
			else if (param instanceof byte[]) {
				statement.setBytes(i++, (byte[]) param);
			} else
				statement.setObject(i++, param);
		}

		return statement;
	}

	public void checkConnection() throws SQLException {
		try (var conn = dataSource.getConnection();) {
		}
	}

	/**
	 * Executes query. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(String query, ResultSetAction action) throws SQLException {
		try (var conn = dataSource.getConnection()) {
			executeQuery(conn, query, action);
		}
	}

	
	/**
	 * Executes query with the provided connection. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(Connection conn, String query, ResultSetAction action) throws SQLException {
		try (var statement = conn.createStatement(); var rset = statement.executeQuery(query);) {
			while (rset.next()) {
				if (action != null)
					action.onResultSet(rset);
			}
		}
	}

	/**
	 * Executes query. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(String query, List<Object> params, ResultSetAction action) throws SQLException {
		try (var conn = dataSource.getConnection()) {
			executeQuery(conn, query, params, action);
		}
	}

	/**
	 * Executes query. Action provided is applied to each row of result set.
	 * 
	 */
	public void executeQuery(Connection conn, String query, List<Object> params, ResultSetAction action)
			throws SQLException {
		try (var statement = prepareStatementWithParams(conn, query, params);
				var rset = statement.executeQuery();) {
			while (rset.next()) {
				action.onResultSet(rset);
			}
		}
	}

	/**
	 * Executes query. It tries to map rows to map. You have to cast the result. It
	 * throws unchecked exceptions.
	 * 
	 */
	public ArrayList<Map<String, Object>> executeQueryToList(String query) throws SQLException {
		return executeQueryToList(query, EMPTY_LIST);
	}

	private LinkedHashMap<String, Object> map(ResultSet rset) throws SQLException {
		try {
			return isCcMode() ? ORMUtils.mapCC(rset) : ORMUtils.map(rset);
		} catch (Exception e) {
			throw new SQLException("Could not map result set", e);
		}
	}

	private <T> T map(ResultSet rset, Class<?> clazz) throws SQLException {
		try {
			return ORMUtils.map(rset, clazz);
		} catch (Exception e) {
			throw new SQLException("Could not map result set", e);
		}
	}

	/**
	 * Enables automatic conversion of db columns names to camel case on retrieval.
	 * 
	 * @param enable
	 */
	public void enableCcMode(boolean enable) {
		this.isCcMode = enable;
	}

	/**
	 * Checks if automatic transformation of db columns names to camel case is
	 * active.
	 * 
	 * @return
	 */
	public boolean isCcMode() {
		return this.isCcMode;
	}

	/**
	 * Executes query. It tries to map rows to map. You have to cast the result. It
	 * throws unchecked exceptions.
	 * 
	 */
	public ArrayList<Map<String, Object>> executeQueryToList(String query, List<Object> params) throws SQLException {
		var l = new ArrayList<Map<String, Object>>();
		executeQuery(query, params, rset -> l.add(map(rset)));
		return l;
	}

	/**
	 * Executes query. It tries to map rows to the given class. You have to cast the
	 * result. It throws unchecked exceptions.
	 * 
	 */
	public <T> T executeQueryToList(String query, Class<?> clazz) throws SQLException {
		return executeQueryToList(query, EMPTY_LIST, clazz);
	}
	
	/**
	 * Executes query with the provided connection. It tries to map rows to the given class. You have to cast the
	 * result. It throws unchecked exceptions.
	 * 
	 */
	public <T> T executeQueryToList(Connection conn, String query, Class<?> clazz) throws SQLException {
		return executeQueryToList(conn, query, EMPTY_LIST, clazz);
	}



	/**
	 * Executes query. It tries to map rows to the given class. You have to cast the
	 * result. It throws unchecked exceptions.
	 * 
	 * @param <T>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T executeQueryToList(String query, List<Object> params, Class<?> clazz) throws SQLException {
		var l = new ArrayList<>();
		executeQuery(query, params, rset -> l.add(map(rset, clazz)));
		return (T) l;
	}

	/**
	 * Executes query with provided connection. It tries to map rows to the given class. You have to cast the
	 * result. It throws unchecked exceptions.
	 * 
	 * @param <T>
	 * 
	 */
	@SuppressWarnings("unchecked")
	public <T> T executeQueryToList(Connection conn, String query, List<Object> params, Class<?> clazz) throws SQLException {
		var l = new ArrayList<>();
		executeQuery(conn, query, params, rset -> l.add(map(rset, clazz)));
		return (T) l;
	}
	
	/**
	 * Executes query. It tries to map first row of result set to a map. Run this if
	 * you expect your query to have a single row result set. You have to cast the
	 * result. It throws unchecked exceptions.
	 * 
	 */
	public Map<String, Object> executeQueryToMap(String query) throws SQLException {
		return executeQueryToMap(query, EMPTY_LIST);
	}

	/**
	 * Executes query. It tries to map first row of result set to a map. Run this if
	 * you expect your query to have a single row result set. You have to cast the
	 * result. It throws unchecked exceptions.
	 * 
	 */
	public Map<String, Object> executeQueryToMap(String query, List<Object> params) throws SQLException {
		try (var conn = dataSource.getConnection();
				var statement = prepareStatementWithParams(conn, query, params);
				var rset = statement.executeQuery();) {
			while (rset.next()) {
				return map(rset);
			}
		}
		return null;
	}

	/**
	 * Executes query. It tries to map first row of result set to the given class.
	 * Run this if you expect your query to have a single row result set. You have
	 * to cast the result. It throws unchecked exceptions.
	 * 
	 */
	public <T> T executeQueryToObject(String query, Class<?> clazz) throws SQLException {
		return executeQueryToObject(query, EMPTY_LIST, clazz);
	}

	/**
	 * Executes query with the provided connection. It tries to map first row of result set to the given class.
	 * Run this if you expect your query to have a single row result set. You have
	 * to cast the result. It throws unchecked exceptions.
	 * 
	 */
	public <T> T executeQueryToObject(Connection conn, String query, Class<?> clazz) throws SQLException {
		return executeQueryToObject(conn, query, EMPTY_LIST, clazz);
	}
	
	/**
	 * Executes query. It tries to map first row of result set to the given class.
	 * Run this if you expect your query to have a single row result set. You have
	 * to cast the result. It throws unchecked exceptions.
	 * 
	 */
	public <T> T executeQueryToObject(String query, List<Object> params, Class<?> clazz) throws SQLException {
		try (var conn = dataSource.getConnection()) {
			return executeQueryToObject(conn, query, params, clazz);
		}
	}

	/**
	 * Executes query with the provided connection. It tries to map first row of result set to the given class.
	 * Run this if you expect your query to have a single row result set. You have
	 * to cast the result. It throws unchecked exceptions.
	 * 
	 */
	public <T> T executeQueryToObject(Connection conn, String query, List<Object> params, Class<?> clazz) throws SQLException {
		try (var statement = prepareStatementWithParams(conn, query, params);
				var rset = statement.executeQuery();) {
			while (rset.next()) {
				return map(rset, clazz);
			}
		}
		return null;
	}
	
	/**
	 * Executes update query.
	 * 
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(String query) throws SQLException {
		return executeUpdate(query, EMPTY_LIST);
	}

	/**
	 * Executes update query with the provided connection.
	 * 
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(Connection conn, String query) throws SQLException {
		return executeUpdate(conn, query, EMPTY_LIST);
	}
	
	/**
	 * Executes update query.
	 * 
	 * @param query
	 * @param params
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(String query, List<Object> params) throws SQLException {
		try (var conn = dataSource.getConnection();
				var statement = prepareStatementWithParams(conn, query, params);) {
			return statement.executeUpdate();
		}
	}

	/**
	 * Executes update query with the provided connection.
	 * 
	 * @param query
	 * @return
	 * @throws SQLException
	 */
	public int executeUpdate(Connection conn, String query, List<Object> params) throws SQLException {
		try (var statement = prepareStatementWithParams(conn, query, params);) {
			return statement.executeUpdate();
		}
	}

	/**
	 * Executes procedure by name.
	 * 
	 * @param procedureName
	 * @throws SQLException
	 */
	public void executreProcedure(String procedureName) throws SQLException {
		this.executeProcedure(procedureName, EMPTY_LIST);
	}
	
	/**
	 * Executes procedure by name with the provided connection.
	 * 
	 * @param procedureName
	 * @throws SQLException
	 */
	public void executreProcedure(Connection conn, String procedureName) throws SQLException {
		this.executeProcedure(conn, procedureName, EMPTY_LIST);
	}

	/**
	 * Executes procedure by name with arguments.
	 * 
	 * @param procedureName
	 * @param params
	 * @throws SQLException
	 */
	public void executeProcedure(String procedureName, List<Object> params) throws SQLException {
		try(var conn = getConnection()) {
			executeProcedure(conn, procedureName, params);
		}
	}
	
	/**
	 * Executes procedure with the provided connection, by name with arguments.
	 * 
	 * @param procedureName
	 * @param params
	 * @throws SQLException
	 */
	public void executeProcedure(Connection conn, String procedureName, List<Object> params) throws SQLException {
		var callQuery = "call " + procedureName + "(";
		for (var i = 0; i < params.size(); i++) {
			callQuery += "?,";
		}
		callQuery = callQuery.substring(0, callQuery.length() - 1) + ")";

		this.executeUpdate(conn, callQuery, params);
	}

	public void rollbackQuitely(Connection conn) {
		try {
			conn.rollback();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public void transaction(SqlTransaction transaction) throws SQLException {
		try (var conn = dataSource.getConnection()) {
			conn.setAutoCommit(false);
			transaction.run(conn);
			conn.commit();
			conn.setAutoCommit(true);
		}
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}

	public String getDriver() {
		return driver;
	}

}
