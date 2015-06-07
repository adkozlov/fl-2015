package ru.spbau.kozlov.slr.parser;

import ru.spbau.kozlov.slr.gramar.EnrichedGrammar;
import ru.spbau.kozlov.slr.gramar.Grammar;
import ru.spbau.kozlov.slr.gramar.model.Attribute;
import ru.spbau.kozlov.slr.gramar.model.Production;
import ru.spbau.kozlov.slr.parser.exceptions.UnexpectedEOFException;
import ru.spbau.kozlov.slr.parser.exceptions.UnexpectedEOLException;
import ru.spbau.kozlov.slr.parser.exceptions.UnexpectedTokenException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author adkozlov
 */
public class GrammarParser {

    private final Path path;

    private final Map<String, Integer> symbolCodes;
    private final ArrayList<String> symbolNames;
    private final ArrayList<ArrayList<Attribute>> symbolAttributes;
    private final ArrayList<ArrayList<Production>> symbolProductions;

    private Grammar grammar;
    private EnrichedGrammar enrichedGrammar;

    public GrammarParser(Path path) throws IOException {
        this.path = path;
        symbolCodes = new HashMap<>();
        symbolNames = new ArrayList<>();
        symbolAttributes = new ArrayList<>();
        symbolProductions = new ArrayList<>();
    }

    public Grammar getGrammar() throws UnexpectedTokenException, UnexpectedEOLException, IOException, UnexpectedEOFException {
        if (grammar == null) {
            try (GrammarTokenizer grammarTokenizer = new GrammarTokenizer(path)) {
                grammar = parseGrammar(grammarTokenizer);
            }
        }
        return grammar;
    }

    public EnrichedGrammar getEnrichedGrammar() throws UnexpectedEOLException, UnexpectedTokenException, UnexpectedEOFException, IOException {
        if (enrichedGrammar == null) {
            Grammar grammar = getGrammar();
            enrichedGrammar = new EnrichedGrammar(grammar, symbolNames, symbolAttributes);
        }
        return enrichedGrammar;
    }

    private Grammar parseGrammar(GrammarTokenizer grammarTokenizer) throws UnexpectedTokenException, UnexpectedEOLException, IOException, UnexpectedEOFException {
        String grammarName = readProperty(grammarTokenizer, "grammar"); // name
        String grammarPackage = readProperty(grammarTokenizer, "package"); // package

        readGrammarItems(grammarTokenizer); // non-terminals

        int startSymbolCode = symbolNames.size();
        addGrammarItem("!start", new ArrayList<>());

        int nonTerminalsCount = symbolNames.size();
        readGrammarItems(grammarTokenizer); // terminals

        for (int i = 0; i < nonTerminalsCount; i++) {
            symbolProductions.add(new ArrayList<>());
        }
        readProductions(grammarTokenizer); // productions

        // TODO check last production
        String startSymbolName = readProperty(grammarTokenizer, "start"); // start symbol
        int originalStartSymbolCode = symbolCodes.get(startSymbolName);
        symbolProductions.get(startSymbolCode).add(
                new Production(Collections.singletonList(originalStartSymbolCode), new ArrayList<>()));

        return new Grammar(grammarName, grammarPackage, originalStartSymbolCode, startSymbolCode, nonTerminalsCount, symbolProductions);
    }

    private static String readProperty(GrammarTokenizer grammarTokenizer, String propertyName) throws IOException, UnexpectedEOFException, UnexpectedEOLException, UnexpectedTokenException {
        grammarTokenizer.nextLines();
        grammarTokenizer.nextToken(propertyName);
        return grammarTokenizer.nextToken();
    }

    private void readGrammarItems(GrammarTokenizer grammarTokenizer) throws IOException, UnexpectedEOFException, UnexpectedEOLException, UnexpectedTokenException {
        grammarTokenizer.nextLines();

        while (grammarTokenizer.ready()) {
            symbolCodes.put(grammarTokenizer.nextToken(), symbolNames.size());
            addGrammarItem(grammarTokenizer.nextToken(), readAttributes(grammarTokenizer));

            grammarTokenizer.nextLine();
        }
    }

    private void addGrammarItem(String name, ArrayList<Attribute> attributes) {
        symbolNames.add(name);
        symbolAttributes.add(attributes);
    }

    private static ArrayList<Attribute> readAttributes(GrammarTokenizer grammarTokenizer) throws UnexpectedTokenException, UnexpectedEOLException {
        ArrayList<Attribute> result = new ArrayList<>();
        if (grammarTokenizer.ready()) {
            grammarTokenizer.nextToken("{");

            String token;
            do {
                result.add(new Attribute(grammarTokenizer.nextToken(), grammarTokenizer.nextToken()));
                token = grammarTokenizer.nextToken(",", "}");
            } while (!token.equals("}"));
        }
        return result;
    }

    private void readProductions(GrammarTokenizer grammarTokenizer) throws UnexpectedEOLException, UnexpectedTokenException, IOException, UnexpectedEOFException {
        grammarTokenizer.nextLines();

        while (grammarTokenizer.ready()) {
            Integer leftSide = symbolCodes.get(grammarTokenizer.nextToken());
            grammarTokenizer.nextToken("->");

            ArrayList<Integer> rightSide = new ArrayList<>();
            String token = "";
            while (grammarTokenizer.ready() && !(token = grammarTokenizer.nextToken()).equals("{")) {
                rightSide.add(symbolCodes.get(token));
            }

            ArrayList<String> actions = new ArrayList<>();
            if (token.equals("{")) {
                String line;
                while (!(line = grammarTokenizer.nextLine().trim()).equals("}")) {
                    actions.add(line);
                }
            }
            grammarTokenizer.nextLine();

            symbolProductions.get(leftSide).add(new Production(rightSide, actions));
        }
    }
}
