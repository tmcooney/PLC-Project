package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject> {

    private Scope scope = new Scope(null);

    public Interpreter(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", 1, args -> {
            System.out.println(args.get(0).getValue());
            return Environment.NIL;
        });
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Environment.PlcObject visit(Ast.Source ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) {
        throw new UnsupportedOperationException(); //TODO (in lecture)
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) {
        throw new UnsupportedOperationException(); //TODO (in lecture)
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Return ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Literal ast)
    {
        if (ast.getLiteral() == null)
        {
            return Environment.NIL;
        }
        return Environment.create(ast.getLiteral());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Group ast)
    {
        return visit(ast.getExpression());
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Binary ast)
    {
        Object left = ast.getLeft();
        Object right = ast.getRight();
        switch (ast.getOperator())
        {
            case "AND":
                if (((Ast.Expr.Literal) left).getLiteral().equals(true)) //if the left operand is true
                {
                    if (((Ast.Expr.Literal) right).getLiteral().equals(true))
                    {
                        return Environment.create(true);
                    }
                }
                return Environment.create(false);
            case "OR":
                if (((Ast.Expr.Literal) left).getLiteral().equals(true))
                {
                    return Environment.create(true);
                }
                if (((Ast.Expr.Literal) right).getLiteral().equals(true))
                {
                    return Environment.create(true);
                }
                return Environment.create(false);
            case "<":

                if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                {

                }

            case "+":

                if (((Ast.Expr.Literal) left).getLiteral().getClass().equals(((Ast.Expr.Literal) right).getLiteral().getClass()))
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof String)
                    {
                        String string1 = (String)((Ast.Expr.Literal) left).getLiteral();
                        String string2 = (String)((Ast.Expr.Literal) right).getLiteral();
                        return Environment.create(string1 + string2);
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.add(righty));
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.add(righty));
                    }
                }
                throw new RuntimeException();

            case "-":

                if (((Ast.Expr.Literal) left).getLiteral().getClass().equals(((Ast.Expr.Literal) right).getLiteral().getClass()))
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.subtract(righty));
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.subtract(righty));
                    }
                }
                throw new RuntimeException();

            case "*":
                if (((Ast.Expr.Literal) left).getLiteral().getClass().equals(((Ast.Expr.Literal) right).getLiteral().getClass()))
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.multiply(righty));
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.multiply(righty));
                    }

                }
                throw new RuntimeException();

            case "/":
                if (((Ast.Expr.Literal) left).getLiteral().getClass().equals(((Ast.Expr.Literal) right).getLiteral().getClass()))
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.divide(righty));
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        return Environment.create(lefty.divide(righty, RoundingMode.HALF_EVEN));
                    }
                }
                throw new RuntimeException();

        }
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    /**
     * Helper function to ensure an object is of the appropriate type.
     */
    private static <T> T requireType(Class<T> type, Environment.PlcObject object) {
        if (type.isInstance(object.getValue())) {
            return type.cast(object.getValue());
        } else {
            throw new RuntimeException("Expected type " + type.getName() + ", received " + object.getValue().getClass().getName() + ".");
        }
    }

    /**
     * Exception class for returning values.
     */
    private static class Return extends RuntimeException {

        private final Environment.PlcObject value;

        private Return(Environment.PlcObject value) {
            this.value = value;
        }

    }

}
