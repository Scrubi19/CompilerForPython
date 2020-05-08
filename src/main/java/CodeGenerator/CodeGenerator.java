package CodeGenerator;

import Parser.AST.AstNode;
import Parser.AST.AstNode.AstNodeType;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CodeGenerator {

    private final List<Register> Registers = new ArrayList<>();
    int countIntVar = 0;

    private final List <String> dotData = new ArrayList<>();
    private final List <String> dotText = new ArrayList<>();


    public void init() {
        dotData.add(".data");
        dotText.add(".text\n"+".globl main\n"+"main:");

        Registers.add(new Register("%eax", false));
        Registers.add(new Register("%ebx", false));
        Registers.add(new Register("%ecx", false));
        Registers.add(new Register("%edx", false));
        Registers.add(new Register("%esp", false));
        Registers.add(new Register("%ebp", false));
        Registers.add(new Register("%esi", false));
        Registers.add(new Register("%edi", false));
        Registers.add(new Register("%rbp", false));
        Registers.add(new Register("%rsp", false));

    }

    public void analysis(AstNode node) {
        switch (node.getType()) {
            case ASSIGN:
                for(int i = 0; i < node.getChildren().size(); i++) {
                    if(node.getChildren().get(i).getType() == AstNodeType.NUMBER) {
                        countIntVar++;
                    }
                    if(node.getChildren().get(i).getType() == AstNodeType.NUMBER && !getRegisters().get(7).isValue()) {
                        dotText.add("\t\tpushq   %rbp\n\t\tmovq    %rsp, %rbp");
                        getRegisters().get(7).setValue(true);
                        if(countIntVar > 2 && !getRegisters().get(8).isValue()) {
                            dotText.add("\t\tsubq    $16, %rsp");
                            getRegisters().get(8).setValue(true);
                        }
                    }
                    //выделение памяти на стеке для локальных переменных
                    if( i < node.getChildren().size()-1) {
                        if (node.getChildren().get(i).getType() == AstNodeType.NUMBER &&
                                node.getChildren().get(i+1).getType() == AstNodeType.NUMBER) {
                            dotText.add("\t\tmovl    $"+node.getChildren().get(i+1).getToken().getString()+", %rsp");
                        }

                    }
                }
//                if(node.) {
//
//                }
            break;
        }
        for (AstNode temp : node.getChildren() ) {
            analysis(temp);
        }
    }

    public List<Register> getRegisters() {
        return Registers;
    }

    public void dumpAsmToFile() {
        Writer writer = null;
        try {
            writer = new FileWriter("dumpAsm.s");
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
