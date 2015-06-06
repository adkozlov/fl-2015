package ru.spbau.kozlov.slr.gramar;

import java.util.ArrayList;
import java.util.Set;

/**
 * @author adkozlov
 */
public class GrammarInformation {

    private final ArrayList<Boolean> nullable;
    private final ArrayList<Set<Integer>> firsts;
    private final ArrayList<Set<Integer>> follows;

    public GrammarInformation(ArrayList<Boolean> nullable, ArrayList<Set<Integer>> firsts, ArrayList<Set<Integer>> follows) {
        this.nullable = nullable;
        this.firsts = firsts;
        this.follows = follows;
    }

    public ArrayList<Boolean> getNullable() {
        return nullable;
    }

    public ArrayList<Set<Integer>> getFirsts() {
        return firsts;
    }

    public ArrayList<Set<Integer>> getFollows() {
        return follows;
    }
}
