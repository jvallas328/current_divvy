/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.example.jordan.divvyupv12;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author briantoone
 */
public class DbTest {

    public static void main(String[] args) throws SQLException {
        Connection connection = DatabaseConnection.getConnection();
        Statement stmt = connection.createStatement();
        ResultSet rs;
        rs = stmt.executeQuery("SELECT * FROM users;");
        while (rs.next()) {
            String username = rs.getString("username");
            System.out.println(username);
        }
        connection.close();

    }

}
