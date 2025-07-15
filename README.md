
# <img src="https://www.svgrepo.com/show/252871/lemur.svg" alt="SQLemur Logo" width="50"/> SQLemur  

**SQLemur** is a lightweight JDBC wrapper for Java that simplifies executing SQL queries, updates, and stored procedures while providing convenient mapping utilities for converting `ResultSet` rows to `Map` or custom objects.  

---

## Features

- Simple API for executing queries and updates
- Automatic parameter binding for prepared statements
- Result mapping to `Map` or POJOs
- Optional camel-case conversion for column names
- Easy transaction support
- Stored procedure execution helper

---

## Installation

Add SQLemur to your project as a module or include it in your build system (e.g., Maven or Gradle).  
Make sure to include a compatible JDBC driver and Apache DBCP2 (for the datasource).

---

## Quick Start

```java
import gr.kgdev.sqlemur.core.SQLemur;
import java.util.Map;
import java.util.Arrays;

public class Example {
    public static void main(String[] args) throws Exception {
        var sqlemur = new SQLemur(
            "org.postgresql.Driver",
            "jdbc:postgresql://localhost:5432/mydb",
            "myuser",
            "mypassword"
        );

        sqlemur.checkConnection(); // Test connection

        // Execute a simple query
        var rows = sqlEmsqlemurur.executeQueryToList(
            "SELECT * FROM users WHERE age > ?",
            Arrays.asList(18)
        );

        for (var row : rows) {
            System.out.println(row);
        }

        // Execute an update
        var rowsAffected = sqlemur.executeUpdate(
            "UPDATE users SET active = ? WHERE last_login < ?",
            Arrays.asList(false, "2023-01-01")
        );

        // Execute a transaction
        sqlemur.transaction(conn -> {
            sqlemur.executeUpdate(conn, "DELETE FROM sessions WHERE expired = ?", Arrays.asList(true));
        });
    }
}
