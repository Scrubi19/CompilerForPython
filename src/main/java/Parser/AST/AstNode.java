package Parser.AST;

import Lexer.*;
import java.util.ArrayList;
import java.util.List;

public class AstNode {

    private AstNodeType Type;
    private Token Token;
    private int level;

    private AstNode Parent;
    private List <AstNode> children = new ArrayList<AstNode>();

    public List<AstNode> getChildren() {
        return children;
    }

    public AstNode getParent() {
        return Parent;
    }

    public AstNode() {
        this.Type = AstNodeType.UNKNOWN;
        this.Token = null;
        this.Parent = null;
        this.level = 0;
    }

    public AstNode(AstNodeType type) {
        this.Type = type;
        this.Token = null;
        this.Parent = null;
        this.level = 0;
    }

    public AstNode(AstNodeType type, Token token) {
        this.Type = type;
        this.Token = token;
        this.level = 0;
    }

    public Token getToken() {
        return Token;
    }

    public AstNode(AstNodeType type, Token token, int level) {
        this.Type = type;
        this.Token = token;
        this.level = level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getLevel() {
        return level;
    }


    public AstNodeType getType() {
        return Type;
    }

    public void addChild(AstNode child) {
        if (child.Parent != null) {
            child.Parent.children.remove(child);
        }
        children.remove(child);
        children.add(child);
        child.Parent = this;
    }

    public void RemoveChild(AstNode child) {
        children.remove(child);
        if (child.Parent == this)
            child.Parent = null;
    }

    public void setToken(Token token) {
        Token = token;
    }

    public enum AstNodeType {
        PROGRAM, NUMBER, ID, ASSIGN, IF, ELSE,
        ELIF, FOR, WHILE, INPUT, DEF, STRLITERAL,
        UNKNOWN, EXPRESSION, OPERATOR, STATEMENT,
        PRINT, ARG, IN, TYPE, RETURN, NULL, LOGIC,
        ARRAY
    }
}
