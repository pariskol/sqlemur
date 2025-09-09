package gr.kgdev.sqlemur.orm;

import java.sql.ResultSet;
import java.util.LinkedHashMap;



public class ORMUtils {

	/**
	 * Maps a row of result set into a HashMap. 
	 * For mapping columns aliases are used.
	 * 
	 * @param rset
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	public static LinkedHashMap<String, Object> map(ResultSet rset) throws Exception {

		var dto = new LinkedHashMap<String, Object> ();
		var rsmd = rset.getMetaData();
		
		for (var i=1;i<= rsmd.getColumnCount();i++) {
			var value = dto.get(rsmd.getColumnLabel(i));
			if (value != null)
				dto.put(rsmd.getTableName(i) + "." + rsmd.getColumnLabel(i), rset.getObject(i));
			else dto.put(rsmd.getColumnLabel(i), rset.getObject(i));
		}
		
		return dto;
	}


	/**
	 * Maps a row of result set into the given class (which has @DTO annotation and its fields have @Column annotation).
	 * Database's column types and class fields must be type compatible.
	 * 
	 * @param rset
	 * @param clazz
	 * @return
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static <T>T map(ResultSet rset, Class<?> clazz) throws Exception {
	
		if (clazz.getAnnotation(Table.class) == null)
			throw new IllegalAccessException(
					"Class " + clazz.getSimpleName() + " has no annotation " + Table.class.getName());
	
		var dto = clazz.getDeclaredConstructor().newInstance();
	
		for (var field : clazz.getDeclaredFields()) {
			field.setAccessible(true);
			var annotation = field.getAnnotation(Column.class);
			if (annotation != null) {
				// get object from result set and cast it to field's class
				Object value = null;
				try {
					value = rset.getObject(((Column) annotation).value(), field.getType());
				} catch(Exception e) {
					// getObject(name, class) may be unsupported in some jdbc drivers (ex sqlite)
					value = rset.getObject(((Column) annotation).value());
				}
				field.set(dto, value);
			}
		}
	
		return (T) dto;
	}


	/**
	 * Maps a row of result set into a HashMap. 
	 * For mapping columns real names are used.
	 * 
	 * @param rset
	 * @return LinkedHashMap
	 * @throws Exception
	 */
	public static LinkedHashMap<String, Object> mapR(ResultSet rset) throws Exception {

		var dto = new LinkedHashMap<String, Object> ();
		var rsmd = rset.getMetaData();
		
		for (var i=1;i<= rsmd.getColumnCount();i++) {
			var value = dto.get(rsmd.getColumnName(i));
			if (value != null)
				dto.put(rsmd.getTableName(i) + "." + rsmd.getColumnName(i), rset.getObject(i));
			else dto.put(rsmd.getColumnName(i), rset.getObject(i));
		}
		
		return dto;
	}
	
	public static LinkedHashMap<String, Object> mapCC(ResultSet rset) throws Exception {

		var dto = new LinkedHashMap<String, Object> ();
		var rsmd = rset.getMetaData();
		
		for (int i=1;i<= rsmd.getColumnCount();i++) {
			var key = toCamelCase(rsmd.getColumnLabel(i));
			var value = dto.get(key);
			if (value != null)
				dto.put(key + " (" + rsmd.getTableName(i) + ")", rset.getObject(i));
			else
				dto.put(key, rset.getObject(i));
		}
		
		return dto;
	}
	
	/**
	 * Maps a row of result set into a LinkedHashMap.
	 * Throws unchecked exception.
	 * Use this if a more generic exception handling system exists.
	 * 
	 * @param rset
	 * @return HashMap
	 * @throws RuntimeException
	 */
	public static LinkedHashMap<String, Object> mapUnsafe(ResultSet rset) throws RuntimeException {
		try {
			return map(rset);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static LinkedHashMap<String, Object> mapUnsafeCC(ResultSet rset) throws RuntimeException {

		try {
			return mapCC(rset);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	/**
	 * Maps a row of result set into the given class (which has @DTO annotation and its fields have @Column annotation).
	 * Database's column types and class fields must be type compatible.
	 * Throws unchecked exception.
	 * Use this if a more generic exception handling system exists.
	 * 
	 * @param rset
	 * @param clazz
	 * @return
	 * @throws RuntimeException 
	 */
	public static <T>T mapUnsafe(ResultSet rset, Class<?> clazz) throws RuntimeException {
		try {
			return map(rset, clazz);
		} catch (Throwable e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}
	
	public static String toCamelCase(String str) {
		var parts = str.toLowerCase().split("_");
		var camelCaseString = "";
		for (var part : parts) {
			camelCaseString += 
					part.substring(0, 1).toUpperCase() +
					part.substring(1, part.length());
		}
		if (!camelCaseString.isEmpty())
			camelCaseString = camelCaseString.substring(0, 1).toLowerCase() + camelCaseString.substring(1, camelCaseString.length());
		else
			camelCaseString = str;
		
		return camelCaseString;
	}
	
	public static String toSnakeCase(String str) {
		String regex = "([a-z])([A-Z]+)";
		String replacement = "$1_$2";
		str = str.replaceAll(regex, replacement).toLowerCase();
		return str;
	}
	
}