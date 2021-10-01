package com.example.library;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MainFrame extends JFrame {

    static Connection conn = null;
    static PreparedStatement statement = null;
    static ResultSet resultSet = null;
    static int id = -1;

    JTabbedPane tabbedPane = new JTabbedPane();

    BooksTab booksTab= new BooksTab();
    ClientsTab clientsTab = new ClientsTab();
    RentedTab rentedBooksTab = new RentedTab();
    RentedBooksUpdateTab rentedBooksUpdateTab = new RentedBooksUpdateTab();

    MainFrame(){
        this.setSize(500,600);
        this.setResizable(false);
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.add(tabbedPane);
        this.setVisible(true);

        //books tab
        tabbedPane.add(booksTab, "Books");

        //clients tab
        tabbedPane.add(clientsTab, "Clients");

        //rented books tab
        tabbedPane.add(rentedBooksTab, "Rented Books");

        //rented update books tab
        tabbedPane.add(rentedBooksUpdateTab, "Update rented books");
    }
}

class BooksTab extends JPanel{

    JTable booksTable = new JTable();
    JScrollPane bookTableScroller = new JScrollPane(booksTable);

    JPanel upperBookPanel = new JPanel();
    JPanel middleBookPanel = new JPanel();
    JPanel lowerBookPanel = new JPanel();

    JLabel bookTitleLabel = new JLabel("Book Title");
    JLabel bookAuthorLabel = new JLabel("Book Author");
    JLabel bookYearLabel = new JLabel("Year of publishing");
    JLabel bookPagesLabel = new JLabel("Number of pages");

    JTextField bookTitleTextField = new JTextField();
    JTextField bookAuthorTextField = new JTextField();
    JTextField bookYearTextField = new JTextField();
    JTextField bookPagesTextField = new JTextField();

    JButton saveBookButton = new JButton("Save book");
    JButton updateBookButton = new JButton("Update book");
    JButton deleteBookButton = new JButton("Delete book");

    JButton searchBookButton = new JButton("Search");
    JTextField searchBookTextField = new JTextField("Enter a book name", 20);

    JButton refreshBooksButton = new JButton("Refresh");

    BooksTab(){
        this.add(upperBookPanel);
        this.add(middleBookPanel);
        this.add(lowerBookPanel);
        this.setLayout(new GridLayout(3,1));

        //upper book panel
        upperBookPanel.setLayout(new GridLayout(4,2));
        upperBookPanel.add(bookTitleLabel);
        upperBookPanel.add(bookTitleTextField);
        upperBookPanel.add(bookAuthorLabel);
        upperBookPanel.add(bookAuthorTextField);
        upperBookPanel.add(bookYearLabel);
        upperBookPanel.add(bookYearTextField);
        upperBookPanel.add(bookPagesLabel);
        upperBookPanel.add(bookPagesTextField);

        //middle book panel
        middleBookPanel.add(saveBookButton);
        saveBookButton.addActionListener(new SaveAction());

        middleBookPanel.add(updateBookButton);
        updateBookButton.addActionListener(new UpdateAction());

        middleBookPanel.add(deleteBookButton);
        deleteBookButton.addActionListener(new DeleteAction());

        middleBookPanel.add(searchBookTextField);
        middleBookPanel.add(searchBookButton);
        searchBookButton.addActionListener(new SearchAction());

        middleBookPanel.add(refreshBooksButton);
        refreshBooksButton.addActionListener(new RefreshAction());

        //lower book panel
        lowerBookPanel.add(bookTableScroller);
        bookTableScroller.setPreferredSize(new Dimension(450,160));
        booksTable.setModel(DBHelper.getAllBookData());
        booksTable.addMouseListener(new BooksTableListener());
    }

    public void clearBookForm(){
        bookTitleTextField.setText("");
        bookAuthorTextField.setText("");
        bookPagesTextField.setText("");
        bookYearTextField.setText("");
    }

    class SaveAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String bookTitle = bookTitleTextField.getText();
            String bookAuthor = bookAuthorTextField.getText();
            short bookYear;
            try{
                bookYear = Short.parseShort(bookYearTextField.getText());
            }catch (Exception ex){
                bookYearTextField.setText("Invalid year");
                return;
            }
            short bookPages;
            try{
                bookPages = Short.parseShort(bookPagesTextField.getText());
            }catch (Exception ex){
                bookPagesTextField.setText("Invalid pages");
                return;
            }
            MainFrame.conn = DBHelper.getConnection();
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement("insert into books(bookId, bookTitle, bookAuthor, bookYear, bookPages) values(null,?,?,?,?)");
                MainFrame.statement.setString(1, bookTitle);
                MainFrame.statement.setString(2, bookAuthor);
                MainFrame.statement.setShort(3,bookYear);
                MainFrame.statement.setShort(4,bookPages);

