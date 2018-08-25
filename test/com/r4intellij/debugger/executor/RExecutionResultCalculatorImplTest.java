package com.r4intellij.debugger.executor;

import com.r4intellij.debugger.data.RCommands;
import com.r4intellij.debugger.data.RLanguageConstants;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;

import static com.r4intellij.debugger.data.RCommands.EXECUTE_AND_STEP_COMMAND;
import static com.r4intellij.debugger.data.RFunctionConstants.SERVICE_ENTER_FUNCTION_SUFFIX;
import static com.r4intellij.debugger.data.RFunctionConstants.SERVICE_FUNCTION_PREFIX;
import static com.r4intellij.debugger.data.RLanguageConstants.LINE_SEPARATOR;
import static com.r4intellij.debugger.data.RResponseConstants.*;
import static com.r4intellij.debugger.executor.RExecutionResultType.*;
import static org.junit.Assert.*;

public class RExecutionResultCalculatorImplTest {

    @NotNull
    private static final String JETBRAINS_THER_X_ENTER = SERVICE_FUNCTION_PREFIX + "x" + SERVICE_ENTER_FUNCTION_SUFFIX;

    @NotNull
    private static final RExecutionResultCalculatorImpl CALCULATOR = new RExecutionResultCalculatorImpl();


    @Test
    public void completePlus() {
        assertTrue(CALCULATOR.isComplete("x <- function() {" + LINE_SEPARATOR + PLUS_AND_SPACE));
    }


    @Test
    public void completeBrowser() {
        assertTrue(CALCULATOR.isComplete("ls()" + LINE_SEPARATOR + "[1] \"x\"" + LINE_SEPARATOR + BROWSE_PREFIX + "1" + BROWSE_SUFFIX));
    }


    @Test
    public void completeIncomplete() {
        assertFalse(CALCULATOR.isComplete("ls()" + LINE_SEPARATOR + "[1] \"x\"" + LINE_SEPARATOR + BROWSE_PREFIX));
    }


    @Test
    public void calculatePlus() {
        check(
                "x <- function() {",
                "",
                PLUS_AND_SPACE,
                PLUS,
                ""
        );
    }


    @Test
    public void calculateJustBrowse() {
        check(
                RCommands.debugCommand("x"),
                "",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                EMPTY,
                ""
        );
    }


    @Test(expected = IllegalArgumentException.class)
    public void calculateIncomplete() {
        check(
                "ls()",
                "[1] \"x\"",
                BROWSE_PREFIX,
                RESPONSE,
                "[1] \"x\""
        );
    }


    @Test
    public void calculateDebuggingIn() {
        check(
                "x()",
                DEBUGGING_IN_PREFIX + "x()" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "{" + LINE_SEPARATOR +
                        "    .doTrace(" + JETBRAINS_THER_X_ENTER + "(), \"on entry\")" + LINE_SEPARATOR +
                        "    {" + LINE_SEPARATOR +
                        "        print(\"x\")" + LINE_SEPARATOR +
                        "    }" + LINE_SEPARATOR +
                        "}",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                DEBUGGING_IN,
                ""
        );
    }


