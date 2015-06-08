package ru.spbau.kozlov.slr.generator;

import ru.spbau.kozlov.slr.gramar.Automaton;
import ru.spbau.kozlov.slr.gramar.EnrichedGrammar;
import ru.spbau.kozlov.slr.gramar.exceptions.ReduceReduceConflictException;
import ru.spbau.kozlov.slr.gramar.exceptions.ShiftReduceConflictException;
import ru.spbau.kozlov.slr.gramar.model.Attribute;
import ru.spbau.kozlov.slr.gramar.model.Item;
import ru.spbau.kozlov.slr.gramar.model.Production;

import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * @author adkozlov
 */
public class SourceFilesGenerator {

    private static class IndentWriter implements Closeable {

        private static final String JAVA_EXTENSION = ".java";

        private final BufferedWriter bufferedWriter;
        private int indent = 0;
        private String tab = "\t";

        public IndentWriter(String directoryName, String fileName) throws IOException {
            bufferedWriter = Files.newBufferedWriter(Paths.get(directoryName + File.separatorChar + fileName + JAVA_EXTENSION));
        }

        public String getTab() {
            return tab;
        }

        public void setTab(String tab) {
            this.tab = tab;
        }

        public void printlnLeftBrace() throws IOException {
            bufferedWriter.write(" {");
            println();
            indent++;
        }

        public void printlnRightBrace() throws IOException {
            if (indent == 0) {
                throw new RuntimeException("Indent cannot be decreased");
            }
            indent--;
            println("}");
        }

        public void printWithoutIndent(String string) throws IOException {
            bufferedWriter.write(string);
        }

        public void print(String string) throws IOException {
            printIndent();
            printWithoutIndent(string);
        }

        public void println() throws IOException {
            bufferedWriter.newLine();
        }

        public void println(String string) throws IOException {
            print(string);
            println();
        }

        public void printfWithoutIndent(String format, Object... args) throws IOException {
            printWithoutIndent(String.format(format, args));
        }

        public void printf(String format, Object... args) throws IOException {
            printIndent();
            printfWithoutIndent(String.format(format, args));
        }

        @Override
        public void close() throws IOException {
            bufferedWriter.close();
        }

        private void printIndent() throws IOException {
            for (int i = 0; i < indent; i++) {
                bufferedWriter.write(tab);
            }
        }
    }

    private final EnrichedGrammar grammar;
    private final Automaton automaton;
    private final String directoryName;

    public SourceFilesGenerator(EnrichedGrammar grammar) throws ShiftReduceConflictException, ReduceReduceConflictException {
        this.grammar = grammar;
        automaton = grammar.getAutomaton();
        directoryName = grammar.getGrammarPackage().replace(".", File.separator);
    }

    public void generateSourceFiles() throws IOException {
        generateTokenSourceFiles();
        generateLexicalAnalyzerSourceFiles();
        generateParseTreesSourceFiles();
        generateStatesSourceFiles();
        generateParserSourceFile();
    }

    private IndentWriter getIndentWriter(String subPackage, String fileName, String suffix) throws IOException {
        String newDirectoryName = subPackage != null && !subPackage.isEmpty() ? directoryName + File.separatorChar + subPackage : directoryName;
        new File(newDirectoryName).mkdirs();
        return new IndentWriter(newDirectoryName, suffix != null ? fileName + suffix : fileName);
    }

    private void printPackage(IndentWriter indentWriter, String subPackage) throws IOException {
        indentWriter.printf("package %s", grammar.getGrammarPackage());
        if (subPackage != null && !subPackage.isEmpty()) {
            indentWriter.printfWithoutIndent(".%s", subPackage);
        }
        indentWriter.println(";");
        indentWriter.println();
    }

    private void printAttributeFields(IndentWriter indentWriter, ArrayList<Attribute> attributes, boolean makeFinal) throws IOException {
        for (Attribute attribute : attributes) {
            indentWriter.printf("public%s %s %s;", makeFinal ? " final" : "", attribute.getType(), attribute.getName());
            indentWriter.println();
        }
        if (!attributes.isEmpty()) {
            indentWriter.println();
        }
    }

