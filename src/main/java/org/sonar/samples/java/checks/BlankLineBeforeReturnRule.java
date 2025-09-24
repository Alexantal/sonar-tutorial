package org.sonar.samples.java.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;

/**
 * Custom rule to enforce blank line before return statements
 * Rules:
 * 1. If method has more than one line, blank line before return is mandatory
 * 2. Lambda expressions should NOT have blank line before return
 * 3. If block operator (if-else) has more than one line and contains return,
 *    blank line before return is mandatory
 */
@Rule(key = "BlankLineBeforeReturn")
public class BlankLineBeforeReturnRule extends IssuableSubscriptionVisitor {

    private static final String MESSAGE = "Add a blank line before this return statement.";
    private static final String MESSAGE_LAMBDA = "Remove blank line before return in lambda expression.";

    @Override
    public List<Tree.Kind> nodesToVisit() {
        return Arrays.asList(Tree.Kind.RETURN_STATEMENT);
    }

    @Override
    public void visitNode(Tree tree) {
        ReturnStatementTree returnStatement = (ReturnStatementTree) tree;

        // Get the parent tree to determine context
        Tree parent = getParentBlock(returnStatement);

        if (parent == null) {
            return;
        }

        // Check if we're in a lambda expression
        if (isInLambdaExpression(returnStatement)) {
            checkLambdaReturn(returnStatement);
            return;
        }

        // Check if we're in a method or block
        if (parent.is(Tree.Kind.BLOCK)) {
            BlockTree blockTree = (BlockTree) parent;
            checkReturnInBlock(returnStatement, blockTree);
        }
    }

    /**
     * Checks return statements in lambda expressions
     */
    private void checkLambdaReturn(ReturnStatementTree returnStatement) {
        if (hasBlankLineBeforeReturn(returnStatement)) {
            reportIssue(returnStatement.returnKeyword(), MESSAGE_LAMBDA);
        }
    }

    /**
     * Checks return statements in blocks (methods, if-else, etc.)
     */
    private void checkReturnInBlock(ReturnStatementTree returnStatement, BlockTree blockTree) {
        List<StatementTree> statements = blockTree.body();

        // Find the position of return statement in the block
        int returnIndex = findReturnIndex(statements, returnStatement);
        if (returnIndex == -1) {
            return;
        }

        if (statements.size() == 1) {
            // Single statement block - should NOT have blank line before return
            if (hasBlankLineBeforeReturn(returnStatement)) {
                reportIssue(returnStatement.returnKeyword(), "Remove blank line before return in single statement block.");
                return;
            }
        } else {
            // Multiple statements - if return is not the first, check for blank line
            if (returnIndex > 0) {
                // Check if there's NO blank line before return
                if (!hasBlankLineBeforeReturn(returnStatement)) {
                    reportIssue(returnStatement.returnKeyword(), MESSAGE);
                }
            }
        }
    }

    /**
     * Checks if there's a blank line before the return statement
     */
    private boolean hasBlankLineBeforeReturn(ReturnStatementTree returnStatement) {
        SyntaxToken returnToken = returnStatement.returnKeyword();
        if (returnToken == null) {
            return false;
        }

        // Get the line number of the return statement
        int returnLine = returnToken.range().start().line();

        // Get file content
        String fileContent = context.getFileContent();
        if (fileContent == null) {
            return false;
        }

        String[] lines = fileContent.split("\\r?\\n");

        // Check if there's a blank line before return (line before return should be empty)
        if (returnLine >= 2 && returnLine <= lines.length) {
            String previousLine = lines[returnLine - 2]; // -2 because lines are 0-indexed but line numbers are 1-indexed
            return previousLine.trim().isEmpty();
        }

        return false;
    }

    /**
     * Finds the parent block of a return statement
     */
    private Tree getParentBlock(Tree tree) {
        Tree parent = tree.parent();
        while (parent != null) {
            if (parent.is(Tree.Kind.BLOCK)) {
                return parent;
            }
            parent = parent.parent();
        }
        return null;
    }

    /**
     * Checks if return statement is inside a lambda expression
     */
    private boolean isInLambdaExpression(Tree tree) {
        Tree parent = tree.parent();
        while (parent != null) {
            if (parent.is(Tree.Kind.LAMBDA_EXPRESSION)) {
                return true;
            }
            // Stop searching if we hit a method declaration
            if (parent.is(Tree.Kind.METHOD)) {
                return false;
            }
            parent = parent.parent();
        }
        return false;
    }

    /**
     * Finds the index of return statement in the statements list
     */
    private int findReturnIndex(List<StatementTree> statements, ReturnStatementTree returnStatement) {
        for (int i = 0; i < statements.size(); i++) {
            if (statements.get(i) == returnStatement) {
                return i;
            }
        }
        return -1;
    }
}