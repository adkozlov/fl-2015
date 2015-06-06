package ru.spbau.kozlov.slr;

import ru.spbau.kozlov.slr.gramar.Grammar;
import ru.spbau.kozlov.slr.parser.*;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
	    try {
            GrammarParser grammarParser = new GrammarParser(Paths.get("arithmetic.g"));
            Grammar grammar = grammarParser.getGrammar();
        } catch (IOException | AbstractParserException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
