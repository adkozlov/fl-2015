package ru.spbau.kozlov.slr.gramar.model;

import java.util.ArrayList;
import java.util.List;

/**
 * @author adkozlov
 */
public class Production implements Comparable<Production> {

    public static final ArrayList<Production> PRODUCTION_LIST = new ArrayList<>();

    private final int id;
    private final ArrayList<Integer> rightSide;
    private final ArrayList<String> actions;

    public Production(List<Integer> rightSide, ArrayList<String> actions) {
        this.rightSide = new ArrayList<>(rightSide);
        this.actions = actions;

        id = PRODUCTION_LIST.size();
        PRODUCTION_LIST.add(this);
    }

    public ArrayList<Integer> getRightSide() {
        return rightSide;
    }

    public ArrayList<String> getActions() {
        return actions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null) return false;
        if (!(o instanceof Production)) return false;

        Production that = (Production) o;
        return id == that.id;
    }

    @Override
    public int hashCode() {
        return id;
    }

    @Override
    public int compareTo(Production o) {
        return Integer.compare(id, o.id);
    }
}
