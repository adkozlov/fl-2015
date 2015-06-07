package ru.spbau.kozlov.slr.parser;

import ru.spbau.kozlov.slr.parser.exceptions.UnexpectedEOFException;
import ru.spbau.kozlov.slr.parser.exceptions.UnexpectedEOLException;
import ru.spbau.kozlov.slr.parser.exceptions.UnexpectedTokenException;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.StringTokenizer;

/**
 * @author adkozlov
 */
public class GrammarTokenizer implements Closeable {

    private final BufferedReader bufferedReader;
    private int lineNumber = 0;
    private StringTokenizer stringTokenizer;

    public GrammarTokenizer(Path path) throws IOException {
        bufferedReader = Files.newBufferedReader(path);
    }

    public ArrayList<String> nextLines() throws UnexpectedEOFException, IOException {
        ArrayList<String> result = new ArrayList<>();
        do {
            result.add(nextLine());
        } while (!ready());
        return result;
    }

    public String nextLine() throws IOException, UnexpectedEOFException {
        if (!bufferedReader.ready()) {
            throw new UnexpectedEOFException(lineNumber);
        }

        String result = bufferedReader.readLine();
        lineNumber++;
        stringTokenizer = new StringTokenizer(result);
        return result;
    }

    public boolean ready() {
        return stringTokenizer.hasMoreTokens();
    }

    public String nextToken() throws UnexpectedEOLException {
        if (!ready()) {
            throw new UnexpectedEOLException(lineNumber);
        }

        return stringTokenizer.nextToken();
    }

    public String nextToken(String expectedToken) throws UnexpectedTokenException, UnexpectedEOLException {
        return nextToken(new String[] {expectedToken});
    }

    public String nextToken(String... expectedTokens) throws UnexpectedEOLException, UnexpectedTokenException {
        HashSet<String> expectedTokensSet = new HashSet<>();
        expectedTokensSet.addAll(Arrays.asList(expectedTokens));

        if (!ready()) {
            throw new UnexpectedEOLException(lineNumber, expectedTokensSet);
        }

        String actualToken = stringTokenizer.nextToken();
        if (!expectedTokensSet.contains(actualToken)) {
            throw new UnexpectedTokenException(lineNumber, expectedTokensSet, actualToken);
        }
        return actualToken;
    }

    @Override
    public void close() throws IOException {
        bufferedReader.close();
    }
}
