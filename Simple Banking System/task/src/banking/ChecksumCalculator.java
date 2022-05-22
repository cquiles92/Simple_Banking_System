package banking;

public class ChecksumCalculator {
    public static int calculateChecksum(String cardNumber) {
        // last digit is not needed to calculate sum
        final int cardLength = 15;
        int[] digits = new int[cardLength];
        int total = 0;

        //transform string into integer array
        for (int i = 0; i < cardLength; i++) {
            digits[i] = Integer.parseInt(cardNumber.substring(i, i + 1));
        }

        //Luhn's Algorithm
        for (int i = 0; i < digits.length; i++) {
            if (i % 2 == 0) {
                digits[i] *= 2;
                if (digits[i] > 9) {
                    digits[i] -= 9;
                }
            }
            total += digits[i];
        }

        total %= 10;
        total = (10 - total) % 10;
        return total;
    }
}
