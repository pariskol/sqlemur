package gr.kgdev.sqlemur.core;

import java.time.Duration;

import javax.sql.DataSource;

import org.apache.commons.dbcp2.BasicDataSource;

import gr.kgdev.sqlemur.utils.PropertiesLoader;

public class DatasourceFactory {

	public static DataSource createDbcp2Datasource(String driver, String url, String user, String password) {
		var dbcp2DataSource = new BasicDataSource();
		dbcp2DataSource.setDriverClassName(driver);
		dbcp2DataSource.setUrl(url);
		dbcp2DataSource.setUsername(user);
		dbcp2DataSource.setPassword(password);
		dbcp2DataSource.setInitialSize((Integer) PropertiesLoader.getProperty("sqlemur.pool.initialsize", Integer.class, 4));
		dbcp2DataSource.setMaxIdle((Integer) PropertiesLoader.getProperty("sqlemur.pool.idlesize", Integer.class, 16));
		dbcp2DataSource.setMaxTotal((Integer) PropertiesLoader.getProperty("sqlemur.pool.maxsize", Integer.class, 32));
		var maxWaitMillis = (Integer) PropertiesLoader.getProperty("sqlemur.pool.maxwaitmillis", Integer.class, 10000);
		dbcp2DataSource.setMaxWait(Duration.ofMillis(maxWaitMillis));
		return dbcp2DataSource;
	}
	
}
