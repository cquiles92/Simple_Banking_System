import java.util.Scanner;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        Integer[] arr = {scanner.nextInt(), scanner.nextInt(), scanner.nextInt()};
        // write your code here
        List<Integer> arrayAsList = Arrays.stream(arr).collect(Collectors.toList());

        System.out.println(arrayAsList.get(0));
    }
}