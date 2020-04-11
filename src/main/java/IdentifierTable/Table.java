package IdentifierTable;

import Lexer.Token;
import Lexer.Token.tokenType;
import Parser.AST.AstNode;

import java.util.HashMap;

public class Table {

    private static HashMap<String, Integer> identifierTable = new HashMap<>();

    public static void tableInitilization(AstNode root) {
        if(root.getType() == AstNode.AstNodeType.ID) {
            identifierTable.put(root.getToken().getString(),root.getLevel());
        }
        if (!root.getChildren().isEmpty()) {
            for(AstNode temp : root.getChildren()) {
                tableInitilization(temp);
            }
        }

    }

    public static HashMap<String, Integer> getIdentifierTable() {
        return identifierTable;
    }
}
