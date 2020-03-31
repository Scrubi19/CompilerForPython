import Lexer.*;
import Parser.Parser;
import org.w3c.dom.ls.LSOutput;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

//    public static void main(String[] args) throws IOException {
//        switch (args[0]){
//            case ("--dump-tokens"):
//                Lexer.readText(args[1]);
//
//                // Вывод содержания Токена
//                for(int i = 0; i < Lexer.getTokenList().size(); i++) {
//                    System.out.println("Loc=<" +  Lexer.getTokenList().get(i).getCol() + ":"
//                            +  Lexer.getTokenList().get(i).getRow() + ">   "+  Lexer.getTokenList().get(i).getToken()
//                            + " " + "\'"+ Lexer.getTokenList().get(i).getString()+"\'");
//                }
////                Parser.parsingToken(Lexer.getTokenList());
//            break;
//
//            case (" --dump-ast"):
//                System.out.println("AST in Developing");
//                break;
//
//            case (" --dump-asm"):
//                System.out.println("ASM in Developing");
//                break;
//
//            default:
//                Lexer.readText(args[1]);
//                break;
//        }
//    }
    public static void main(String[] args) throws IOException {
        switch (args[0]){
            case ("--dump-tokens"):
                new LexerThread(args[1]).start();


                // Вывод содержания Токена
                for(int i = 0; i < LexerThread.getTokenList().size(); i++) {
                    System.out.println("Loc=<" +  LexerThread.getTokenList().get(i).getCol() + ":"
                            +  LexerThread.getTokenList().get(i).getRow() + ">   "+  LexerThread.getTokenList().get(i).getToken()
                            + " " + "\'"+ LexerThread.getTokenList().get(i).getString()+"\'");
                }
//                Parser.parsingToken(Lexer.getTokenList());
            break;

            case (" --dump-ast"):
                System.out.println("AST in Developing");
                break;

            case (" --dump-asm"):
                System.out.println("ASM in Developing");
                break;

            default:
                Lexer.readText(args[1]);
                break;
        }
    }
}
