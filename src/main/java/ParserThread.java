
import Lexer.Token;
import java.util.ArrayList;
import static Lexer.Token.tokenType.*;

public class ParserThread extends Thread {

    public void run(ArrayList<Token> tokenList, int currToken, int nextToken) {

        for(int i = 0; i < tokenList.size(); i++) {
            switch (tokenList.get(i).getToken()) {
                case DEF: // <def> <id> <lParen> <rParen> <Separator>
//                    while(tokenList.get(i).getToken() != Separator) {//описание правил
                    if(tokenList.get(nextToken).getToken() != ID) {
                    }
//                    }
                    break;

                default:
                    break;
            }

        }
    }


    public static void showTree() {

    }

}
