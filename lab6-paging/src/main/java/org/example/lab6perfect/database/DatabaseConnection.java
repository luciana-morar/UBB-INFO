package org.example.lab6perfect.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConnection{
    private static Connection connection=null;
    public static Properties properties;

    public static void setProperties(Properties props){
        properties=props;
    }
    public static Connection getConnection() throws SQLException {
        if(connection==null || connection.isClosed()){
            String url=properties.getProperty("db.url");
            String username=properties.getProperty("db.username");
            String password=properties.getProperty("db.password");

            connection = DriverManager.getConnection(url,username,password);
        }
        return connection;
    }


}
