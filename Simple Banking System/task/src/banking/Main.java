package banking;


public class Main {
    public static void main(String[] args) {
        BankProgram bankProgram;
        if (args.length > 1 && args[0].equals("-fileName")) {
            String url = "jdbc:sqlite:" + args[1];
            bankProgram = new BankProgram(url);
        } else {
            //default created directory
            bankProgram = new BankProgram("jdbc:sqlite:card.s3db");
        }
        bankProgram.mainMenu();
    }
}