import ru.spbau.kozlov.slr.arithmetic.analyzers.ArithmeticExpressionsLexicalAnalyzer;
import ru.spbau.kozlov.slr.arithmetic.parser.ArithmeticExpressionsParser;
import ru.spbau.kozlov.slr.arithmetic.trees.ExpressionParseTree;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * @author adkozlov
 */
public class ArithmeticMain {

    public static void main(String[] args) {
        try (FileInputStream inputStream = new FileInputStream(args[0])) {
            ArithmeticExpressionsLexicalAnalyzer lexicalAnalyzer = new ArithmeticExpressionsLexicalAnalyzer(inputStream);
            ExpressionParseTree tree = ArithmeticExpressionsParser.parseExpression(lexicalAnalyzer);
            System.out.println(tree.value);
        } catch (IOException e) {
            System.err.println(e.getClass() + ": " + e.getMessage());
        } catch (ParseException e) {
            System.err.println(e.getClass() + ": " + e.getMessage() + ", position: " + e.getErrorOffset());
        }
    }
}
