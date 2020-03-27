package Lexer;

import java.io.*;
import java.util.ArrayList;
import  Lexer.Token.tokenType;
import Parser.Parser;

import static java.util.regex.Pattern.matches;

public class Lexer {

    private static int tokenCounter = 0;

    public static void increaseTokenCounter() {
        tokenCounter++;
    }

    public static int getTokenCounter() {
        return tokenCounter;
    }

    public static void readText(String path, ArrayList<Token> tokenList) {
        try {
            int counter =  0;
            String bufLine = new String("");//путь
            File file = new File("./"+path);
            FileReader fr = new FileReader(file);
            BufferedReader reader = new BufferedReader(fr);

            bufLine = reader.readLine();
            while ( bufLine != null) {
                counter++;
                if (bufLine.contains("#")) {
                    bufLine = bufLine.split("#", 2)[0];
                }
                if(bufLine.trim().length() != 0) {
                    getTokenFromString(bufLine, counter, tokenList);
//                    System.out.println(bufLine);
                }
                bufLine = reader.readLine();
            }
        } catch (FileNotFoundException e) {
            System.out.println("File "+"does not exist");
            e.printStackTrace();
        } catch (IOException e) {
            System.out.println("File does not exist");
            e.printStackTrace();
        }
    }

    public static void getTokenFromString(String string, int counter, ArrayList<Token> tokenList) {
        String buf = new String("");
        int strLiteralFlag = 0;
        String[] bufStrings;
        ArrayList <String> bufLiteral = new ArrayList<>();
        String strToken = extraSpace(string);

        char [] strChar = strToken.toCharArray();
        int numQuotes = (int) (string.chars().filter(num -> num == '"').count() + string.chars().filter(num -> num == '\'').count());

        if(numQuotes%2 == 0 && numQuotes != 0) {
            for (char c : strChar) {
                if (c == '\'' || c == '\"') {
                    buf = String.valueOf(c);
                    bufStrings = strToken.split(buf);
                    for (int i = 1; i < bufStrings.length; i+=2) {
                        strToken = strToken.replace(bufStrings[i], "");
                        bufLiteral.add(bufStrings[i]);
                    }
                }
            }
        }

        bufStrings = strToken.split(" ");

        for (String bufString : bufStrings) {
            if (bufString != null) {
                if (bufString.contentEquals("\"\"") || bufString.contentEquals("''")) {
                    strLiteralFlag = 1;
                    for (int i = 0 ; i < bufLiteral.size(); i++) {
                        if(!bufLiteral.get(i).equals("")) {
                            tokenList.add(getTokenCounter(), new Token(counter, string.indexOf(bufLiteral.get(i).split(" ")[0]),
                                          checkToken(bufLiteral.get(i), strLiteralFlag), bufLiteral.get(i)));
                            Parser.parsingToken(tokenList.get(getTokenCounter() - 1), tokenList.get(getTokenCounter()));
                            increaseTokenCounter();
                            bufLiteral.remove(bufLiteral.get(i));
                        }
                    }
                } else {
                    if(bufString.equals("_")) {
                        tokenList.add(getTokenCounter(), new Token(counter, 0, checkToken(bufString, strLiteralFlag),  bufString));
                        increaseTokenCounter();
                    } else {
                        strLiteralFlag = 0;
                        tokenList.add(getTokenCounter(), new Token(counter, string.indexOf(bufString),
                                      checkToken(bufString, strLiteralFlag),  bufString));
                        increaseTokenCounter();
                    }
                }
            }
        }
    }
    public static String extraSpace(String string) {
        String [] template = new String[] {  "  - ", "\\[", "]", ":", ",", "\\(", "\\)", "=",
                                             "\\+", "-", "\\*","/", "%", "\\*  \\*", "/  /",
                                             "=  =", "!  =","<  >", "<", ">", ">  =", "<  =",
                                             "\\+  =", "-  =", "\\*  =", "/  =", "%  =",  "\\*  \\*  =",
                                             "\\*\\*  =", "/  /  =", "//  =", "&", "\\|", "\\^",
                                             "~", "<  <", ">  >", "! = ", " \\* \\*= ", "//   =",
                                             "  %=  ", "  >=  ","  <=  ", "  \\+=  ", "  ==  ", "  //  ",
                                             "  \\*\\*  ", "  !=  ","  -=  ", "  \\*=  ", "  /=  ",
                                             "  \\*\\*=  ", "  //=  ", "  <<  ", "  >>  ", "\t"};

        String [] pasteBuf = new String[] {  " - ", " [ ", " ] " ," : ", " , " ," ( ",
                                             " ) ", " = ", " + ", " - ", " * ", " / ",
                                             " % ", "**", "//", "==", " != ", " <> ",
                                             " < ", " > ", ">=", "<=", "+=","-=", "*=",
                                             "/=", "%=", " **= ", " **= "," //= ", " //=",
                                             " & ", " | ", " ^ ", " ~ ", "<<",">>", " != ",
                                             " **= ", " //= ", " %= ", " >= ", " <= ", " += ",
                                             " == ", " // ", " ** ", " != ", " -= ", " *= ",
                                             " /= ", " **= ", " //= ", " << ", " >> ", "_ "};

        for(int i = 0; i < pasteBuf.length; i++) {
             string = string.replaceAll(template[i], pasteBuf[i]);
        }

        return string.trim().replaceAll(" +", " ");
    }

