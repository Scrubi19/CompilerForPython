import Lexer.Lexer;
import Lexer.Lexer.*;
import Lexer.Token.tokenType;
import org.junit.Assert;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class LexerTest {

    @Test
    void idTest(){
        ArrayList<tokenType> expected = new ArrayList<>();
        expected.add(tokenType.UNKNOWN);
        expected.add(tokenType.ID);
        expected.add(tokenType.ID);

        ArrayList<tokenType> actual = new ArrayList<>();
        actual.add(Lexer.checkToken("2131edgc", 0));
        actual.add(Lexer.checkToken("main", 0));
        actual.add(Lexer.checkToken("out.append", 0));

        Assert.assertEquals(expected, actual);
    }

    @Test
    void binaryTest() {
        ArrayList<tokenType> expected = new ArrayList<>();
        expected.add(tokenType.numBinary);
        expected.add(tokenType.UNKNOWN);
        expected.add(tokenType.UNKNOWN);

        ArrayList<tokenType> actual = new ArrayList<>();
        actual.add(Lexer.checkToken("0b10101", 0));
        actual.add(Lexer.checkToken("0b123", 0));
        actual.add(Lexer.checkToken("1b0123", 0));

        Assert.assertEquals(expected, actual);
    }

    @Test
    void octalTest() {
        ArrayList<tokenType> expected = new ArrayList<>();
        expected.add(tokenType.numOctal);
        expected.add(tokenType.numOctal);
        expected.add(tokenType.UNKNOWN);

        ArrayList<tokenType> actual = new ArrayList<>();
        actual.add(Lexer.checkToken("0o1234567", 0));
        actual.add(Lexer.checkToken("0o74324", 0));
        actual.add(Lexer.checkToken("0o459", 0));

        Assert.assertEquals(expected, actual);
    }

    @Test
    void hexTest() {
        ArrayList<tokenType> expected = new ArrayList<>();
        expected.add(tokenType.numHex);
        expected.add(tokenType.numHex);
        expected.add(tokenType.UNKNOWN);

        ArrayList<tokenType> actual = new ArrayList<>();
        actual.add(Lexer.checkToken("0fFFF", 0));
        actual.add(Lexer.checkToken("0fADE", 0));
        actual.add(Lexer.checkToken("0f088", 0));

        Assert.assertEquals(expected, actual);

    }

    @Test
    void operatorTest() {
        ArrayList<tokenType> expected = new ArrayList<>();
        expected.add(tokenType.opAssign);
        expected.add(tokenType.opAdd);
        expected.add(tokenType.opIncrement);

        ArrayList<tokenType> actual = new ArrayList<>();
        actual.add(Lexer.checkToken("=", 0));
        actual.add(Lexer.checkToken("+", 0));
        actual.add(Lexer.checkToken("+=", 0));

        Assert.assertEquals(expected, actual);
    }

    @Test
    void keywordFor() {
        tokenType expected = tokenType.KeywordFor;
        tokenType actual = Lexer.checkToken("for", 0);
        Assert.assertEquals(expected, actual);
    }

    @Test
    void keywordWhile() {
        tokenType expected = tokenType.KeywordWhile;
        tokenType actual = Lexer.checkToken("while", 0);
        Assert.assertEquals(expected, actual);
    }

    @Test
    void keywordIf() {
        tokenType expected = tokenType.KeywordIf;
        tokenType actual = Lexer.checkToken("if", 0);
        Assert.assertEquals(expected, actual);
    }

    @Test
    void keywordElse() {
        tokenType expected = tokenType.KeywordElse;
        tokenType actual = Lexer.checkToken("else", 0);
        Assert.assertEquals(expected, actual);
    }

    @Test
    void keywordElif() {
        tokenType expected = tokenType.KeywordElif;
        tokenType actual = Lexer.checkToken("elif", 0);
        Assert.assertEquals(expected, actual);
    }

    @Test
    void keywordBreak() {
        tokenType expected = tokenType.KeywordBreak;
        tokenType actual = Lexer.checkToken("break", 0);
        Assert.assertEquals(expected, actual);
    }

    @Test
    void keywordContinue() {
        tokenType expected = tokenType.KeywordContinue;
        tokenType actual = Lexer.checkToken("continue", 0);
        Assert.assertEquals(expected, actual);
    }


    @Test
    void braceTest() {
        ArrayList<tokenType> expected = new ArrayList<>();
        expected.add(tokenType.lParen);
        expected.add(tokenType.rParen);
        expected.add(tokenType.lParen);
        expected.add(tokenType.rParen);

        ArrayList<tokenType> actual = new ArrayList<>();
        actual.add(Lexer.checkToken("(", 0));
        actual.add(Lexer.checkToken(")", 0));
        actual.add(Lexer.checkToken("[", 0));
        actual.add(Lexer.checkToken("]", 0));

        Assert.assertEquals(expected, actual);
    }

}