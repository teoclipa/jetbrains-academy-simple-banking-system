# Banking System Simulation

This project is a Java-based simulation of a banking system. It utilizes an SQLite database for storing and managing user data. Below is an overview of its key features:

## Features

### 1. Database Management
- Utilizes SQLite database to store user information.
- Includes a `card` table with fields for card number, PIN, and balance.

### 2. Account Creation
- Allows users to create new bank accounts.
- Generates unique card numbers using the Luhn Algorithm.
- Assigns a randomly generated PIN for each new account.

### 3. User Authentication
- Enables users to log in using their card number and PIN.
- Authenticates user details against the SQLite database records.

### 4. Account Operations
Users can perform various operations once logged in:
   #### Checking Balance
   - Users can view their current account balance.
   
   #### Adding Income
   - Simulates depositing money into the account.
   
   #### Money Transfer
   - Permits transferring funds to other accounts within the system.
   - Includes validation for the existence of the target account and sufficiency of funds.
   
   #### Account Closure
   - Allows users to close their accounts, deleting their information from the database.

### 5. Transaction Management
- Manages monetary transactions using SQL transactions to maintain data integrity.
- Ensures proper handling of database connections and implements transaction rollbacks in case of errors.

### 6. Error Handling and Logging
- Implements error handling, especially for database operations.
- Logs error messages for troubleshooting, particularly for database access and transaction issues.

### 7. User Interface
- Provides a text-based console interface for user interactions.
- Enables users to input choices, card details, PINs, and transaction amounts.

### 8. Database Connection Management
- Uses JDBC (Java Database Connectivity) for database interactions.
- Efficiently manages database connections to avoid issues like database locking.

## Getting Started

To run this project, you will need a Java development environment and SQLite.