    @Test
    public void calculateDebugAt() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                DEBUG_AT_LINE_PREFIX + "1: x <- c(1)",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                DEBUG_AT,
                ""
        );
    }


    @Test
    public void calculateUnbraceDebugAt() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                DEBUG_AT_PREFIX + "x <- c(1)",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                DEBUG_AT,
                ""
        );
    }


    @Test
    public void calculateUnbraceDebugAtWithOutput() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                "[1] 1 2 3" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "x <- c(1)",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                DEBUG_AT,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateDebugAtWithOutput() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                "[1] 1 2 3" + LINE_SEPARATOR +
                        DEBUG_AT_LINE_PREFIX + "1: x <- c(1)",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                DEBUG_AT,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateDebugAtFunction() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                DEBUG_AT_LINE_PREFIX + "2: x <- function() {" + LINE_SEPARATOR +
                        "print(\"x\")" + LINE_SEPARATOR +
                        "}",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                DEBUG_AT,
                ""
        );
    }


    @Test
    public void calculateStartTraceBraceTopLevel() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                TRACING_PREFIX + "x() on entry " + LINE_SEPARATOR +
                        "[1] \"x\"" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "{" + LINE_SEPARATOR +
                        "    print(\"x\")" + LINE_SEPARATOR +
                        "}",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                START_TRACE_BRACE,
                ""
        );
    }


    @Test
    public void calculateStartTraceBraceInside() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                TRACING_PREFIX + "f() on entry " + LINE_SEPARATOR +
                        "[1] \"f\"" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "for (i in 1:2) {" + LINE_SEPARATOR +
                        "    print(i)" + LINE_SEPARATOR +
                        "}",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                START_TRACE_BRACE,
                ""
        );
    }


    @Test
    public void calculateStartTraceUnbrace() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                TRACING_PREFIX + "x() on entry " + LINE_SEPARATOR +
                        "[1] \"x\"" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "print(\"x\")",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                START_TRACE_UNBRACE,
                ""
        );
    }


    @Test
    public void calculateContinueTrace() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[1L]], ...)" + LINE_SEPARATOR +
                        DEBUGGING_IN_PREFIX + "FUN(c(-1, 0, 1)[[2L]], ...)" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "{" + LINE_SEPARATOR +
                        "    .doTrace(" + JETBRAINS_THER_X_ENTER + "(), \"on entry\")" + LINE_SEPARATOR +
                        "    {" + LINE_SEPARATOR +
                        "        print(\"x\")" + LINE_SEPARATOR +
                        "    }" + LINE_SEPARATOR +
                        "}",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                CONTINUE_TRACE,
                ""
        );
    }


    @Test
    public void calculateContinueTraceWithOutputBefore() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                "[1] 1 2 3" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[1L]], ...)" + LINE_SEPARATOR +
                        DEBUGGING_IN_PREFIX + "FUN(c(-1, 0, 1)[[2L]], ...)" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "{" + LINE_SEPARATOR +
                        "    .doTrace(" + JETBRAINS_THER_X_ENTER + "(), \"on entry\")" + LINE_SEPARATOR +
                        "    {" + LINE_SEPARATOR +
                        "        print(\"x\")" + LINE_SEPARATOR +
                        "    }" + LINE_SEPARATOR +
                        "}",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                CONTINUE_TRACE,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateContinueTraceWithOutputAfter() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[1L]], ...)" + LINE_SEPARATOR +
                        "[1] 1 2 3" + LINE_SEPARATOR +
                        DEBUGGING_IN_PREFIX + "FUN(c(-1, 0, 1)[[2L]], ...)" + LINE_SEPARATOR +
                        DEBUG_AT_PREFIX + "{" + LINE_SEPARATOR +
                        "    .doTrace(" + JETBRAINS_THER_X_ENTER + "(), \"on entry\")" + LINE_SEPARATOR +
                        "    {" + LINE_SEPARATOR +
                        "        print(\"x\")" + LINE_SEPARATOR +
                        "    }" + LINE_SEPARATOR +
                        "}",
                BROWSE_PREFIX + "3" + BROWSE_SUFFIX,
                CONTINUE_TRACE,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateExitingFrom() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RExecutionResultType.EXITING_FROM,
                ""
        );
    }


    @Test
    public void calculateExitingFromWithOutputBefore() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                "[1] 1 2 3" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RExecutionResultType.EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateExitingFromWithOutputAfter() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        "[1] 1 2 3",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RExecutionResultType.EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateExitingFromWithOutputBeforeAndDebugAt() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                "[1] 1 2 3" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        DEBUG_AT_LINE_PREFIX + "1: x <- c(1)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RExecutionResultType.EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateExitingFromWithOutputAfterAndDebugAt() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        "[1] 1 2 3" + LINE_SEPARATOR +
                        DEBUG_AT_LINE_PREFIX + "1: x <- c(1)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RExecutionResultType.EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateRecursiveExitingFrom() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RECURSIVE_EXITING_FROM,
                ""
        );
    }


    @Test
    public void calculateRecursiveExitingFromWithOutputBefore() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                "[1] 1 2 3" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RECURSIVE_EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateRecursiveExitingFromWithOutputInside() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "foo()" + LINE_SEPARATOR +
                        "[1] 2 3 4 5 6" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "bar()",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RECURSIVE_EXITING_FROM,
                "[1] 2 3 4 5 6"
        );
    }


    @Test
    public void calculateRecursiveExitingFromWithOutputAfter() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        "[1] 1 2 3",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RECURSIVE_EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateRecursiveExitingFromWithOutputBeforeAndDebugAt() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                "[1] 1 2 3" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        DEBUG_AT_LINE_PREFIX + "1: x <- c(1)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RECURSIVE_EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateRecursiveExitingFromWithOutputInsideAndDebugAt() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "foo()" + LINE_SEPARATOR +
                        "[1] 2 3 4 5 6" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "bar()" + LINE_SEPARATOR +
                        DEBUG_AT_LINE_PREFIX + "1: x <- c(1)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RECURSIVE_EXITING_FROM,
                "[1] 2 3 4 5 6"
        );
    }


    @Test
    public void calculateRecursiveExitingFromWithOutputAfterAndDebugAt() {
        check(
                EXECUTE_AND_STEP_COMMAND,
                EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        EXITING_FROM_PREFIX + "FUN(c(-1, 0, 1)[[3L]], ...)" + LINE_SEPARATOR +
                        "[1] 1 2 3" + LINE_SEPARATOR +
                        DEBUG_AT_LINE_PREFIX + "1: x <- c(1)",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RECURSIVE_EXITING_FROM,
                "[1] 1 2 3"
        );
    }


    @Test
    public void calculateOutputAndBrowse() {
        check(
                "ls()",
                "[1] \"x\"",
                BROWSE_PREFIX + "1" + BROWSE_SUFFIX,
                RESPONSE,
                "[1] \"x\""
        );
    }


    private void check(@NotNull final String command,
                       @NotNull final String expectedOutput,
                       @NotNull final String tail,
                       @NotNull final RExecutionResultType expectedType,
                       @NotNull final String expectedResult) {
        final RExecutionResult result = CALCULATOR.calculate(
                calculateFinalCommand(command, expectedOutput, tail),
                ""
        );

        assertEquals(expectedOutput, result.getOutput());
        assertEquals(expectedType, result.getType());
        assertEquals(expectedResult, result.getResultRange().substring(result.getOutput()));
        assertEquals("", result.getError());
    }


    private String calculateFinalCommand(@NotNull final String command,
                                         @NotNull final String expectedOutput,
                                         @NotNull final String tail) {
        final StringBuilder sb = new StringBuilder();

        sb.append(command);
        sb.append(RLanguageConstants.LINE_SEPARATOR);

        if (!expectedOutput.isEmpty()) {
            sb.append(expectedOutput);
            sb.append(RLanguageConstants.LINE_SEPARATOR);
        }

        sb.append(tail);

        return sb.toString();
    }
}
