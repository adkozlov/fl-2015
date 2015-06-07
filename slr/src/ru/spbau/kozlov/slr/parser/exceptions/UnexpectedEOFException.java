package ru.spbau.kozlov.slr.parser.exceptions;

/**
 * @author adkozlov
 */
public class UnexpectedEOFException extends AbstractParserException {

    public UnexpectedEOFException(int lineNumber) {
        super(String.format("Unexpected EOF, line: %d", lineNumber), lineNumber);
    }
}
