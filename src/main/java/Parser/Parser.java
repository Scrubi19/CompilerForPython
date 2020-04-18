package Parser;

import Lexer.Token.*;
import Lexer.Token;
import Parser.AST.AstNode;
import Parser.AST.AstNode.AstNodeType;

import java.util.Objects;
import static Lexer.Lexer.*;
import static Lexer.Token.tokenType.*;

public class Parser {
    public static AstNode root;
    public static Token currentToken;

    private static tokenType[]arithmeticOp = new tokenType[] {opAdd, opSub, opMul, opDiv, opMod, opIntegerDiv};
    private static tokenType[]numbers = new tokenType[] {num, numHex, numOctal, numBinary, numFLOAT};

    private static tokenType searchOperators() {
        for (int i = 0; i < arithmeticOp.length; i++) {
            if (currentToken.getTokenType() == arithmeticOp[i]) {
                return arithmeticOp[i];
            }
        }
        return null;
    }

    private static tokenType searchNumbers() {
        for (int i = 0; i < numbers.length; i++) {
            if (currentToken.getTokenType() == numbers[i]) {
                return numbers[i];
            }
        }
        return null;
    }

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
        root = new AstNode(AstNodeType.PROGRAM, new Token(), 0);
        currentToken = getNextToken();
        assert currentToken != null;
        while(getIndexCurrToken() != getTokenList().size() || currentToken.getTokenType() != EOF) {
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
                    int CalculateLevel = nestingLevelCalculation();
                    if(CalculateLevel == 0) {
                        CalculateLevel++;
                    }
                    Token idToken = currentToken;
                    isMatch(ID);
                    if(currentToken.getTokenType() == opAssign ) {
                        lookup();
                        if(currentToken.getTokenType() == ID || currentToken.getTokenType() == num ||
                                currentToken.getTokenType() == numFLOAT || currentToken.getTokenType() == STR
                                    || currentToken.getTokenType() == StrLiteral) {
                            AstNode expression = new AstNode(AstNodeType.ASSIGN, new Token(), CalculateLevel);
                            AstNode child = new AstNode(AstNodeType.ID, idToken, CalculateLevel);
                            expression.addChild(child);
                            parseAssign(expression, CalculateLevel, expression.getToken().getCol(), child);
                            root.addChild(expression);

                        } else if (currentToken.getTokenType() == lBrace) {
                            root.addChild(parseArray(idToken, CalculateLevel));
                        } else if (currentToken.getTokenType() == INPUT) {
                            root.addChild(parseInput(idToken, CalculateLevel, null));
                        } else if (currentToken.getTokenType() == INT || currentToken.getTokenType() == FLOAT) {
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
                    decreaseLookup();
                    break;
             }
            lookup();
         }
        return 0;
    }
    public static AstNode parseAssign(AstNode expression, int level, int procStr, AstNode idToken) throws ParserExceptions {
        while(currentToken.getTokenType() != Separator && getIndexCurrToken() != getTokenList().size()
                || currentToken.getCol() == procStr) {
            if(currentToken.getTokenType() == ID) {
                expression.addChild(new AstNode(AstNodeType.ID, currentToken, level));
            } else if(currentToken.getTokenType() == StrLiteral || currentToken.getTokenType() == STR) {
                expression.addChild(new AstNode(AstNodeType.STRLITERAL, currentToken, level));
                expression.lookupChildrenFromAstNode(idToken).setType(AstNodeType.STRLITERAL);

            } else if(currentToken.getTokenType() == num) {
                int index = getIndexCurrToken()-3;
                expression.addChild(new AstNode(AstNodeType.NUMBER, currentToken, level));
                expression.lookupChildrenfromTokenString(getTokenList().get(index).getString()).setType(AstNodeType.NUMBER);

            } else if(currentToken.getTokenType() == numFLOAT) {
                int index = getIndexCurrToken()-3;
                expression.addChild(new AstNode(AstNodeType.FLOAT, currentToken, level));
                expression.lookupChildrenfromTokenString(getTokenList().get(index).getString()).setType(AstNodeType.FLOAT);

            }
            else if(searchOperators() != null) {
                expression.addChild(new AstNode(AstNodeType.OPERATOR, currentToken, level));
            }
            lookup();
        }
        return expression;
    }

