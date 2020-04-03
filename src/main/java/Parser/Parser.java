package Parser;

import Lexer.Lexer;
import Lexer.Token.*;
import Lexer.Token;
import Parser.AST.AstNode;
import java.util.Objects;

import static Lexer.Lexer.*;
import static Lexer.Token.tokenType.*;

public class Parser {
    public static Lexer lexer;
    public static Token currentToken;
    public static AstNode root;

    public Parser(Lexer lexer) {
        this.lexer = lexer;
        root = new AstNode(AstNode.AstNodeType.PROGRAM, null, 0);

    }
    private static void lookup() {
        currentToken = Lexer.getNextToken();
    }
    public static void isMatch(tokenType type) throws ParserExceptions {
        if (currentToken.getTokenType().equals(type)) {
            lookup();
        } else {
            throw new ParserExceptions("> error in ("+currentToken.getCol()+","+currentToken.getRow()+") "+
                    "expecting <" + type+ ">, but found is <"+ currentToken.getTokenType()+"> : \"" + currentToken.getString()+"\"");
        }
    }

    public static int start() throws ParserExceptions {
         while(getIndexCurrToken() != getTokenList().size()) {
             switch (getTokenList().get(getIndexCurrToken()).getTokenType()) {
                 case DEF :
                     lookup();
                     root.AddChild(parseDef());
                     break;
                 case KeywordFor :
                     root.AddChild(parseFor());
                     break;
                 case KeywordIf :
                     root.AddChild(parseIf());
                     break;
                 case ID :
                    int CalculateLevel = 0;
                    int startPosition = getIndexCurrToken();
                    //вычисление уровня вложений
                    while(Objects.requireNonNull(getCurrentToken()).getTokenType() == Separator) {
                        decreaseCurrToken();
                        CalculateLevel++;
                    }
                    CalculateLevel++;
                    setIndexCurrToken(startPosition);
                    lookup();
                    Token idToken = currentToken;
                    lookup();

                    if(currentToken.getTokenType() == opAssign) {
                        lookup();
                        if(currentToken.getTokenType() == INPUT) {
                            root.AddChild(parseInput(idToken, CalculateLevel, null));
                        } else if(currentToken.getTokenType() == INT || currentToken.getTokenType() == FLOAT) {
                            root.AddChild(parseInput(idToken, CalculateLevel, currentToken));
                        }
                    }
                    getPrevToken();
                    getPrevToken();
                    break;
                 case PRINT :
                     lookup();
                     root.AddChild(parsePrint());
                     break;

             }
             lookup();
         }
        return 0;
    }
    /**
     * <def>:
     * 	"DEF" "ID" (<argumentList>) "ExpSemi"
     * 	    "Separator+"
     * @return AstNode
     * @throws ParserExceptions
     */
    private static AstNode parseDef() throws ParserExceptions {
        int startPosition = getIndexCurrToken();
        int defStart = currentToken.getCol();
        int defEnd = 0;
        int SeparatorCounter = 0;
        int pointer = 0;
        int DefPointer = 0;
        lookup();
        AstNode node = new AstNode(AstNode.AstNodeType.DEF, currentToken, 1);

        isMatch(ID);
        isMatch(lParen);

        parseArgumentList(node);

        isMatch(rParen);
        isMatch(ExpSemi);

        DefPointer = getIndexCurrToken();

        // поиск последнего return
        while(getIndexCurrToken() != getTokenList().size()) {
            if(currentToken.getTokenType() == RETURN) {
                pointer = getIndexCurrToken();
            }
            lookup();
        }
        setIndexCurrToken(startPosition);

        // подсчет Separator-ов
        while(getIndexCurrToken() != pointer) {
            if(currentToken.getTokenType() == Separator) {
                SeparatorCounter++;
            }
            lookup();
        }
        defEnd = currentToken.getCol();
        if(SeparatorCounter <= defEnd - defStart) {
            throw new ParserExceptions("expected an indented block between: "+defStart+"-"+defEnd);
        }
        setIndexCurrToken(DefPointer);

        return node;
    }
    /**(<argumentList>)
     *
     * @return AstNode
     * @throws ParserExceptions
     */
    public static AstNode parseArgumentList(AstNode node) throws ParserExceptions {
        if(currentToken.getTokenType() == lParen) {
            lookup();
        }

        while(!currentToken.getTokenType().equals(rParen)) {
            if (currentToken.getTokenType().equals(ID) ||
                    currentToken.getTokenType().equals(num) ||
                    currentToken.getTokenType().equals(StrLiteral) ||
                    currentToken.getTokenType().equals(numFLOAT) ||
                    currentToken.getTokenType().equals(numOctal) ||
                    currentToken.getTokenType().equals(numBinary) ||
                    currentToken.getTokenType().equals(LIST) ||
                    currentToken.getTokenType().equals(numHex)) {
                node.AddChild(new AstNode(AstNode.AstNodeType.ARG, currentToken,node.getLevel()));
                lookup();
            } else if(currentToken.getTokenType().equals(Semi)) {
                lookup();
            } else {
                throw new ParserExceptions("expecting <Id, number" +
                        ", Strliteral or Semi(,)>, but found is <"
                        + currentToken.getTokenType()
                        + ":" + currentToken.getString()
                        + "> in (" + currentToken.getCol()+","+currentToken.getRow()+")");
            }
        }
        return node;
    }