    public static tokenType checkToken(String string, int strLiteralFlag) {

        switch (string) {
            case ("input"):
                return tokenType.INPUT;
            case ("_"):
                return tokenType.Separator;
            case ("print"):
                return tokenType.PRINT;
            case ("def"):
                return tokenType.DEF;
            //типы данных
            case ("int"):
                return tokenType.INT;
            case ("float"):
                return tokenType.FLOAT;
            case ("str"):
                return tokenType.STR;
            case ("list"):
                return tokenType.LIST;
            case ("dict"):
                return tokenType.DICT;

            ////Скобки
            case ("{"):
            case ("["):
                return tokenType.lBrace;
            case ("}"):
            case ("]"):
                return tokenType.rBrace;
            case ("("):
                return tokenType.lParen;
            case (")"):
                return tokenType.rParen;
            case (":"):
            case (","):
            case ("."):
                return tokenType.Semi;

            //Ариф.Операции
            case ("+"):
                return tokenType.opAdd;
            case ("-"):
                return tokenType.opSub;
            case ("*"):
                return tokenType.opMul;
            case ("/"):
                return tokenType.opDiv;
            case ("%"):
                return tokenType.opMod;
            case ("**"):
                return tokenType.opExponentiation;
            case ("//"):
                return tokenType.opIntegerDiv;

            //Операторы сравнения
            case ("=="):
                return tokenType.opEqual;
            case ("!="):
            case ("<>"):
                return tokenType.opInEqual;
            case (">"):
                return tokenType.opMore;
            case ("<"):
                return tokenType.opLess;
            case (">="):
                return tokenType.opMoreEq;
            case ("<="):
                return tokenType.opLessEq;

            //Операторы присваивания
            case ("="):
                return tokenType.opAssign;
            case ("+="):
                return tokenType.opIncrement;
            case ("-="):
                return tokenType.opDecrement;
            case ("*="):
                return tokenType.opIncrementMul;
            case ("/="):
                return tokenType.opDecrementDiv;
            case ("%="):
                return tokenType.opDecrementMod;
            case ("**="):
                return tokenType.opIncrementExponentiation;
            case ("//="):
                return tokenType.opDecrementIntegerDiv;

            //Логические операции
            case ("and"):
                return tokenType.opAND;
            case ("or"):
                return tokenType.opOR;
            case ("not"):
                return tokenType.opNOT;

            //Побитовые операции
            case ("&"):
                return tokenType.BinaryAnd;
            case ("|"):
                return tokenType.opBinaryOr;
            case ("^"):
                return tokenType.opBinaryExceptionalOr;
            case ("~"):
                return tokenType.opBinaryInverting;
            case ("<<"):
                return tokenType.opShiftLeft;
            case (">>"):
                return tokenType.opShiftRight;

            //Управляющие конструкции
            case ("if"):
                return tokenType.KeywordIf;
            case ("else"):
                return tokenType.KeywordElse;
            case ("elif"):
                return tokenType.KeywordElif;
            case ("for"):
                return tokenType.KeywordFor;
            case ("while"):
                return tokenType.KeywordWhile;
            case ("break"):
                return tokenType.KeywordBreak;
                case ("continue"):
            return tokenType.KeywordContinue;
            case ("in"):
                return tokenType.KeywordIn;
            case ("is"):
                return tokenType.KeywordIs;
            case ("return"):
                return tokenType.RETURN;
            default:
                if (strLiteralFlag == 1) {
                        return tokenType.StrLiteral;
                } else if (string.matches("[-+]?\\d+")) {
                    return tokenType.num;
                } else if(string.matches("^[0-9]*[.][0-9]+$")) { // float
                    return tokenType.numFLOAT;
                } else if(string.matches("0[bB][01]+")) {// binary
                    return tokenType.numBinary;
                } else if(string.matches("0[oO][0-7]+")) {// octal
                    return tokenType.numOctal;
                }else if(string.matches("0[fF][0-9a-fA-F]+")) {// hex // "0[fF][0-9a-fA-F]+"
                    return tokenType.numHex;
                } else if (matches("[\\w&&[^\\d]][[\\.\\w]\\w]*", string)) {
                    return tokenType.ID;
                } else if (matches("\\w&&[^\\d]][[\\w]\\w]*&&[-+]?\\d+]", string)) {
                    return tokenType.UNKNOWN;
                }
                return tokenType.UNKNOWN;
        }
    }
}
