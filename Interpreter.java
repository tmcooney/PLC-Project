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
    public Environment.PlcObject visit(Ast.Method ast)  //TODO
    {
        try
        {
            scope = new Scope(scope);
            scope.defineFunction(ast.getName(), ast.getParameters().size(), args -> {

                for (int i = 0; i < args.size(); i++)
                {
                    scope.defineVariable(ast.getParameters().get(i), args.get(i));
                }
                for (Ast.Stmt stmt: ast.getStatements())
                {
                    visit(stmt);
                }
                return Environment.NIL;
            });
        }
        finally
        {
            scope = scope.getParent();
        }
        return Environment.NIL;

    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.Expression ast)
    {
        if (ast.getExpression() instanceof Ast.Expr.Function)
        {
            if (((Ast.Expr.Function) ast.getExpression()).getName().equals("print"))
            {
                List<Ast.Expr> list = (((Ast.Expr.Function) ast.getExpression()).getArguments());
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
        if (ast.getReceiver() instanceof Ast.Expr.Access) // ensure the receiver is an Ast.Expr.Access
        {
            if (((Ast.Expr.Access) ast.getReceiver()).getReceiver().isPresent())// if that access expression has a receiver, evaluate it and set a field
            {
                visit(((Ast.Expr.Access) ast.getReceiver()).getReceiver().get()).setField(((Ast.Expr.Access) ast.getReceiver()).getName(), visit(ast.getValue()));
            }
            else //otherwise lookup and set a variable in the current scope
            {
                scope.lookupVariable(((Ast.Expr.Access) ast.getReceiver()).getName()).setValue(visit(ast.getValue()));
            }
            return Environment.NIL;
        }
        throw new RuntimeException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.If ast)
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
    public Environment.PlcObject visit(Ast.Stmt.For ast)
    {
        if(requireType (Iterable.class, visit(ast.getValue())) != null) // ensure value is type Iterable
        {
            for (Object obj: (Iterable<?>) visit(ast.getValue()).getValue())
            {
                try
                {
                    scope = new Scope(scope);
                    scope.defineVariable(ast.getName(), (Environment.PlcObject)obj);
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
        throw new RuntimeException();
    }

    @Override
    public Environment.PlcObject visit(Ast.Stmt.While ast) //from lecture
    {
        while(requireType(Boolean.class, visit(ast.getCondition())))
        {
            try
            {
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
    public Environment.PlcObject visit(Ast.Stmt.Return ast)
    {
        Environment.PlcObject value = Environment.NIL;
        if (ast.getValue() != null)
        {
            value = visit(ast.getValue());
        }
        throw new Return(value);
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
        switch (ast.getOperator())
        {
            case "AND":
                if (requireType(Boolean.class, visit(ast.getLeft()))) //LHS must be bool
                {
                    if (visit(ast.getLeft()).getValue().equals(true)) //if the left operand is true
                    {
                        if (visit(ast.getRight()).getValue().equals(true))
                        {
                            return Environment.create(true);
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
                    Object lhs= visit(ast.getLeft()).getValue();
                    Object rhs = visit(ast.getRight()).getValue();
                    if (lhs instanceof BigInteger && rhs instanceof BigInteger)
                    {
                        if (((BigInteger) lhs).compareTo((BigInteger) rhs) == -1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    else if (lhs instanceof BigDecimal && rhs instanceof BigDecimal)
                    {
                        if (((BigDecimal) lhs).compareTo((BigDecimal) rhs) == -1)
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
                    Object lhs= visit(ast.getLeft()).getValue();
                    Object rhs = visit(ast.getRight()).getValue();
                    if (lhs instanceof BigInteger && rhs instanceof BigInteger)
                    {
                        if (((BigInteger) lhs).compareTo((BigInteger) rhs) == 1)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    else if (lhs instanceof BigDecimal && rhs instanceof BigDecimal)
                    {
                        if (((BigDecimal) lhs).compareTo((BigDecimal) rhs) == 1)
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
                    Object lhs= visit(ast.getLeft()).getValue();
                    Object rhs = visit(ast.getRight()).getValue();
                    if (lhs instanceof BigInteger && rhs instanceof BigInteger)
                    {
                        if (((BigInteger) lhs).compareTo((BigInteger) rhs) == -1 || ((BigInteger) lhs).compareTo((BigInteger) rhs) == 0)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    else if (lhs instanceof BigDecimal && rhs instanceof BigDecimal)
                    {
                        if (((BigDecimal) lhs).compareTo((BigDecimal) rhs) == -1 || ((BigDecimal) lhs).compareTo((BigDecimal) rhs) == 0)
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
                    Object lhs= visit(ast.getLeft()).getValue();
                    Object rhs = visit(ast.getRight()).getValue();
                    if (lhs instanceof BigInteger && rhs instanceof BigInteger)
                    {
                        if (((BigInteger) lhs).compareTo((BigInteger) rhs) == 1 || ((BigInteger) lhs).compareTo((BigInteger) rhs) == 0)
                        {
                            return Environment.create(true);
                        }
                        else
                        {
                            return Environment.create(false);
                        }
                    }
                    else if (lhs instanceof BigDecimal && rhs instanceof BigDecimal)
                    {
                        if (((BigDecimal) lhs).compareTo((BigDecimal) rhs) == 1 || ((BigDecimal) lhs).compareTo((BigDecimal) rhs) == 0)
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
                {
                    return Environment.create(true);
                }
                else
                {
                    return Environment.create(false);
                }

            case "!=":

                if (!visit(ast.getLeft()).getValue().equals(visit(ast.getRight()).getValue()))
                {
                    return Environment.create(true);
                }
                else
                {
                    return Environment.create(false);
                }
            case "+":
                if (visit(ast.getLeft()).getValue() instanceof String || visit(ast.getRight()).getValue() instanceof String )
                {
                    return Environment.create(visit(ast.getLeft()).getValue() + (String)visit(ast.getRight()).getValue());
                }
                if (visit(ast.getLeft()).getClass().equals(visit(ast.getRight()).getClass()))
                {
                    if (visit(ast.getLeft()).getValue() instanceof BigInteger)
                    {
                        return Environment.create(((BigInteger) visit(ast.getLeft()).getValue()).add((BigInteger)visit(ast.getRight()).getValue()));
                    }
                    if (visit(ast.getLeft()).getValue() instanceof BigDecimal)
                    {
                        return Environment.create(((BigDecimal) visit(ast.getLeft()).getValue()).add((BigDecimal) visit(ast.getRight()).getValue()));
                    }
                }
                throw new RuntimeException();

            case "-":

                if (visit(ast.getLeft()).getClass().equals(visit(ast.getRight()).getClass()))
                {
                    if (visit(ast.getLeft()).getValue() instanceof BigInteger)
                    {
                        return Environment.create(((BigInteger) visit(ast.getLeft()).getValue()).subtract((BigInteger)visit(ast.getRight()).getValue()));
                    }
                    if (visit(ast.getRight()).getValue() instanceof BigDecimal)
                    {
                        return Environment.create(((BigDecimal) visit(ast.getLeft()).getValue()).subtract((BigDecimal) visit(ast.getRight()).getValue()));
                    }
                }
                throw new RuntimeException();

            case "*":
                if (visit(ast.getLeft()).getClass().equals(visit(ast.getRight()).getClass()))
                {
                    if (visit(ast.getLeft()).getValue() instanceof BigInteger)
                    {
                        return Environment.create(((BigInteger) visit(ast.getLeft()).getValue()).multiply((BigInteger)visit(ast.getRight()).getValue()));
                    }
                    if (visit(ast.getRight()).getValue() instanceof BigDecimal)
                    {
                        return Environment.create(((BigDecimal) visit(ast.getLeft()).getValue()).multiply((BigDecimal) visit(ast.getRight()).getValue()));
                    }
                }
                throw new RuntimeException();

            case "/":
                if (visit(ast.getLeft()).getClass().equals(visit(ast.getRight()).getClass()))
                {
                    if (visit(ast.getLeft()).getValue() instanceof BigInteger)
                    {
                        BigInteger lefty = (BigInteger) visit(ast.getLeft()).getValue();
                        BigInteger righty = (BigInteger) visit(ast.getRight()).getValue();
                        if (righty.equals(BigInteger.ZERO))
                        {
                            throw new RuntimeException();
                        }
                        return Environment.create(lefty.divide(righty));
                    }
                    if (visit(ast.getRight()).getValue() instanceof BigDecimal)
                    {
                        BigDecimal lefty = (BigDecimal) visit(ast.getLeft()).getValue();
                        BigDecimal righty = (BigDecimal) visit(ast.getRight()).getValue();
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
    public Environment.PlcObject visit(Ast.Expr.Access ast)
    {
        if (ast.getReceiver().isPresent()) // if the expression has a receiver,
        {
            return visit(ast.getReceiver().get()).getField(ast.getName()).getValue();
        }
        return scope.lookupVariable(ast.getName()).getValue();
    }

    @Override
    public Environment.PlcObject visit(Ast.Expr.Function ast)
    {
        List<Environment.PlcObject> arguments = new ArrayList<>();
        for (Ast.Expr argument: ast.getArguments()) // evaluate the arguments
        {
            arguments.add(visit(argument));
        }
        if (ast.getReceiver().isPresent()) // if the expression has a receiver
        {
            Environment.PlcObject callee = visit(ast.getReceiver().get());

            return callee.callMethod(ast.getName(), arguments);
        }
        else
        {
            if (ast.getName().equals("print"))
            {
                return Environment.NIL;
            }
            return scope.lookupFunction(ast.getName(), ast.getArguments().size()).invoke(arguments);
        }
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