    /**<loop>
     * for ID KeywordIn ID ExpSemi
     *  Separator+
     * or
     * for ID KeywordIn KeywordRange lParen num rParen
     *  Separator+
     * @return AstNode
     * @throws ParserExceptions
     */
    public static AstNode parseFor() throws ParserExceptions {
        AstNode node = new AstNode(AstNode.AstNodeType.FOR, getCurrentToken());
        int CalculateLevel = 0;
        int startPosition = getIndexCurrToken();
        do {
            decreaseCurrToken();
            CalculateLevel++;

        } while(Objects.requireNonNull(getCurrentToken()).getTokenType() == Separator);
        setIndexCurrToken(startPosition);
        node.setLevel(CalculateLevel);
        lookup();
        lookup();

        node.AddChild(new AstNode(AstNode.AstNodeType.ID, currentToken, CalculateLevel));
        isMatch(ID);
        node.AddChild(new AstNode(AstNode.AstNodeType.IN, currentToken, CalculateLevel));
        isMatch(KeywordIn);

        while (!currentToken.getTokenType().equals(Separator)) {
            if (currentToken.getTokenType().equals(ID)) {// for ID in ID:
                node.AddChild(new AstNode(AstNode.AstNodeType.ID, currentToken, CalculateLevel));
                lookup();
                isMatch(ExpSemi);
            } else if(currentToken.getTokenType().equals(KeywordRange)) {// for ID in KeywordRange lParen num rParen
                lookup();
                isMatch(lParen);
                isMatch(num);
                isMatch(rParen);
                isMatch(ExpSemi);
                isMatch(Separator);
            }
        }
        node.setLevel(CalculateLevel);
        return node;
    }

    public static AstNode parseIf() throws ParserExceptions {
        int CalculateLevel = 0;
        int startPosition = getIndexCurrToken();
        // вычисление уровня вложенности
        do {
            decreaseCurrToken();
            CalculateLevel++;

        } while(Objects.requireNonNull(getCurrentToken()).getTokenType() == Separator);
        setIndexCurrToken(startPosition);

        AstNode node = new AstNode(AstNode.AstNodeType.IF, getCurrentToken(),CalculateLevel);

        lookup();
        node.AddChild(parseExpression(CalculateLevel));
        isMatch(ExpSemi);
        node.AddChild(parseStatement(CalculateLevel));

        return node;
    }
    /**
     *
     * @param level
     * @return AtsNode
     * @throws ParserExceptions
     */
    public static AstNode parseExpression(int level) throws ParserExceptions {
        AstNode node = new AstNode(AstNode.AstNodeType.EXPRESSION, null, level);
        lookup();

        while(!currentToken.getTokenType().equals(ExpSemi)) {
            if (currentToken.getTokenType().equals(ID) ||//  if ID or if ID lParen ID rParen
                    currentToken.getTokenType().equals(num)) {
                node.AddChild(new AstNode(AstNode.AstNodeType.EXPRESSION, currentToken, level));
                lookup();
                if (currentToken.getTokenType().equals(lParen)) {
                    lookup();
                    isMatch(ID);
                    if(currentToken.getTokenType() == rParen) { // ID(ID)
                        lookup();
                    } else if (currentToken.getTokenType() == lParen) {
                        lookup();
                        isMatch(ID);
                        while(currentToken.getTokenType() != ExpSemi) {
                            lookup();
                        }
                    }
                }
            } else if(currentToken.getTokenType().equals(opMore) || // operator
                        currentToken.getTokenType().equals(opLess) ||
                        currentToken.getTokenType().equals(opMoreEq) ||
                        currentToken.getTokenType().equals(opLessEq) ||
                        currentToken.getTokenType().equals(opInEqual)) {
                node.AddChild(new AstNode(AstNode.AstNodeType.OPERATOR, currentToken, level));
                lookup();
            } else {
                throw new ParserExceptions("expecting <ID, Number" +
                        ", operator or ExpSemi (:) >, but found is <"
                        + currentToken.getTokenType()
                        + ":" + currentToken.getString()
                        + "> in (" + currentToken.getCol()+","+currentToken.getRow()+")");
            }
        }
        return node;
    }
    public static AstNode parseStatement(int level) throws ParserExceptions {
        AstNode node = new AstNode(AstNode.AstNodeType.STATEMENT, null, level);
        lookup();

        while(!currentToken.getTokenType().equals(Separator)) {
            if (currentToken.getTokenType() == opAssign) {
                lookup();
                if (currentToken.getTokenType() != ID && currentToken.getTokenType() != num) {
                    throw new ParserExceptions("expecting <ID, Number" +
                            ", operator or ExpSemi (:) >, but found is <"
                            + currentToken.getTokenType()
                            + ":" + currentToken.getString()
                            + "> in (x" + currentToken.getCol() + "," + currentToken.getRow() + ")");
                }
            }
            lookup();
        }
        return node;
    }

