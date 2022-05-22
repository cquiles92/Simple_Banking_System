package banking;


import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;

public class BankProgram {
    private final Scanner scanner;
    private final int bankIdentificationNumber;
    private final DataBaseManager dataBaseManager;

    BankProgram(String url) {
        scanner = new Scanner(System.in);
        bankIdentificationNumber = 400000;
        dataBaseManager = new DataBaseManager(url);
    }

    public void mainMenu() {
        String userChoice = "";
        while (!userChoice.equals("0")) {
            printMainMenu();
            userChoice = scanner.nextLine().trim();
            switch (userChoice) {
                case "1":
                    Account currentAccount = createAccount();
                    if (dataBaseManager.insertAccount(currentAccount)) {
                        cardCreationMessage(currentAccount);
                    }
                    break;
                case "2":
                    getLogInCredentials();
                    break;
                case "0":
                    System.out.print("\nBye!");
                    break;
                default:
                    System.out.println("Invalid Choice");
            }
        }
        scanner.close();
    }

    private void printMainMenu() {
        System.out.println("1. Create an account\n" +
                           "2. Log into account\n" +
                           "0. Exit");
    }

    private void printAccountManagementMenu() {
        System.out.println("1. Balance\n" +
                           "2. Add income\n" +
                           "3. Do transfer\n" +
                           "4. Close account\n" +
                           "5. Log out\n" +
                           "0. Exit");
    }

    private Account createAccount() {
        int accountID = new Random().nextInt(1000000000);
        //create card by using string builder and adding checksum and generating random PIN
        StringBuilder card = new StringBuilder(String.valueOf(bankIdentificationNumber) + accountID);
        //pad card number out with extra 0s if necessary to make 15 digits
        while (card.length() < 15) {
            card.insert(6, 0);
        }
        card.append(ChecksumCalculator.calculateChecksum(card.toString()));
        String PIN = generateRandomPIN();

        return new Account(card.toString(), 0, PIN);
    }

    private String generateRandomPIN() {
        Random random = new Random();

        return String.valueOf(random.nextInt(10)) +
               random.nextInt(10) +
               random.nextInt(10) +
               random.nextInt(10);
    }

    private void cardCreationMessage(Account account) {
        System.out.println();
        System.out.println("Your card has been created");
        System.out.println("Your card number:");
        System.out.println(account.getCardNumber());
        System.out.println("Your card PIN:");
        System.out.println(account.getPIN());
        System.out.println();
    }

    private void getLogInCredentials() {
        String cardNumber;
        String PIN;

        System.out.println("Enter your card number:");
        cardNumber = scanner.nextLine();
        System.out.println("Enter your PIN:");
        PIN = scanner.nextLine();
        System.out.println();

        logInAccount(cardNumber, PIN);
    }

    private void logInAccount(String cardNumber, String PIN) {
        Account currentAccount = dataBaseManager.findAccount(cardNumber);

        if (currentAccount != null && currentAccount.getPIN().equals(PIN)) {
            System.out.println("You have successfully logged in!\n");
            accountManagement(currentAccount);
        } else {
            System.out.println("Wrong card number or PIN!\n");
        }
    }

    private void getBalance(Account currentAccount) {
        String balanceFormatted = String.format("%.2f", currentAccount.getBalance());
        System.out.println("\nBalance: " + balanceFormatted + "\n");
    }

    private void addIncome(Account currentAccount) {
        System.out.println("\nEnter income:");
        try {
            int incomingFunds = Integer.parseInt(scanner.nextLine());
            if (dataBaseManager.updateIncome(currentAccount.getCardNumber(), incomingFunds)) {
                currentAccount.addIncome(incomingFunds);
                System.out.println("Income was added!\n");
            }
        } catch (Exception e) {
            System.out.println("Invalid input\n");
        }
    }

    private void transferFunds(Account currentAccount) {
        System.out.println("Transfer");
        System.out.println("Enter card number:");
        try {
            long accountNumberAsInt = Long.parseLong(scanner.nextLine());
            String externalAccountNumber = String.valueOf(accountNumberAsInt);
            //if account is the same //if card does not match checksum
            if (sameAccountTest(currentAccount, externalAccountNumber) || !checkSumTest(externalAccountNumber)) {
                return;
            }

            Account externalAccount = dataBaseManager.findAccount(externalAccountNumber);

            //if account does not exist in database
            if (externalAccount == null) {
                System.out.println("Such a card does not exist.\n");
                return;
            }

            System.out.println("Enter how much money you want to transfer:");
            int fundsToTransfer = Integer.parseInt(scanner.nextLine());

            //if there are enough funds
            if (currentAccount.getBalance() < fundsToTransfer) {
                System.out.println("Not enough money!\n");
                return;
            }
            //execute transaction
            accountTransferTransaction(currentAccount, externalAccount, fundsToTransfer);

        } catch (InputMismatchException e) {
            System.out.println("Invalid input\n");
        }
    }


    private boolean sameAccountTest(Account currentAccount, String externalAccountNumber) {
        if (currentAccount.getCardNumber().equals(externalAccountNumber)) {
            System.out.println("You can't transfer money to the same account!\n");
            return true;
        }
        return false;
    }

    private boolean checkSumTest(String externalAccountNumber) {
        int lastDigit = Integer.parseInt(String.valueOf(externalAccountNumber.charAt(15)));

        if (lastDigit != ChecksumCalculator.calculateChecksum(externalAccountNumber)) {
            System.out.println("Probably you made a mistake in the card number. Please try again!\n");
            return false;
        }
        return true;
    }

    private void accountTransferTransaction(Account transferOut, Account transferTo, int amount) {
        transferOut.addIncome(-amount);
        transferTo.addIncome(amount);
        if (dataBaseManager.transferFunds(transferOut, transferTo)) {
            System.out.println("Success!\n");
        }
    }

    private void deleteAccount(Account account) {
        if (dataBaseManager.deleteAccount(account)) {
            System.out.println("\nThe account has been closed!\n");
        }
    }

    private void exitProgram() {
        System.out.print("\nBye!");
        System.exit(0);
    }

    private void accountManagement(Account currentAccount) {
        while (true) {
            printAccountManagementMenu();
            String userChoice = scanner.nextLine();
            switch (userChoice) {
                case "1":
                    getBalance(currentAccount);
                    break;
                case "2":
                    addIncome(currentAccount);
                    break;
                case "3":
                    transferFunds(currentAccount);
                    break;
                case "4":
                    deleteAccount(currentAccount);
                    return;
                case "5":
                    System.out.println("\nYou have successfully logged out!\n");
                    return;
                case "0":
                    exitProgram();
                default:
                    System.out.println("Invalid Selection\n");
            }
        }
    }
}
