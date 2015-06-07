package ru.spbau.kozlov.slr.parser.exceptions;

import java.util.Set;

/**
 * @author adkozlov
 */
public class UnexpectedEOLException extends AbstractParserException {

    private static final String MESSAGE_PREFIX_FORMAT = "Unexpected end of line, line: %d";

    private final Set<String> expectedTokens;

    public UnexpectedEOLException(int lineNumber) {
        super(String.format(MESSAGE_PREFIX_FORMAT, lineNumber), lineNumber);
        expectedTokens = null;
    }

    public UnexpectedEOLException(int lineNumber, Set<String> expectedTokens) {
        super(String.format(MESSAGE_PREFIX_FORMAT + ", expected: %s", lineNumber, expectedTokens), lineNumber);
        this.expectedTokens = expectedTokens;
    }

    public Set<String> getExpectedTokens() {
        return expectedTokens;
    }
}
