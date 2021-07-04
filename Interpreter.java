package plc.project;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            if (method.getName() == ("main"))
            {
                System.out.println(method.getName());
            }
            visit(method);
        }

        //return Environment.NIL;
        throw new UnsupportedOperationException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Field ast)
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
    public Environment.PlcObject visit(Ast.Method ast) {
        throw new UnsupportedOperationException(); //TODO
    }


    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast)//TODO
    {
        if (ast.getExpression() instanceof Ast.Expr.Function)
        {
            if (((Ast.Expr.Function) ast.getExpression()).getName().equals("print"))
            {
                List list = (((Ast.Expr.Function) ast.getExpression()).getArguments());
                for (Object lit: list)
                {
                    Ast.Expr.Literal printMe = (Ast.Expr.Literal)lit;
                    System.out.println(printMe.getLiteral());
                }
            }
            else
            {
                visit(ast.getExpression());
            }
        }
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
        if (ast.getReceiver() instanceof Ast.Expr.Access)
        {
            if (((Ast.Expr.Access) ast.getReceiver()).getReceiver().isPresent())
            {
            }
            else
            {

            }
            return Environment.NIL;
        }
        throw new RuntimeException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast)//TODO
    {
        try
        {
            if (requireType(Boolean.class, visit(ast.getCondition())))
            {
                for (Ast.Stmt stmt: ast.getThenStatements())
                {
                    visit(stmt);
                }
            }
            else if (!ast.getElseStatements().isEmpty())
            {
                for (Ast.Stmt stmt: ast.getElseStatements())
                {
                    visit(stmt);
                }
            }
        }
        finally
        {
            scope = scope.getParent();
        }

        return Environment.NIL;
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.For ast)//TODO
    {
        List<Ast.Expr.Stmt> statements = ast.getStatements();


        throw new RuntimeException();
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
                if (requireType(Boolean.class, visit(ast.getLeft()))) //LHS must be bool
                {
                    if (visit(ast.getLeft()).getValue().equals(true)) //if the left operand is true
                    {
                        if (((Ast.Expr.Literal) right).getLiteral().equals(true))
                        {
                            if (requireType(Boolean.class, visit(ast.getRight()))) //RHS must be bool
                            {
                                if (visit(ast.getRight()).getValue().equals(true)) //if the right operand is true
                                {
                                    return Environment.create(true);
                                }
                            }
                            else
                            {
                                throw new RuntimeException();
                            }
                        }

                    }
                    return Environment.create(false);
                }
                throw new RuntimeException();

            case "OR":
                if (requireType(Boolean.class, visit(ast.getLeft()))) //LHS must be bool
                {
                    if (visit(ast.getLeft()).getValue().equals(true)) //if the left operand is true
                    {
                        return Environment.create(true);
                    }
                    if (requireType(Boolean.class, visit(ast.getRight()))) //RHS must be bool
                    {
                        if (visit(ast.getRight()).getValue().equals(true)) //if the right operand is true
                        {
                            return Environment.create(true);
                        }
                    }
                    else
                    {
                        throw new RuntimeException();
                    }
                    return Environment.create(false); // otherwise this is false
                }
                throw new RuntimeException(); // was not a bool

            case "<":
                if (requireType(Comparable.class, visit(ast.getLeft())) != null) //LHO must be comparable type
                {
                    if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                    {
                        Object lhs= visit(ast.getLeft()).getValue();
                        Object rhs = visit(ast.getRight()).getValue();
                        if (lhs.toString().compareTo(rhs.toString()) == -1)
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
            case ">":
                if (requireType(Comparable.class, visit(ast.getLeft())) != null) //LHO must be comparable type
                {
                    if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                    {
                        Object lhs= visit(ast.getLeft()).getValue();
                        Object rhs = visit(ast.getRight()).getValue();
                        if (lhs.toString().compareTo(rhs.toString()) == 1)
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
                if (requireType(Comparable.class, visit(ast.getLeft())) != null) //LHO must be comparable type
                {
                    if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                    {
                        Object lhs= visit(ast.getLeft()).getValue();
                        Object rhs = visit(ast.getRight()).getValue();
                        if (lhs.toString().compareTo(rhs.toString()) == 0)
                        {
                            return Environment.create(true);
                        }
                        if (lhs.toString().compareTo(rhs.toString()) == -1)
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
                if (requireType(Comparable.class, visit(ast.getLeft())) != null) //LHO must be comparable type
                {
                    if (left.getClass().equals(right.getClass())) // must be the same class as LHO
                    {
                        Object lhs= visit(ast.getLeft()).getValue();
                        Object rhs = visit(ast.getRight()).getValue();
                        if (lhs.toString().compareTo(rhs.toString()) == 0)
                        {
                            return Environment.create(true);
                        }
                        if (lhs.toString().compareTo(rhs.toString()) == 1)
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

                if (visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue()))
                //if (((Ast.Expr.Literal) left).getLiteral().equals(((Ast.Expr.Literal) right).getLiteral()))
                {
                    return Environment.create(true);
                }
                else
                {
                    return Environment.create(false);
                }

            case "!=":

                if (!visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue()))
                //if (!(((Ast.Expr.Literal) left).getLiteral().equals(((Ast.Expr.Literal) right).getLiteral())))
                {
                    return Environment.create(true);
                }
                else
                {
                    return Environment.create(false);
                }
            case "+":

                if (visit(ast.getLeft()).getClass().equals(visit(ast.getRight()).getClass()))
                //if (((Ast.Expr.Literal) left).getLiteral().getClass().equals(((Ast.Expr.Literal) right).getLiteral().getClass()))
                {
                    if (visit(ast.getLeft()).getValue() instanceof String)
                    //if (((Ast.Expr.Literal) left).getLiteral() instanceof String)
                    {
                        String string1 = (String)((Ast.Expr.Literal) left).getLiteral();
                        String string2 = (String)((Ast.Expr.Literal) right).getLiteral();
                        return Environment.create(string1 + string2);
                    }
                    if (visit(ast.getLeft()).getValue() instanceof BigInteger)
                    //if (((Ast.Expr.Literal) left).getLiteral() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) visit(ast.getLeft()).getValue();
                        BigInteger righty = (BigInteger) visit(ast.getRight()).getValue();
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
        if (ast.getReceiver().isPresent())
        {
            scope.defineVariable(ast.getName(), visit(ast.getReceiver().get()));
            return scope.lookupVariable(ast.getName()).getValue();
        }
        return Environment.create(ast.getName());

    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast) //TODO
    {

        if (ast.getReceiver().isPresent())
        {
            Environment.PlcObject callee = visit(ast.getReceiver().get());

            return visit(ast.getReceiver().get());
        }
        else
        {

            return Environment.create(ast.getName());
        }
        //throw new UnsupportedOperationException();
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
