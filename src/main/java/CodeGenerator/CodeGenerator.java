package CodeGenerator;

import Parser.AST.AstNode;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class CodeGenerator {

    private List<Register> Registers = new ArrayList<>();

    private List <String> dotData = new ArrayList<>();
    private List <String> dotText = new ArrayList<>();


    public void init() {
        dotData.add(".data");
        dotText.add(".prog");

        Registers.add(new Register("%eax", false));
        Registers.add(new Register("%ebx", false));
        Registers.add(new Register("%ecx", false));
        Registers.add(new Register("%edx", false));
        Registers.add(new Register("%esp", false));
        Registers.add(new Register("%ebp", false));
        Registers.add(new Register("%esi", false));
        Registers.add(new Register("%edi", false));
    }

    public void analysis(AstNode node) {


        switch (node.getType()) {
            case WHILE:
                break;
        }
        for (AstNode temp : node.getChildren() ) {
            analysis(temp);
        }

    }

    public void dumpAsmToFile() {
        Writer writer = null;
        try {
            writer = new FileWriter("dumpAsm.txt");
            for (String line : dotData) {
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
            writer.flush();
            for (String line : dotText) {
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
        } catch (Exception e) {
            System.out.println("dumpAsm writer error");
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException ex) {
                }
            }
        }
    }
}
