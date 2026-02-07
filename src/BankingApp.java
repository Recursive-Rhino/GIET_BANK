import java.sql.*;
import java.util.Scanner;
import java.util.Random;

public class BankingApp {

    static final String URL = "jdbc:mysql://localhost:3306/bank_db";
    static final String USER = "root";
    static final String PASSWORD = "DBMS@#2965&Smruti";

    static Connection conn;
    static Scanner sc = new Scanner(System.in);
    static int loggedInUserId = -1;
    static String loggedInUserName = "";

    public static void main(String[] args) {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connected to database!");

            System.out.println("\n===== BANKING SYSTEM =====");

            while (true) {
                if (loggedInUserId == -1) {
                    mainMenu();
                } else {
                    userMenu();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    static void mainMenu() throws SQLException {
        System.out.println("\n1. Register");
        System.out.println("2. Login");
        System.out.println("3. Exit");
        System.out.print("Choice: ");
        int ch = sc.nextInt();
        sc.nextLine();

        switch (ch) {
            case 1: register(); break;
            case 2: login(); break;
            case 3:
                conn.close();
                System.out.println("Goodbye!");
                System.exit(0);
        }
    }
    static void userMenu() throws SQLException {
        System.out.println("\n--- Welcome " + loggedInUserName + " ---");
        System.out.println("1. Open Account");
        System.out.println("2. Check Balance");
        System.out.println("3. Deposit");
        System.out.println("4. Withdraw");
        System.out.println("5. Transfer");
        System.out.println("6. View Accounts");
        System.out.println("7. Logout");
        System.out.print("Choice: ");
        int ch = sc.nextInt();
        sc.nextLine();

        switch (ch) {
            case 1: openAccount(); break;
            case 2: checkBalance(); break;
            case 3: deposit(); break;
            case 4: withdraw(); break;
            case 5: transfer(); break;
            case 6: viewAccounts(); break;
            case 7:
                loggedInUserId = -1;
                System.out.println("Logged out!");
                break;
        }
    }

    static void register() throws SQLException {
        System.out.print("Enter name: ");
        String name = sc.nextLine();
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, name);
        ps.setString(2, email);
        ps.setString(3, password);
        ps.executeUpdate();
        System.out.println("Registration successful!");
    }


    static void login() throws SQLException {
        System.out.print("Enter email: ");
        String email = sc.nextLine();
        System.out.print("Enter password: ");
        String password = sc.nextLine();

        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, email);
        ps.setString(2, password);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            loggedInUserId = rs.getInt("user_id");
            loggedInUserName = rs.getString("name");
            System.out.println("Login successful!");
        } else {
            System.out.println("Invalid credentials!");
        }
    }

    // Generate account number
    static String generateAccountNumber() {
        Random r = new Random();
        return "ACC" + (10000000 + r.nextInt(90000000));
    }

    // Open new account
    static void openAccount() throws SQLException {
        System.out.print("Account Type (SAVINGS/CURRENT): ");
        String type = sc.nextLine();
        System.out.print("Initial Deposit: ");
        double amount = sc.nextDouble();
        sc.nextLine();

        String accNo = generateAccountNumber();
        String sql = "INSERT INTO accounts (account_number, user_id, balance, account_type) VALUES (?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, accNo);
        ps.setInt(2, loggedInUserId);
        ps.setDouble(3, amount);
        ps.setString(4, type);
        ps.executeUpdate();

        System.out.println("Account created! Your Account Number: " + accNo);
    }

    static void checkBalance() throws SQLException {
        System.out.print("Enter Account Number: ");
        String accNo = sc.nextLine();

        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setString(1, accNo);
        ResultSet rs = ps.executeQuery();

        if (rs.next()) {
            System.out.println("Balance: Rs. " + rs.getDouble("balance"));
        } else {
            System.out.println("Account not found!");
        }
    }

    // Deposit money
    static void deposit() throws SQLException {
        System.out.print("Enter Account Number: ");
        String accNo = sc.nextLine();
        System.out.print("Enter Amount: ");
        double amount = sc.nextDouble();
        sc.nextLine();

        String sql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setDouble(1, amount);
        ps.setString(2, accNo);
        int rows = ps.executeUpdate();

        if (rows > 0) {
            System.out.println("Deposited Rs. " + amount);
        } else {
            System.out.println("Account not found!");
        }
    }

    // Withdraw money
    static void withdraw() throws SQLException {
        System.out.print("Enter Account Number: ");
        String accNo = sc.nextLine();
        System.out.print("Enter Amount: ");
        double amount = sc.nextDouble();
        sc.nextLine();

        // First check balance
        String checkSql = "SELECT balance FROM accounts WHERE account_number = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, accNo);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next()) {
            if (rs.getDouble("balance") >= amount) {
                String sql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDouble(1, amount);
                ps.setString(2, accNo);
                ps.executeUpdate();
                System.out.println("Withdrawn Rs. " + amount);
            } else {
                System.out.println("Insufficient balance!");
            }
        } else {
            System.out.println("Account not found!");
        }
    }

    static void transfer() throws SQLException {
        System.out.print("From Account: ");
        String fromAcc = sc.nextLine();
        System.out.print("To Account: ");
        String toAcc = sc.nextLine();
        System.out.print("Amount: ");
        double amount = sc.nextDouble();
        sc.nextLine();

        String checkSql = "SELECT balance FROM accounts WHERE account_number = ?";
        PreparedStatement checkPs = conn.prepareStatement(checkSql);
        checkPs.setString(1, fromAcc);
        ResultSet rs = checkPs.executeQuery();

        if (rs.next() && rs.getDouble("balance") >= amount) {
            String debitSql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ?";
            PreparedStatement debitPs = conn.prepareStatement(debitSql);
            debitPs.setDouble(1, amount);
            debitPs.setString(2, fromAcc);
            debitPs.executeUpdate();

            String creditSql = "UPDATE accounts SET balance = balance + ? WHERE account_number = ?";
            PreparedStatement creditPs = conn.prepareStatement(creditSql);
            creditPs.setDouble(1, amount);
            creditPs.setString(2, toAcc);
            creditPs.executeUpdate();

            System.out.println("Transferred Rs. " + amount);
        } else {
            System.out.println("Transfer failed! Check balance or account.");
        }
    }

    static void viewAccounts() throws SQLException {
        String sql = "SELECT * FROM accounts WHERE user_id = ?";
        PreparedStatement ps = conn.prepareStatement(sql);
        ps.setInt(1, loggedInUserId);
        ResultSet rs = ps.executeQuery();

        System.out.println("\n--- Your Accounts ---");
        while (rs.next()) {
            System.out.println("Acc No: " + rs.getString("account_number") +
                    " | Type: " + rs.getString("account_type") +
                    " | Balance: Rs. " + rs.getDouble("balance"));
        }
    }
}
