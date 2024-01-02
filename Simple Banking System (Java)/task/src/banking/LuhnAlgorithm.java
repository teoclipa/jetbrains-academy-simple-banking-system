package banking;

public class LuhnAlgorithm {

    public static int calculateLuhnChecksum(String cardNumberWithoutChecksum) {
        int sum = 0;
        for (int i = 0; i < cardNumberWithoutChecksum.length(); i++) {
            int digit = Character.getNumericValue(cardNumberWithoutChecksum.charAt(i));
            if (i % 2 == 0) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }
            sum += digit;
        }

        return (10 - (sum % 10)) % 10;
    }

    public static boolean isValidLuhnNumber(String targetCardNumber) {
        int checksum = Character.getNumericValue(targetCardNumber.charAt(targetCardNumber.length() - 1));
        String cardNumberWithoutChecksum = targetCardNumber.substring(0, targetCardNumber.length() - 1);
        return checksum == calculateLuhnChecksum(cardNumberWithoutChecksum);
    }
}
