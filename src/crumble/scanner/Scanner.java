package crumble.scanner;

import crumble.Crumble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {
    private final String source;
    private int current = 0;  // Index of the current character, starting from 0
    private int start = 0;    // Start index of the current token being processed
    private int line = 1;     // Line number for error reporting
    private final List<Token> tokens = new ArrayList<>();

    private static final Map<String, TokenType> keywords;
    static {
        keywords = new HashMap<>();
        keywords.put("and", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("for", TokenType.FOR);
        keywords.put("fun", TokenType.FUN);
        keywords.put("if", TokenType.IF);
        keywords.put("null", TokenType.NULL);
        keywords.put("or", TokenType.OR);
        keywords.put("print", TokenType.PRINT);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    public Scanner(String source) {
        this.source = source;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = next();
        switch (c) {
            case '+': addToken(TokenType.PLUS); break;
            case '-': addToken(TokenType.MINUS); break;
            case '(': addToken(TokenType.LEFT_PAREN); break;
            case ')': addToken(TokenType.RIGHT_PAREN); break;
            case '{': addToken(TokenType.LEFT_BRACE); break;
            case '}': addToken(TokenType.RIGHT_BRACE); break;
            case '*': addToken(TokenType.STAR); break;
            case ';': addToken(TokenType.SEMICOLON); break;
            case ',': addToken(TokenType.COMMA); break;
            case '.': addToken(TokenType.DOT); break;

            case '!': addToken(advanceOnMatch('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(advanceOnMatch('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(advanceOnMatch('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(advanceOnMatch('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '/':
                if (advanceOnMatch('/')) {
                    while (peek() != '\n' && !isAtEnd()) next(); // Skip comment
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            case ' ': case '\r': case '\t': break;
            case '\n': line++; break;

            case '"': processString(); break;

            default:
                if (isDigit(c)) {
                    processNumber();
                } else if (isAlpha(c)) {
                    processIdentifier();
                } else {
                    Crumble.error(line, "Unexpected character: " + c);
                }
        }
    }

    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    private char next() {
        return source.charAt(current++);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current);
    }

    private char peekNext() {
        return (current + 1 >= source.length()) ? '\0' : source.charAt(current + 1);
    }

    private boolean advanceOnMatch(char expected) {
        if (isAtEnd() || source.charAt(current) != expected) return false;
        current++;
        return true;
    }

    private void processString() {
        while (!isAtEnd() && peek() != '"') {
            if (peek() == '\n') line++;
            next();
        }

        if (isAtEnd()) {
            Crumble.error(line, "Unterminated string.");
            return;
        }

        next(); // Consume the closing "
        String value = source.substring(start + 1, current - 1);
        addToken(TokenType.STRING, value);
    }

    private void processNumber() {
        while (isDigit(peek())) next();
        if (peek() == '.' && isDigit(peekNext())) {
            next(); // Consume the '.'
            while (isDigit(peek())) next();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    private void processIdentifier() {
        while (isAlphaNumeric(peek())) next();
        String text = source.substring(start, current);
        TokenType type = keywords.getOrDefault(text, TokenType.IDENTIFIER);
        addToken(type);
    }

    private boolean isAtEnd() {
        return current >= source.length();
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlpha(char c) {
        return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) || isDigit(c);
    }
}
