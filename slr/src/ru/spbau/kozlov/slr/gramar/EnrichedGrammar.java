package ru.spbau.kozlov.slr.gramar;

import ru.spbau.kozlov.slr.gramar.model.Attribute;

import java.util.ArrayList;
import java.util.Map;

/**
 * @author adkozlov
 */
public class EnrichedGrammar extends Grammar {

    private final Map<String, Integer> symbolCodes;
    private final ArrayList<String> symbolNames;
    private final ArrayList<ArrayList<Attribute>> symbolAttributes;

    public EnrichedGrammar(Grammar grammar, Map<String, Integer> symbolCodes, ArrayList<String> symbolNames, ArrayList<ArrayList<Attribute>> symbolAttributes) {
        super(grammar);
        this.symbolCodes = symbolCodes;
        this.symbolNames = symbolNames;
        this.symbolAttributes = symbolAttributes;
    }
}
