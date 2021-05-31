package plc.project;

import java.util.ArrayList;
import java.util.List;

/**
 * The lexer works through three main functions:
 *
 *  - {@link #lex()}, which repeatedly calls lexToken() and skips whitespace
 *  - {@link #lexToken()}, which lexes the next token
 *  - {@link CharStream}, which manages the state of the lexer and literals
 *
 * If the lexer fails to parse something (such as an unterminated string) you
 * should throw a {@link ParseException} with an index at the character which is
 * invalid or missing.
 *
 * The {@link #peek(String...)} and {@link #match(String...)} functions are
 * helpers you need to use, they will make the implementation a lot easier.
 */
public final class Lexer {

    private final CharStream chars;

    public Lexer(String input) {
        chars = new CharStream(input);
    }

    /**
     * Repeatedly lexes the input using {@link #lexToken()}, also skipping over
     * whitespace where appropriate.
     */
    public List<Token> lex()
    {
        List<Token> tokens = new ArrayList<>();
        //while(chars.has(0))
        while(chars.index <= chars.input.length())
        //while(peek("."))
        {
            if(peek("[^ \b\n\r\t]")) // if its not whitespace
            {
                tokens.add(lexToken());
            }
            else
            {
                chars.advance();
                chars.skip();
            }
        }
        return tokens;
    }

    /**
     * This method determines the type of the next token, delegating to the
     * appropriate lex method. As such, it is best for this method to not change
     * the state of the char stream (thus, use peek not match).
     *
     * The next character should start a valid token since whitespace is handled
     * by {@link #lex()}
     */
    public Token lexToken()
    {
        if(peek("[A-Za-z_]")) // identifier
        {
            return lexIdentifier();
        }
        else if(peek("[+-]", "[0-9]")) // number
        {
            return lexNumber();
        }
        else if(peek( "[0-9]")) // number
        {
            return lexNumber();
        }
        else if(peek("\'")) // character
        {
            return lexCharacter();
        }
        else if(peek("\"")) // string
        {
            return lexString();
        }
        else if(peek("\\\\")) // escape
        {
            lexEscape();
        }
        else
        {
            return lexOperator();
        }
        throw new ParseException("Error at index: " + chars.index, chars.index);
    }

    public Token lexIdentifier() {
        if(match("[A-Za-z_]"))
        {
            while(peek("[A-Za-z0-9_-]"))
            {
                match("[A-Za-z0-9_-]");
            }
        }
        return chars.emit(Token.Type.IDENTIFIER);
    }

    public Token lexNumber()
    {
        match("[+-]");
        while(peek("[0-9]"))
        {
            match("[0-9]");
        }
        if(peek("\\.", "[0-9]"))
        {
            if(match("\\."))
            {
                while(peek("[0-9]"))
                {
                    match("[0-9]");
                }
                return chars.emit(Token.Type.DECIMAL);
            }
        }
        return chars.emit(Token.Type.INTEGER);
    }

    public Token lexCharacter()
    {
        if(match("\'"))
        {
            if (match("\\\\"))
            {
                if (match("[bnrt'\"\\\\]"))
                {
                    if (match("\'"))
                    {
                        return chars.emit(Token.Type.CHARACTER);
                    }
                }
            }
            if (match("[^'\n\r]"))
            {
                if (match("'"))
                {
                    return chars.emit(Token.Type.CHARACTER);
                }
            }

        }
        throw new ParseException("invalid Char", chars.index);

    }

    public Token lexString()
    {
        match("\"");
        while(match("[^\"\n\r]"))
        {
            if(match("[\n\r]"))
            {
                throw new ParseException("invalid escape", chars.index);
            }
            if(match("\\\\"))
            {
                if(!match("[bnrt'\"\\\\]"))
                {
                    throw new ParseException("invalid escape", chars.index);
                }
            }
        }
        if (match("\""))
        {
            return chars.emit(Token.Type.STRING);
        }
        else
        {
            throw new ParseException("invalid string", chars.index);
        }
    }

    public void lexEscape()
    {
        if (match("\\\\"))
        {
            if (match("[bnrt\'\"\\\\]"))
            {
                return;
            }
        }
        throw new ParseException("Invalid Escape", chars.index);
    }

    public Token lexOperator()
    {

        if (match("[<>!=]"))
        {
            if (match("="))
            {
                return chars.emit(Token.Type.OPERATOR);
            }
            else
            {
                return chars.emit(Token.Type.OPERATOR);
            }
        }
        match(".");
        return chars.emit(Token.Type.OPERATOR);
    }

    /**
     * Returns true if the next sequence of characters match the given patterns,
     * which should be a regex. For example, {@code peek("a", "b", "c")} would
     * return true if the next characters are {@code 'a', 'b', 'c'}.
     */
    public boolean peek(String... patterns)
    {
        for(int i = 0; i < patterns.length; i++)
        {
            if(!chars.has(i) || !String.valueOf(chars.get(i)).matches(patterns[i]))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns true in the same way as {@link #peek(String...)}, but also
     * advances the character stream past all matched characters if peek returns
     * true. Hint - it's easiest to have this method simply call peek.
     */
    public boolean match(String... patterns)
    {
        boolean peek = peek(patterns);
        if(peek)
        {
            for(int i = 0; i < patterns.length; i++)
            {
                chars.advance();
            }
        }
        return peek;
    }

    /**
     * A helper class maintaining the input string, current index of the char
     * stream, and the current length of the token being matched.
     *
     * You should rely on peek/match for state management in nearly all cases.
     * The only field you need to access is {@link #index} for any {@link
     * ParseException} which is thrown.
     */
    public static final class CharStream {

        private final String input; // the source string
        private int index = 0; // position within source
        private int length = 0; // size of current token

        public CharStream(String input) {
            this.input = input;
        }

        // checks if input has offset characters remaining "Ask has before get!"
        public boolean has(int offset) {
            return index + offset < input.length();
        }

        // returns char at offset position "Ask has before get!"
        public char get(int offset) {
            return input.charAt(index + offset);
        }

        // moves to the next char position in the input
        public void advance() {
            index++;
            length++;
        }

        // resets the size of the current token to 0. Used with advance
        public void skip() {
            length = 0;
        }

        // instantiates the current token
        public Token emit(Token.Type type) {
            int start = index - length;
            skip();
            return new Token(type, input.substring(start, index), start);
        }

    }

}
