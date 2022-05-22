package banking;

import org.sqlite.SQLiteDataSource;

import java.sql.*;

public class DataBaseManager {
    private final SQLiteDataSource dataSource;

    DataBaseManager(String url) {
        dataSource = new SQLiteDataSource();
        dataSource.setUrl(url);
        createTable();
    }

    private void createTable() {
        try (Connection connection = dataSource.getConnection()) {
            try (Statement statement = connection.createStatement()) {
                String createTableSQLStatement = "CREATE TABLE IF NOT EXISTS card (\n"
                                                 + "     id INTEGER PRIMARY KEY,\n"
                                                 + "     number TEXT,\n"
                                                 + "     pin TEXT,\n"
                                                 + "     balance INTEGER DEFAULT 0"
                                                 + ");";
                statement.execute(createTableSQLStatement);
            } catch (SQLException e) {
                System.out.println("ERROR: Could not execute Create Table Statement");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not establish a connection");
        }
    }

    protected boolean insertAccount(Account account) {
        String cardNumber = account.getCardNumber();
        String PIN = account.getPIN();

        String insertSQLStatement = String.format("INSERT INTO card(number,pin) VALUES ('%s','%s');\n", cardNumber, PIN);

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(insertSQLStatement)) {
                statement.execute();
            } catch (SQLException e) {
                System.out.println("ERROR: Could not insert account");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not establish a connection");
            return false;
        }
        return true;
    }

    protected Account findAccount(String cardNumber) {
        Account queriedAccount = null;
        String querySQLStatement = "SELECT * FROM card WHERE number = '" + cardNumber + "';";

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(querySQLStatement)) {
                try (ResultSet cardAccounts = statement.executeQuery()) {
                    while (cardAccounts.next()) {
                        String database_cardNumber = cardAccounts.getString("number");
                        double database_balance = cardAccounts.getInt("balance");
                        String database_PIN = cardAccounts.getString("pin");
                        queriedAccount = new Account(database_cardNumber, database_balance, database_PIN);
                    }
                } catch (SQLException e) {
                    System.out.println("ERROR: Invalid query");
                }
            } catch (SQLException e) {
                System.out.println("ERROR: Could not process statement");
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not establish a connection");
        }
        return queriedAccount;
    }

    protected boolean updateIncome(String accountNumber, int incomeToAdd) {
        String updateSQLStatement = "UPDATE card SET balance = balance + ? WHERE number = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement preparedStatement = connection.prepareStatement(updateSQLStatement)) {
                preparedStatement.setInt(1, incomeToAdd);
                preparedStatement.setString(2, accountNumber);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                System.out.println("ERROR: Could not process update");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not establish a connection");
            return false;
        }
        return true;
    }

    protected boolean transferFunds(Account transferOutOfAccount, Account transferIntoAccount) {
        boolean transaction = true;

        String updateSQLStatement = "UPDATE card SET balance = ? WHERE number = ?;";
        String selectSQLStatement = "SELECT number, balance FROM card WHERE number IN (?, ?);";

        String sendingAccount = transferOutOfAccount.getCardNumber();
        int sendingBalance = (int) transferOutOfAccount.getBalance();
        String receivingAccount = transferIntoAccount.getCardNumber();
        int receivingBalance = (int) transferIntoAccount.getBalance();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement updateSendingAccountStatement = connection.prepareStatement(updateSQLStatement);
                 PreparedStatement updateReceivingAccountStatement = connection.prepareStatement(updateSQLStatement)) {

                //create savepoint in case of a problem
                Savepoint savepoint = connection.setSavepoint();

                updateSendingAccountStatement.setInt(1, sendingBalance);
                updateSendingAccountStatement.setString(2, sendingAccount);

                updateReceivingAccountStatement.setInt(1, receivingBalance);
                updateReceivingAccountStatement.setString(2, receivingAccount);

                //after both send and receive updates have been prepared, process them
                updateSendingAccountStatement.executeUpdate();
                updateReceivingAccountStatement.executeUpdate();

                //test to see changes with select statement
                PreparedStatement selectSendingAccountStatement = connection.prepareStatement(selectSQLStatement);
                selectSendingAccountStatement.setString(1, sendingAccount);
                selectSendingAccountStatement.setString(2, receivingAccount);

                ResultSet resultSet = selectSendingAccountStatement.executeQuery();

                //if the changes are not made correctly rollback
                while (resultSet.next()) {
                    if (resultSet.getString(1).equals(sendingAccount) && resultSet.getInt(2) != sendingBalance) {
                        connection.rollback(savepoint);
                        transaction = false;
                        break;
                    }
                    if (resultSet.getString(1).equals(receivingAccount) && resultSet.getInt(2) != receivingBalance) {
                        connection.rollback(savepoint);
                        transaction = false;
                        break;
                    }
                }

                connection.commit();
            } catch (SQLException e) {
                System.out.println("ERROR: Could not process update");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not establish a connection");
            return false;
        }
        return transaction;
    }

    protected boolean deleteAccount(Account account) {
        String cardNumber = account.getCardNumber();
        String PIN = account.getPIN();
        String deleteSQLStatement = "DELETE FROM card WHERE number = '" + cardNumber + "' AND pin = '" + PIN + "'";

        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(deleteSQLStatement)) {
                statement.execute();
            } catch (SQLException e) {
                System.out.println("ERROR: Could not process statement");
                return false;
            }
        } catch (SQLException e) {
            System.out.println("ERROR: Could not establish a connection");
            return false;
        }
        return true;
    }
}
