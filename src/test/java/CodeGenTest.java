import CodeGenerator.CodeGenerator;
import Lexer.Token;
import Parser.AST.AstNode;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import java.util.ArrayList;
import java.util.List;

class CodeGenTest {

    @Test
    void assignTest(){
        List<String> expected = new ArrayList<>();

        expected.add(".text\n"+".globl main\n"+"main:");
        expected.add("\t\tpushq   %rbp\n\t\tmovq    %rsp, %rbp");
        expected.add("\t\tsubq    $16, %rsp");
        expected.add("\t\tmovl    $10, -4(%rbp)");

        AstNode Node = new AstNode(AstNode.AstNodeType.ASSIGN, new Token(), 1);
        Node.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("b"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("10"), 1));

        CodeGenerator codeGen = new CodeGenerator();
        codeGen.init(1, Node);
        codeGen.analysis(Node);

        List<String> actual = codeGen.getDotText();

        Assert.assertEquals(expected, actual);
    }

    @Test
    void arrayTest(){
        List<String> expected = new ArrayList<>();

        expected.add(".text\n"+".globl main\n"+"main:");
        expected.add("\t\tpushq   %rbp\n\t\tmovq    %rsp, %rbp");
        expected.add("\t\tsubq    $48, %rsp");
        expected.add("\t\tmovl    $5, -48(%rbp)");
        expected.add("\t\tmovl    $1, -44(%rbp)");
        expected.add("\t\tmovl    $3, -40(%rbp)");
        expected.add("\t\tmovl    $10, -36(%rbp)");
        expected.add("\t\tmovl    $22, -32(%rbp)");



        AstNode Node = new AstNode(AstNode.AstNodeType.ARRAY, new Token("array"), 1);
        Node.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("5"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("1"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("3"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("10"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("22"), 1));


        CodeGenerator codeGen = new CodeGenerator();
        codeGen.init(5, Node);
        codeGen.analysis(Node);

        List<String> actual = codeGen.getDotText();

        Assert.assertEquals(expected, actual);
    }
    @Test
    void stringTest(){
        List<String> expected = new ArrayList<>();

        expected.add(".data");
        expected.add("stack:\n\t\t.string  \"searching string index\"");
        expected.add("search:\n\t\t.string  \"ing\"");


        AstNode root = new AstNode(AstNode.AstNodeType.PROGRAM, new Token(), 0);

        AstNode Node = new AstNode(AstNode.AstNodeType.ASSIGN, new Token("array"), 1);
        Node.addChild(new AstNode(AstNode.AstNodeType.STRLITERAL, new Token("stack"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.STRLITERAL, new Token("searching string index"), 1));
        root.addChild(Node);

        AstNode Node2 = new AstNode(AstNode.AstNodeType.ASSIGN, new Token("array"), 1);
        Node2.addChild(new AstNode(AstNode.AstNodeType.STRLITERAL, new Token("search"), 1));
        Node2.addChild(new AstNode(AstNode.AstNodeType.STRLITERAL, new Token("ing"), 1));
        root.addChild(Node2);


        CodeGenerator codeGen = new CodeGenerator();
        codeGen.init(2, root);
        codeGen.analysis(root);

        List<String> actual = codeGen.getDotData();

        Assert.assertEquals(expected, actual);
    }

    @Test
    void whileTestWithoutVars(){
        List<String> expected = new ArrayList<>();

        expected.add(".text\n"+".globl main\n"+"main:");
        expected.add("\t\tpushq   %rbp\n\t\tmovq    %rsp, %rbp");
        expected.add("\t\tsubq    $48, %rsp");
        expected.add("\t\tjmp    .L0");
        expected.add(".L0:");
        expected.add("\t\tmovl    null, %eax");
        expected.add("\t\tcmpl    null, %eax");
        expected.add("\t\tjge     .L1");

        AstNode Node = new AstNode(AstNode.AstNodeType.WHILE, new Token("while"), 1);
        Node.addChild(new AstNode(AstNode.AstNodeType.ARG, new Token("i"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.OPERATOR, new Token("<"), 1));
        Node.addChild(new AstNode(AstNode.AstNodeType.ARG, new Token("n"), 1));


        CodeGenerator codeGen = new CodeGenerator();
        codeGen.init(5, Node);
        codeGen.analysis(Node);

        List<String> actual = codeGen.getDotText();

        Assert.assertEquals(expected, actual);
    }

    @Test
    void whileTestWithVars(){
        List<String> expected = new ArrayList<>();

        expected.add(".text\n"+".globl main\n"+"main:");
        expected.add("\t\tpushq   %rbp\n\t\tmovq    %rsp, %rbp");
        expected.add("\t\tsubq    $48, %rsp");
        expected.add("\t\tmovl    $0, -4(%rbp)");
        expected.add("\t\tmovl    $5, -8(%rbp)");
        expected.add("\t\tjmp    .L0");
        expected.add(".L0:");
        expected.add("\t\tmovl    -4(%rbp), %eax");
        expected.add("\t\tcmpl    -8(%rbp), %eax");
        expected.add("\t\tjge     .L1");

        AstNode root = new AstNode(AstNode.AstNodeType.PROGRAM, new Token(), 0);

        AstNode Node1 = new AstNode(AstNode.AstNodeType.ASSIGN, new Token("while"), 1);
        Node1.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("i"), 1));
        Node1.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("0"), 1));
        root.addChild(Node1);

        AstNode Node2 = new AstNode(AstNode.AstNodeType.ASSIGN, new Token("while"), 1);
        Node2.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("n"), 1));
        Node2.addChild(new AstNode(AstNode.AstNodeType.NUMBER, new Token("5"), 1));
        root.addChild(Node2);

        AstNode Node3 = new AstNode(AstNode.AstNodeType.WHILE, new Token("while"), 1);
        Node3.addChild(new AstNode(AstNode.AstNodeType.ARG, new Token("i"), 1));
        Node3.addChild(new AstNode(AstNode.AstNodeType.OPERATOR, new Token("<"), 1));
        Node3.addChild(new AstNode(AstNode.AstNodeType.ARG, new Token("n"), 1));
        root.addChild(Node3);

        CodeGenerator codeGen = new CodeGenerator();
        codeGen.init(5, root);
        codeGen.analysis(root);

        List<String> actual = codeGen.getDotText();

        Assert.assertEquals(expected, actual);
    }
}