package ru.spbau.kozlov.slr.parser.exceptions;

import java.util.Set;

/**
 * @author adkozlov
 */
public class UnexpectedTokenException extends AbstractParserException {

    private final Set<String> expectedTokens;
    private final String actualToken;

    public UnexpectedTokenException(int lineNumber, Set<String> expectedTokens, String actualToken) {
        super(String.format("Unexpected token, line: %d, expected: %s, actual: %s", lineNumber, expectedTokens, actualToken), lineNumber);
        this.expectedTokens = expectedTokens;
        this.actualToken = actualToken;
    }

    public Set<String> getExpectedTokens() {
        return expectedTokens;
    }

    public String getActualToken() {
        return actualToken;
    }
}
