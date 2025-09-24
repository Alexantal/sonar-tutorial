import java.util.List;
import java.util.stream.Stream;

public class BlankLineBeforeReturnRuleTestCases {

    public boolean singleLineMethodWithoutBlankLineReturn() {
        return true; // Compliant
    }

    public boolean singleLineMethodWithBlankLineReturn() {

        return true; // Noncompliant
    }

    public int multiLineMethodWithBlankLineReturn() {
        int a = 1;
        int b = 2;

        return a + b; // Compliant
    }

    public int multiLineMethodWithoutBlankLineReturn() {
        int a = 1;
        int b = 2;
        return a + b; // Noncompliant
    }

    public int lambdaMethodWithoutBlankLineReturn() {
        return List.of(1, 2, 3, 4, 5).stream()
                .map(item -> {
                    result = item * 2;
                    return result;}) // Compliant
                .count();
    }

    public int lambdaMethodWithBlankLineReturn() {
        return List.of(1, 2, 3, 4, 5).stream()
                .map(item -> {
                    result = item * 2;

                    return result;}) // Noncompliant
                .count();
    }

    public int ifElseMethodWithBlankLineReturn() {
        int a = 1;
        int b = 2;

        if (b > a) {
            int c = a + b;

            return c; // Compliant
        } else {
            return b; // Compliant
        }
    }

    public int ifElseMethodWithoutBlankLineReturn() {
        int a = 1;
        int b = 2;

        if (b > a) {
            int c = a + b;
            return c; // Noncompliant
        } else {
            return b; // Compliant
        }
    }
}