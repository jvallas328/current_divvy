package com.example.jordan.divvyupv12;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    public static Connection getConnection() {

        Connection connection = null;

        try {
            String database_URl = 
                    "jdbc:mysql://cslinux.samford.edu:3306/codedb";
//                    "jdbc:mysql://192.168.1.3:3306/codedb";

            connection = DriverManager.getConnection(database_URl,Input_Data.USERNAME,Input_Data.PW);
            System.out.println(database_URl);
            System.out.println(Input_Data.USERNAME);
            System.out.println(Input_Data.PW);
            System.out.println(connection);
            return connection;

        } catch (SQLException e) {
            System.out.println(e);
            return null;
        } catch (Exception e) {
            System.out.println(e);
            return null;

        }

    }//end of getConnection method

    private boolean usernameExists(String username) {
        String sql = "select * from users where username = ?  ";
        System.out.println(username);
        try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean accountExists(String username, String password) {
        String sql = "select username, password from users where username = ? and password = ?";
    try (Connection connection = getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);) {
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                return false;
            } else {
                return true;
            }
        } catch (SQLException e) {
            System.out.println(e);
            return false;
        }
    }

    public boolean addUser(String username, String password) {
        if (!usernameExists(username)) {
            String sql
                    = "INSERT INTO users (username, password) VALUES(?,?);";

            try (Connection con = getConnection();
                 PreparedStatement ps = con.prepareStatement(sql)) {

                ps.setString(1, username);
                ps.setString(2, password);

                ps.executeUpdate();

                return true;

            } catch (SQLException e) {
                System.out.println(e);
                return false;
            }
        } else {
            System.out.println("Username already taken");
            return false;
        }
    }

}//end of class file