                MainFrame.statement.execute();
                booksTable.setModel(DBHelper.getAllBookData());

                RentedTab.bookComboBox.removeAllItems();
                RentedTab.clientComboBox.removeAllItems();
                DBHelper.fillBookComboBox(RentedTab.bookComboBox);
                DBHelper.fillClientComboBox(RentedTab.clientComboBox);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }finally {
                try {
                    MainFrame.statement.close();
                    MainFrame.conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            clearBookForm();
        }
    }//end save action

    class BooksTableListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 1) {
                int row = booksTable.getSelectedRow();
                MainFrame.id = Integer.parseInt(booksTable.getValueAt(row, 0).toString());
            }else if (e.getClickCount() == 2){
                int row = booksTable.getSelectedRow();
                MainFrame.id = Integer.parseInt(booksTable.getValueAt(row,0).toString());
                bookTitleTextField.setText(booksTable.getValueAt(row, 1).toString());
                bookAuthorTextField.setText(booksTable.getValueAt(row, 2).toString());
                bookYearTextField.setText(booksTable.getValueAt(row,3).toString());
                bookPagesTextField.setText(booksTable.getValueAt(row, 4).toString());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }//end books table listener

    class DeleteAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            MainFrame.conn = DBHelper.getConnection();
            String sql = "delete from books where bookId = ?";
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement(sql);
                MainFrame.statement.setInt(1, MainFrame.id);

                MainFrame.statement.execute();
                booksTable.setModel(DBHelper.getAllBookData());
                MainFrame.id = -1;

                RentedTab.bookComboBox.removeAllItems();
                RentedTab.clientComboBox.removeAllItems();
                DBHelper.fillBookComboBox(RentedTab.bookComboBox);
                DBHelper.fillClientComboBox(RentedTab.clientComboBox);
                RentedTab.rentedBooksTable.setModel(DBHelper.getAllRentedBooksData());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }//end delete action

    class UpdateAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

            String bookTitle = bookTitleTextField.getText();
            String bookAuthor = bookAuthorTextField.getText();
            short bookYear;
            try{
                bookYear = Short.parseShort(bookYearTextField.getText());
            }catch (Exception ex){
                bookYearTextField.setText("Invalid year");
                return;
            }
            short bookPages;
            try{
                bookPages = Short.parseShort(bookPagesTextField.getText());
            }catch (Exception ex){
                bookPagesTextField.setText("Invalid pages");
                return;
            }
            MainFrame.conn = DBHelper.getConnection();
            String sql = "update books set bookTitle = ?, bookAuthor = ?, bookYear = ?, bookPages = ? where bookId = ?";
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement(sql);
                MainFrame.statement.setString(1,bookTitle);
                MainFrame.statement.setString(2,bookAuthor);
                MainFrame.statement.setShort(3,bookYear);
                MainFrame.statement.setShort(4,bookPages);
                MainFrame.statement.setInt(5,MainFrame.id);

                MainFrame.statement.execute();
                booksTable.setModel(DBHelper.getAllBookData());
                MainFrame.id = -1;
                clearBookForm();

                RentedTab.bookComboBox.removeAllItems();
                RentedTab.clientComboBox.removeAllItems();
                DBHelper.fillBookComboBox(RentedTab.bookComboBox);
                DBHelper.fillClientComboBox(RentedTab.clientComboBox);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }//end update action

    class SearchAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String searchBookTitle = searchBookTextField.getText();
            MainFrame.conn = DBHelper.getConnection();
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement("select bookTitle, bookAuthor from books where bookTitle ilike \'"+searchBookTitle+"%\'");
                //MainFrame.statement.setString(1,searchBookTitle);

                MainFrame.resultSet = MainFrame.statement.executeQuery();
                booksTable.setModel(new MyModel(MainFrame.resultSet));
                searchBookTextField.setText("");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }//end search action

    class RefreshAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            booksTable.setModel(DBHelper.getAllBookData());
        }
    }//end refresh action
}

