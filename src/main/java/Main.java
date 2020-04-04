import IdentifierTable.Table;
import Lexer.*;
import Parser.Parser;
import Parser.ParserExceptions;
import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ParserExceptions {
        switch (args[0]){
            case ("--dump-tokens"):
//                new LexerThread(args[1]).start();
                Lexer.readText(args[1]);
                Lexer.dumpTokens();

            break;
            case ("--dump-ast"):
                Lexer.readText(args[1]);
                Parser.start();
                Parser.showTree();

                Table.tableInitilization(Parser.root);
                System.out.println("\nIdentifier Table\n"+Table.getIdentifierTable());
                break;

            case (" --dump-asm"):
                System.out.println("ASM in Developing");
                break;

            default:
                break;
        }
    }
}
