
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
```

Result set can also be mapped to class by using @Table, @Column annotations

Assuming we have a pojo like this:

``` java 
@Table
public class User {
    @Column("id")
    private int id;

    @Column("first_name")
    private String firstName;

    @Column("last_name")
    private String lastName;

    @Column("email")
    private String email;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
```

queries ca be mapped to class using:

``` java 
	// Query all users and map results to User objects
	List<User> users = sqlemur.executeQueryToList(
		"SELECT id, first_name, last_name, email FROM users",
		User.class
	);

	// Print retrieved users
	for (var user : users) {
		System.out.printf("User[id=%d, name=%s %s, email=%s]%n",
			user.getId(), 
			user.getFirstName(), 
			user.getLastName(), 
			user.getEmail());
	}
```