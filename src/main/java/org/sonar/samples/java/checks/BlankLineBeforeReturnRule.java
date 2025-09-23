package org.sonar.samples.java.checks;

import java.util.Arrays;
import java.util.List;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.tree.*;
import org.sonar.plugins.java.api.tree.SyntaxToken;

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
        SyntaxToken firstToken = returnStatement.firstToken();
        if (firstToken == null) {
            return;
        }

        int returnLine = firstToken.range().start().line();
        List<String> lines = context.getFileLines();

        // Check if there's a blank line before return in lambda
        if (returnLine > 1 && returnLine <= lines.size() && isBlankLine(lines.get(returnLine - 2))) {
            reportIssue(returnStatement.returnKeyword(), MESSAGE_LAMBDA);
        }
    }

    /**
     * Checks return statements in blocks (methods, if-else, etc.)
     */
    private void checkReturnInBlock(ReturnStatementTree returnStatement, BlockTree blockTree) {
        List<StatementTree> statements = blockTree.body();

        if (statements.size() <= 1) {
            // Single statement blocks don't need blank lines
            return;
        }

        // Find the position of return statement in the block
        int returnIndex = findReturnIndex(statements, returnStatement);
        if (returnIndex == -1 || returnIndex == 0) {
            return;
        }

        // Check if there are multiple lines of code before return
        if (hasMultipleLogicalLines(statements, returnIndex)) {
            SyntaxToken firstToken = returnStatement.firstToken();
            if (firstToken == null || firstToken.range() == null) {
                return;
            }

            int returnLine = firstToken.range().start().line();
            List<String> lines = context.getFileLines();

            // Check if there's a blank line before return
            if (returnLine > 1 && returnLine <= lines.size() && !isBlankLine(lines.get(returnLine - 2))) {
                reportIssue(returnStatement.returnKeyword(), MESSAGE);
            }
        }
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

    /**
     * Checks if there are multiple logical lines before the return statement
     */
    private boolean hasMultipleLogicalLines(List<StatementTree> statements, int returnIndex) {
        if (returnIndex == 0) {
            return false;
        }

        // Count statements before return (excluding empty statements and comments)
        int logicalLines = 0;
        for (int i = 0; i < returnIndex; i++) {
            StatementTree stmt = statements.get(i);
            if (!stmt.is(Tree.Kind.EMPTY_STATEMENT)) {
                logicalLines++;
            }
        }

        return logicalLines > 0;
    }

    /**
     * Checks if a line is blank (contains only whitespace)
     */
    private boolean isBlankLine(String line) {
        return line.trim().isEmpty();
    }
}