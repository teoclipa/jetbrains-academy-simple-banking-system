package banking;

import java.sql.*;
import java.util.Random;
import java.util.Scanner;

public class BankingSystem {
    private final String databaseUrl;
    private final Scanner scanner;

    public BankingSystem(String databaseFileName) {
        databaseUrl = "jdbc:sqlite:" + databaseFileName;
        scanner = new Scanner(System.in);
        createNewDatabase();
    }

    private void createNewDatabase() {
        try (Connection conn = DriverManager.getConnection(databaseUrl); Statement stmt = conn.createStatement()) {
            String sql = "CREATE TABLE IF NOT EXISTS card (" + "id INTEGER PRIMARY KEY," + "number TEXT," + "pin TEXT," + "balance INTEGER DEFAULT 0" + ");";
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void createAccount() {
        String cardNumber = generateCardNumber();
        String pin = String.format("%04d", new Random().nextInt(10000));
        insertCardIntoDatabase(cardNumber, pin);

        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(cardNumber);
        System.out.println("Your card PIN:");
        System.out.println(pin);
    }

    private void performLoggedInActions(String cardNumber, Connection conn) {
        boolean isLoggedIn = true;
        while (isLoggedIn) {
            System.out.println("1. Balance\n2. Add income\n3. Do transfer\n4. Close account\n5. Log out\n0. Exit");
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    showBalance(cardNumber);
                    break;
                case 2:
                    addIncome(cardNumber);
                    break;
                case 3:
                    doTransfer(cardNumber, conn);
                    break;
                case 4:
                    closeAccount(cardNumber);
                    isLoggedIn = false; // Log out after closing the account
                    break;
                case 5:
                    System.out.println("You have successfully logged out!");
                    isLoggedIn = false;
                    break;
                case 0:
                    System.out.println("Bye!");
                    System.exit(0);
                    break;
                default:
                    System.out.println("Unknown option!");
            }
        }
    }

    private void insertCardIntoDatabase(String cardNumber, String pin) {
        String sql = "INSERT INTO card(number, pin) VALUES(?,?)";
        try (Connection conn = DriverManager.getConnection(databaseUrl); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardNumber);
            pstmt.setString(2, pin);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private String login() {
        System.out.println("Enter your card number:");
        String number = scanner.next();
        System.out.println("Enter your PIN:");
        String pin = scanner.next();

        String sql = "SELECT number FROM card WHERE number = ? AND pin = ?";
        try (Connection conn = DriverManager.getConnection(databaseUrl); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, number);
            pstmt.setString(2, pin);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    System.out.println("You have successfully logged in!");
                    return number; // Return the card number on successful login
                } else {
                    System.out.println("Wrong card number or PIN!");
                    return null; // Return null on unsuccessful login
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    private String generateCardNumber() {
        String cardNumberWithoutChecksum = "400000" + String.format("%09d", new Random().nextInt(1000000000));
        int checksum = LuhnAlgorithm.calculateLuhnChecksum(cardNumberWithoutChecksum);
        return cardNumberWithoutChecksum + checksum;
    }

    public void displayMenu() {
        try (Connection conn = DriverManager.getConnection(databaseUrl)) {

            while (true) {
                System.out.println("1. Create an account\n2. Log into account\n0. Exit");
                int choice = scanner.nextInt();
                switch (choice) {
                    case 1:
                        createAccount();
                        break;
                    case 2:
                        String cardNumber = login();
                        if (cardNumber != null) {
                            performLoggedInActions(cardNumber, conn);
                        }
                        break;
                    case 0:
                        System.out.println("Bye!");
                        return;
                    default:
                        System.out.println("Unknown option!");
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private void addIncome(String cardNumber) {
        System.out.println("Enter income:");
        int income = scanner.nextInt();
        if (income < 0) {
            System.out.println("Income cannot be negative.");
            return;
        }

        String sql = "UPDATE card SET balance = balance + ? WHERE number = ?";
        try (Connection conn = DriverManager.getConnection(databaseUrl); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, income);
            pstmt.setString(2, cardNumber);
            pstmt.executeUpdate();
            System.out.println("Income was added!");
        } catch (SQLException e) {
            System.out.println("Error adding income: " + e.getMessage());
        }
    }

    private void doTransfer(String sourceCardNumber, Connection conn) {
        System.out.println("Transfer\nEnter card number:");
        String targetCardNumber = scanner.next();

        if (!LuhnAlgorithm.isValidLuhnNumber(targetCardNumber)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!");
            return;
        }
        if (sourceCardNumber.equals(targetCardNumber)) {
            System.out.println("You can't transfer money to the same account!");
            return;
        }

        String checkAccountSql = "SELECT number FROM card WHERE number = ?";
        String balanceSql = "SELECT balance FROM card WHERE number = ?";
        try (PreparedStatement checkStmt = conn.prepareStatement(checkAccountSql); PreparedStatement balanceStmt = conn.prepareStatement(balanceSql)) {

            // Check if target account exists
            checkStmt.setString(1, targetCardNumber);
            ResultSet rs = checkStmt.executeQuery();
            if (!rs.next()) {
                System.out.println("Such a card does not exist.");
                return;
            }

            System.out.println("Enter how much money you want to transfer:");
            int amount = scanner.nextInt();

            // Check if enough balance in source account
            balanceStmt.setString(1, sourceCardNumber);
            rs = balanceStmt.executeQuery();
            if (rs.next()) {
                int currentBalance = rs.getInt("balance");
                if (currentBalance < amount) {
                    System.out.println("Not enough money!");
                    return;
                }
            }

            // Perform transfer
            performTransfer(sourceCardNumber, targetCardNumber, amount, conn);
            System.out.println("Success!");

        } catch (SQLException e) {
            System.out.println("Error during transfer: " + e.getMessage());
        }
    }

    private void performTransfer(String sourceCardNumber, String targetCardNumber, int amount, Connection conn) {
        String deductSql = "UPDATE card SET balance = balance - ? WHERE number = ?";
        String addSql = "UPDATE card SET balance = balance + ? WHERE number = ?";

        PreparedStatement deductStmt = null;
        PreparedStatement addStmt = null;

        try {
            conn.setAutoCommit(false); // Start transaction

            // Deduct amount from source account
            deductStmt = conn.prepareStatement(deductSql);
            deductStmt.setInt(1, amount);
            deductStmt.setString(2, sourceCardNumber);
            deductStmt.executeUpdate();

            // Add amount to target account
            addStmt = conn.prepareStatement(addSql);
            addStmt.setInt(1, amount);
            addStmt.setString(2, targetCardNumber);
            addStmt.executeUpdate();

            conn.commit(); // Commit transaction
            System.out.println("Success!");
        } catch (SQLException e) {
            try {
                conn.rollback(); // Rollback transaction on error
                System.out.println("Transaction rolled back due to error: " + e.getMessage());
            } catch (SQLException ex) {
                System.out.println("Error during rollback: " + ex.getMessage());
            }
        } finally {
            try {
                if (deductStmt != null) {
                    deductStmt.close();
                }
                if (addStmt != null) {
                    addStmt.close();
                }
            } catch (SQLException e) {
                System.out.println("Error closing statements: " + e.getMessage());
            }
        }
    }

    private void closeAccount(String cardNumber) {
        String sql = "DELETE FROM card WHERE number = ?";
        try (Connection conn = DriverManager.getConnection(databaseUrl); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardNumber);
            pstmt.executeUpdate();
            System.out.println("The account has been closed!");
        } catch (SQLException e) {
            System.out.println("Error closing account: " + e.getMessage());
        }
    }

    private void showBalance(String cardNumber) {
        String sql = "SELECT balance FROM card WHERE number = ?";
        try (Connection conn = DriverManager.getConnection(databaseUrl); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, cardNumber);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    int balance = rs.getInt("balance");
                    System.out.println("Balance: " + balance);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error retrieving balance: " + e.getMessage());
        }
    }

}
