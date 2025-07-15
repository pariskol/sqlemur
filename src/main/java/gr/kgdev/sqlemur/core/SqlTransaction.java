package gr.kgdev.sqlemur.core;

import java.sql.Connection;
import java.sql.SQLException;

@FunctionalInterface
public interface SqlTransaction {
	public void run(Connection conn) throws SQLException;
}
