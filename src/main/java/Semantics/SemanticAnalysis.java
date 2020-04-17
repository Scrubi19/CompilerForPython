package Semantics;

import Parser.AST.AstNode;
import Parser.ParserExceptions;

import java.util.HashMap;

import static Lexer.Token.tokenType.*;
import static Parser.AST.AstNode.AstNodeType.INTtoFLOAT;
import static Parser.AST.AstNode.AstNodeType.OPERATOR;

public class SemanticAnalysis {

    public AstNode root;

    public HashMap<String, Integer> identifierTable = new HashMap<>();

    public SemanticAnalysis(AstNode node, HashMap<String, Integer> Table) {
        root = node;
        identifierTable = Table;
    }
    public void start() throws ParserExceptions {
        for(int i = 0; i < root.getChildren().size(); i++) {
            analysis(root.getChildren().get(i));
        }
    }

    public void analysis(AstNode node) throws ParserExceptions {
        if (node.getType() == OPERATOR) {
            if(node.getToken().getTokenType() == opAdd) {
                AstNode parent = node.getParent();

                int opAdd = parent.lookupChildren(node);
                int firstArg = opAdd - 1;
                int secondArg = opAdd + 1;

                //  'int' and 'str'
                if(parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.NUMBER &&
                    parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.STRLITERAL ||
                    parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.NUMBER &&
                    parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.STRLITERAL) {
                    throw new ParserExceptions("\n\tTypeError: unsupported operand type for : 'int' and 'str'"
                            + " in line " + node.getToken().getCol());

                //  'float' and 'str'
                } else if(parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.FLOAT &&
                        parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.STRLITERAL ||
                        parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.FLOAT &&
                                parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.STRLITERAL) {
                    throw new ParserExceptions("\n\tTypeError: unsupported operand type for : 'float' and 'str'"
                            + " in line " + node.getToken().getCol());

                // case INT to FLOAT
                } else if(parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.NUMBER &&
                        parent.getChildren().get(secondArg).getType() == AstNode.AstNodeType.FLOAT) {
                    parent.getChildren().get(firstArg).addChild(new AstNode(INTtoFLOAT, parent.getChildren().get(firstArg).getToken(), node.getLevel()));
                } else if(parent.getChildren().get(firstArg).getType() == AstNode.AstNodeType.FLOAT &&
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