class ClientsTab extends JPanel{

    JTable clientsTable = new JTable();
    JScrollPane clientTableScroller = new JScrollPane(clientsTable);

    JPanel upperClientsPanel = new JPanel();
    JPanel middleClientsPanel = new JPanel();
    JPanel lowerClientsPanel = new JPanel();

    JLabel firstNameLabel = new JLabel("First Name");
    JLabel lastNameLabel = new JLabel("Last Name");
    JLabel ageLabel = new JLabel("Age");
    JLabel addressLabel = new JLabel("Address");

    JTextField firstNameTextField = new JTextField();
    JTextField lastNameTextField = new JTextField();
    JTextField ageTextField = new JTextField();
    JTextField addressTextField = new JTextField();

    JButton saveClientButton = new JButton("Save client");
    JButton updateClientButton = new JButton("Update client");
    JButton deleteClientButton = new JButton("Delete client");

    JButton searchClientButton = new JButton("Search");
    JTextField searchClientTextField = new JTextField("Enter client name", 20);

    JButton refreshClientButton = new JButton("Refresh");

    ClientsTab(){
        this.add(upperClientsPanel);
        this.add(middleClientsPanel);
        this.add(lowerClientsPanel);
        this.setLayout(new GridLayout(3,1));

        //upper client panel
        upperClientsPanel.setLayout(new GridLayout(4,2));
        upperClientsPanel.add(firstNameLabel);
        upperClientsPanel.add(firstNameTextField);
        upperClientsPanel.add(lastNameLabel);
        upperClientsPanel.add(lastNameTextField);
        upperClientsPanel.add(ageLabel);
        upperClientsPanel.add(ageTextField);
        upperClientsPanel.add(addressLabel);
        upperClientsPanel.add(addressTextField);

        //middle client panel
        middleClientsPanel.add(saveClientButton);
        saveClientButton.addActionListener(new SaveAction());

        middleClientsPanel.add(updateClientButton);
        updateClientButton.addActionListener(new UpdateAction());

        middleClientsPanel.add(deleteClientButton);
        deleteClientButton.addActionListener(new DeleteAction());

        middleClientsPanel.add(searchClientTextField);
        middleClientsPanel.add(searchClientButton);
        searchClientButton.addActionListener(new SearchAction());

        middleClientsPanel.add(refreshClientButton);
        refreshClientButton.addActionListener(new RefreshAction());

        //lower client panel
        lowerClientsPanel.add(clientTableScroller);
        clientTableScroller.setPreferredSize(new Dimension(450,160));
        clientsTable.setModel(DBHelper.getAllClientData());
        clientsTable.addMouseListener(new ClientsTab.ClientsTableListener());

    }

    public void clearClientForm(){
        firstNameTextField.setText("");
        lastNameTextField.setText("");
        ageTextField.setText("");
        addressTextField.setText("");
    }

    class SaveAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String firstName = firstNameTextField.getText();
            String lastName = lastNameTextField.getText();
            String address = addressTextField.getText();
            byte age;
            try{
                age = Byte.parseByte(ageTextField.getText());
            }catch (Exception ex){
                ageTextField.setText("Invalid age: 1 - 127");
                return;
            }

            MainFrame.conn = DBHelper.getConnection();
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement("insert into clients(clientId, fname, lname, age, address) values(null,?,?,?,?)");
                MainFrame.statement.setString(1, firstName);
                MainFrame.statement.setString(2, lastName);
                MainFrame.statement.setByte(3,age);
                MainFrame.statement.setString(4,address);

                MainFrame.statement.execute();
                clientsTable.setModel(DBHelper.getAllClientData());

