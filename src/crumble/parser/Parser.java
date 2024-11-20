package crumble.parser;

import crumble.Crumble;
import crumble.Expr;
import crumble.scanner.Token;
import crumble.scanner.TokenType;

import java.util.List;

import static crumble.scanner.TokenType.*;

public class Parser {
    /**
     * Exception for parse errors that stops further parsing and triggers recovery.
     */
    private static class ParseError extends RuntimeException {
        ParseError(String message) {
            super(message);
        }
    }

    private final List<Token> tokens;
    private int current = 0; // Start at 0 to process the first token immediately.

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    /**
     * Parses the tokens into an expression syntax tree.
     *
     * @return the root of the syntax tree or null if parsing fails.
     */
    public Expr parse() {
        try {
            return expression();
        } catch (ParseError error) {
            synchronize(); // Attempt to recover.
            return null;
        }
    }

    // ===================
    // Token Manipulation
    // ===================

    private Token next() {
        if (current >= tokens.size()) {
            throw new ParseError("Unexpected end of input.");
        }
        return tokens.get(current++);
    }

    private Token peek() {
        if (isAtEnd()) {
            throw new ParseError("No token to peek.");
        }
        return tokens.get(current);
    }

    private Token previous() {
        if (current <= 0) {
            throw new ParseError("No previous token available.");
        }
        return tokens.get(current - 1);
    }

    private boolean isAtEnd() {
        return current >= tokens.size() || tokens.get(current).getType() == EOF;
    }

    /**
     * Advances if the next token matches any of the provided types.
     *
     * @param types the token types to check.
     * @return true if a match was found and the parser advanced, false otherwise.
     */
    private boolean conditionalAdvance(TokenType... types) {
        for (TokenType type : types) {
            if (!isAtEnd() && peek().getType() == type) {
                next();
                return true;
            }
        }
        return false;
    }

    /**
     * Advances if the next token matches the expected type or throws an error.
     *
     * @param expected the expected token type.
     * @param message the error message if the token does not match.
     */
    private void consume(TokenType expected, String message) {
        if (!isAtEnd() && peek().getType() == expected) {
            next();
        } else {
            throw error(peek(), message);
        }
    }

    // ===================
    // Expression Parsing
    // ===================

    private Expr expression() {
        return equality();
    }

    private Expr equality() {
        Expr expr = comparison();

        while (conditionalAdvance(EQUAL_EQUAL, BANG_EQUAL)) {
            Token operator = previous();
            Expr right = comparison();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr comparison() {
        Expr expr = term();

        while (conditionalAdvance(GREATER, GREATER_EQUAL, LESS, LESS_EQUAL)) {
            Token operator = previous();
            Expr right = term();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr term() {
        Expr expr = factor();

        while (conditionalAdvance(MINUS, PLUS)) {
            Token operator = previous();
            Expr right = factor();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr factor() {
        Expr expr = unary();

        while (conditionalAdvance(SLASH, STAR)) {
            Token operator = previous();
            Expr right = unary();
            expr = new Expr.Binary(expr, operator, right);
        }

        return expr;
    }

    private Expr unary() {
        if (conditionalAdvance(BANG, MINUS)) {
            Token operator = previous();
            Expr right = unary();
            return new Expr.Unary(operator, right);
        }

        return primary();
    }

    private Expr primary() {
        if (conditionalAdvance(FALSE)) return new Expr.Literal(false);
        if (conditionalAdvance(TRUE)) return new Expr.Literal(true);
        if (conditionalAdvance(NULL)) return new Expr.Literal(null);

        if (conditionalAdvance(NUMBER, STRING)) {
            return new Expr.Literal(previous().getLiteral());
        }

        if (conditionalAdvance(LEFT_PAREN)) {
            Expr expr = expression();
            consume(RIGHT_PAREN, "Expect ')' after expression.");
            return new Expr.Grouping(expr);
        }

        throw error(peek(), "Expect expression.");
    }

    // ===================
    // Error Handling
    // ===================

    /**
     * Generates a parse error for the given token and message.
     *
     * @param token the token that caused the error.
     * @param message the error message.
     * @return a ParseError exception.
     */
    private ParseError error(Token token, String message) {
        Crumble.error(token, message);
        return new ParseError(message);
    }

    /**
     * Attempts to recover from a parsing error by advancing to a safe state.
     */
    private void synchronize() {
        next();

        while (!isAtEnd()) {
            if (previous().getType() == SEMICOLON) return;

            switch (peek().getType()) {
                case CLASS:
                case FUN:
                case VAR:
                case FOR:
                case IF:
                case WHILE:
                case PRINT:
                case RETURN:
                    return;
            }

            next();
        }
    }
}
