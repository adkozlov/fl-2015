package ru.spbau.kozlov.slr.gramar;

import ru.spbau.kozlov.slr.gramar.model.Production;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author adkozlov
 */
public class Grammar {

    private final String grammarName;
    private final String grammarPackage;

    private final int originalStartSymbolCode;
    private final int startSymbolCode;
    private final int nonTerminalsCount;
    private final ArrayList<ArrayList<Production>> productions;

    private GrammarInformation grammarInformation;

    public Grammar(String grammarName, String grammarPackage, int originalStartSymbolCode, int startSymbolCode, int nonTerminalsCount, ArrayList<ArrayList<Production>> productions) {
        this.grammarName = grammarName;
        this.grammarPackage = grammarPackage;
        this.originalStartSymbolCode = originalStartSymbolCode;
        this.startSymbolCode = startSymbolCode;
        this.nonTerminalsCount = nonTerminalsCount;
        this.productions = productions;
    }

    protected Grammar(Grammar grammar) {
        this(grammar.grammarName, grammar.grammarPackage, grammar.originalStartSymbolCode, grammar.startSymbolCode, grammar.nonTerminalsCount, grammar.productions);
    }

    public String getGrammarName() {
        return grammarName;
    }

    public String getGrammarPackage() {
        return grammarPackage;
    }

    public GrammarInformation getGrammarInformation() {
        if (grammarInformation == null) {
            ArrayList<Boolean> nullable = getNullable();
            ArrayList<Set<Integer>> firsts = getFirsts(nullable);
            ArrayList<Set<Integer>> follows = getFollows(nullable, firsts);
            grammarInformation = new GrammarInformation(nullable, firsts, follows);
        }

        return grammarInformation;
    }

    protected int getProductionsCount() {
        return productions.size();
    }

    protected ArrayList<Production> getProductions(int symbol) {
        return productions.get(symbol);
    }

    protected int getOriginalStartSymbolCode() {
        return originalStartSymbolCode;
    }

    protected int getStartSymbolCode() {
        return startSymbolCode;
    }

    protected int getNonTerminalsCount() {
        return nonTerminalsCount;
    }

    protected boolean isTerminal(int symbol) {
        return symbol >= nonTerminalsCount;
    }

    @Override
    public String toString() {
        return "grammar name: " + grammarName + System.lineSeparator() + "grammar package: " + grammarPackage + System.lineSeparator();
    }

    private ArrayList<Boolean> getNullable() {
        ArrayList<Boolean> result = new ArrayList<>();
        for (int i = 0; i < nonTerminalsCount; i++) {
            result.add(false);
        }

        boolean isNotFinished = true;
        while (isNotFinished) {
            isNotFinished = false;

            for (int i = 0; i < nonTerminalsCount; i++) {
                if (result.get(i)) {
                    continue;
                }

                for (Production production : getProductions(i)) {
                    boolean isNullable = true;
                    for (int j : production.getRightSide()) {
                        if (isTerminal(j) || !result.get(j)) {
                            isNullable = false;
                            break;
                        }
                    }

                    if (isNullable) {
                        result.set(i, true);
                        isNotFinished = true;
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<Set<Integer>> getFirsts(ArrayList<Boolean> nullable) {
        ArrayList<Set<Integer>> result = initSets();

        boolean isNotFinished = true;
        while (isNotFinished) {
            isNotFinished = false;

            for (int i = 0; i < nonTerminalsCount; i++) {
                for (Production production : getProductions(i)) {
                    for (int j : production.getRightSide()) {
                        if (isTerminal(j)) {
                            isNotFinished |= result.get(i).add(j);
                            break;
                        } else {
                            for (int k : result.get(j)) {
                                isNotFinished |= result.get(i).add(k);
                            }

                            if (!nullable.get(j)) {
                                break;
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<Set<Integer>> getFollows(ArrayList<Boolean> nullable, ArrayList<Set<Integer>> first) {
        ArrayList<Set<Integer>> result = initSets();
        result.get(startSymbolCode).add(-1); // EOF

        boolean isNotFinished = true;
        while (isNotFinished) {
            isNotFinished = false;

            for (int i = 0; i < nonTerminalsCount; i++) {
                for (Production production : productions.get(i)) {
                    for (int j = 0; j < production.getRightSide().size(); j++) {
                        int jthItem = production.getRightSide().get(j);
                        if (isTerminal(jthItem)) {
                            continue;
                        }

                        boolean isSuffixNullable = true;
                        for (int k = j + 1; k < production.getRightSide().size() && isSuffixNullable; k++) {
                            int kthItem = production.getRightSide().get(k);

                            if (isTerminal(kthItem)) {
                                isNotFinished |= result.get(jthItem).add(kthItem);
                                isSuffixNullable = false;
                            } else {
                                for (int symbol : first.get(kthItem)) {
                                    isNotFinished |= result.get(jthItem).add(symbol);
                                }

                                if (!nullable.get(kthItem)) {
                                    isSuffixNullable = false;
                                }
                            }
                        }

                        if (isSuffixNullable) {
                            for (int symbol : result.get(i)) {
                                isNotFinished |= result.get(jthItem).add(symbol);
                            }
                        }
                    }
                }
            }
        }

        return result;
    }

    private ArrayList<Set<Integer>> initSets() {
        ArrayList<Set<Integer>> result = new ArrayList<>();
        for (int i = 0; i < nonTerminalsCount; i++) {
            result.add(new HashSet<>());
        }

        return result;
    }
}