                RentedTab.bookComboBox.removeAllItems();
                RentedTab.clientComboBox.removeAllItems();
                DBHelper.fillBookComboBox(RentedTab.bookComboBox);
                DBHelper.fillClientComboBox(RentedTab.clientComboBox);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }finally {
                try {
                    MainFrame.statement.close();
                    MainFrame.conn.close();
                } catch (SQLException throwables) {
                    throwables.printStackTrace();
                }
            }
            clearClientForm();
        }
    }//end save action

    class ClientsTableListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 1) {
                int row = clientsTable.getSelectedRow();
                MainFrame.id = Integer.parseInt(clientsTable.getValueAt(row, 0).toString());
            }else if (e.getClickCount() == 2){
                int row = clientsTable.getSelectedRow();
                MainFrame.id = Integer.parseInt(clientsTable.getValueAt(row,0).toString());
                firstNameTextField.setText(clientsTable.getValueAt(row, 1).toString());
                lastNameTextField.setText(clientsTable.getValueAt(row, 2).toString());
                ageTextField.setText(clientsTable.getValueAt(row,3).toString());
                addressTextField.setText(clientsTable.getValueAt(row, 4).toString());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }//end clients table listener

    class DeleteAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            MainFrame.conn = DBHelper.getConnection();
            String sql = "delete from clients where clientId = ?";
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement(sql);
                MainFrame.statement.setInt(1, MainFrame.id);

                MainFrame.statement.execute();
                clientsTable.setModel(DBHelper.getAllClientData());
                MainFrame.id = -1;

                RentedTab.bookComboBox.removeAllItems();
                RentedTab.clientComboBox.removeAllItems();
                DBHelper.fillBookComboBox(RentedTab.bookComboBox);
                DBHelper.fillClientComboBox(RentedTab.clientComboBox);
                RentedTab.rentedBooksTable.setModel(DBHelper.getAllRentedBooksData());
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }//end delete action

    class UpdateAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {

            String firstName = firstNameTextField.getText();
            String lastName = lastNameTextField.getText();
            String address = addressTextField.getText();
            byte age;
            try{
                age = Byte.parseByte(ageTextField.getText());
            }catch (Exception ex){
                ageTextField.setText("Invalid age: 1 - 127");
                return;
            }

            MainFrame.conn = DBHelper.getConnection();
            String sql = "update clients set fname = ?, lname = ?, age = ?, address = ? where clientId = ?";
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement(sql);
                MainFrame.statement.setString(1,firstName);
                MainFrame.statement.setString(2,lastName);
                MainFrame.statement.setByte(3,age);
                MainFrame.statement.setString(4,address);
                MainFrame.statement.setInt(5,MainFrame.id);

                MainFrame.statement.execute();
                clientsTable.setModel(DBHelper.getAllClientData());
                MainFrame.id = -1;
                clearClientForm();

                RentedTab.bookComboBox.removeAllItems();
                RentedTab.clientComboBox.removeAllItems();
                DBHelper.fillBookComboBox(RentedTab.bookComboBox);
                DBHelper.fillClientComboBox(RentedTab.clientComboBox);
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }//end update action

    class SearchAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String searchClient = searchClientTextField.getText();
            MainFrame.conn = DBHelper.getConnection();
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement("select fname, lname from clients where fname ilike \'"+searchClient+"%\'");
                //MainFrame.statement.setString(1,searchClient);

                MainFrame.resultSet = MainFrame.statement.executeQuery();
                clientsTable.setModel(new MyModel(MainFrame.resultSet));
                searchClientTextField.setText("");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }//end search action

    class RefreshAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            clientsTable.setModel(DBHelper.getAllClientData());
        }
    }//end refresh action
}

class RentedTab extends JPanel{
    static JTable rentedBooksTable = new JTable();
    JScrollPane rentedBooksTableScroller = new JScrollPane(rentedBooksTable);

    JPanel upperRentedPanel = new JPanel();
    JPanel middleRentedPanel = new JPanel();
    JPanel lowerRentedPanel = new JPanel();

    JLabel clientLabel = new JLabel("Select a client");
    JLabel bookLabel = new JLabel("Select a book");
    JLabel dateLabel = new JLabel("Date");
    JTextField dateTextField = new JTextField("YYYY-MM-DD");

    static JComboBox<String> clientComboBox = new JComboBox<>();
    static JComboBox<String> bookComboBox = new JComboBox<>();

    JButton saveRentedBookButton = new JButton("Save rented book");
    JButton deleteRentedBookButton = new JButton("Delete rented book");

    JButton searchRentedBookButton = new JButton("Search");
    JTextField searchRentedBookTextField = new JTextField("Enter book ");

    JButton refreshButton = new JButton("Refresh");

