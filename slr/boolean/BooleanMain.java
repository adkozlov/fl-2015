import ru.spbau.kozlov.slr.bool.analyzers.BoolExpressionLexicalAnalyzer;
import ru.spbau.kozlov.slr.bool.parser.BoolExpressionParser;
import ru.spbau.kozlov.slr.bool.trees.ExpressionParseTree;

import java.io.FileInputStream;
import java.io.IOException;
import java.text.ParseException;

/**
 * @author adkozlov
 */
public class BooleanMain {

    public static void main(String[] args) {
        try (FileInputStream inputStream = new FileInputStream(args[0])) {
            BoolExpressionLexicalAnalyzer lexicalAnalyzer = new BoolExpressionLexicalAnalyzer(inputStream);
            ExpressionParseTree tree = BoolExpressionParser.parseExpression(lexicalAnalyzer);
            System.out.println("" + tree.value);
        } catch (IOException e) {
            System.err.println(e.getClass() + ": " + e.getMessage());
        } catch (ParseException e) {
            System.err.println(e.getClass() + ": " + e.getMessage() + ", position: " + e.getErrorOffset());
        }
    }
}
