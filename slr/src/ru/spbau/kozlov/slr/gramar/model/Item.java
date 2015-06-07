package ru.spbau.kozlov.slr.gramar.model;

import ru.spbau.kozlov.slr.gramar.EnrichedGrammar;

import java.util.ArrayList;

/**
 * @author adkozlov
 */
public class Item implements Comparable<Item> {

    private final EnrichedGrammar enrichedGrammar;
    private final int leftSide;
    private final Production production;
    private final int dot;

    public Item(EnrichedGrammar enrichedGrammar, int leftSide, Production production, int dot) {
        this.enrichedGrammar = enrichedGrammar;
        this.leftSide = leftSide;
        this.production = production;
        this.dot = dot;
    }

    public Item(EnrichedGrammar enrichedGrammar, Item item) {
        this(enrichedGrammar, item.leftSide, item.production, item.dot + 1);
    }

    public int getLeftSide() {
        return leftSide;
    }

    public ArrayList<Integer> getRightSide() {
        return production.getRightSide();
    }

    public int getProductionId() {
        return production.getId();
    }

    public int getSymbolAfterDot() {
        return getRightSide().get(dot);
    }

    public boolean dotIsLast() {
        return getRightSide().size() == dot;
    }

    @Override
    public int compareTo(Item o) {
        int result = production.compareTo(o.production);
        return result == 0 ? Integer.compare(dot, o.dot) : result;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(enrichedGrammar.getSymbolName(leftSide));
        stringBuilder.append(" ->");

        ArrayList<Integer> rightSide = production.getRightSide();
        for (int i = 0; i < rightSide.size(); i++) {
            stringBuilder.append(" ");
            appendDot(stringBuilder, i);
            stringBuilder.append(enrichedGrammar.getSymbolName(rightSide.get(i)));
        }
        appendDot(stringBuilder, rightSide.size());

        return stringBuilder.toString();
    }

    private void appendDot(StringBuilder stringBuilder, int i) {
        if (i == dot) {
            stringBuilder.append(".");
        }
    }
}