    private void printReduction(IndentWriter indentWriter, Item reduction) throws IOException {
        String grammarName = grammar.getGrammarName();

        ArrayList<Integer> rightSide = reduction.getRightSide();
        for (int i = rightSide.size() - 1; i >= 0; i--) {
            int symbol = rightSide.get(i);
            String type = grammar.isTerminal(symbol) ? "Token" : "ParseTree";
            String symbolName = grammar.getSymbolName(symbol);
            indentWriter.printf("%s%s arg%d = %sParser.extract%s(stack, lexicalAnalyzer);", symbolName, type, i, grammarName, symbolName);
            indentWriter.println();
        }
        if (!rightSide.isEmpty()) {
            indentWriter.println();
        }

        int leftSide = reduction.getLeftSide();
        indentWriter.printf("Abstract%sParseTree parseTree = new %sParseTree(", grammarName, grammar.getSymbolName(leftSide));
        for (String action : reduction.getProductionActions()) {
            indentWriter.printfWithoutIndent("%s, ", action.replace("$", "arg"));
        }
        for (int i = 0; i < rightSide.size(); i++) {
            if (grammar.isTerminal(rightSide.get(i))) {
                indentWriter.printfWithoutIndent("new Terminal%sParseTree(arg%d)", grammarName, i);
            } else {
                indentWriter.printfWithoutIndent("arg%d", i);
            }

            if (i != rightSide.size() - 1) {
                indentWriter.printWithoutIndent(", ");
            }
        }
        indentWriter.printWithoutIndent(");");
        indentWriter.println();

        indentWriter.printf("stack.add(new %sPair(%sParser.getState(%sParser.getState(stack).next(%d), lexicalAnalyzer), parseTree));", grammarName, grammarName, grammarName, leftSide);
        indentWriter.println();
    }

    private Production getStartProduction() {
        return grammar.getProductions(grammar.getStartSymbolCode()).get(0);
    }

