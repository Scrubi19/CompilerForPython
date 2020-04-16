package Lexer;


public class Token {
    int col;
    int row;
    tokenType token;
    String string;

    public enum tokenType {
        INPUT, PRINT, DEF, Separator,
        ID, num, numFLOAT, numBinary, numOctal, numHex,  StrLiteral, UNKNOWN,
        INT, FLOAT, STR, LIST, DICT,
        lParen, rParen, Semi, ExpSemi, lBrace, rBrace,
        //operation
        opAdd, opSub, opMul, opDiv, opMod, opExponentiation, opIntegerDiv,
        opMoreEq, opLessEq, opLess, opMore, opInEqual, opEqual, opAssign,
        opDecrementIntegerDiv, opIncrementExponentiation, opDecrementMod,
        opDecrementDiv, opIncrementMul, opDecrement, opIncrement, opAND,
        opOR, opNOT, BinaryAnd, opBinaryOr, opBinaryExceptionalOr,
        opBinaryInverting, opShiftLeft, opShiftRight,
        //keywords
        KeywordIf, KeywordElse, KeywordElif, KeywordFor, KeywordWhile,
        KeywordBreak, KeywordContinue, KeywordIn, KeywordIs, RETURN, KeywordRange
    }


    public Token() {
        this.col = 0;
        this.row = 0;
        token = tokenType.UNKNOWN;
        string = "null";
    }

    public Token(int col, int row, tokenType newToken, String lexeme) {
        this.col = col;
        this.row = row;
        token = newToken;
        string = lexeme;
    }

    public int getCol() {
        return col;
    }

    public int getRow() {
        return row;
    }

    public tokenType getTokenType() throws NullPointerException {
        if (token != null) {
            return token;
        }
        return null;
    }

    public String getString() {
        return string;
    }
}
