package ru.spbau.kozlov.slr.parser.exceptions;

/**
 * @author adkozlov
 */
public class AbstractParserException extends Exception {

    private final int lineNumber;

    public AbstractParserException(String message, int lineNumber) {
        super(message);
        this.lineNumber = lineNumber;
    }

    public int getLineNumber() {
        return lineNumber;
    }
}
