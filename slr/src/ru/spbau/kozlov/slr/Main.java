package ru.spbau.kozlov.slr;

import ru.spbau.kozlov.slr.gramar.Automaton;
import ru.spbau.kozlov.slr.gramar.EnrichedGrammar;
import ru.spbau.kozlov.slr.gramar.exceptions.AbstractConflictException;
import ru.spbau.kozlov.slr.parser.GrammarParser;
import ru.spbau.kozlov.slr.parser.exceptions.AbstractParserException;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
	    try {
            GrammarParser grammarParser = new GrammarParser(Paths.get("arithmetic.g"));
            EnrichedGrammar enrichedGrammar = grammarParser.getEnrichedGrammar();
            System.out.println(enrichedGrammar);

            Automaton automaton = Automaton.createAutomaton(enrichedGrammar);
        } catch (IOException | AbstractParserException | AbstractConflictException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
