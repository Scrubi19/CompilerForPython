import Lexer.*;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        switch (args[0]){
            case ("--dump-tokens"):
                ArrayList <Token> tokenList = new ArrayList<Token>();
                Lexer.readText(args[1], tokenList);

                // Вывод содержания Токена
                for(int i = 0; i < tokenList.size(); i++) {
                    System.out.println("Loc=<" + tokenList.get(i).getCol() + ":"
                            + tokenList.get(i).getRow() + ">   "+ tokenList.get(i).getToken()
                            + " " + "\'"+tokenList.get(i).getString()+"\'");
                }
            break;

            case (" --dump-ast"):
                System.out.println("AST in Developing");
                break;

            case (" --dump-asm"):
                System.out.println("ASM in Developing");
                break;

            default:
                break;
        }
    }
}