    RentedTab(){
        this.add(upperRentedPanel);
        this.add(middleRentedPanel);
        this.add(lowerRentedPanel);
        this.setLayout(new GridLayout(3,2));

        //upper rented panel
        upperRentedPanel.setLayout(new FlowLayout());

        upperRentedPanel.add(clientLabel);
        upperRentedPanel.add(clientComboBox);
        DBHelper.fillClientComboBox(clientComboBox);
        clientComboBox.setPreferredSize(new Dimension(450, 30));
        clientComboBox.setMaximumSize(new Dimension(450,30));
        clientComboBox.setMinimumSize(new Dimension(450,30));

        upperRentedPanel.add(bookLabel);
        upperRentedPanel.add(bookComboBox);
        DBHelper.fillBookComboBox(bookComboBox);
        bookComboBox.setPreferredSize(new Dimension(450, 30));
        bookComboBox.setMaximumSize(new Dimension(450,30));
        bookComboBox.setMinimumSize(new Dimension(450,30));

        upperRentedPanel.add(dateLabel);
        upperRentedPanel.add(dateTextField);
        dateTextField.setPreferredSize(new Dimension(450,30));

        //middle rented panel
        middleRentedPanel.add(saveRentedBookButton);
        saveRentedBookButton.addActionListener(new SaveAction());

        middleRentedPanel.add(deleteRentedBookButton);
        deleteRentedBookButton.addActionListener(new DeleteAction());

        middleRentedPanel.add(searchRentedBookTextField);
        searchRentedBookTextField.setPreferredSize(new Dimension(350,30));
        middleRentedPanel.add(searchRentedBookButton);
        searchRentedBookButton.addActionListener(new SearchAction());

        middleRentedPanel.add(refreshButton);
        refreshButton.addActionListener(new RefreshAction());

        //lower rented panel
        lowerRentedPanel.add(rentedBooksTableScroller);
        rentedBooksTableScroller.setPreferredSize(new Dimension(450, 160));
        rentedBooksTable.setModel(DBHelper.getAllRentedBooksData());
        rentedBooksTable.addMouseListener(new RentedBooksTableListener());
    }

    public int getClientId(){
        String item = clientComboBox.getSelectedItem().toString();
        String [] content = item.split(" ");

        String firstName = content[0];
        String lastName = content[1];

        int clientId = -1;

        MainFrame.conn = DBHelper.getConnection();
        try {
            MainFrame.statement = MainFrame.conn.prepareStatement("Select clientId from clients where fname =\'"+firstName+"\' and lname =\'"+lastName+"\'");
            //MainFrame.statement.setString(1,firstName);
            //MainFrame.statement.setString(2,lastName);
            MainFrame.resultSet = MainFrame.statement.executeQuery();
            while(MainFrame.resultSet.next()){
                clientId = MainFrame.resultSet.getInt(1);
            }//end while
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return clientId;
    }//end getClientId

    public int getBookId(){
        String bookTitle = bookComboBox.getSelectedItem().toString();

        int bookId = -1;

        MainFrame.conn = DBHelper.getConnection();
        try {
            MainFrame.statement = MainFrame.conn.prepareStatement("Select bookId from books where bookTitle =?");
            MainFrame.statement.setString(1,bookTitle);
            MainFrame.resultSet = MainFrame.statement.executeQuery();
            while(MainFrame.resultSet.next()){
                bookId = MainFrame.resultSet.getInt(1);
            }//end while
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bookId;
    }//end getBookId

    class SaveAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            int bookId = getBookId();
            int clientId = getClientId();
            String rentedDate = dateTextField.getText();

            MainFrame.conn = DBHelper.getConnection();
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement("Insert into rented_books (clientId, bookId, rentedDate) values(?,?,\'"+rentedDate+"\')");
                MainFrame.statement.setInt(1,clientId);
                MainFrame.statement.setInt(2,bookId);
                //MainFrame.statement.setString(3,rentedDate);

                MainFrame.statement.execute();
                rentedBooksTable.setModel(DBHelper.getAllRentedBooksData());
                clientId = -1;
                bookId = -1;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }//end save action

    class RentedBooksTableListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {
                int row = rentedBooksTable.getSelectedRow();
                MainFrame.id = Integer.parseInt(rentedBooksTable.getValueAt(row, 0).toString());
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }//end rented books table listener

    class DeleteAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            MainFrame.conn = DBHelper.getConnection();
            String sql = "delete from rented_Books where rentedBookId = ?";
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement(sql);
                MainFrame.statement.setInt(1, MainFrame.id);

                MainFrame.statement.execute();
                rentedBooksTable.setModel(DBHelper.getAllRentedBooksData());
                MainFrame.id = -1;
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        }
    }//end delete action

    class SearchAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String searchRentedBook = searchRentedBookTextField.getText();
            MainFrame.conn = DBHelper.getConnection();
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement("Select fname, lname, bookTitle from clients c join rented_books rb on c.clientId = rb.clientId join books b on rb.bookId = b.bookId where bookTitle ilike \'"+searchRentedBook+"%\'");

                MainFrame.resultSet = MainFrame.statement.executeQuery();
                rentedBooksTable.setModel(new MyModel(MainFrame.resultSet));
                searchRentedBookTextField.setText("");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }//end search action

    class RefreshAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            rentedBooksTable.setModel(DBHelper.getAllRentedBooksData());
        }
    }//end refresh action

}

