package com.example.library;

import javax.swing.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Scanner;

public class DBHelper {

    private static Connection conn = null;
    private static PreparedStatement statement = null;
    private static ResultSet resultSet = null;
    private static MyModel model = null;


    public static Connection getConnection(){

        try {
            File file = new File("D:\\Programming Projects\\Java\\Library_v1\\Resources\\config.txt");
            Scanner scanner = new Scanner(file);
            String connectionString = "";
            String username = "";
            String password ="";
            while (scanner.hasNextLine()){
                connectionString = scanner.nextLine().trim();
                username = scanner.nextLine().trim();
                password = scanner.nextLine().trim();
            }
            Class.forName("org.h2.Driver");
            conn = DriverManager.getConnection(connectionString, username, password);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return conn;
    }//end method getConnection

    public static MyModel getAllBookData(){
        conn = getConnection();
        try {
            statement = conn.prepareStatement("Select bookId, bookTitle, bookAuthor, bookYear, bookPages from books");
            resultSet = statement.executeQuery();
            model = new MyModel(resultSet);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }//end getAllBookData

    public static MyModel getAllClientData(){
        conn = getConnection();
        try {
            statement = conn.prepareStatement("Select clientId, fname, lname, age, address from clients");
            resultSet = statement.executeQuery();
            model = new MyModel(resultSet);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }//end getAllClientData

    public static MyModel getAllRentedBooksData(){
        conn = getConnection();
        try {
            statement = conn.prepareStatement("Select rentedbookID, fname, lname, bookTitle, rentedDate, returnDate, comment from clients c join rented_books rb on c.clientId = rb.clientId join books b on rb.bookId = b.bookId");
            resultSet = statement.executeQuery();
            model = new MyModel(resultSet);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return model;
    }//end getAllRentedBooksData

    public static void fillBookComboBox(JComboBox<String> comboBox){
        conn = getConnection();
        String sql = "select bookTitle from books";
        try {
            statement = conn.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while(resultSet.next()){
                String item = resultSet.getObject(1).toString();
                comboBox.addItem(item);
            }//end while
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }//end fillBookComboBox

    public static void fillClientComboBox(JComboBox<String> comboBox){
        conn = getConnection();
        String sql = "select fname, lname from clients";
        try {
            statement = conn.prepareStatement(sql);
            resultSet = statement.executeQuery();
            while(resultSet.next()){
                String item = resultSet.getObject(1).toString() +" "+ resultSet.getObject(2).toString();
                comboBox.addItem(item);
            }//end while
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }//end fillClientComboBox

}//end class