    public static AstNode parsePrint() throws ParserExceptions {
        int CalculateLevel = 0;
        int startPosition = getIndexCurrToken();
        //вычисление уровня вложений
        while(Objects.requireNonNull(getCurrentToken()).getTokenType() == Separator) {
            decreaseCurrToken();
            CalculateLevel++;
        }
        CalculateLevel++;
        setIndexCurrToken(startPosition);

        AstNode node = new AstNode(AstNode.AstNodeType.PRINT, currentToken, CalculateLevel);
        isMatch(PRINT);
        isMatch(lParen);
        while (currentToken.getTokenType() != rParen || currentToken.getTokenType() != Separator) {
            if(currentToken.getTokenType() == ID || currentToken.getTokenType() == StrLiteral) {
                node.AddChild(new AstNode(AstNode.AstNodeType.ARG, currentToken, CalculateLevel));
                lookup();
            } else if (currentToken.getTokenType() == Semi) {
                lookup();
            } else if(currentToken.getTokenType() == lParen) {
                parseArgumentList(node);

            } else if(currentToken.getTokenType() == rParen) {
                break;
            }
            else {
                throw new ParserExceptions("expecting <Id, number" +
                        ", Strliteral or Semi(,)>, but found is <"
                        + currentToken.getTokenType()
                        + ":" + currentToken.getString()
                        + "> in (" + currentToken.getCol()+","+currentToken.getRow()+")");
            }
        }
        return node;
    }

    public static AstNode parseInput(Token ID, int level, Token type) throws ParserExceptions {
        AstNode node = new AstNode(AstNode.AstNodeType.INPUT, null, level);
        if(type != null) {
            lookup();
            isMatch(lParen);
            isMatch(INPUT);
            isMatch(lParen);
            isMatch(rParen);
            isMatch(rParen);
            node.setToken(type);
        } else {
            lookup();
            isMatch(lParen);
            isMatch(rParen);
        }
        node.AddChild(new AstNode(AstNode.AstNodeType.ID, ID, level));
        return node;
    }

    public static void showTree() {
        for(int i = 0; i < root.getChildren().size(); i++) {
            showTreeNode(root.getChildren().get(i));
        }


    }
    public static void showTreeNode(AstNode node) {
        System.out.println(""+node.getType()+" ("+node.getToken().getString()+") level = "+node.getLevel());
        if(node.getLevel() > 0) {
            if (!node.getChildren().isEmpty()) {
                for(AstNode temp : node.getChildren()) {
                    for(int i = 0; i < temp.getLevel(); i++) {
                        if(i == 0) {
                            System.out.print("|");
                        }
                        System.out.print(" ");
                    }
                    System.out.print("\\");
                    showTreeNode(temp);
                }
            }
        }



    }
}
