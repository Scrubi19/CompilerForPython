package CodeGenerator;

import Lexer.Token;
import Parser.AST.AstNode;
import Parser.AST.AstNode.AstNodeType;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeGenerator {

    private final List<Register> Registers = new ArrayList<>();
    private final List <String> dotText = new ArrayList<>();

    private static HashMap<String, String> varTable = new HashMap<>();// выделенные переменные на стеке
    private static HashMap<String, String> LC = new HashMap<>();

    int countIntVar = 0;
    int countLC = 0;
    int countRbp = 0;

    public void init() {
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
        Registers.add(new Register("%jne", false));
        Registers.add(new Register("%while", false));
        Registers.add(new Register("%LC0", false));
        Registers.add(new Register("exit", false));
        Registers.add(new Register("print", false));

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
                        dotText.add("\t\tsubq    $16, %rsp");
                        countLC++;
                        countLC++;
                        getRegisters().get(7).setValue(true);
                    }
                    if( i < node.getChildren().size()-1) {
                        if (node.getChildren().get(i).getType() == AstNodeType.NUMBER &&
                                node.getChildren().get(i+1).getType() == AstNodeType.NUMBER) {
                            countRbp = countRbp + 4;
                            dotText.add("\t\tmovl    $"+node.getChildren().get(i+1).getToken().getString()+", -"+countRbp +"(%rbp)");
                            varTable.put(node.getChildren().get(i).getToken().getString(),"-"+countRbp +"(%rbp)");
                        }
                    }
                }
                break;
            case WHILE:
                dotText.add("\t\tjmp    .L"+countLC);
                dotText.add(".L"+countLC+":");
                LC.put( "while_begin", ".L"+countLC);
                for (int i = 0; i < node.getChildren().size(); i++) {
                    if(node.getChildren().get(i).getToken().getString().equals("!=")) {
                        dotText.add("\t\tcmpl   $"+node.getChildren().get(i+1).getToken().getString()+
                                ", "+ varTable.get(node.getChildren().get(i-1).getToken().getString()));
                        if(getRegisters().get(10).isValue()) {
                            countLC++;
                            dotText.add("\t\tjne   .L"+countLC);
                            LC.put("while_in", ".L"+countLC);
                        } else {
                            countLC++;
                            dotText.add("\t\tje    .L"+countLC);
                            LC.put("while_out", ".L"+countLC);
                            getRegisters().get(11).setValue(true);

                        }
                    }
                    if (node.getChildren().get(i).getToken().getString().equals("and")) {
                        getRegisters().get(10).setValue(true);
                    }
                }
                break;
            case IF:
                getRegisters().get(12).setValue(true);
                dotText.add(".L"+countLC+":");
                AstNode temp;
                for (int i = 0; i < node.getChildren().size(); i++) {
                    if(node.getChildren().get(i).getType() == AstNodeType.EXPRESSION) {
                        temp = node.getChildren().get(i);
                        for (int j = 0; j < temp.getChildren().size(); j++) {
                            if(temp.getChildren().get(j).getToken().getString().equals(">")) {
                                dotText.add("\t\tmovl  "+varTable.get(temp.getChildren().get(j-1).getToken().getString())+", %eax");
                                getRegisters().get(0).setValue(true);
                                dotText.add("\t\tcmpl  "+varTable.get(temp.getChildren().get(j+1).getToken().getString())+", %eax");
                                countLC++;
                                dotText.add("\t\tjle    .L"+countLC);
                                LC.put("if_out", ".L"+countLC);
                            }
                        }
                    }
                    if(node.getChildren().get(i).getType() == AstNodeType.STATEMENT) {
                        temp = node.getChildren().get(i);
                        for (int j = 0; j < temp.getChildren().size(); j++) {
                            if(temp.getChildren().get(j).getToken().getString().equals("%=")) {
                                dotText.add("\t\tmovl  "+varTable.get(temp.getChildren().get(j-1).getToken().getString())+", %eax");
                                dotText.add("\t\tcltd");
                                dotText.add("\t\tidivl  "+varTable.get(temp.getChildren().get(j+1).getToken().getString()));
                                dotText.add("\t\tmovl   %edx, "+varTable.get(temp.getChildren().get(j-1).getToken().getString()));
                                dotText.add("\t\tjmp    "+LC.get("while_begin"));
                            }
                        }
                    }
                }
                break;
            case ELSE:
                dotText.add(".L"+countLC+":");
                for (int i = 0; i < node.getChildren().size(); i++) {
                    if(node.getChildren().get(i).getType() == AstNodeType.STATEMENT) {
                        temp = node.getChildren().get(i);
                        for (int j = 0; j < temp.getChildren().size(); j++) {
                            if (temp.getChildren().get(j).getToken().getString().equals("%=")) {
                                dotText.add("\t\tmovl  " + varTable.get(temp.getChildren().get(j - 1).getToken().getString()) + ", %eax");
                                dotText.add("\t\tcltd");
                                dotText.add("\t\tidivl  " + varTable.get(temp.getChildren().get(j + 1).getToken().getString()));
                                dotText.add("\t\tmovl   %edx, " + varTable.get(temp.getChildren().get(j - 1).getToken().getString()));
                            }
                        }
                    }
                }
                break;
            case PRINT:
                if(getRegisters().get(11).isValue()) {
                    dotText.add(LC.get("while_out")+":");
                } else {
                    dotText.add(".L"+countLC+":");
                }
                for (int i = 0; i < node.getChildren().size(); i++) {
                    if(node.getChildren().get(i).getToken().getString().equals("+")) {
                        dotText.add("\t\tmovl  " + varTable.get(node.getChildren().get(i-1).getToken().getString()) + ", %edx");
                        dotText.add("\t\tmovl  " + varTable.get(node.getChildren().get(i+1).getToken().getString()) + ", %eax");
                        dotText.add("\t\taddl  %edx, %eax");
                        dotText.add("\t\tmovl  %edx, %esi");
                    }
                }
                dotText.add("\t\tmovl    $.LC0, %edi");
                dotText.add("\t\tmovl    $0, %eax");
                dotText.add("\t\tcall    printf");
                getRegisters().get(12).setValue(true);
                getRegisters().get(13).setValue(true);
                getRegisters().get(14).setValue(true);
                break;
            case EOF:
                if(!getRegisters().get(13).isValue()) {
                    dotText.add(LC.get("while_out")+":");
                }
                dotText.add("\t\tnop\n\t\tleave\n\t\tret");
                if(getRegisters().get(14).isValue()) {
                    dotText.add(".LC0:\n\t\t.string \"%d\\n\"");
                }
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

    public void dumpAsmFromFile() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        File f = new File("dumpAsm.s");
        BufferedReader fin = new BufferedReader(new FileReader(f));
        String name;
        String line;
        System.out.println("Print File "+f.getName()+"? y/n");
        name = br.readLine();
        if(name.equals("y"))
            while ((line = fin.readLine()) != null) System.out.println(line);
    }
}
