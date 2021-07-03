package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Interpreter implements Ast.Visitor<Environment.PlcObject>
{

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
    public Environment.PlcObject visit(Ast.Source ast) //TODO
    {
        boolean hasMain = false;
        List<Ast.Field> fields = ast.getFields();
        List<Ast.Method> methods = ast.getMethods();
        for (Ast.Field field : fields)
        {

            visit(field);
        }
        for (Ast.Method method: methods)
        {
            if (method.getName().equals("main"))
            {
                System.out.println(method.toString());
                List<Ast.Stmt> statements = method.getStatements();
                for (Ast.Stmt statement: statements)
                {
                    System.out.println(statement.toString());
                    //requireType(statement.getClass(), visit(ast));
                }
                hasMain = true;
            }
            visit(method);
        }

        //return Environment.NIL;
        throw new UnsupportedOperationException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast)//TODO
    {

        throw new UnsupportedOperationException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Method ast) {
        throw new UnsupportedOperationException(); //TODO
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast)//TODO
    {
        visit(ast.getExpression());

        return Environment.NIL;
        //throw new UnsupportedOperationException();

    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Declaration ast) //from lecture
    {
        if(ast.getValue().isPresent())
        {
            scope.defineVariable(ast.getName(), visit(ast.getValue().get()));
        }
        else
        {
            scope.defineVariable(ast.getName(), Environment.NIL);
        }
        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Assignment ast)
    {
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
    public Environment.PlcObject visit(Ast.Stmt.While ast) //in lecture
    {
        while(requireType(Boolean.class, visit(ast.getCondition())))
        {
            try
            {
                //ast.getStatements().forEach(this::visit);
                scope = new Scope(scope);
                for (Ast.Stmt stmt: ast.getStatements())
                {
                    visit(stmt);
                }
            }
            finally
            {
                scope = scope.getParent();
            }
        }
        return Environment.NIL;
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
        Ast.Expr left = ast.getLeft();
        Ast.Expr right = ast.getRight();

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
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == -1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal )
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == -1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    System.out.println(ast.getLeft().getClass());
                }
                throw new RuntimeException();
            case ">":
                if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == 1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == 1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                }
                throw new RuntimeException();
            case "<=":
                if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == 0)
                        {
                            return Environment.create(true);
                        }
                        else if (lefty.compareTo(righty) == -1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == 0)
                        {
                            return Environment.create(true);
                        }
                        else if (lefty.compareTo(righty) == -1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                }
                throw new RuntimeException();

            case ">=":
                if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == 0)
                        {
                            return Environment.create(true);
                        }
                        else if (lefty.compareTo(righty) == 1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        if (lefty.compareTo(righty) == 0)
                        {
                            return Environment.create(true);
                        }
                        else if (lefty.compareTo(righty) == 1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                }
                throw new RuntimeException();
            case "==":
                if (((Ast.Expr.Literal) left).getLiteral().equals(((Ast.Expr.Literal) right).getLiteral()))
                {
                    return Environment.create(true);
                }
                else
                {
                    return Environment.create(false);
                }

            case "!=":
                if (!(((Ast.Expr.Literal) left).getLiteral().equals(((Ast.Expr.Literal) right).getLiteral())))
                {
                    return Environment.create(true);
                }
                else
                {
                    return Environment.create(false);
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
                if (((Ast.Expr.Literal) left).getLiteral().getClass().equals(((Ast.Expr.Literal) right).getLiteral().getClass())) //gotta be the same type
                {
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) ((Ast.Expr.Literal) left).getLiteral();
                        BigInteger righty = (BigInteger) (((Ast.Expr.Literal) right).getLiteral());
                        if (righty.equals(BigInteger.ZERO))
                        {
                            throw new RuntimeException();
                        }
                        return Environment.create(lefty.divide(righty));
                    }
                    if (((Ast.Expr.Literal) left).getLiteral() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) ((Ast.Expr.Literal) left).getLiteral();
                        BigDecimal righty = (BigDecimal) (((Ast.Expr.Literal) right).getLiteral());
                        if (righty.equals(BigDecimal.ZERO))
                        {
                            throw new RuntimeException();
                        }
                        return Environment.create(lefty.divide(righty, RoundingMode.HALF_EVEN));
                    }
                }
                throw new RuntimeException();
        }
        throw new RuntimeException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Access ast)  //TODO
    {

        return Environment.create(ast.getName());
        //throw new UnsupportedOperationException();
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
