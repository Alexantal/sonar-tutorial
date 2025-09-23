import java.util.List;
import java.util.stream.Stream;

public class BlankLineBeforeReturnRuleTestCases {

    public boolean singleLineMethodWithoutBlankLineReturn() { //Compliant
        return true;
    }

    public boolean singleLineMethodWithBlankLineReturn() { //Non-Compliant

        return true;
    }

    public int multiLineMethodWithBlankLineReturn() { //Compliant
        int a = 1;
        int b = 2;

        return a + b;
    }

    public int multiLineMethodWithoutBlankLineReturn() { //Non-Compliant
        int a = 1;
        int b = 2;
        return a + b;
    }

    public int lambdaMethodWithoutBlankLineReturn() { //Compliant
        return List.of(1, 2, 3, 4, 5).stream()
                .map(item -> {
                    result = item * 2;
                    return result;})
                .count();
    }

    public int lambdaMethodWithBlankLineReturn() { //Non-Compliant
        return List.of(1, 2, 3, 4, 5).stream()
                .map(item -> {
                    result = item * 2;

                    return result;})
                .count();
    }

    public int ifElseMethodWithBlankLineReturn() { //Compliant
        int a = 1;
        int b = 2;

        if (a > b) {
            int c = a + b;

            return c;
        } else {
            return b;
        }
    }

    public int ifElseMethodWithoutBlankLineReturn() { //Non-Compliant
        int a = 1;
        int b = 2;

        if (a > b) {
            int c = a + b;
            return c;
        } else {
            return b;
        }
    }
}