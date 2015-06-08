package ru.spbau.kozlov.slr;

import ru.spbau.kozlov.slr.generator.SourceFilesGenerator;
import ru.spbau.kozlov.slr.gramar.EnrichedGrammar;
import ru.spbau.kozlov.slr.gramar.exceptions.AbstractConflictException;
import ru.spbau.kozlov.slr.parser.GrammarParser;
import ru.spbau.kozlov.slr.parser.exceptions.AbstractParserException;

import java.io.IOException;
import java.nio.file.Paths;

public class Main {

    public static void main(String[] args) {
	    try {
            GrammarParser grammarParser = new GrammarParser(Paths.get(args[0]));
            EnrichedGrammar enrichedGrammar = grammarParser.getEnrichedGrammar();

            SourceFilesGenerator sourceFilesGenerator = new SourceFilesGenerator(enrichedGrammar);
            sourceFilesGenerator.generateSourceFiles();
        } catch (IOException | AbstractParserException | AbstractConflictException e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
        }
    }
}
