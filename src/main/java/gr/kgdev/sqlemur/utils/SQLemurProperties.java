package gr.kgdev.sqlemur.utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Properties;

public class SQLemurProperties {

    private static HashMap<String, Properties> propertiesMap = new HashMap<>();

    static {
        loadProperties("./sqlemur.properties");
    }

    public static void loadProperties(String filePath) {
        try (var inputStream = new FileInputStream(filePath)) {
            Properties props = new Properties();
            props.load(inputStream);
            propertiesMap.put(Paths.get(filePath).getFileName().toString(), props);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Returns the first matching key from all loaded properties
     *
     * @param key
     * @param clazz
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> T getProperty(String key, Class<?> clazz) {
        try {
            Object value = null;
            for (Properties props : propertiesMap.values()) {
                value = props.get(key);
                if (value != null) {
                    break;
                }
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
    public static <T> T getProperty(String key, Class<?> clazz, Object defaultValue) {
        var value = getProperty(key, clazz);
        return value != null ? (T) value : (T) defaultValue;
    }

}
