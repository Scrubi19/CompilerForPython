package CodeGenerator;

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

    int varsInStack = 0;
    int countLC = 0;
    int count = 0;

    public void init(HashMap<String, Integer> idTable) {
        dotText.add(".text\n"+".globl main\n"+"main:");
        dotText.add("\t\tpushq   %rbp\n\t\tmovq    %rsp, %rbp");
        if (idTable.size() <= 4) {
            varsInStack = 16;
        }
        if(idTable.size() >= 4) {
            varsInStack = 48;
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
                            count = count + 4;
                            dotText.add("\t\tmovl    $" + node.getChildren().get(i + 1).getToken().getString() + ", -" + count + "(%rbp)");
                            varTable.put(node.getChildren().get(i).getToken().getString(), "-" + count + "(%rbp)");
                        }
                    }
                    // id = arrayElement[]
                     else if (i == node.getChildren().size() - 1 && node.getChildren().size() == 3) {
                        if (node.getChildren().get(i-2).getType() == AstNodeType.ID &&
                                node.getChildren().get(i - 1).getType() == AstNodeType.NUMBER &&
                                node.getChildren().get(i).getType() == AstNodeType.NUMBER) {
                            count = count + 4;
                            dotText.add("\t\tmovl    -" + varTable.get(node.getChildren().get(i).getToken().getString())+", %eax");
                            dotText.add("\t\tmovl    %eax, -4(%rbp)");
                            varTable.put(node.getChildren().get(i-2).getToken().getString(),  "-" + count + "(%rbp)");

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
                            } else if (temp.getChildren().get(j).getToken().getString().equals("=")) {
                                dotText.add("\t\tmovl    "+varTable.get(temp.getChildren().get(j+1).getToken().getString())+", %eax");
                                dotText.add("\t\tmovl    %eax, "+varTable.get(temp.getChildren().get(j-1).getToken().getString()));
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
                    }
                    if (varTable.get(LC.get("return")) != null && printFlag == 0) {
                        dotText.add("\t\tmovl  "+varTable.get(LC.get("return"))+", %eax");
                        dotText.add("\t\tmovl  %eax, %esi");
                        printFlag = 1;
                    }
                }
                dotText.add("\t\tmovl  $.LC0, %edi");
                dotText.add("\t\tmovl  $0, %eax");
                dotText.add("\t\tcall  printf");
                getRegisters().get(13).setValue(true);
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