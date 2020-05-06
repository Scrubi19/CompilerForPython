import Lexer.Lexer;
import Parser.Parser;
import org.junit.Test;
import Parser.ParserExceptions;
import java.io.IOException;

public class ParserTest {
    /**
     * <file>
     *    while a!=0 and b!=0:
     * 	    if a>b
     *        <ERROR>
     * 	    else:
     * 		  b%=a
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseIf() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/ifTest.py");
        Parser.start();
    }
    /**
     * <file>
     *    while a!=0 and <ERROR>
     * 	    if a>b
     * 		  a%=b
     * 	    else:
     * 		  b%=a
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseWhile() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/whileTest.py");
        Parser.start();
    }
    /**
     * <file>
     *     def <ERROR>(text, sub):
     *        return text.find(sub)
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseDef() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/defTest.py");
        Parser.start();
    }
    /**
     * <file>
     *     	for <ERROR> in arr:
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseFor() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/forTest.py");
        Parser.start();
    }
    /**
     * <file>
     *     	else a > b:
     * 		     b%=a
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseElse() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/elseTest.py");
        Parser.start();
    }
    /**
     * <file>
     *     	elif <ERROR>
     * 		   b%=a
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseElif() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/elifTest.py");
        Parser.start();
    }
    /**
     * <file>
     *     	print <ERROR> " ")
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parsePrint() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/printTest.py");
        Parser.start();
    }
    /**
     * <file>
     *     	return <ERROR>
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseReturn() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/returnTest.py");
        Parser.start();
    }
    /**
     * <file>
     *     	a=int(input() <ERROR>
     *      b=input()
     * </>
     * @throws ParserExceptions
     */
    @Test(expected = ParserExceptions.class)
    public void parseID() throws IOException, ParserExceptions {
        Lexer.readText("/src/test/java/testFiles/idTest.py");
        Parser.start();
    }
}