class RentedBooksUpdateTab extends JPanel{
    static JTable rentedBooksUpdateTable = new JTable();
    JScrollPane rentedBooksTableScroller = new JScrollPane(rentedBooksUpdateTable);

    JPanel upperRentedBooksUpdatePanel = new JPanel();
    JPanel middleRentedBooksUpdatePanel = new JPanel();
    JPanel lowerRentedBooksUpdatePanel = new JPanel();

    JLabel clientFirstNameLabel = new JLabel("Client first name:");
    JLabel clientLastNameLabel = new JLabel("Client last name");
    JLabel bookTitleLabel = new JLabel("Book title:");
    JLabel rentedDateLabel = new JLabel("Date of rent:");
    JLabel returnDateLabel = new JLabel("Return date:");

    static JTextField clientFirstNameTextField = new JTextField();
    static JTextField clientLastNameTextField = new JTextField();
    static JTextField bookTitleTextField = new JTextField();
    static JTextField rentedDateTextField = new JTextField();
    static JTextField returnDateTextField = new JTextField("YYYY-MM-DD");

    JButton updateRentedBookButton = new JButton("Update");

    JTextField searchBookTitleTextField = new JTextField("Enter book title");
    JTextField searchRentedDateTextField = new JTextField("Enter rented date");
    JButton searchButton = new JButton("Search");

    JButton refreshButton = new JButton("Refresh");

    RentedBooksUpdateTab(){
        this.add(upperRentedBooksUpdatePanel);
        this.add(middleRentedBooksUpdatePanel);
        this.add(lowerRentedBooksUpdatePanel);
        this.setLayout(new GridLayout(3,2));

        //upper panel
        upperRentedBooksUpdatePanel.setLayout(new GridLayout(5,2));
        upperRentedBooksUpdatePanel.add(clientFirstNameLabel);
        upperRentedBooksUpdatePanel.add(clientFirstNameTextField);
        upperRentedBooksUpdatePanel.add(clientLastNameLabel);
        upperRentedBooksUpdatePanel.add(clientLastNameTextField);
        upperRentedBooksUpdatePanel.add(bookTitleLabel);
        upperRentedBooksUpdatePanel.add(bookTitleTextField);
        upperRentedBooksUpdatePanel.add(rentedDateLabel);
        upperRentedBooksUpdatePanel.add(rentedDateTextField);
        upperRentedBooksUpdatePanel.add(returnDateLabel);
        upperRentedBooksUpdatePanel.add(returnDateTextField);

        //middle panel
        middleRentedBooksUpdatePanel.add(updateRentedBookButton);
        updateRentedBookButton.addActionListener(new UpdateAction());

        middleRentedBooksUpdatePanel.add(searchBookTitleTextField);
        searchBookTitleTextField.setPreferredSize(new Dimension(400,30));

        middleRentedBooksUpdatePanel.add(searchRentedDateTextField);
        searchRentedDateTextField.setPreferredSize(new Dimension(400,30));

        middleRentedBooksUpdatePanel.add(searchButton);
        searchButton.addActionListener(new SearchAction());

        middleRentedBooksUpdatePanel.add(refreshButton);
        refreshButton.addActionListener(new RefreshAction());

        //lower panel
        lowerRentedBooksUpdatePanel.add(rentedBooksTableScroller);
        rentedBooksTableScroller.setPreferredSize(new Dimension(450,160));
        rentedBooksUpdateTable.setModel(DBHelper.getAllRentedBooksData());
        rentedBooksUpdateTable.addMouseListener(new RentedBooksUpdateTableListener());
    }

    class UpdateAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Calendar calendarRent = Calendar.getInstance();
            Date calendarRent30DaysLater;
            Date returnDate;
            String returnDateFormatted;
            Date rentedDateCheck;

