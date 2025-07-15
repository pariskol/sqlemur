package gr.kgdev.sqlemur.core;

import java.sql.ResultSet;
import java.sql.SQLException;

@FunctionalInterface
public interface ResultSetAction {

	public void onResultSet(ResultSet rset) throws SQLException;
}
