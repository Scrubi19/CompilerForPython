package Parser;

import Lexer.Token.*;
import Lexer.Token;
import Parser.AST.AstNode;
import Parser.AST.AstNode.AstNodeType;
import java.util.Objects;
import static Lexer.Lexer.*;
import static Lexer.Token.tokenType.*;

public class Parser {
    public static Token currentToken;
    public static AstNode root;

    private static void lookup() {
        currentToken = getNextToken();
    }

    private static void decreaseLookup() {
        currentToken = getPrevToken();
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
        root = new AstNode(AstNodeType.PROGRAM, null, 0);
        currentToken = getNextToken();
        while(getIndexCurrToken() != getTokenList().size()) {
            assert currentToken != null;
            switch (currentToken.getTokenType()) {
                 case DEF :
                     root.addChild(parseDef());
                     break;
                 case KeywordFor :
                     root.addChild(parseFor());
                     decreaseCurrToken();
                     break;
                 case KeywordWhile :
                     root.addChild(parseWhile());
                     break;
                 case KeywordIf :
                     root.addChild(parseIf());
                     break;
                case KeywordElif :
                    root.addChild(parseElif());
                    break;
                case KeywordElse :
                    root.addChild(parseElse());
                    break;
                case ID :
                    int CalculateLevel = calculateLevel();
                    Token idToken = currentToken;
                    isMatch(ID);
                    if(currentToken.getTokenType() == opAssign ) {
                        lookup();
                        if(currentToken.getTokenType() == ID) {
                            root.addChild(new AstNode(AstNodeType.ID, idToken, CalculateLevel));
                        }
                        else if(currentToken.getTokenType() == INPUT) {
                            root.addChild(parseInput(idToken, CalculateLevel, null));
                        } else if(currentToken.getTokenType() == INT || currentToken.getTokenType() == FLOAT) {
                            root.addChild(parseInput(idToken, CalculateLevel, currentToken));
                        }
                    }
                    decreaseCurrToken();

                    break;
                case PRINT :
                     root.addChild(parsePrint());
                     break;
                case RETURN :
                    root.addChild(parseReturn());
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
        AstNode node = new AstNode(AstNodeType.DEF, currentToken, 1);
        lookup();

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
                node.addChild(new AstNode(AstNodeType.ARG, currentToken,node.getLevel()));
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
        AstNode node = new AstNode(AstNodeType.FOR, new Token());
        int CalculateLevel = calculateLevel();

        node.setLevel(CalculateLevel);

        lookup();

        node.addChild(new AstNode(AstNodeType.ID, currentToken, CalculateLevel));
        isMatch(ID);
        node.addChild(new AstNode(AstNodeType.IN, currentToken, CalculateLevel));
        isMatch(KeywordIn);

        while (!currentToken.getTokenType().equals(Separator)) {
            if (currentToken.getTokenType().equals(ID)) {// for ID in ID:
                node.addChild(new AstNode(AstNodeType.ID, currentToken, CalculateLevel));
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
    /** IF <expression> ExpSemi <Statement>
     *  Separator+
     * @return AtsNode
     * @throws ParserExceptions
     */
    public static AstNode parseIf() throws ParserExceptions {
        int CalculateLevel = calculateLevel();

        AstNode node = new AstNode(AstNodeType.IF, new Token(),CalculateLevel);

        lookup();
        node.addChild(parseExpression(CalculateLevel));
        isMatch(ExpSemi);
        node.addChild(parseStatement(CalculateLevel));
        decreaseCurrToken();

        return node;
    }
    /** ELIF <expression> ExpSemi <Statement>
     *  Separator+
     * @return AtsNode
     * @throws ParserExceptions
     */
    public static AstNode parseElif() throws ParserExceptions {
        int CalculateLevel = calculateLevel();

        AstNode node = new AstNode(AstNodeType.ELIF, currentToken,CalculateLevel);

        lookup();
        node.addChild(parseExpression(CalculateLevel));
        isMatch(ExpSemi);
        node.addChild(parseStatement(CalculateLevel));
        decreaseCurrToken();

        return node;
    }

    public static AstNode parseElse() throws ParserExceptions {
        int CalculateLevel = calculateLevel();

        AstNode node = new AstNode(AstNodeType.ELSE, currentToken,CalculateLevel);

        lookup();
        isMatch(ExpSemi);
        isMatch(Separator);
        node.addChild(parseStatement(CalculateLevel));
        decreaseCurrToken();

        return node;
    }
    /**<expression> ExpSemi
     *
     * @param level
     * @return AtsNode
     * @throws ParserExceptions
     */
    public static AstNode parseExpression(int level) throws ParserExceptions {
        AstNode node = new AstNode(AstNodeType.EXPRESSION, new Token(), level);

        while(!currentToken.getTokenType().equals(ExpSemi)) {
            if (currentToken.getTokenType().equals(ID) ||//  if ID or if ID lParen ID rParen
                    currentToken.getTokenType().equals(num)) {
                node.addChild(new AstNode(AstNodeType.ARG, currentToken, level));
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
                node.addChild(new AstNode(AstNode.AstNodeType.OPERATOR, currentToken, level));
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
        int calc = level;
        AstNode node = new AstNode(AstNode.AstNodeType.STATEMENT, new Token(), level);
        while(currentToken.getTokenType() == Separator) {
            lookup();
        }

        do {
            calc = calculateLevel();
//            System.out.println("id = "+calc);
            if (currentToken.getTokenType() == ID) {
                node.addChild(new AstNode(AstNodeType.ID, currentToken, level));
                lookup();
//                System.out.println("iter "+currentToken.getTokenType());
            } else if(currentToken.getTokenType() == opAssign ||
                    currentToken.getTokenType() == opIncrement ||
                    currentToken.getTokenType() == opDecrement ||
                    currentToken.getTokenType() == opIncrementMul ||
                    currentToken.getTokenType() == opDecrementDiv ||
                    currentToken.getTokenType() == opDecrementMod ||
                    currentToken.getTokenType() == opIncrementExponentiation ||
                    currentToken.getTokenType() == opIntegerDiv) {
                node.addChild(new AstNode(AstNodeType.OPERATOR, currentToken, level));
//                System.out.println("iter");
                lookup();
                if(currentToken.getTokenType() == ID
                    || currentToken.getTokenType() == num) {
                    node.addChild(new AstNode(AstNodeType.ID, currentToken, level));

                }
                lookup();
            } else {
                throw new ParserExceptions("\nexpecting <ID, Number" +
                        ", operator or ExpSemi (:) >\nbut found is <"
                        + currentToken.getTokenType()
                        + " : " + currentToken.getString()
                        + "> in (" + currentToken.getCol() + "," + currentToken.getRow() + ")");
            }
        } while(calc >= level);
//        while(calc >= level) {
//            calc = calculateLevel();
//            if (currentToken.getTokenType() == ID) {
//                node.addChild(new AstNode(AstNodeType.ID, currentToken, level));
//                lookup();
//            } else if(currentToken.getTokenType() == opAssign ||
//                        currentToken.getTokenType() == opIncrement ||
//                        currentToken.getTokenType() == opDecrement ||
//                        currentToken.getTokenType() == opIncrementMul ||
//                        currentToken.getTokenType() == opDecrementDiv ||
//                        currentToken.getTokenType() == opDecrementMod ||
//                        currentToken.getTokenType() == opIncrementExponentiation ||
//                        currentToken.getTokenType() == opIntegerDiv) {
//                node.addChild(new AstNode(AstNodeType.OPERATOR, currentToken, level));
//                lookup();
//            } else {
//                throw new ParserExceptions("\nexpecting <ID, Number" +
//                        ", operator or ExpSemi (:) >\nbut found is <"
//                        + currentToken.getTokenType()
//                        + " : " + currentToken.getString()
//                        + "> in (" + currentToken.getCol() + "," + currentToken.getRow() + ")");
//            }
//        }
        return node;
    }

    public static AstNode parsePrint() throws ParserExceptions {
        int CalculateLevel = calculateLevel();

        AstNode node = new AstNode(AstNodeType.PRINT, currentToken, CalculateLevel);
        isMatch(PRINT);
        isMatch(lParen);
        while (currentToken.getTokenType() != rParen || currentToken.getTokenType() != Separator) {
            if(currentToken.getTokenType() == ID || currentToken.getTokenType() == StrLiteral) {
                node.addChild(new AstNode(AstNodeType.ARG, currentToken, CalculateLevel));
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
        AstNode node = new AstNode(AstNodeType.INPUT, new Token(), level);
        if(type != null) {
            lookup();
            isMatch(lParen);
            isMatch(INPUT);
            isMatch(lParen);
            isMatch(rParen);
            isMatch(rParen);
            node.setToken(type);
            getPrevToken();
            getPrevToken();
        } else {
            lookup();
            isMatch(lParen);
            isMatch(rParen);
        }
        node.addChild(new AstNode(AstNodeType.ID, ID, level));
        return node;
    }
    /**
     * WHILE <condition> ExpSemi
     *  Separator+
     * @return AstNode
     * @throws ParserExceptions
     */
    public static AstNode parseWhile() throws ParserExceptions {
        int CalculateLevel = calculateLevel();

        AstNode node = new AstNode(AstNodeType.WHILE, currentToken, CalculateLevel);
        lookup();

        while(!currentToken.getTokenType().equals(ExpSemi)) {
            if (currentToken.getTokenType().equals(ID) ||
                    currentToken.getTokenType().equals(num)) {
                node.addChild(new AstNode(AstNodeType.ARG, currentToken,node.getLevel()));
                lookup();
            } else if(currentToken.getTokenType().equals(opMore) || // operator
                    currentToken.getTokenType().equals(opLess) ||
                    currentToken.getTokenType().equals(opMoreEq) ||
                    currentToken.getTokenType().equals(opLessEq) ||
                    currentToken.getTokenType().equals(opInEqual)) {
                node.addChild(new AstNode(AstNodeType.OPERATOR, currentToken, node.getLevel()));
                lookup();
            }  else if(currentToken.getTokenType().equals(opAND) || // operator
                    currentToken.getTokenType().equals(opOR) ||
                    currentToken.getTokenType().equals(opNOT)) {
                node.addChild(new AstNode(AstNodeType.LOGIC, currentToken, node.getLevel()));
                lookup();
            } else {
                throw new ParserExceptions("\nexpecting in condition (while) <ID, number" +
                        ", Operator or LogicOperator>\nbut found is <"
                        + currentToken.getTokenType()
                        + ":" + currentToken.getString()
                        + "> in (" + currentToken.getCol()+","+currentToken.getRow()+")");
            }
        }
        isMatch(ExpSemi);
        isMatch(Separator);
        decreaseCurrToken();

        return node;
    }

    public static AstNode parseReturn() throws ParserExceptions {
        int CalculateLevel = calculateLevel();

        AstNode node = new AstNode(AstNodeType.RETURN, currentToken, CalculateLevel);

        lookup();

        node.addChild(new AstNode(AstNodeType.ID, currentToken, CalculateLevel));
        isMatch(ID);

        if(currentToken.getTokenType() == lParen) {
            while(currentToken.getTokenType() != rParen) {
                if(currentToken.getTokenType() == ID) {
                    node.addChild(new AstNode(AstNodeType.ID, currentToken, CalculateLevel));
                }
                lookup();
            }
        }
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
    //вычисление уровня вложений
    public static int calculateLevel() {
        int CalculateLevel = 0;
        int startPosition = getIndexCurrToken();
        if(startPosition == 1) {
            return 1;
        }
        decreaseCurrToken();
        decreaseCurrToken();
        while(Objects.requireNonNull(getCurrentToken()).getTokenType() == Separator) {
            decreaseCurrToken();
            CalculateLevel++;
        }
        CalculateLevel++;
        setIndexCurrToken(startPosition);

        return CalculateLevel;
    }
}
