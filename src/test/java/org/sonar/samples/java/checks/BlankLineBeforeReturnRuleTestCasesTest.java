package org.sonar.samples.java.checks;

import org.junit.jupiter.api.Test;
import org.sonar.java.checks.verifier.CheckVerifier;

public class BlankLineBeforeReturnRuleTestCasesTest {

    @Test
    void test() {
        CheckVerifier.newVerifier()
                .onFile("src/test/files/BlankLineBeforeReturnRuleTestCases.java")
                .withCheck(new BlankLineBeforeReturnRule())
                .verifyIssues();
    }
}
