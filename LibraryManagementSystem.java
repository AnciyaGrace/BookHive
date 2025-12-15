/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 */

package com.mycompany.mavenproject1;

/**
 *
 * @author Anciya Grace Vaz
 */
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

public class LibraryManagementSystem extends JFrame {

    // ================= BOOK STRUCTURE =================
    class Book implements Serializable {
        int id;
        String title;
        String author;
        boolean issued;

        Book(int id, String title, String author) {
            this.id = id;
            this.title = title;
            this.author = author;
            this.issued = false;
        }
    }

    // ================= ISSUE RECORD STRUCTURE =================
    class IssueRecord implements Serializable {
        int bookId;
        String title;
        String borrowerName;
        String borrowerEmail;
        LocalDate issueDate;
        LocalDate returnDate;
        int fine;

        IssueRecord(int bookId, String title, String name, String email, LocalDate issueDate) {
            this.bookId = bookId;
            this.title = title;
            this.borrowerName = name;
            this.borrowerEmail = email;
            this.issueDate = issueDate;
            this.returnDate = null;
            this.fine = 0;
        }
    }

    // ================= RESERVATION STRUCTURE =================
    class Reservation implements Serializable {
        int bookId;
        String title;
        String reserverName;
        String reserverEmail;
        LocalDate reservationDate;
        String status;   // Reserved / Issued / Not Issued

        Reservation(int bookId, String title, String name, String email) {
            this.bookId = bookId;
            this.title = title;
            this.reserverName = name;
            this.reserverEmail = email;
            this.reservationDate = LocalDate.now();
            this.status = "Reserved";
        }
    }

    // ================= DATA STRUCTURES =================
    ArrayList<Book> books = new ArrayList<>();
    ArrayList<IssueRecord> records = new ArrayList<>();
    ArrayList<Reservation> reservations = new ArrayList<>();

    // ================= GUI COMPONENTS =================
    DefaultTableModel bookModel, recordModel, reservationModel;
    JTextField txtId, txtTitle, txtAuthor, txtUserName, txtUserEmail, txtSearch;

    final int MAX_DAYS = 3;
    final int FINE_PER_DAY = 5;
    final String FILE_NAME = "library.dat";

