package crumble;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The {@code Scanner} class is responsible for lexical analysis of a source code string.
 * It breaks the source code into individual tokens which can be further processed by a parser.
 * This scanner supports keywords, identifiers, numbers, strings, and several operators.
 */
public class Scanner {
    private final String source;
    private int current = -1; // Index of the current character
    private int start = 0;    // Start index of the current token being processed
    private int line = 1;     // Line number for error reporting
    private final List<Token> tokens = new ArrayList<>(); // List of tokens identified by the scanner

    private static final Map<String, TokenType> keywords; // Map of keywords to their respective token types
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

    /**
     * Constructs a new {@code Scanner} instance with the provided source code.
     * @param source the source code to scan
     */
    public Scanner(String source) {
        this.source = source;
    }

    /**
     * Scans the source code and returns a list of tokens.
     * If an error occurs during scanning, it will attempt to recover and continue.
     * @return a list of tokens from the source code
     */

    public List<Token> scanTokens() {
        //Process token at each loop
        while(!isAtEnd()){
            start = current;
            scanToken();
        }
        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    /**
     * Scans and adds a single token from the current position in the source code.
     */
    private void scanToken() {
        char c = next();
        switch (c) {
            // Single-character tokens
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

            // Two-character tokens
            case '!': addToken(advanceOnMatch('=') ? TokenType.BANG_EQUAL : TokenType.BANG); break;
            case '=': addToken(advanceOnMatch('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL); break;
            case '<': addToken(advanceOnMatch('=') ? TokenType.LESS_EQUAL : TokenType.LESS); break;
            case '>': addToken(advanceOnMatch('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER); break;
            case '/':
                if (advanceOnMatch('/')) {
                    while (peek() != '\n' && !isAtEnd()) next(); // Skip line comment
                } else {
                    addToken(TokenType.SLASH);
                }
                break;

            // Whitespace and line breaks
            case ' ': case '\r': case '\t': break; // Ignore whitespace
            case '\n': line++; break;

            // String literals
            case '"': processString(); break;

            default:
                if (isDigit(c)) processNumber();
                else if (isAlpha(c)) processIdentifier();
                else Crumble.error(line, "Unexpected character: " + c);
        }
    }

    /**
     * Adds a token with the specified type and literal value.
     * @param type the type of the token
     * @param literal the literal value of the token, if any
     */
    private void addToken(TokenType type, Object literal) {
        String lexeme = source.substring(start, current);
        tokens.add(new Token(type, lexeme, literal, line));
    }

    private void addToken(TokenType type) {
        addToken(type, null);
    }

    /**
     * Advances and returns the next character in the source code.
     * Throws an exception if it reads past the end of the source.
     * @return the next character
     */
    private char next() {
        if (++current >= source.length()) {
            throw new IndexOutOfBoundsException("Unexpected end of input");
        }
        return source.charAt(current);
    }

    private char peek() {
        return isAtEnd() ? '\0' : source.charAt(current + 1);
    }

    /**
     * Advances if the next character matches the expected character.
     * @param expected the character to match
     * @return true if the next character matched, false otherwise
     */
    private boolean advanceOnMatch(char expected) {
        if (isAtEnd()) return false;
        if (source.charAt(current + 1) == expected) {
            next();
            return true;
        }
        return false;
    }

    /**
     * Processes a string literal, adding it as a token.
     */
    private void processString() {
        while (!isAtEnd() && peek() != '"') {
            if (next() == '\n') line++;
        }
        if (isAtEnd()) {
            Crumble.error(line, "Unterminated string.");
            return;
        }
        next(); // Consume the closing '"'
        String value = source.substring(start + 1, current); // Remove quotes
        addToken(TokenType.STRING, value);
    }

    /**
     * Processes a numeric literal, adding it as a token.
     */
    private void processNumber() {
        while (isDigit(peek())) next();
        if (peek() == '.' && isDigit(peek())) {
            next(); // Consume '.'
            while (isDigit(peek())) next();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(source.substring(start, current)));
    }

    /**
     * Processes an identifier or a keyword, adding it as a token.
     */
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
