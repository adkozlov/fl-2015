package ru.spbau.kozlov.slr.gramar.exceptions;

/**
 * @author adkozlov
 */
public class ReduceReduceConflictException extends AbstractConflictException {

    public ReduceReduceConflictException() {
        super("reduce/reduce conflict");
    }
}
