import Lexer.*;
import Parser.Parser;
import Parser.Parser.*;
import Parser.ParserExceptions;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException, ParserExceptions {
        switch (args[0]){
            case ("--dump-tokens"):
//                new LexerThread(args[1]).start();

                Lexer lexer = new Lexer();
                lexer.readText(args[1]);
                Lexer.dumpTokens();

            break;
            case ("--dump-ast"):
                Lexer Lexer = new Lexer();
                Lexer.readText(args[1]);
                Parser parser = new Parser(Lexer);
                Parser.start();
                Parser.showTree();
                break;

            case (" --dump-asm"):
                System.out.println("ASM in Developing");
                break;

            default:
                break;
        }
    }
}
