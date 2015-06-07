package ru.spbau.kozlov.slr.gramar.exceptions;

/**
 * @author adkozlov
 */
public class ShiftReduceConflictException extends AbstractConflictException {

    public ShiftReduceConflictException() {
        super("shift/reduce conflict");
    }
}