    public static AstNode parseArray(Token array, int level) throws ParserExceptions {
        AstNode node = new AstNode(AstNodeType.ARRAY, array, level);

        isMatch(lBrace);

        while (currentToken.getTokenType() != Separator || currentToken.getTokenType() != EOF) {
            if(currentToken.getTokenType() == searchNumbers()) {
                node.addChild(new AstNode(AstNodeType.NUMBER, currentToken, level));
                lookup();
                if(currentToken.getTokenType() == rBrace) {
                    isMatch(rBrace);
                    return node;
                } else if (currentToken.getTokenType() == Semi) {
                    isMatch(Semi);
                    decreaseCurrToken();
                } else {
                    throw new ParserExceptions("expecting < number" +
                            ", Strliteral, \",\" or \"]\" >, but found is <"
                            + currentToken.getTokenType()
                            + ":" + currentToken.getString()
                            + "> in (" + currentToken.getCol()+","+currentToken.getRow()+")");
                }
            }
            lookup();
        }

        return node;
    }
    /**
     * <def>:
     * 	"DEF" "ID" (<argumentList>) "ExpSemi"
     * 	    "Separator+"
     * @return AstNode
     * @throws ParserExceptions
     */
    public static AstNode parseDef() throws ParserExceptions {
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
        int CalculateLevel = nestingLevelCalculation()+1;

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
        int CalculateLevel = nestingLevelCalculation()+1;

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
        int CalculateLevel = nestingLevelCalculation()+1;

        AstNode node = new AstNode(AstNodeType.ELIF, currentToken,CalculateLevel);

        lookup();
        node.addChild(parseExpression(CalculateLevel));
        isMatch(ExpSemi);
        node.addChild(parseStatement(CalculateLevel));
        decreaseCurrToken();

        return node;
    }

    public static AstNode parseElse() throws ParserExceptions {
        int CalculateLevel = nestingLevelCalculation()+1;

        AstNode node = new AstNode(AstNodeType.ELSE, currentToken,CalculateLevel);

        lookup();
        isMatch(ExpSemi);
        isMatch(Separator);
        node.addChild(parseStatement(CalculateLevel));
        decreaseCurrToken();

        return node;
    }
    /**<expression> ExpSemi
     * @param level
     * @return AtsNode
     * @throws ParserExceptions
     */
    public static AstNode parseExpression(final int level) throws ParserExceptions {
        AstNode node = new AstNode(AstNodeType.EXPRESSION, new Token(), level);

        while(!currentToken.getTokenType().equals(ExpSemi)) {
            if (currentToken.getTokenType().equals(ID) ||// if ID or if ID lParen ID rParen
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
            } else if (currentToken.getTokenType().equals(Separator)) {
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
    public static AstNode parseStatement(final int level) throws ParserExceptions {
        int calc = level;
        AstNode node = new AstNode(AstNode.AstNodeType.STATEMENT, new Token(), level);
        while(currentToken.getTokenType() == Separator) {
            lookup();
        }

        do {
            calc = nestingLevelCalculation();
            if (currentToken.getTokenType() == ID) {
                node.addChild(new AstNode(AstNodeType.ID, currentToken, level));
                lookup();
            } else if(currentToken.getTokenType() == opAssign ||
                    currentToken.getTokenType() == opIncrement ||
                    currentToken.getTokenType() == opDecrement ||
                    currentToken.getTokenType() == opIncrementMul ||
                    currentToken.getTokenType() == opDecrementDiv ||
                    currentToken.getTokenType() == opDecrementMod ||
                    currentToken.getTokenType() == opIncrementExponentiation ||
                    currentToken.getTokenType() == opIntegerDiv) {
                node.addChild(new AstNode(AstNodeType.OPERATOR, currentToken, level));
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
        return node;
    }

    public static AstNode parsePrint() throws ParserExceptions {
        int CalculateLevel = nestingLevelCalculation()+1;

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
        int CalculateLevel = nestingLevelCalculation()+1;

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
        int CalculateLevel = nestingLevelCalculation();

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
        for(int i = 0; i < node.getLevel(); i++) {
            if(i == 0) {
                System.out.print("|");
            }
            System.out.print(" ");
        }
        if (node.getParent().getType() != AstNodeType.PROGRAM) {
            System.out.println(" ⇘"+node.getType()+" \""+node.getToken().getString()+"\" lvl = "+node.getLevel());
        } else {
            System.out.println("|"+node.getType()+" \""+node.getToken().getString()+"\" lvl = "+node.getLevel());
        }
        if(node.getLevel() >= 0) {
            if (!node.getChildren().isEmpty()) {
                for(AstNode temp : node.getChildren()) {
                    showTreeNode(temp);
                }
            }
        }
    }
    public static int nestingLevelCalculation() {
        int CalculateLevel = 0;
        int startPosition = getIndexCurrToken();
        if(startPosition == 1 || startPosition == 0) {
            return 1;
        }
        decreaseCurrToken();
        decreaseCurrToken();
        while(Objects.requireNonNull(getCurrentToken()).getTokenType() == Separator) {
            decreaseCurrToken();
            CalculateLevel++;
        }
        setIndexCurrToken(startPosition);

        return CalculateLevel;
    }
}
