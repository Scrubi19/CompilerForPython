package CodeGenerator;

import Parser.AST.AstNode;

public class CodeGenerator {

    public void analysis(AstNode node) {

        switch (node.getType()) {
            case DEF:
                break;
        }
        for (AstNode temp : node.getChildren() ) {
            analysis(temp);
        }

    }
}
