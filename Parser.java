package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * The parser takes the sequence of tokens emitted by the lexer and turns that
 * into a structured representation of the program, called the Abstract Syntax
 * Tree (AST).
 *
 * The parser has a similar architecture to the lexer, just with {@link Token}s
 * instead of characters. As before, {@link #peek(Object...)} and {@link
 * #match(Object...)} are helpers to make the implementation easier.
 *
 * This type of parser is called <em>recursive descent</em>. Each rule in our
 * grammar will have it's own function, and reference to other rules correspond
 * to calling that functions.
 */
public final class Parser {

    private final TokenStream tokens;

    public Parser(List<Token> tokens) {
        this.tokens = new TokenStream(tokens);
    }

    /**
     * Parses the {@code source} rule.
     */
    public Ast.Source parseSource() throws ParseException
    {
        List <Ast.Field> fields = new ArrayList<>();
        List <Ast.Method> methods = new ArrayList<>();
        while(peek("LET")) // if the next token starts a field
        {
            fields.add(parseField());
        }
        while(peek("DEF"))
        {
            methods.add(parseMethod());
        }
        return new Ast.Source(fields, methods);
    }

    /**
     * Parses the {@code field} rule. This method should only be called if the
     * next tokens start a field, aka {@code LET}.
     */
    public Ast.Field parseField() throws ParseException
    {
        if(match("LET", Token.Type.IDENTIFIER))
        {
            String name = tokens.get(-1).getLiteral();
            if(match("="))
            {
                Ast.Expr expr = parseExpression();
                if(match(";"))
                {
                    return new Ast.Field(name, Optional.of(expr));
                }
                else
                {
                    int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                    throw new ParseException("Field Missing Semi-Colon", index);
                }
            }
            else if(match(";"))
            {
                return new Ast.Field(name, Optional.empty());
            }
        }
        throw new ParseException("invalid field", tokens.get(0).getIndex());

    }

    /**
     * Parses the {@code method} rule. This method should only be called if the
     * next tokens start a method, aka {@code DEF}.
     */
    public Ast.Method parseMethod() throws ParseException {

        List<String> parameters = new ArrayList<>();
        List<Ast.Stmt> statements = new ArrayList<>();
        if(match("DEF")) {
            if (match(Token.Type.IDENTIFIER))
            {
                if (match("("))
                {
                    String name = tokens.get(-2).getLiteral();
                    if(match(Token.Type.IDENTIFIER)) // if there is something in the paren
                    {
                        while(match(","))
                        {
                            if(match(Token.Type.IDENTIFIER))
                            {
                                parameters.add(tokens.get(-1).getLiteral());
                            }
                            else
                            {
                                throw new ParseException("invalid identifier", tokens.get(0).getIndex());
                            }
                        }
                    }
                    else if (match(")"))
                    {

                        if (match("DO"))
                        {

                            while (!peek("END") && tokens.has(2))
                            {
                                statements.add(parseStatement());
                            }
                            if (match("END") && !tokens.has(0))
                            {
                                return new Ast.Method(name, parameters, statements);
                            }
                            else
                            {
                                int index = (tokens.get(1).getIndex() + tokens.get(1).getLiteral().length());
                                throw new ParseException("Method Missing \"END\"", index);
                            }
                        }
                        else
                        {
                            if (tokens.has(0))
                            {
                                int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
                                throw new ParseException("Expected \"DO\"", index);
                            }
                            else
                            {
                                int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                                throw new ParseException("Expected \"DO\"", index);
                            }
                        }
                    }
                    else
                    {
                        if (tokens.has(0))
                        {
                            int index = (tokens.get(-1).getIndex());// + tokens.get(-1).getLiteral().length());
                            throw new ParseException("Method missing Closing paren", index);
                        }
                        else
                        {
                            int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                            throw new ParseException("Method missing Closing paren", index);
                        }
                    }
                }
                else
                {
                    if (tokens.has(0))
                    {
                        int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
                        throw new ParseException("Method missing opening paren", index);
                    }
                    else
                    {
                        int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                        throw new ParseException("Method missing opening paren", index);
                    }

                }
            }
            else
            {
                int index = (tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                throw new ParseException("Method missing identifier", index);
            }
        }

        System.out.println(tokens.get(1).getIndex() + tokens.get(1).getLiteral().length());
        throw new ParseException("Invalid Method", tokens.get(1).getIndex());
    }

    /**
     * Parses the {@code statement} rule and delegates to the necessary method.
     * If the next tokens do not start a declaration, if, while, or return
     * statement, then it is an expression/assignment statement.
     */
    public Ast.Stmt parseStatement() throws ParseException
    {
        if(peek("LET"))
        {
            return parseDeclarationStatement();
        }
        else if(peek("IF"))
        {
            return parseIfStatement();
        }
        else if(peek("FOR"))
        {
            return parseForStatement();
        }
        else if(peek("WHILE"))
        {
            return parseWhileStatement();
        }
        else if(peek("RETURN"))
        {
            return parseReturnStatement();
        }
        else
        {
            Ast.Stmt.Expr receiver = parseExpression();
            if (match("="))
            {
                Ast.Expr value = parseExpression();
                if(match(";"))
                {
                    return new Ast.Stmt.Assignment(receiver, value);
                }
                else
                {
                    int index = tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length();
                    throw new ParseException("Missing semi-colon", index);
                }
            }
            if (match(";"))
            {
                return new Ast.Stmt.Expression(receiver);
            }
            else
            {
                int index = tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length();
                throw new ParseException("Missing semicolon", index);
            }
        }
    }

    /**
     * Parses a declaration statement from the {@code statement} rule. This
     * method should only be called if the next tokens start a declaration
     * statement, aka {@code LET}.
     */
    public Ast.Stmt.Declaration parseDeclarationStatement() throws ParseException
    {
        if(match("LET", Token.Type.IDENTIFIER))
        {
            String name = tokens.get(-1).getLiteral();
            if(match("="))
            {
                Ast.Expr expr = parseExpression();
                if(match(";"))
                {
                    return new Ast.Stmt.Declaration(name, Optional.of(expr));
                }
            }
            else if(match(";"))
            {
                return new Ast.Stmt.Declaration(name, Optional.empty());
            }
        }
        throw new ParseException("invalid declaration statement", tokens.get(-1).getIndex());
    }

    /**
     * Parses an if statement from the {@code statement} rule. This method
     * should only be called if the next tokens start an if statement, aka
     * {@code IF}.
     */
    public Ast.Stmt.If parseIfStatement() throws ParseException
    {
        List<Ast.Stmt> doStatements = new ArrayList<>();
        List<Ast.Stmt> elseStatements = new ArrayList<>();
        if (match("IF"))
        {
            Ast.Expr expr = parseExpression();
            if (match("DO"))
            {
                while (!peek("ELSE") && !peek("END"))
                {
                    doStatements.add(parseStatement());
                }
                while(match("ELSE"))
                {
                    elseStatements.add(parseStatement());
                }
                if (match("END"))
                {
                    return new Ast.Stmt.If(expr, doStatements, elseStatements);
                }
                else
                {
                    if (tokens.has(0))
                    {
                        int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
                        throw new ParseException("Expected \"END\"", index);
                    }
                    else
                    {
                        int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                        throw new ParseException("Expected \"END\"", index);
                    }
                }
            }
            else
            {
                if (tokens.has(0))
                {
                    int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
                    throw new ParseException("Expected \"DO\"", index);
                }
                else
                {
                    int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                    throw new ParseException("Expected \"DO\"", index);
                }
            }
        }
        throw new ParseException("invalid IF statement", tokens.index); //TODO fix index!
    }

    /**
     * Parses a for statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a for statement, aka
     * {@code FOR}.
     */
    public Ast.Stmt.For parseForStatement() throws ParseException
    {
        if (match("FOR"))
        {
            if (match(Token.Type.IDENTIFIER))
            {
                if (match("IN"))
                {
                    List<Ast.Stmt> statements = new ArrayList<>();
                    String name = tokens.get(-2).getLiteral();
                    Ast.Expr expr = parseExpression();
                    if (match("DO"))
                    {
                        while (!peek("END") && tokens.has(0))
                        {
                            statements.add(parseStatement());
                        }
                        if (match("END"))
                        {
                            return new Ast.Stmt.For(name, expr, statements);
                        }
                        else
                        {
                            int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                            throw new ParseException("Expected \"END\"", index);
                        }
                    }
                    else
                    {
                        if (tokens.has(0))
                        {
                            int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
                            throw new ParseException("Expected \"DO\"", index);
                        }
                        else
                        {
                            int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                            throw new ParseException("Expected \"DO\"", index);
                        }
                    }
                }
                else // expected IN
                {
                    if (tokens.has(0))
                    {
                        int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
                        throw new ParseException("Expected \"IN\"", index);
                    }
                    else
                    {
                        int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                        throw new ParseException("Expected \"IN\"", index);
                    }
                }
            }
            else //invalid identifier
            {
                if (tokens.has(0))
                {
                    int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
                    throw new ParseException("Invalid Identifier", index);
                }
                else
                {
                    int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
                    throw new ParseException("Invalid Identifier", index);
                }
            }


        }

        if (tokens.has(0))
        {
            int index = (tokens.get(0).getIndex());// + tokens.get(-1).getLiteral().length());
            throw new ParseException("Invalid FOR statement", index);
        }
        else
        {
            int index = (tokens.get(-1).getIndex()) + tokens.get(-1).getLiteral().length();
            throw new ParseException("Invalid FOR statement", index);
        }
        //throw new ParseException("Invalid For Statement", tokens.index); //TODO fix index!
    }

    /**
     * Parses a while statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a while statement, aka
     * {@code WHILE}.
     */
    public Ast.Stmt.While parseWhileStatement() throws ParseException
    {
        if (match("WHILE"))
        {
            List<Ast.Stmt> statements = new ArrayList<>();
            Ast.Expr expr = parseExpression();
            if (match("DO"))
            {
                while (!peek("END") && tokens.has(2))
                {
                    statements.add(parseStatement());
                }
                if (match("END"))
                {
                    return new Ast.Stmt.While(expr, statements);
                }
            }
        }
        throw new ParseException("Invalid while statement", tokens.get(0).getIndex()); //TODO
    }

    /**
     * Parses a return statement from the {@code statement} rule. This method
     * should only be called if the next tokens start a return statement, aka
     * {@code RETURN}.
     */
    public Ast.Stmt.Return parseReturnStatement() throws ParseException
    {
        if (match("RETURN"))
        {
            Ast.Expr expr = parseExpression();
            if (match(";"))
            {
                return new Ast.Stmt.Return(expr);
            }
        }
        throw new ParseException("Invalid return statement", tokens.get(0).getIndex()); //TODO
    }

    /**
     * Parses the {@code expression} rule.
     */
    public Ast.Expr parseExpression() throws ParseException {
        return parseLogicalExpression();
    }

    /**
     * Parses the {@code logical-expression} rule.
     */
    public Ast.Expr parseLogicalExpression() throws ParseException {
        Ast.Expr expr = parseEqualityExpression();
        while(match("AND") || match("OR"))
        {
            String operator = tokens.get(-1).getLiteral();
            if(tokens.has(0))
            {
                Ast.Expr right = parseEqualityExpression();
                expr = new Ast.Expr.Binary(operator, expr, right);
            }
            else
            {
                throw new ParseException("Missing Operand",tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }

        }
        return expr;
    }

    /**
     * Parses the {@code equality-expression} rule.
     */
    public Ast.Expr parseEqualityExpression() throws ParseException {
        Ast.Expr expr = parseAdditiveExpression();
        while(match("<") || match("<=") || match(">") || match(">=") || match("==") || match("!="))
        {
            String operator = tokens.get(-1).getLiteral();
            if(tokens.has(0))
            {
                Ast.Expr right = parseAdditiveExpression();
                expr = new Ast.Expr.Binary(operator, expr, right);
            }
            else
            {
                throw new ParseException("Missing Operand",tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
        }
        return expr;
    }

    /**
     * Parses the {@code additive-expression} rule.
     */
    public Ast.Expr parseAdditiveExpression() throws ParseException {
        Ast.Expr expr = parseMultiplicativeExpression();
        while (match("+") || match("-"))
        {
            String operator = tokens.get(-1).getLiteral();
            if(tokens.has(0))
            {
                Ast.Expr right = parseMultiplicativeExpression();
                expr = new Ast.Expr.Binary(operator, expr, right);
            }
            else
            {
                throw new ParseException("Missing Operand", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
        }
        return expr;
    }

    /**
     * Parses the {@code multiplicative-expression} rule.
     */
    public Ast.Expr parseMultiplicativeExpression() throws ParseException {
        Ast.Expr expr = parseSecondaryExpression();
        while(match("*") || match("/"))
        {
            String operator = tokens.get(-1).getLiteral();
            if(tokens.has(0))
            {
                Ast.Expr right = parseSecondaryExpression();
                expr = new Ast.Expr.Binary(operator, expr, right);
            }
            else
            {
                throw new ParseException("Missing Operand", tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
            }
        }
        return expr;
    }

    /**
     * Parses the {@code secondary-expression} rule.
     */
    public Ast.Expr parseSecondaryExpression() throws ParseException {
        Ast.Expr expr = parsePrimaryExpression();

        while(match("."))
        {
            if(match(Token.Type.IDENTIFIER))
            {
                String methodName = tokens.get(-1).getLiteral();
                List <Ast.Expr> exprs = new ArrayList<>();

                if(match("("))
                {

                    if(match(")")) // if the parenthesis are empty
                    {
                        exprs = Arrays.asList();
                        expr = new Ast.Expr.Function(Optional.of(expr), methodName ,exprs);
                        continue;
                    }
                    else // the paren are not empty
                    {
                        exprs = new ArrayList<>(exprs);
                        exprs.add(parseExpression());
                        while(match(","))
                        {
                            exprs.add(parseExpression());
                        }
                        if(match(")")) // if the parenthesis are empty
                        {
                            return new Ast.Expr.Function(Optional.of(expr), methodName ,exprs);
                        }
                        else
                        {
                            throw new ParseException("Invalid Identifier.", tokens.get(-1).getIndex());
                        }
                    }
                }
                if(Character.isLetter(tokens.get(-1).getLiteral().charAt(0)) || tokens.get(-1).getLiteral().charAt(0) == '_')
                {

                    expr = new Ast.Expr.Access(Optional.of(expr), tokens.get(-1).getLiteral());
                }
                else
                {

                    throw new ParseException("Invalid Identifier.", tokens.get(-1).getIndex());
                }

            }
            else
            {
                throw new ParseException("Invalid Identifier.", tokens.get(-1).getIndex());
            }
        }
        return expr;

    }

    /**
     * Parses the {@code primary-expression} rule. This is the top-level rule
     * for expressions and includes literal values, grouping, variables, and
     * functions. It may be helpful to break these up into other methods but is
     * not strictly necessary.
     */
    public Ast.Expr parsePrimaryExpression() throws ParseException {
        if(match("TRUE"))
        {
            return new Ast.Expr.Literal(true);
        }
        else if(match("FALSE"))
        {
            return new Ast.Expr.Literal(false);
        }
        else if(match("NIL"))
        {
            return new Ast.Expr.Literal(null);
        }
        else if(match(Token.Type.INTEGER))
        {
            return new Ast.Expr.Literal(new BigInteger(tokens.get(-1).getLiteral()));
        }
        else if(match(Token.Type.DECIMAL))
        {
            return new Ast.Expr.Literal(new BigDecimal(tokens.get(-1).getLiteral()));
        }
        else if(match(Token.Type.CHARACTER))
        {
            String charString = tokens.get(-1).getLiteral();
            charString = charString.substring(1, charString.length() - 1);
            charString = charString.replace("\\b", "\b");
            charString = charString.replace("\\n", "\n");
            charString = charString.replace("\\r", "\r");
            charString = charString.replace("\\t", "\t");
            charString = charString.replace("\\'", "\'");
            charString = charString.replace("\\\"", "\"");
            charString = charString.replace("\\\\", "\\");
            char newChar = charString.charAt(0);
            return new Ast.Expr.Literal(newChar);
        }
        else if(match(Token.Type.STRING))
        {
            String newString = tokens.get(-1).getLiteral();
            newString = newString.substring(1, newString.length() - 1);
            newString = newString.replace("\\b", "\b");
            newString = newString.replace("\\n", "\n");
            newString = newString.replace("\\r", "\r");
            newString = newString.replace("\\t", "\t");
            newString = newString.replace("\\'", "\'");
            newString = newString.replace("\\\"", "\"");
            newString = newString.replace("\\\\", "\\");
            return new Ast.Expr.Literal(newString);
        }
        else if(match(Token.Type.IDENTIFIER)) //variable
        {
            String functionName = tokens.get(-1).getLiteral();

            if(match("(")) // TODO: handle function case if next token is '('
            {
                List <Ast.Expr> exprs;
                exprs = Arrays.asList();
                if(match(")")) // if the parenthesis are empty
                {

                    return new Ast.Expr.Function(Optional.empty(), functionName, exprs);
                }
                exprs = new ArrayList<>(exprs);
                exprs.add(parseExpression());
                while(match(","))
                {
                    exprs.add(parseExpression());
                }
                if(match(")")) // if the parenthesis are not empty
                {
                    return new Ast.Expr.Function(Optional.empty(), functionName, exprs);
                }
                else
                {
                    int index = (tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                    throw new ParseException("Expected Closing Paren.", index);
                }
            }
            return new Ast.Expr.Access(Optional.empty(), functionName); // obj.method() obj is receiver "Alan Kay message passing"

        }
        else if(match("(")) // grouped expression
        {
            Ast.Expr expr = parseExpression();
            if(!match(")")) // if we don't find closing paren
            {
                int index = (tokens.get(-1).getIndex() + tokens.get(-1).getLiteral().length());
                throw new ParseException("Expected closing parenthesis.", index);
            }
            return new Ast.Expr.Group(expr);
        }
        else
        {
            if(tokens.get(0).getLiteral().length() == 1) //invalid expression
            {
                throw new ParseException("Invalid Expression", 0);
            }
            throw new ParseException("Invalid primary expression.", (tokens.get(0).getIndex() + tokens.get(0).getLiteral().length()));
        }
    }

    /**
     * As in the lexer, returns {@code true} if the current sequence of tokens
     * matches the given patterns. Unlike the lexer, the pattern is not a regex;
     * instead it is either a {@link Token.Type}, which matches if the token's
     * type is the same, or a {@link String}, which matches if the token's
     * literal is the same.
     *
     * In other words, {@code Token(IDENTIFIER, "literal")} is matched by both
     * {@code peek(Token.Type.IDENTIFIER)} and {@code peek("literal")}.
     */
    private boolean peek(Object... patterns) {
        for (int i = 0; i < patterns.length; i++)
        {
            if (!tokens.has(i))
            {
                return false;
            }
            else if(patterns[i] instanceof Token.Type)
            {
                if(patterns[i] != tokens.get(i).getType())
                {
                    return false;
                }
            }
            else if(patterns[i] instanceof String)
            {
                if(!patterns[i].equals(tokens.get(i).getLiteral()))
                {
                    return false;
                }
            }
            else
            {
                throw new AssertionError("Invalid pattern object: " + patterns[i].getClass());
            }
        }
        return true;
    }

    /**
     * As in the lexer, returns {@code true} if {@link #peek(Object...)} is true
     * and advances the token stream.
     */
    private boolean match(Object... patterns) {
        boolean peek = peek(patterns);
        if(peek)
        {
            for(int i = 0; i < patterns.length; i++)
            {
                tokens.advance();
            }
        }
        return peek;
    }

    private static final class TokenStream {

        private final List<Token> tokens;
        private int index = 0;

        private TokenStream(List<Token> tokens) {
            this.tokens = tokens;
        }

        /**
         * Returns true if there is a token at index + offset.
         */
        public boolean has(int offset) {
            return index + offset < tokens.size();
        }

        /**
         * Gets the token at index + offset.
         */
        public Token get(int offset) {
            return tokens.get(index + offset);
        }

        /**
         * Advances to the next token, incrementing the index.
         */
        public void advance() {
            index++;
        }
    }
}
