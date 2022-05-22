package banking;

public class Account {
    private final int bankIdentificationNumber;
    private final int accountID;
    private final int checksum;
    private final String cardNumber;

    private double balance;
    private final String PIN;

    public Account(String cardNumber, double balance, String PIN) {
        this.bankIdentificationNumber = Integer.parseInt(cardNumber.substring(0, 6));
        this.accountID = Integer.parseInt(cardNumber.substring(6, cardNumber.length() - 1));
        this.checksum = Integer.parseInt(cardNumber.substring(cardNumber.length() - 1));
        this.balance = balance;
        this.cardNumber = cardNumber;
        this.PIN = PIN;
    }

    public double getBalance() {
        return balance;
    }

    public String getPIN() {
        return PIN;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void addIncome(int incomingFunds) {
        this.balance += incomingFunds;
    }
}
