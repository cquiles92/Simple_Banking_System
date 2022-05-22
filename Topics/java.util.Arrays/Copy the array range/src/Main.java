import java.util.Scanner;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int[] arr = {scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.nextInt()};
        int[] newArr = Arrays.copyOfRange(arr, scanner.nextInt(), scanner.nextInt());

        System.out.println(Arrays.toString(newArr));
    }
}