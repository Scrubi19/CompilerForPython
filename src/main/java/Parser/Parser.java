package Parser;

import Lexer.Token;

import java.util.ArrayList;

import static Lexer.Token.tokenType.*;

public class Parser {

    public static int parsingToken(ArrayList<Token> tokenList, int currToken, int nextToken) {

        for(int i = 0; i < tokenList.size(); i++) {
            switch (tokenList.get(i).getToken()) {
                case DEF: // <def> <id> <lParen> <rParen> <Separator>
//                    while(tokenList.get(i).getToken() != Separator) {//описание правил
                        if(tokenList.get(nextToken).getToken() != ID) {
                            return 1;
                        }
//                    }
                    break;

                default:
                    break;
            }

        }
        return 0;
    }


    public static void showTree() {

    }

}