    // ================= CONSTRUCTOR =================
    public LibraryManagementSystem() {
        setTitle("Library Management System");
        setSize(1050, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        loadData();
        updateReservationStatus();

        JLabel heading = new JLabel("Library Management System", JLabel.CENTER);
        heading.setFont(new Font("Arial", Font.BOLD, 26));
        add(heading, BorderLayout.NORTH);

        // ================= DASHBOARD TABS =================
        JTabbedPane tabs = new JTabbedPane();

        bookModel = new DefaultTableModel(
                new String[]{"ID", "Title", "Author", "Status"}, 0);
        tabs.add("Books", new JScrollPane(new JTable(bookModel)));

        recordModel = new DefaultTableModel(
                new String[]{"Book ID", "Title", "Name", "Email", "Issue Date", "Return Date", "Fine"}, 0);
        tabs.add("Issue / Return Records", new JScrollPane(new JTable(recordModel)));

        reservationModel = new DefaultTableModel(
                new String[]{"Book ID", "Title", "Name", "Email", "Reserved On", "Status"}, 0);
        tabs.add("Reservations", new JScrollPane(new JTable(reservationModel)));

        add(tabs, BorderLayout.CENTER);

        // ================= INPUT PANEL =================
        JPanel panel = new JPanel(new GridLayout(2, 6, 10, 10));

        txtId = new JTextField();
        txtTitle = new JTextField();
        txtAuthor = new JTextField();
        txtUserName = new JTextField();
        txtUserEmail = new JTextField();
        txtSearch = new JTextField();

        panel.add(new JLabel("Book ID"));
        panel.add(new JLabel("Title"));
        panel.add(new JLabel("Author"));
        panel.add(new JLabel("Name"));
        panel.add(new JLabel("Email"));
        panel.add(new JLabel("Search"));

        panel.add(txtId);
        panel.add(txtTitle);
        panel.add(txtAuthor);
        panel.add(txtUserName);
        panel.add(txtUserEmail);
        panel.add(txtSearch);

        add(panel, BorderLayout.SOUTH);

        // ================= BUTTON PANEL =================
        JPanel btnPanel = new JPanel(new GridLayout(6, 1, 10, 10));

        JButton btnAdd = new JButton("Add Book");
        JButton btnIssue = new JButton("Issue Book");
        JButton btnReturn = new JButton("Return Book");
        JButton btnReserve = new JButton("Reserve Book");
        JButton btnSearch = new JButton("Search");
        JButton btnClear = new JButton("Clear");

        btnPanel.add(btnAdd);
        btnPanel.add(btnIssue);
        btnPanel.add(btnReturn);
        btnPanel.add(btnReserve);
        btnPanel.add(btnSearch);
        btnPanel.add(btnClear);

        add(btnPanel, BorderLayout.EAST);

        // ================= ACTION LISTENERS =================
        btnAdd.addActionListener(e -> addBook());
        btnIssue.addActionListener(e -> issueBook());
        btnReturn.addActionListener(e -> returnBook());
        btnReserve.addActionListener(e -> reserveBook());
        btnSearch.addActionListener(e -> searchBook());
        btnClear.addActionListener(e -> clearFields());

        refreshAllTables();
    }

    // ================= EMAIL VALIDATION =================
    boolean isValidEmail(String email) {
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    }

    // ================= ADD BOOK =================
    void addBook() {
        try {
            int id = Integer.parseInt(txtId.getText());
            for (Book b : books)
                if (b.id == id) {
                    JOptionPane.showMessageDialog(this, "Duplicate Book ID");
                    return;
                }

            books.add(new Book(id, txtTitle.getText(), txtAuthor.getText()));
            saveData();
            refreshAllTables();
            clearFields();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Input");
        }
    }

    // ================= RESERVE BOOK =================
    void reserveBook() {
        try {
            int id = Integer.parseInt(txtId.getText());
            String name = txtUserName.getText();
            String email = txtUserEmail.getText();

            if (name.isEmpty() || email.isEmpty() || !isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Enter valid Name and Email");
                return;
            }

            for (Reservation r : reservations)
                if (r.bookId == id && r.status.equals("Reserved")) {
                    JOptionPane.showMessageDialog(this, "Book already reserved");
                    return;
                }

            for (Book b : books) {
                if (b.id == id && !b.issued) {
                    reservations.add(new Reservation(b.id, b.title, name, email));
                    saveData();
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Book reserved for 1 day");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Book not available");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Input");
        }
    }

    // ================= ISSUE BOOK =================
    void issueBook() {
        try {
            int id = Integer.parseInt(txtId.getText());
            String name = txtUserName.getText();
            String email = txtUserEmail.getText();

            if (name.isEmpty() || email.isEmpty() || !isValidEmail(email)) {
                JOptionPane.showMessageDialog(this, "Enter valid Name and Email");
                return;
            }

            for (Reservation r : reservations) {
                if (r.bookId == id && r.status.equals("Reserved")) {
                    long days = ChronoUnit.DAYS.between(r.reservationDate, LocalDate.now());
                    if (days >= 1) {
                        r.status = "Not Issued";
                        break;
                    }
                    if (!r.reserverEmail.equals(email)) {
                        JOptionPane.showMessageDialog(this, "Book reserved by another user");
                        return;
                    }
                    r.status = "Issued";
                }
            }

            for (Book b : books) {
                if (b.id == id && !b.issued) {
                    b.issued = true;
                    records.add(new IssueRecord(id, b.title, name, email, LocalDate.now()));
                    saveData();
                    refreshAllTables();
                    JOptionPane.showMessageDialog(this, "Book Issued Successfully");
                    return;
                }
            }
            JOptionPane.showMessageDialog(this, "Book not available");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Input");
        }
    }

    // ================= RETURN BOOK =================
    void returnBook() {
        try {
            int id = Integer.parseInt(txtId.getText());

            for (IssueRecord r : records) {
                if (r.bookId == id && r.returnDate == null) {
                    long days = ChronoUnit.DAYS.between(r.issueDate, LocalDate.now());
                    r.returnDate = LocalDate.now();
                    r.fine = (days > MAX_DAYS) ? (int) (days - MAX_DAYS) * FINE_PER_DAY : 0;
                }
            }

            for (Book b : books)
                if (b.id == id) b.issued = false;

            saveData();
            refreshAllTables();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Invalid Input");
        }
    }

    // ================= UPDATE RESERVATION STATUS =================
    void updateReservationStatus() {
        for (Reservation r : reservations) {
            long days = ChronoUnit.DAYS.between(r.reservationDate, LocalDate.now());
            if (days >= 1 && r.status.equals("Reserved")) {
                r.status = "Not Issued";
            }
        }
    }

    // ================= SEARCH =================
    void searchBook() {
        String key = txtSearch.getText().toLowerCase();
        for (Book b : books)
            if (b.title.toLowerCase().contains(key) ||
                b.author.toLowerCase().contains(key) ||
                String.valueOf(b.id).equals(key)) {
                JOptionPane.showMessageDialog(this,
                        "Book Found:\n" + b.title + " (" + (b.issued ? "Issued" : "Available") + ")");
                return;
            }
        JOptionPane.showMessageDialog(this, "Book Not Found");
    }

    // ================= REFRESH TABLES =================
    void refreshAllTables() {
        updateReservationStatus();

        bookModel.setRowCount(0);
        recordModel.setRowCount(0);
        reservationModel.setRowCount(0);

        for (Book b : books)
            bookModel.addRow(new Object[]{b.id, b.title, b.author, b.issued ? "Issued" : "Available"});

        for (IssueRecord r : records)
            recordModel.addRow(new Object[]{
                    r.bookId, r.title, r.borrowerName, r.borrowerEmail,
                    r.issueDate, r.returnDate == null ? "-" : r.returnDate, r.fine
            });

        for (Reservation r : reservations)
            reservationModel.addRow(new Object[]{
                    r.bookId, r.title, r.reserverName, r.reserverEmail,
                    r.reservationDate, r.status
            });
    }

    // ================= UTILITIES =================
    void clearFields() {
        txtId.setText("");
        txtTitle.setText("");
        txtAuthor.setText("");
        txtUserName.setText("");
        txtUserEmail.setText("");
        txtSearch.setText("");
    }

    // ================= FILE HANDLING =================
    void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(FILE_NAME))) {
            oos.writeObject(books);
            oos.writeObject(records);
            oos.writeObject(reservations);
        } catch (Exception ignored) {}
    }

    void loadData() {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(FILE_NAME))) {
            books = (ArrayList<Book>) ois.readObject();
            records = (ArrayList<IssueRecord>) ois.readObject();
            reservations = (ArrayList<Reservation>) ois.readObject();
        } catch (Exception e) {
            books = new ArrayList<>();
            records = new ArrayList<>();
            reservations = new ArrayList<>();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LibraryManagementSystem().setVisible(true));
    }
}
