package gr.kgdev.sqlemur.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

public class SQLemurProperties {

	private static HashMap<String, Properties> propertiesMap = new HashMap<>();
	
	static {
		loadProperties("./sqlemur.properties");
	}
	
	public static void loadProperties(String rootPath) {
		try {
			Files.walk(Paths.get(rootPath))
	        .filter(Files::isRegularFile)
	        .filter(path -> path.toString().endsWith(".properties"))
	        .forEach(path -> {
	        	try (InputStream inputStream = new FileInputStream(path.toFile())) {
					Properties props = new Properties();
					props.load(inputStream);
					propertiesMap.put(path.getFileName().toString(), props);
		        } catch (IOException e) {
	        		e.printStackTrace();
				}
	        });
		} catch (IOException e) {
    		e.printStackTrace();
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T>T getProperty(String fileKey, String key, Class<?> clazz) {
		var value = propertiesMap.get(fileKey).get(key);
		try {
			var cons = clazz.getConstructor(String.class);
			var returnedValue = cons.newInstance(value.toString());
			return (T) returnedValue;
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	public static <T>T getProperty(String fileKey, String key, Class<?> clazz, Object defaultValue) {
		var value = getProperty(fileKey, key, clazz);
		return value != null ? (T) value : (T) defaultValue;
	}
	
	/**
	 * Returns the first matching key from all loaded properties
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T>T getProperty(String key, Class<?> clazz) {
		try {
			Object value = null;
			for (Properties props : propertiesMap.values()) {
				value = props.get(key);
				if (value != null)
					break;
			}
			var cons = clazz.getConstructor(String.class);
			var returnedValue = cons.newInstance(value.toString());
			return (T) returnedValue;
		} catch (Exception e) {
    		e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Returns the first matching key from all loaded properties
	 * 
	 * @param key
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public static <T>T getProperty(String key, Class<?> clazz, Object defaultValue) {
		var value = getProperty(key, clazz);
		return value != null ? (T) value : (T) defaultValue;
	}
	
	public static Properties getPropertiesFromFile(String fileNamePart) {
		for (var key : propertiesMap.keySet()) {
			if (key.contains(fileNamePart))
				return propertiesMap.get(key);
		}
		return null;
	}
	
	
}
