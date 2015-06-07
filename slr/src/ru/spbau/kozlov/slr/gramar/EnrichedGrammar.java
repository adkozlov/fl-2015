package ru.spbau.kozlov.slr.gramar;

import ru.spbau.kozlov.slr.gramar.exceptions.ReduceReduceConflictException;
import ru.spbau.kozlov.slr.gramar.exceptions.ShiftReduceConflictException;
import ru.spbau.kozlov.slr.gramar.model.Attribute;
import ru.spbau.kozlov.slr.gramar.model.Production;

import java.util.ArrayList;

/**
 * @author adkozlov
 */
public class EnrichedGrammar extends Grammar {

    private static final String TAB = "\t";

    private final ArrayList<String> symbolNames;
    private final ArrayList<ArrayList<Attribute>> symbolAttributes;

    private Automaton automaton;

    public EnrichedGrammar(Grammar grammar, ArrayList<String> symbolNames, ArrayList<ArrayList<Attribute>> symbolAttributes) {
        super(grammar);
        this.symbolNames = symbolNames;
        this.symbolAttributes = symbolAttributes;
    }

    public Automaton getAutomaton() throws ShiftReduceConflictException, ReduceReduceConflictException {
        if (automaton == null) {
            automaton = Automaton.createAutomaton(this);
        }
        return automaton;
    }

    public String getSymbolName(int i) {
        return symbolNames.get(i);
    }

    public int getSymbolsCount() {
        return symbolNames.size();
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder(super.toString());

        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("non-terminals:");
        stringBuilder.append(System.lineSeparator());
        for (int i = 0; i < getNonTerminalsCount(); i++) {
            stringBuilder.append(grammarItemToString(i));
        }

        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("terminals:");
        stringBuilder.append(System.lineSeparator());
        for (int i = getNonTerminalsCount(); i < getSymbolsCount(); i++) {
            stringBuilder.append(grammarItemToString(i));
        }

        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("productions:");
        stringBuilder.append(System.lineSeparator());
        for (int i = 0; i < getProductionsCount(); i++) {
            stringBuilder.append(grammarItemProductionsToString(i));
        }

        stringBuilder.append(System.lineSeparator());
        stringBuilder.append("start: ");
        stringBuilder.append(symbolNames.get(getOriginalStartSymbolCode()));
        stringBuilder.append(System.lineSeparator());

        return stringBuilder.toString();
    }

    private String grammarItemToString(int i) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append(symbolNames.get(i));
        stringBuilder.append("{");
        stringBuilder.append(System.lineSeparator());
        symbolAttributes.get(i).stream()
                .forEach(attribute -> stringBuilder.append(
                        String.format("%s%s %s%s", TAB, attribute.getType(), attribute.getName(), System.lineSeparator())));
        stringBuilder.append("}");
        stringBuilder.append(System.lineSeparator());

        return stringBuilder.toString();
    }

    private String grammarItemProductionsToString(int i) {
        StringBuilder stringBuilder = new StringBuilder();

        String symbolName = symbolNames.get(i);
        for (Production production : getProductions(i)) {
            stringBuilder.append(symbolName);
            stringBuilder.append(" -> ");
            production.getRightSide().stream()
                    .forEach(j -> stringBuilder.append(String.format("%s ", symbolNames.get(j))));
            stringBuilder.append("{");
            stringBuilder.append(System.lineSeparator());
            production.getActions().stream()
                    .forEach(action -> stringBuilder.append(
                            String.format("%s%s%s", TAB, action, System.lineSeparator())));

            stringBuilder.append("}");
            stringBuilder.append(System.lineSeparator());
        }

        return stringBuilder.toString();
    }
}
