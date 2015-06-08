package ru.spbau.kozlov.slr.gramar;

import ru.spbau.kozlov.slr.gramar.exceptions.ReduceReduceConflictException;
import ru.spbau.kozlov.slr.gramar.exceptions.ShiftReduceConflictException;
import ru.spbau.kozlov.slr.gramar.model.Item;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author adkozlov
 */
public class Automaton {

    public static class State {

        private final TreeSet<Item> state;
        private final int[] step;

        private State(TreeSet<Item> items, int size) {
            this.state = items;
            step = new int[size];
            Arrays.fill(step, -1);
        }

        public TreeSet<Item> getItems() {
            return state;
        }

        public int[] getStep() {
            return step;
        }
    }

    private final ArrayList<State> states;
    private final ArrayList<Item[]> reduces = new ArrayList<>();

    public ArrayList<State> getStates() {
        return states;
    }

    public ArrayList<Item[]> getReduces() {
        return reduces;
    }

    private Automaton(ArrayList<State> states, GrammarInformation grammarInformation, int symbolsCount) throws ShiftReduceConflictException, ReduceReduceConflictException {
        this.states = states;

        for (State state : states) {
            Item[] items = new Item[symbolsCount + 1]; // EOF

            for (Item item : state.getItems()) {
                if (item.dotIsLast()) {
                    for (int symbol : grammarInformation.getFollows().get(item.getLeftSide())) {
                        if (symbol == -1) {
                            symbol = symbolsCount;
                        }
                        if (items[symbol] != null && items[symbol].getProductionId() != item.getProductionId()) {
                            throw new ReduceReduceConflictException();
                        }
                        if (symbol != symbolsCount && state.getStep()[symbol] != -1) {
                            throw new ShiftReduceConflictException();
                        }

                        items[symbol] = item;
                    }
                }
            }

            reduces.add(items);
        }
    }

    public static Automaton createAutomaton(EnrichedGrammar enrichedGrammar) throws ShiftReduceConflictException, ReduceReduceConflictException {
        TreeSet<Item> initialState = getInitialState(enrichedGrammar);
        Map<TreeSet<Item>, Integer> statesNumbering = new TreeMap<>((o1, o2) -> {
            int result = Integer.compare(o1.size(), o2.size());
            if (result != 0) {
                return result;
            }

            for (Iterator<Item> it1 = o1.iterator(), it2 = o2.iterator(); it1.hasNext() && it2.hasNext(); ) {
                result = it1.next().compareTo(it2.next());
                if (result != 0) {
                    return result;
                }
            }

            return 0;
        });
        statesNumbering.put(initialState, 0);
        statesNumbering.put(new TreeSet<>(), -1);

        int symbolsCount = enrichedGrammar.getSymbolsCount();
        ArrayList<State> states = new ArrayList<>(Collections.singletonList(new State(initialState, symbolsCount)));

        for (int i = 0; i < states.size(); i++) {
            State state = states.get(i);

            int[] stateStep = state.getStep();
            for (int j = 0; j < stateStep.length; j++) {
                TreeSet<Item> closure = getClosure(enrichedGrammar,
                        performStep(enrichedGrammar, state.getItems(), j));
                Integer index = statesNumbering.get(closure);

                if (index != null) {
                    stateStep[j] = index;
                } else {
                    stateStep[j] = states.size();
                    statesNumbering.put(closure, states.size());
                    states.add(new State(closure, symbolsCount));
                }
            }
        }

        return new Automaton(states, enrichedGrammar.getGrammarInformation(), symbolsCount);
    }

    private static TreeSet<Item> getInitialState(EnrichedGrammar enrichedGrammar) {
        int startSymbolCode = enrichedGrammar.getStartSymbolCode();
        return getClosure(enrichedGrammar, new TreeSet<>(Collections.singletonList(
                new Item(enrichedGrammar,
                        startSymbolCode,
                        enrichedGrammar.getProductions(startSymbolCode).get(0),
                        0))));
    }

    private static TreeSet<Item> performEpsMove(EnrichedGrammar enrichedGrammar, Item item) {
        if (item.dotIsLast()) {
            return new TreeSet<>();
        }

        int symbol = item.getSymbolAfterDot();
        if (enrichedGrammar.isTerminal(symbol)) {
            return new TreeSet<>();
        }

        return new TreeSet<>(enrichedGrammar.getProductions(symbol).stream()
                .map(production -> new Item(enrichedGrammar, symbol, production, 0))
                .collect(Collectors.toList()));
    }

    private static TreeSet<Item> performStep(EnrichedGrammar enrichedGrammar, TreeSet<Item> items, int symbol) {
        return new TreeSet<>(items.stream()
                .filter(item -> !item.dotIsLast())
                .filter(item -> item.getSymbolAfterDot() == symbol)
                .map(item -> new Item(enrichedGrammar, item))
                .collect(Collectors.toList()));
    }

    private static TreeSet<Item> getClosure(EnrichedGrammar enrichedGrammar, TreeSet<Item> set) {
        TreeSet<Item> result = new TreeSet<>(set);

        boolean isItemAdded = true;
        while (isItemAdded) {
            isItemAdded = false;

            for (Item item : result) {
                for (Item addItem : performEpsMove(enrichedGrammar, item)) {
                    isItemAdded |= result.add(addItem);
                }

                if (isItemAdded) {
                    break;
                }
            }
        }

        return result;
    }
}