    private void generateTokenSourceFiles() throws IOException {
        String grammarName = grammar.getGrammarName();
        String subPackage = "tokens";
        String suffix = "Token";
        try (IndentWriter indentWriter = getIndentWriter(subPackage, "Abstract" + grammarName, suffix)) {
            printPackage(indentWriter, subPackage);

            indentWriter.printf("public abstract class Abstract%sToken", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            indentWriter.println("private final String symbol;");
            indentWriter.println();

            indentWriter.printf("public Abstract%sToken(String symbol)", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("this.symbol = symbol;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.print("public String getSymbol()");
            indentWriter.printlnLeftBrace();
            indentWriter.println("return symbol;");
            indentWriter.printlnRightBrace();
            indentWriter.printlnRightBrace();
        }

        try (IndentWriter indentWriter = getIndentWriter(subPackage, "EOF" + grammarName, suffix)) {
            printPackage(indentWriter, subPackage);

            indentWriter.printf("public class EOF%sToken extends Abstract%sToken", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            indentWriter.printf("private static final EOF%sToken instance = new EOF%sToken();", grammarName, grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("private EOF%sToken()", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("super(\"$\");");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public static EOF%sToken getInstance()", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("return instance;");
            indentWriter.printlnRightBrace();

            indentWriter.printlnRightBrace();
        }

        for (int i = grammar.getNonTerminalsCount(); i < grammar.getSymbolsCount(); i++) {
            String symbolName = grammar.getSymbolName(i);
            try (IndentWriter indentWriter = getIndentWriter(subPackage, symbolName, suffix)) {
                printPackage(indentWriter, subPackage);

                indentWriter.printf("public class %sToken extends Abstract%sToken", symbolName, grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.println();

                ArrayList<Attribute> attributes = grammar.getSymbolAttributes().get(i);
                printAttributeFields(indentWriter, attributes, false);

                indentWriter.printf("public %sToken()", symbolName);
                indentWriter.printlnLeftBrace();
                indentWriter.printf("super(\"%s\");", grammar.getToken(symbolName));
                indentWriter.println();
                indentWriter.printlnRightBrace();
                indentWriter.printlnRightBrace();
            }
        }
    }

    private void generateLexicalAnalyzerSourceFiles() throws IOException {
        String grammarName = grammar.getGrammarName();
        String subPackage = "analyzers";
        String suffix = "LexicalAnalyzer";
        String grammarPackage = grammar.getGrammarPackage();
        try (IndentWriter indentWriter = getIndentWriter(subPackage, "I" + grammarName, suffix)) {
            printPackage(indentWriter, subPackage);

            indentWriter.println("import java.text.ParseException;");
            indentWriter.printf("import %s.tokens.Abstract%sToken;", grammarPackage, grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public interface I%sLexicalAnalyzer", grammarName);
            indentWriter.printlnLeftBrace();

            indentWriter.println("boolean isEmpty();");
            indentWriter.println("int getCurrentPosition();");
            indentWriter.printf("Abstract%sToken getCurrentToken();", grammarName);
            indentWriter.println();
            indentWriter.println("void nextToken() throws ParseException;");

            indentWriter.printlnRightBrace();
        }

        try (IndentWriter indentWriter = getIndentWriter(subPackage, grammarName, suffix)) {
            printPackage(indentWriter, subPackage);

            indentWriter.println("import java.io.IOException;");
            indentWriter.println("import java.io.InputStream;");
            indentWriter.println("import java.text.ParseException;");
            indentWriter.printf("import %s.tokens.*;", grammarPackage);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public class %sLexicalAnalyzer implements I%sLexicalAnalyzer", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            indentWriter.println("private InputStream inputStream;");
            indentWriter.println("private String buffer = \"\";");
            indentWriter.println("private int currentCharacter;");
            indentWriter.println("private int currentPosition = 0;");
            indentWriter.printf("private Abstract%sToken currentToken;", grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public %sLexicalAnalyzer(InputStream inputStream) throws ParseException", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("this.inputStream = inputStream;");
            indentWriter.println("nextToken();");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.print("private void nextCharacter() throws ParseException");
            indentWriter.printlnLeftBrace();
            indentWriter.print("try");
            indentWriter.printlnLeftBrace();
            indentWriter.println("currentPosition++;");
            indentWriter.println("currentCharacter = inputStream.read();");
            indentWriter.print("if (currentCharacter != -1 && !isBlank(currentCharacter))");
            indentWriter.printlnLeftBrace();
            indentWriter.println("buffer += (char) currentCharacter;");
            indentWriter.printlnRightBrace();
            indentWriter.printlnRightBrace();
            indentWriter.print("catch (IOException e)");
            indentWriter.printlnLeftBrace();
            indentWriter.println("throw new ParseException(e.getMessage(), currentPosition);");
            indentWriter.printlnRightBrace();
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.print("private boolean isBlank(int character)");
            indentWriter.printlnLeftBrace();
            indentWriter.println("return character == ' ' || character == '\\n' || character == '\\t' || character == '\\r';");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.println("@Override");
            indentWriter.print("public boolean isEmpty()");
            indentWriter.printlnLeftBrace();
            indentWriter.printf("return currentCharacter == -1 && currentToken == EOF%sToken.getInstance();", grammarName);
            indentWriter.println();
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.println("@Override");
            indentWriter.print("public int getCurrentPosition()");
            indentWriter.printlnLeftBrace();
            indentWriter.println("return currentPosition;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.println("@Override");
            indentWriter.printf("public Abstract%sToken getCurrentToken()", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("return currentToken;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.println("@Override");
            indentWriter.print("public void nextToken() throws ParseException");
            indentWriter.printlnLeftBrace();
            indentWriter.print("while (isBlank(currentCharacter))");
            indentWriter.printlnLeftBrace();
            indentWriter.println("nextCharacter();");
            indentWriter.printlnRightBrace();
            indentWriter.print("while (currentCharacter != -1 && !isBlank(currentCharacter))");
            indentWriter.printlnLeftBrace();
            indentWriter.println("nextCharacter();");
            indentWriter.printlnRightBrace();

            boolean isFirst = true;
            for (int i = grammar.getNonTerminalsCount(); i < grammar.getSymbolsCount(); i++) {
                if (isFirst) {
                    indentWriter.printIndent();
                    isFirst = false;
                } else {
                    indentWriter.print("else ");
                }

                String symbolName = grammar.getSymbolName(i);
                indentWriter.printfWithoutIndent("if (buffer.equals(\"%s\"))", grammar.getToken(symbolName));
                indentWriter.printlnLeftBrace();
                indentWriter.printf("currentToken = new %sToken();", symbolName);
                indentWriter.println();
                indentWriter.printlnRightBrace();
            }
            indentWriter.print("else if (!buffer.equals(\"\"))");
            indentWriter.printlnLeftBrace();
            indentWriter.println("throw new ParseException(String.format(\"Illegal character '%c' at position %d\", currentCharacter, currentPosition), currentPosition);");
            indentWriter.printlnRightBrace();
            indentWriter.print("else if (currentCharacter == -1)");
            indentWriter.printlnLeftBrace();
            indentWriter.printf("currentToken = EOF%sToken.getInstance();", grammarName);
            indentWriter.println();
            indentWriter.printlnRightBrace();
            indentWriter.println("buffer = \"\";");
            indentWriter.printlnRightBrace();

            indentWriter.printlnRightBrace();
        }
    }

    private void generateParseTreesSourceFiles() throws IOException {
        String grammarName = grammar.getGrammarName();
        String subPackage = "trees";
        String suffix = "ParseTree";
        try (IndentWriter indentWriter = getIndentWriter(subPackage, "Abstract" + grammarName, suffix)) {
            printPackage(indentWriter, subPackage);

            indentWriter.println("import java.util.List;");
            indentWriter.println("import java.util.Arrays;");
            indentWriter.println();

            indentWriter.printf("public abstract class Abstract%sParseTree", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            indentWriter.printf("private final List<Abstract%sParseTree> children;", grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("protected Abstract%sParseTree(List<Abstract%sParseTree> children)", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("this.children = children;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public Abstract%sParseTree(Abstract%sParseTree... children)", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("this(Arrays.asList(children));");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public List<Abstract%sParseTree> getChildren()", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("return children;");
            indentWriter.printlnRightBrace();

            indentWriter.printlnRightBrace();
        }

        try (IndentWriter indentWriter = getIndentWriter(subPackage, "Terminal" + grammarName, suffix)) {
            printPackage(indentWriter, subPackage);

            indentWriter.println("import java.util.ArrayList;");
            indentWriter.printf("import %s.tokens.Abstract%sToken;", grammar.getGrammarPackage(), grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public class Terminal%sParseTree extends Abstract%sParseTree", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            indentWriter.printf("private final Abstract%sToken token;", grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public Terminal%sParseTree(Abstract%sToken token)", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("super(new ArrayList<>());");
            indentWriter.println("this.token = token;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public Abstract%sToken getToken()", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("return token;");
            indentWriter.printlnRightBrace();

            indentWriter.printlnRightBrace();
        }

        for (int i = 0; i < grammar.getNonTerminalsCount() - 1; i++) {
            String symbolName = grammar.getSymbolName(i);
            try (IndentWriter indentWriter = getIndentWriter(subPackage, symbolName, suffix)) {
                printPackage(indentWriter, subPackage);

                indentWriter.printf("public class %sParseTree extends Abstract%sParseTree", symbolName, grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.println();

                ArrayList<Attribute> attributes = grammar.getSymbolAttributes().get(i);
                printAttributeFields(indentWriter, attributes, true);

                indentWriter.printf("public %sParseTree(", symbolName);
                for (Attribute attribute : attributes) {
                    indentWriter.printfWithoutIndent("%s %s, ", attribute.getType(), attribute.getName());
                }
                indentWriter.printfWithoutIndent("Abstract%sParseTree... children)", grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.println("super(children);");
                for (Attribute attribute : attributes) {
                    String attributeName = attribute.getName();
                    indentWriter.printf("this.%s = %s;", attributeName, attributeName);
                    indentWriter.println();
                }
                indentWriter.printlnRightBrace();
                indentWriter.printlnRightBrace();
            }
        }
    }

    private void generateStatesSourceFiles() throws IOException {
        String grammarName = grammar.getGrammarName();
        String grammarPackage = grammar.getGrammarPackage();
        String subPackage = "states";
        String suffix = "State";
        try (IndentWriter indentWriter = getIndentWriter(subPackage, grammarName, "Pair")) {
            printPackage(indentWriter, subPackage);

            indentWriter.printf("import %s.trees.Abstract%sParseTree;", grammarPackage, grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public class %sPair", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            indentWriter.printf("private final Abstract%sState state;", grammarName);
            indentWriter.println();
            indentWriter.printf("private final Abstract%sParseTree parseTree;", grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public %sPair(Abstract%sState state, Abstract%sParseTree parseTree)", grammarName, grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("this.state = state;");
            indentWriter.println("this.parseTree = parseTree;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public Abstract%sState getState()", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("return state;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public Abstract%sParseTree getParseTree()", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("return parseTree;");
            indentWriter.printlnRightBrace();

            indentWriter.printlnRightBrace();
        }

        try (IndentWriter indentWriter = getIndentWriter(subPackage, "Abstract" + grammarName, suffix)) {
            printPackage(indentWriter, subPackage);

            indentWriter.println("import java.util.Deque;");
            indentWriter.println("import java.text.ParseException;");
            indentWriter.printf("import %s.analyzers.I%sLexicalAnalyzer;", grammarPackage, grammarName);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public abstract class Abstract%sState", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            indentWriter.println("private final int[] next;");
            indentWriter.println();

            indentWriter.printf("public Abstract%sState(int[] next)", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println("this.next = next;");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.print("public int next(int symbol)");
            indentWriter.printlnLeftBrace();
            indentWriter.println("return next[symbol];");
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public abstract boolean accept(Deque<%sPair> stack, I%sLexicalAnalyzer lexicalAnalyzer) throws ParseException;", grammarName, grammarName);
            indentWriter.println();

            indentWriter.printlnRightBrace();
        }

        Production startProduction = getStartProduction();
        Item item = new Item(grammar, grammar.getStartSymbolCode(), startProduction, 1);

        ArrayList<Automaton.State> states = automaton.getStates();
        ArrayList<Item[]> reduces = automaton.getReduces();
        for (int i = 0; i < states.size(); i++) {
            try (IndentWriter indentWriter = getIndentWriter(subPackage, grammarName, suffix + i)) {
                printPackage(indentWriter, subPackage);

                indentWriter.println("import java.util.Deque;");
                indentWriter.println("import java.text.ParseException;");
                indentWriter.printf("import %s.analyzers.I%sLexicalAnalyzer;", grammarPackage, grammarName);
                indentWriter.println();
                indentWriter.printf("import %s.parser.%sParser;", grammarPackage, grammarName);
                indentWriter.println();
                indentWriter.printf("import %s.trees.*;", grammarPackage, grammarName);
                indentWriter.println();
                indentWriter.printf("import %s.tokens.*;", grammarPackage, grammarName);
                indentWriter.println();
                indentWriter.println();

                indentWriter.printf("public class %sState%d extends Abstract%sState", grammarName, i, grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.println();

                Automaton.State state = states.get(i);
                int[] step = state.getStep();
                indentWriter.printf("public %sState%d()", grammarName, i);
                indentWriter.printlnLeftBrace();
                indentWriter.print("super(new int[]{");

                boolean isFirst = true;
                for (int j : step) {
                    if (isFirst) {
                        isFirst = false;
                    } else {
                        indentWriter.printWithoutIndent(", ");
                    }

                    indentWriter.printWithoutIndent(Integer.toString(j));
                }

                indentWriter.printWithoutIndent("});");
                indentWriter.println();
                indentWriter.printlnRightBrace();
                indentWriter.println();

                indentWriter.println("@Override");
                indentWriter.printf("public boolean accept(Deque<%sPair> stack, I%sLexicalAnalyzer lexicalAnalyzer) throws ParseException", grammarName, grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.print("if (lexicalAnalyzer.isEmpty())");
                indentWriter.printlnLeftBrace();

                Item[] items = reduces.get(i);
                if (state.getItems().contains(item)) {
                    indentWriter.println("return true;");
                    indentWriter.printlnRightBrace();
                    indentWriter.print("else");
                    indentWriter.printlnLeftBrace();
                    indentWriter.println("throw new ParseException(\"Unexpected token: \" + lexicalAnalyzer.getCurrentToken(), lexicalAnalyzer.getCurrentPosition());");
                } else {
                    if (items[grammar.getSymbolsCount()] == null) {
                        indentWriter.println("throw new ParseException(\"Unexpected EOF\", lexicalAnalyzer.getCurrentPosition());");
                    } else {
                        printReduction(indentWriter, items[grammar.getSymbolsCount()]);
                        indentWriter.println("return false;");
                    }

                    indentWriter.printlnRightBrace();

                    indentWriter.print("else");
                    indentWriter.printlnLeftBrace();
                    indentWriter.printf("Abstract%sToken token = lexicalAnalyzer.getCurrentToken();", grammarName);
                    indentWriter.println();

                    isFirst = true;
                    boolean needsReturn = false;
                    for (int j = grammar.getNonTerminalsCount(); j < grammar.getSymbolsCount(); j++) {
                        String terminal = grammar.getSymbolName(j);

                        if (step[j] != -1 || items[j] != null) {
                            if (isFirst) {
                                indentWriter.printIndent();
                                isFirst = false;
                            } else {
                                indentWriter.print("else ");
                            }

                            indentWriter.printfWithoutIndent("if (token instanceof %sToken)", terminal);
                            indentWriter.printlnLeftBrace();
                            if (step[j] != -1) {
                                indentWriter.printf("stack.add(new %sPair(new %sState%d(), new Terminal%sParseTree(token)));", grammarName, grammarName, step[j], grammarName);
                                indentWriter.println();
                                indentWriter.println("lexicalAnalyzer.nextToken();");
                            } else {
                                printReduction(indentWriter, items[j]);
                            }
                            indentWriter.printlnRightBrace();
                            needsReturn = true;
                        }
                    }

                    if (needsReturn) {
                        indentWriter.print("else");
                        indentWriter.printlnLeftBrace();
                        indentWriter.println("throw new ParseException(\"Unexpected token: \" + token, lexicalAnalyzer.getCurrentPosition());");
                        indentWriter.printlnRightBrace();

                        indentWriter.println("return false;");
                    }
                }

                indentWriter.printlnRightBrace();
                indentWriter.printlnRightBrace();
                indentWriter.printlnRightBrace();
            }
        }
    }

    private void generateParserSourceFile() throws IOException {
        String grammarName = grammar.getGrammarName();
        String grammarPackage = grammar.getGrammarPackage();
        String subPackage = "parser";
        try (IndentWriter indentWriter = getIndentWriter(subPackage, grammarName, "Parser")) {
            printPackage(indentWriter, subPackage);

            indentWriter.println("import java.util.Deque;");
            indentWriter.println("import java.util.ArrayDeque;");
            indentWriter.println("import java.text.ParseException;");
            indentWriter.printf("import %s.analyzers.I%sLexicalAnalyzer;", grammarPackage, grammarName);
            indentWriter.println();
            indentWriter.printf("import %s.states.*;", grammarPackage);
            indentWriter.println();
            indentWriter.printf("import %s.trees.*;", grammarPackage);
            indentWriter.println();
            indentWriter.printf("import %s.tokens.*;", grammarPackage);
            indentWriter.println();
            indentWriter.println();

            indentWriter.printf("public class %sParser", grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.println();

            String symbolName = grammar.getSymbolName(getStartProduction().getRightSide().get(0));

            indentWriter.printf("public static %sParseTree parse%s(I%sLexicalAnalyzer lexicalAnalyzer) throws ParseException", symbolName, symbolName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.printf("Deque<%sPair> stack = new ArrayDeque<>();", grammarName);
            indentWriter.println();
            indentWriter.print("while (true)");
            indentWriter.printlnLeftBrace();
            indentWriter.print("if (getState(stack).accept(stack, lexicalAnalyzer))");
            indentWriter.printlnLeftBrace();
            indentWriter.printf("return extract%s(stack, lexicalAnalyzer);", symbolName);
            indentWriter.println();
            indentWriter.printlnRightBrace();
            indentWriter.printlnRightBrace();
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public static Abstract%sState getState(Deque<%sPair> stack)", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            indentWriter.printf("return !stack.isEmpty() ? stack.getLast().getState() : new %sState0();", grammarName);
            indentWriter.println();
            indentWriter.printlnRightBrace();
            indentWriter.println();

            indentWriter.printf("public static Abstract%sState getState(int state, I%sLexicalAnalyzer lexicalAnalyzer) throws ParseException", grammarName, grammarName);
            indentWriter.printlnLeftBrace();
            boolean isFirst = true;
            int statesCount = automaton.getStates().size();
            for (int i = 0; i < statesCount; i++) {
                if (isFirst) {
                    indentWriter.printIndent();
                    isFirst = false;
                } else {
                    indentWriter.print("else ");
                }

                indentWriter.printfWithoutIndent("if (state == %d)", i);
                indentWriter.printlnLeftBrace();
                indentWriter.printf("return new %sState%d();", grammarName, i);
                indentWriter.println();
                indentWriter.printlnRightBrace();
            }
            indentWriter.print("else");
            indentWriter.printlnLeftBrace();
            indentWriter.println("throw new ParseException(\"Illegal state number: \" + state, lexicalAnalyzer.getCurrentPosition());");
            indentWriter.printlnRightBrace();
            indentWriter.printlnRightBrace();

            for (int i = 0; i < grammar.getNonTerminalsCount() - 1; i++) {
                indentWriter.println();

                String nonTerminal = grammar.getSymbolName(i);
                indentWriter.printf("public static %sParseTree extract%s(Deque<%sPair> stack, I%sLexicalAnalyzer lexicalAnalyzer) throws ParseException", nonTerminal, nonTerminal, grammarName, grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.printf("Abstract%sParseTree parseTree = stack.pollLast().getParseTree();", grammarName);
                indentWriter.println();
                indentWriter.printf("if (parseTree instanceof %sParseTree)", nonTerminal);
                indentWriter.printlnLeftBrace();
                indentWriter.printf("return (%sParseTree) parseTree;", nonTerminal);
                indentWriter.println();
                indentWriter.printlnRightBrace();
                indentWriter.print("else");
                indentWriter.printlnLeftBrace();
                indentWriter.printf("throw new ParseException(\"Expected non-terminal '%s'\", lexicalAnalyzer.getCurrentPosition());", nonTerminal);
                indentWriter.println();
                indentWriter.printlnRightBrace();
                indentWriter.printlnRightBrace();
            }
            for (int i = grammar.getNonTerminalsCount(); i < grammar.getSymbolsCount(); i++) {
                indentWriter.println();

                String terminal = grammar.getSymbolName(i);
                indentWriter.printf("public static %sToken extract%s(Deque<%sPair> stack, I%sLexicalAnalyzer lexicalAnalyzer) throws ParseException", terminal, terminal, grammarName, grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.printf("Abstract%sParseTree parseTree = stack.pollLast().getParseTree();", grammarName);
                indentWriter.println();
                indentWriter.printf("if (parseTree instanceof Terminal%sParseTree)", grammarName);
                indentWriter.printlnLeftBrace();
                indentWriter.printf("Abstract%sToken token = ((Terminal%sParseTree) parseTree).getToken();", grammarName, grammarName);
                indentWriter.println();
                indentWriter.printf("if (token instanceof %sToken)", terminal);
                indentWriter.printlnLeftBrace();
                indentWriter.printf("return (%sToken) token;", terminal);
                indentWriter.println();
                indentWriter.printlnRightBrace();
                indentWriter.print("else");
                indentWriter.printlnLeftBrace();
                indentWriter.printf("throw new ParseException(\"Token '%s' expected\", lexicalAnalyzer.getCurrentPosition());", terminal);
                indentWriter.println();
                indentWriter.printlnRightBrace();
                indentWriter.printlnRightBrace();
                indentWriter.print("else");
                indentWriter.printlnLeftBrace();
                indentWriter.println("throw new ParseException(\"Non-terminal expected\", lexicalAnalyzer.getCurrentPosition());");
                indentWriter.printlnRightBrace();
                indentWriter.printlnRightBrace();
            }

            indentWriter.printlnRightBrace();
        }
    }
}