            String rentedDate = rentedDateTextField.getText();
            String returnDateString = returnDateTextField.getText();

            try {
                calendarRent.setTime(sdf.parse(rentedDate));
                returnDate = sdf.parse(returnDateString);
                rentedDateCheck = sdf.parse(rentedDate);

                calendarRent.add(Calendar.DAY_OF_MONTH,30);
                calendarRent30DaysLater = calendarRent.getTime();

                MainFrame.conn = DBHelper.getConnection();
                if(returnDate.compareTo(rentedDateCheck) < 0){
                    returnDateTextField.setText("Invalid year");
                }else if(returnDate.compareTo(calendarRent30DaysLater) > 0){
                    returnDateFormatted = sdf.format(returnDate);
                    MainFrame.statement = MainFrame.conn.prepareStatement("Update rented_books set returnDate = \'"+returnDateFormatted+"\', comment = 'Book is not returned on time' where rentedBookId = ?");
                    MainFrame.statement.setInt(1,MainFrame.id);

                    MainFrame.statement.execute();
                    MainFrame.id = -1;

                    rentedBooksUpdateTable.setModel(DBHelper.getAllRentedBooksData());
                    clearRentedBooksUpdateForm();

                }else if(returnDate.compareTo(calendarRent30DaysLater) < 0){
                    returnDateFormatted = sdf.format(returnDate);
                    MainFrame.statement = MainFrame.conn.prepareStatement("Update rented_books set returnDate = \'"+returnDateFormatted+"\', comment = 'Book is returned on time' where rentedBookId = ?");
                    MainFrame.statement.setInt(1,MainFrame.id);

                    MainFrame.statement.execute();
                    MainFrame.id = -1;

                    rentedBooksUpdateTable.setModel(DBHelper.getAllRentedBooksData());
                    clearRentedBooksUpdateForm();
                }
            } catch (ParseException | SQLException parseException) {
                parseException.printStackTrace();
            }
        }
    }//end update action

    class SearchAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            String searchRentedBook = searchBookTitleTextField.getText();
            String searchRentedDate = searchRentedDateTextField.getText();
            MainFrame.conn = DBHelper.getConnection();
            try {
                MainFrame.statement = MainFrame.conn.prepareStatement("select bookTitle, rentedDate from rented_books rb join books b on rb.bookId = b.bookId where bookTitle ilike \'"+searchRentedBook+"%\' and rentedDate = \'"+searchRentedDate+"\'");

                MainFrame.resultSet = MainFrame.statement.executeQuery();
                rentedBooksUpdateTable.setModel(new MyModel(MainFrame.resultSet));
                searchBookTitleTextField.setText("");
                searchRentedDateTextField.setText("");
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }
    }//end search action

    class RentedBooksUpdateTableListener implements MouseListener{

        @Override
        public void mouseClicked(MouseEvent e) {
            if(e.getClickCount() == 2) {
                int row = RentedBooksUpdateTab.rentedBooksUpdateTable.getSelectedRow();
                MainFrame.id = Integer.parseInt(RentedBooksUpdateTab.rentedBooksUpdateTable.getValueAt(row, 0).toString());
                RentedBooksUpdateTab.clientFirstNameTextField.setText(RentedBooksUpdateTab.rentedBooksUpdateTable.getValueAt(row, 1).toString());
                RentedBooksUpdateTab.clientLastNameTextField.setText(RentedBooksUpdateTab.rentedBooksUpdateTable.getValueAt(row, 2).toString());
                RentedBooksUpdateTab.bookTitleTextField.setText(RentedBooksUpdateTab.rentedBooksUpdateTable.getValueAt(row, 3).toString());
                RentedBooksUpdateTab.rentedDateTextField.setText(RentedBooksUpdateTab.rentedBooksUpdateTable.getValueAt(row, 4).toString());
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {

        }

        @Override
        public void mouseReleased(MouseEvent e) {

        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }
    }//end rented books update table listener

    class RefreshAction implements ActionListener{

        @Override
        public void actionPerformed(ActionEvent e) {
            rentedBooksUpdateTable.setModel(DBHelper.getAllRentedBooksData());
        }
    }//end refresh action

    public void clearRentedBooksUpdateForm(){
        clientFirstNameTextField.setText("");
        clientLastNameTextField.setText("");
        bookTitleTextField.setText("");
        rentedDateTextField.setText("");
        returnDateTextField.setText("");
    }
}


