import CodeGenerator.CodeGenerator;
import IdentifierTable.Table;
import Lexer.*;
import Parser.Parser;
import Parser.ParserExceptions;
import Semantics.SemanticAnalysis;
import Semantics.SemanticsExceptions;

import java.io.IOException;

public class Main {

    public static void main(String[] args) throws IOException, ParserExceptions, SemanticsExceptions, InterruptedException {
        if(args.length == 0) {
            System.out.print("Usage:\n" +
                              "\t [Options] <input_program.py>\n"+
                              "Options:\n" +
                              "\t --dump-tokens — вывести результат работы лексического анализатора\n" +
                              "\t --dump-ast — вывести AST\n" +
                              "\t --dump-asm — вывести ассемблер\n");
            return;
        }
        switch (args[0]){
            case ("--dump-tokens"):
                if(args.length > 1) {
                    Lexer.readText(args[1]);
                    Lexer.dumpTokens();
                    Parser.start();
                    Table.tableInitialization(Parser.root);
                    SemanticAnalysis Sema = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
                    Sema.start();

                    CodeGenerator codeGen = new CodeGenerator();
                    codeGen.init(Table.getIdentifierTable().size(), Parser.root);
                    codeGen.analysis(Parser.root);
                    codeGen.dumpAsmToFile();

                    Process proc1 = Runtime.getRuntime().exec("gcc -no-pie dumpAsm.s -o "+args[1].replace (".py", ""));
                    proc1.waitFor();
                    Process proc2 = Runtime.getRuntime().exec("rm dumpAsm.s");
                    proc2.waitFor();
                    proc1.destroy();
                    proc2.destroy();

                }
            break;

            case ("--dump-ast"):
                if(args.length > 1) {
                    Lexer.readText(args[1]);

                    Parser.start();
                    Parser.showTree();

                    Table.tableInitialization(Parser.root);

                    SemanticAnalysis Sema = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
                    Sema.start();

                    CodeGenerator codeGen = new CodeGenerator();
                    codeGen.init(Table.getIdentifierTable().size(), Parser.root);
                    codeGen.analysis(Parser.root);
                    codeGen.dumpAsmToFile();

                    Process proc1 = Runtime.getRuntime().exec("gcc -no-pie dumpAsm.s -o "+args[1].replace (".py", ""));
                    proc1.waitFor();
                    Process proc2 = Runtime.getRuntime().exec("rm dumpAsm.s");
                    proc2.waitFor();
                    proc1.destroy();
                    proc2.destroy();

                }
                break;

            case ("--dump-asm"):
                if(args.length > 1) {
                    Lexer.readText(args[1]);
                    Parser.start();

                    Table.tableInitialization(Parser.root);

                    SemanticAnalysis Sema = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
                    Sema.start();

                    CodeGenerator codeGen = new CodeGenerator();
                    codeGen.init(Table.getIdentifierTable().size(), Parser.root);
                    codeGen.analysis(Parser.root);
                    codeGen.dumpAsmToFile();
                    codeGen.dumpAsmFromFile();

                    Process proc1 = Runtime.getRuntime().exec("gcc -no-pie dumpAsm.s -o "+args[1].replace (".py", ""));
                    proc1.waitFor();
                    Process proc2 = Runtime.getRuntime().exec("rm dumpAsm.s");

                    proc2.waitFor();
                    proc1.destroy();
                    proc2.destroy();
                }
                break;
            default:
                Lexer.readText(args[0]);
                Parser.start();

                Table.tableInitialization(Parser.root);

                SemanticAnalysis Sema = new SemanticAnalysis(Parser.root, Table.getIdentifierTable());
                Sema.start();

                CodeGenerator codeGen = new CodeGenerator();
                codeGen.init(Table.getIdentifierTable().size(), Parser.root);
                codeGen.analysis(Parser.root);
                codeGen.dumpAsmToFile();

                Process proc1 = Runtime.getRuntime().exec("gcc -no-pie dumpAsm.s -o "+args[0].replace (".py", ""));
                proc1.waitFor();
                Process proc2 = Runtime.getRuntime().exec("rm dumpAsm.s");
                proc2.waitFor();
                proc1.destroy();
                proc2.destroy();

                break;
        }
    }
}
