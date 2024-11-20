package tool;

import crumble.Expr;

public class ASTPrettyPrinter implements Expr.Visitor<String>{

    // Public method to pretty-print the tree
    public String print(Expr expr) {
        return expr.accept(this);
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return buildTree("Binary",
                buildNode("Operator", expr.operator.getLexeme()),
                expr.left.accept(this),
                expr.right.accept(this));
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return buildTree("Grouping", expr.expression.accept(this));
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return buildNode("Literal", expr.value == null ? "null" : expr.value.toString());
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return buildTree("Unary",
                buildNode("Operator", expr.operator.getLexeme()),
                expr.right.accept(this));
    }

    // Helper method to build a single node with a label and optional value
    private String buildNode(String label, String value) {
        return label + ": " + value;
    }

    // Helper method to build a tree structure
    private String buildTree(String label, String... children) {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append("\n");
        for (int i = 0; i < children.length; i++) {
            String prefix = (i == children.length - 1) ? "└── " : "├── ";
            sb.append(indent(children[i], prefix));
        }
        return sb.toString();
    }

    // Helper method to indent child nodes
    private String indent(String text, String prefix) {
        String[] lines = text.split("\n");
        StringBuilder sb = new StringBuilder();
        sb.append(prefix).append(lines[0]).append("\n");
        for (int i = 1; i < lines.length; i++) {
            sb.append("│   ").append(lines[i]).append("\n");
        }
        return sb.toString();
    }
}

