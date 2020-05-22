package CodeGenerator;

import Parser.AST.AstNode;
import Parser.AST.AstNode.AstNodeType;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CodeGenerator {

    private static AstNode AstRoot;
    private final List<Register> Registers = new ArrayList<>();
    private final List <String> dotText = new ArrayList<>();
    private final List <String> dotData = new ArrayList<>();

    private static HashMap<String, String> varTable = new HashMap<>();// выделенные переменные на стеке
    private static HashMap<String, String> LC = new HashMap<>();

    int varsInStack = 0;
    int countLC = 0;
    int countRBP = 0;

    public void init(HashMap<String, Integer> idTable, AstNode root) {
        AstRoot = root;
        dotData.add(".data");
        dotText.add(".text\n"+".globl main\n"+"main:");
        dotText.add("\t\tpushq   %rbp\n\t\tmovq    %rsp, %rbp");
        if (idTable.size() <= 4) {
            varsInStack = 16;
        }
        if(idTable.size() >= 4) {
            varsInStack = 48;
        }
        if(idTable.size() >= 8) {
            varsInStack = 64;
        }
        dotText.add("\t\tsubq    $"+varsInStack+", %rsp");

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
        Registers.add(new Register("%for", false));


    }

    public void analysis(AstNode node) {
        switch (node.getType()) {
            case ASSIGN:
                for(int i = 0; i < node.getChildren().size(); i++) {
                    // id = something
                    if(i < node.getChildren().size() - 1 && node.getChildren().size() == 2) {
                        if (node.getChildren().get(i).getType() == AstNodeType.NUMBER &&
                                node.getChildren().get(i + 1).getType() == AstNodeType.NUMBER) {
                            countRBP = countRBP + 4;
                            dotText.add("\t\tmovl    $" + node.getChildren().get(i + 1).getToken().getString() + ", -" + countRBP + "(%rbp)");
                            varTable.put(node.getChildren().get(i).getToken().getString(), "-" + countRBP + "(%rbp)");

                        }  else if (node.getChildren().get(i).getType() == AstNodeType.STRLITERAL  &&
                                node.getChildren().get(i + 1).getType() == AstNodeType.STRLITERAL) {
                            dotData.add(node.getChildren().get(i).getToken().getString()+":\n\t\t.string  \""+
                                    node.getChildren().get(i+1).getToken().getString()+"\"");
                            varTable.put(node.getChildren().get(i).getToken().getString(), Integer.toString(node.getChildren().get(i+1).getToken().getString().length()));
                        }
                    } else if (node.getChildren().get(i).getToken().getString().equals("len")) {// id = len(something)
                        countRBP = countRBP + 4;
                        varTable.put(node.getChildren().get(i-1).getToken().getString(), "-" + countRBP + "(%rbp)");
                        dotText.add("\t\tmovl    $" + varTable.get(node.getChildren().get(i+1).getToken().getString()) + ", -" + countRBP + "(%rbp)");

                    }
                    // id = arrayElement[]
                     else if (i == node.getChildren().size() - 1 && node.getChildren().size() == 3) {
                        if (node.getChildren().get(i-2).getType() == AstNodeType.ID &&
                                node.getChildren().get(i - 1).getType() == AstNodeType.NUMBER &&
                                node.getChildren().get(i).getType() == AstNodeType.NUMBER) {
                            countRBP = countRBP + 4;
                            dotText.add("\t\tmovl    -" + varTable.get(node.getChildren().get(i).getToken().getString())+", %eax");
                            dotText.add("\t\tmovl    %eax, -4(%rbp)");
                            varTable.put(node.getChildren().get(i-2).getToken().getString(),  "-" + countRBP + "(%rbp)");
                        }
                    }
                }
                break;
            case FOR:
                getRegisters().get(15).setValue(true);
                for (int i = 0; i < node.getChildren().size(); i++) {
                    if (node.getChildren().get(i).getType() == AstNodeType.IN &&
                        node.getChildren().get(i+1).getType() == AstNodeType.ID &&
                        node.getChildren().get(i-1).getType() == AstNodeType.ID) {
                        countLC++;
                        // find length of array
                        dotText.add("\t\tleaq    -" + varTable.get("0") + ", %rax");
                        dotText.add("\t\tmovq    %rax, -24(%rbp)");
                        dotText.add("\t\tmovq    -24(%rbp), %rax");
                        dotText.add("\t\tmovq    %rax, -16(%rbp)");
                        dotText.add("\t\tmovq    -24(%rbp), %rax");
                        dotText.add("\t\taddq    $12, %rax");
                        dotText.add("\t\tmovq    %rax, -32(%rbp)");

                        dotText.add(".L"+countLC+":");
                        LC.put("for_in", ".L"+countLC);

                        dotText.add("\t\tmovq    -16(%rbp), %rax");
                        dotText.add("\t\tcmpq    -32(%rbp), %rax");

                        varTable.put("length", "-32(%rbp)"); // high of array

                        countLC++;
                        LC.put("for_out", ".L"+countLC);
                        dotText.add("\t\tje      "+LC.get("for_out"));
                        dotText.add("\t\tmovq    -16(%rbp), %rax");
                        dotText.add("\t\tmovl    (%rax), %eax");
                        dotText.add("\t\tmovl    %eax, -36(%rbp)");

                        varTable.put(node.getChildren().get(i-1).getToken().getString(), "36(%rbp)");
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
                    } if (node.getChildren().get(i).getToken().getString().equals("<")) {
                        dotText.add("\t\tmovl    "+varTable.get(node.getChildren().get(i-1).getToken().getString())+", %eax");
                        dotText.add("\t\tcmpl    "+varTable.get(node.getChildren().get(i+1).getToken().getString())+", %eax");
                        countLC++;
                        LC.put("while_out<", ".L"+countLC);
                        dotText.add("\t\tjge     "+LC.get("while_out<"));
                    }
                }
                break;
            case IF:
                getRegisters().get(12).setValue(true);
                 if (getRegisters().get(11).isValue()) {
                     dotText.add(".L"+countLC+":");
                 }
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
                            if (temp.getChildren().get(j).getToken().getString().equals("<")) {
                                dotText.add("\t\tmovl    -"+varTable.get(temp.getChildren().get(j-1).getToken().getString())+", %eax");
                                dotText.add("\t\tcmpl    "+varTable.get(temp.getChildren().get(j+1).getToken().getString())+", %eax");
                                countLC++;
                                dotText.add("\t\tjge     .L"+countLC);
                                LC.put("if_out", ".L"+countLC);
                            }
                            if (temp.getChildren().get(j).getToken().getString().equals("==") && temp.getChildren().size() >= 5) {
                                if (temp.getChildren().get(j-1).getType() == AstNodeType.ARRAY) {
                                    dotText.add("\t\tmovl     "+varTable.get(temp.getChildren().get(j-1).getToken().getString())+", %ebx");
                                    dotText.add("\t\tmovb     "+temp.getChildren().get(j-2).getToken().getString()+"(,%ebx,1), %ah");
                                }
                                if (temp.getChildren().get(j+2).getType() == AstNodeType.ARRAY) {
                                    dotText.add("\t\tmovl     " + varTable.get(temp.getChildren().get(j + 2).getToken().getString()) + ", %ebx");
                                    dotText.add("\t\tmovb     " + temp.getChildren().get(j + 1).getToken().getString() + "(,%ebx,1), %dh");
                                }
                                dotText.add("\t\tcmpb     %ah, %dh");
                                countLC++;
                                LC.put("if_out",".L"+countLC);
                                dotText.add("\t\tjne     "+LC.get("if_out"));
                            }
                            if (temp.getChildren().get(j).getToken().getString().equals("==") && temp.getChildren().size() == 3) {
                                dotText.add("\t\tmovl     " + varTable.get(temp.getChildren().get(j - 1).getToken().getString()) + ", %eax");
                                dotText.add("\t\tcmpl     " + varTable.get(temp.getChildren().get(j + 1).getToken().getString()) + ", %eax");
                                countLC++;
                                LC.put("if_out", ".L"+countLC);
                                dotText.add("\t\tjne     "+LC.get("if_out"));

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
                            else if (temp.getChildren().get(j).getToken().getString().equals("=")) {
                                dotText.add("\t\tmovl    "+varTable.get(temp.getChildren().get(j+1).getToken().getString())+", %eax");
                                dotText.add("\t\tmovl    %eax, "+varTable.get(temp.getChildren().get(j-1).getToken().getString()));
                            }
                            else if (temp.getChildren().get(j).getType() == AstNodeType.PRINT &&
                                    temp.getChildren().get(j+1).getType() == AstNodeType.STRLITERAL) {
                                LC.put("print_str", "\""+temp.getChildren().get(j+1).getToken().getString()+"\"");
                                dotText.add("\t\tmovl    $.LC0, %edi\n\t\tcall     puts");
                            }
                            else if (temp.getChildren().get(j).getToken().getString().equals("+=")) {
                                dotText.add("\t\taddl    $"+temp.getChildren().get(j+1).getToken().getString()+
                                            ", "+varTable.get(temp.getChildren().get(j-1).getToken().getString()));
                                if (getRegisters().get(0).isValue()) {
                                    countLC++;
                                }
                                LC.put("if_else",".L"+countLC);
                                getRegisters().get(0).setValue(true);
                            }
                            else if (temp.getChildren().get(j).getType() == AstNodeType.ID &&
                                    temp.getChildren().size() == 1) {
                                if (LC.get("if_out") != null) {
                                    dotText.add(LC.get("if_out")+":");
                                    dotText.add("\t\taddl    $1, "+varTable.get(temp.getChildren().get(j).getToken().getString()));
                                    if (LC.get("while_begin") != null) {
                                        dotText.add("\t\tjmp    "+LC.get("while_begin"));
                                    }
                                }
                            }
                        }
                        if(getRegisters().get(15).isValue() && LC.get("for_in") != null) {
                            dotText.add(LC.get("if_out")+":");
                            dotText.add("\t\taddq    $4, -16(%rbp)");
                            dotText.add("\t\tjmp    "+LC.get("for_in"));

                        }
                    }
                }
                break;
            case ELSE:
                if (LC.get("if_else") != null) {
                    dotText.add("\t\tjmp    "+LC.get("if_else"));
                    dotText.add(LC.get("if_out")+":");
                } else {
                    dotText.add(".L"+countLC+":");
                }
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
                            else if (temp.getChildren().get(j).getToken().getString().equals("=")) {
                                if (temp.getChildren().get(j+1).getToken().getString().equals("0")) {
                                    dotText.add("\t\tmovl    $"+temp.getChildren().get(j+1).getToken().getString()+", "+
                                                varTable.get(temp.getChildren().get(j-1).getToken().getString()));
                                }

                            }
                        }
                    }
                }
                if (LC.get("if_else") != null) {
                    dotText.add(LC.get("if_else")+":");
                }
                break;
            case PRINT:
                int printFlag = 0;
                getRegisters().get(14).setValue(true);
                if(getRegisters().get(11).isValue()) {
                    dotText.add(LC.get("while_out")+":");
                } else if (getRegisters().get(15).isValue()){
                    dotText.add(LC.get("for_out")+":");
                }
                for (int i = 0; i < node.getChildren().size(); i++) {
                    if(node.getChildren().get(i).getToken().getString().equals("+")) {
                        dotText.add("\t\tmovl  " + varTable.get(node.getChildren().get(i-1).getToken().getString()) + ", %edx");
                        dotText.add("\t\tmovl  " + varTable.get(node.getChildren().get(i+1).getToken().getString()) + ", %eax");
                        dotText.add("\t\taddl  %edx, %eax");
                        dotText.add("\t\tmovl  %edx, %esi");
                        dotText.add("\t\tmovl  $.LC0, %edi");
                        dotText.add("\t\tmovl  $0, %eax");
                        dotText.add("\t\tcall  printf");
                    }
                    if (varTable.get(LC.get("return")) != null && printFlag == 0) {
                        dotText.add("\t\tmovl  "+varTable.get(LC.get("return"))+", %eax");
                        dotText.add("\t\tmovl  %eax, %esi");
                        dotText.add("\t\tmovl  $.LC0, %edi");
                        dotText.add("\t\tmovl  $0, %eax");
                        dotText.add("\t\tcall  printf");
                        printFlag = 1;
                    }
                }
                getRegisters().get(13).setValue(true);
                break;
            case EOF:
                if(!getRegisters().get(13).isValue()) {
                    dotText.add(LC.get("while_out")+":");
                }
                if (LC.get("while_out<") != null) {
                    dotText.add(LC.get("while_out<")+":");
                }
                dotText.add("\t\tnop\n\t\tleave\n\t\tret");
                if(LC.get("print_str") != null) {
                    dotText.add(".LC0:\n\t\t.string "+LC.get("print_str")+"\n");
                } else {
                    dotText.add(".LC0:\n\t\t.string \"%d\\n\""+"\n");
                }
                break;
            case ARRAY:
                int count = varsInStack;
                for(int i = 0; i < node.getChildren().size(); i++) {
                    dotText.add("\t\tmovl    $"+node.getChildren().get(i).getToken().getString()+", -"+count+ "(%rbp)");
                    varTable.put(""+i+"",+count+"(%rbp)");
                    count = count - 4;
                }
                break;
            case RETURN:
                for (AstNode tmp: node.getChildren()) {
                    getRegisters().get(6).setValue(true);
                    LC.put("return", tmp.getToken().getString().replace("_", ""));
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
            for (String line : dotData) {
                writer.write(line);
                writer.write(System.getProperty("line.separator"));
            }
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