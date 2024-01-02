package banking;

public class Main {
    private static String databaseFileName = "default.db"; // A default name for the database

    public static void main(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if ("-fileName".equals(args[i])) {
                databaseFileName = args[i + 1];
                break;
            }
        }
        BankingSystem bankingSystem = new BankingSystem(databaseFileName);
        bankingSystem.displayMenu();
    }
}
