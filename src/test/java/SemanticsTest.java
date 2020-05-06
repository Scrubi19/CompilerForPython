import IdentifierTable.Table;
import Lexer.Lexer;
import Parser.Parser;
import Semantics.SemanticAnalysis;
import org.junit.Test;
import Semantics.SemanticsExceptions;
import Parser.ParserExceptions;
import java.io.IOException;

public class SemanticsTest {
    /**
     * <file>
     *    var = "example"
     *    variable = 5
     *    result = var + variable
     * </>
     * @throws SemanticsExceptions
     */
    @Test(expected = SemanticsExceptions.class)
    public void parseNumAddStr() throws IOException, SemanticsExceptions, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/idTestSema.py");
        Parser.start();
        Table.tableInitialization(Parser.root);
        SemanticAnalysis Semantic = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
        Semantic.start();
    }
    /**
     * <file>
     *    var = "example"
     *    variable = 5.0
     *    result = var + variable
     * </>
     * @throws SemanticsExceptions
     */
    @Test(expected = SemanticsExceptions.class)
    public void parseCaseFloatAddStr() throws IOException, SemanticsExceptions, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/idTestSemaFloat.py");
        Parser.start();
        Table.tableInitialization(Parser.root);
        SemanticAnalysis Semantic = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
        Semantic.start();
    }
    /**
     * <file>
     *    result = 5 + "str"
     * </>
     * @throws SemanticsExceptions
     */
    @Test(expected = SemanticsExceptions.class)
    public void parseCaseNumAddStrWithoutID() throws IOException, SemanticsExceptions, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/idTestSema1.py");
        Parser.start();
        Table.tableInitialization(Parser.root);
        SemanticAnalysis Semantic = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
        Semantic.start();
    }
    /**
     * <file>
     *    result = 5.0 + "str"
     * </>
     * @throws SemanticsExceptions
     */
    @Test(expected = SemanticsExceptions.class)
    public void parseCaseFloatAddStrWithoutID() throws IOException, SemanticsExceptions, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/idTestSema2.py");
        Parser.start();
        Table.tableInitialization(Parser.root);
        SemanticAnalysis Semantic = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
        Semantic.start();
    }
}