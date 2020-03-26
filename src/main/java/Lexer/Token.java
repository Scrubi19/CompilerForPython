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
        lBrace, rBrace, lParen, rParen, Semi,
        //operation
        opAdd, opSub, opMul, opDiv, opMod, opExponentiation, opIntegerDiv,
        opMoreEq, opLessEq, opLess, opMore, opInEqual, opEqual, opAssign,
        opDecrementIntegerDiv, opIncrementExponentiation, opDecrementMod,
        opDecrementDiv, opIncrementMul, opDecrement, opIncrement, opAND,
        opOR, opNOT, BinaryAnd, opBinaryOr, opBinaryExceptionalOr,
        opBinaryInverting, opShiftLeft, opShiftRight,
        //keywords
        KeywordIf, KeywordElse, KeywordElif, KeywordFor, KeywordWhile,
        KeywordBreak, KeywordContinue, KeywordIn, KeywordIs, RETURN;
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

    public tokenType getToken() {
        return token;
    }

    public String getString() {
        return string;
    }
}
