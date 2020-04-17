package IdentifierTable;

import Parser.AST.AstNode;
import java.util.HashMap;

public class Table {

    private static HashMap<String, Integer> identifierTable = new HashMap<>();

    public static void tableInitialization(AstNode root) {
        if(root.getType() == AstNode.AstNodeType.ID || root.getType() == AstNode.AstNodeType.ARRAY) {
            identifierTable.put(root.getToken().getString(),root.getLevel());
        }
        if (!root.getChildren().isEmpty()) {
            for(AstNode temp : root.getChildren()) {
                tableInitialization(temp);
            }
        }
    }

    public static HashMap<String, Integer> getIdentifierTable() {
        return identifierTable;
    }
}
