package Semantics;

import Lexer.Token;
import Parser.AST.AstNode;
import Parser.ParserExceptions;

import java.util.HashMap;

import static Lexer.Token.tokenType.*;
import static Parser.AST.AstNode.AstNodeType.*;
import static Parser.AST.AstNode.AstNodeType.FLOAT;
import static Parser.AST.AstNode.AstNodeType.ID;

public class SemanticAnalysis {

    public AstNode root;

    public HashMap<String, Integer> identifierTable = new HashMap<>();

    public SemanticAnalysis(AstNode node, HashMap<String, Integer> Table) {
        root = node;
        identifierTable = Table;
    }
    public void start() throws SemanticsExceptions {
        for(int i = 0; i < root.getChildren().size(); i++) {
            analysis(root.getChildren().get(i));
        }
    }

    public void analysis(AstNode node) throws SemanticsExceptions {
        if (node.getType() == OPERATOR) {
            if(node.getToken().getTokenType() == opAdd || node.getToken().getTokenType() == opSub ||
                node.getToken().getTokenType() == opMul || node.getToken().getTokenType() == opDiv ||
                node.getToken().getTokenType() == opExponentiation || node.getToken().getTokenType() == opIntegerDiv) {
                AstNode parent = node.getParent();

                int opAdd = parent.lookupChildren(node);
                int firstArg = opAdd - 1;
                int secondArg = opAdd + 1;

                // 'int' and 'str' id + int
                if(parent.getChildren().get(firstArg).getType() == ID &&
                    parent.getChildren().get(secondArg).getType() == NUMBER ||
                    parent.getChildren().get(firstArg).getType() == ID &&
                    parent.getChildren().get(secondArg).getType() == FLOAT) {
                    if (node.updatingNodeType(root, parent.getChildren().get(firstArg)).getType() == STRLITERAL) {
                        throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'str' and 'number'"
                                + " in line " + node.getToken().getCol());
                    }
                }
                // 'int' and 'str' int + id
                if(parent.getChildren().get(secondArg).getType() == ID &&
                    parent.getChildren().get(firstArg).getType() == NUMBER ||
                    parent.getChildren().get(secondArg).getType() == ID &&
                    parent.getChildren().get(firstArg).getType() == FLOAT) {
                    if (node.updatingNodeType(root, parent.getChildren().get(secondArg)).getType() == STRLITERAL) {
                        throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'number' and 'str'"
                                + " in line " + node.getToken().getCol());
                    }
                }

                // 'int' and 'str' strLiteral + id
                if(parent.getChildren().get(firstArg).getType() == STRLITERAL &&
                        parent.getChildren().get(secondArg).getType() == ID) {
                    if (node.updatingNodeType(root, parent.getChildren().get(secondArg)).getType() == NUMBER ||
                            node.updatingNodeType(root, parent.getChildren().get(secondArg)).getType() == FLOAT) {
                        throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'str' and 'number'"
                                + " in line " + node.getToken().getCol());
                    }
                }
                // 'int' and 'str' id + strLiteral
                if(parent.getChildren().get(secondArg).getType() == STRLITERAL &&
                        parent.getChildren().get(firstArg).getType() == ID) {
                    if (node.updatingNodeType(root, parent.getChildren().get(firstArg)).getType() == NUMBER ||
                            node.updatingNodeType(root, parent.getChildren().get(firstArg)).getType() == FLOAT) {
                        throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'number' and 'str'"
                                + " in line " + node.getToken().getCol());
                    }
                }

                // id + id
                if(parent.getChildren().get(firstArg).getType() == ID &&
                        parent.getChildren().get(secondArg).getType() == ID) {

                    //'int' and str
                    if(node.updatingNodeType(root, parent.getChildren().get(firstArg)).getType() == STRLITERAL &&
                        node.updatingNodeType(root, parent.getChildren().get(secondArg)).getType() == NUMBER ||
                        node.updatingNodeType(root, parent.getChildren().get(firstArg)).getType() == STRLITERAL &&
                        node.updatingNodeType(root, parent.getChildren().get(secondArg)).getType() == FLOAT) {
                        throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'str' and 'number'"
                                + " in line " + node.getToken().getCol());
                    }
                    if(node.updatingNodeType(root, parent.getChildren().get(secondArg)).getType() == STRLITERAL &&
                        node.updatingNodeType(root, parent.getChildren().get(firstArg)).getType() == NUMBER ||
                        node.updatingNodeType(root, parent.getChildren().get(secondArg)).getType() == STRLITERAL &&
                        node.updatingNodeType(root, parent.getChildren().get(firstArg)).getType() == FLOAT) {
                        throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'number' and 'str'"
                                + " in line " + node.getToken().getCol());
                    }
                }

                //  'int' and 'str'
                if(parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.NUMBER &&
                    parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.STRLITERAL ||
                    parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.NUMBER &&
                    parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.STRLITERAL) {
                    throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'number' and 'str'"
                            + " in line " + node.getToken().getCol());

                //  'float' and 'str'
                } else if(parent.getChildren().get(firstArg).getType() == FLOAT &&
                        parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.STRLITERAL ||
                        parent.getChildren().get(secondArg).getType() == FLOAT &&
                            parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.STRLITERAL) {
                    throw new SemanticsExceptions("\n\tTypeError: unsupported operand type for : 'number' and 'str'"
                            + " in line " + node.getToken().getCol());

                // case INT to FLOAT
                } else if(parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.NUMBER &&
                        parent.getChildren().get(secondArg).getType() == FLOAT) {
                    parent.getChildren().get(firstArg).addChild(new AstNode(INTtoFLOAT, parent.getChildren().get(firstArg).getToken(), node.getLevel()));
                } else if(parent.getChildren().get(firstArg).getType() == FLOAT &&
                        parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.NUMBER) {
                    parent.getChildren().get(secondArg).addChild(new AstNode(INTtoFLOAT, parent.getChildren().get(secondArg).getToken(), node.getLevel()));
                }
            }
        }
        for(AstNode temp : node.getChildren()) {
            analysis(temp);
        }
    }
}
