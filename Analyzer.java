package plc.project;

import jdk.nashorn.internal.codegen.types.Type;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static java.lang.Boolean.TYPE;

/**
 * See the specification for information about what the different visit
 * methods should do.
 */
public final class Analyzer implements Ast.Visitor<Void> {

    public Scope scope;
    private Ast.Method method;

    public Analyzer(Scope parent) {
        scope = new Scope(parent);
        scope.defineFunction("print", "System.out.println", Arrays.asList(Environment.Type.ANY), Environment.Type.NIL, args -> Environment.NIL);
    }

    public Scope getScope() {
        return scope;
    }

    @Override
    public Void visit(Ast.Source ast)
    {
        for (Ast.Field field :  ast.getFields())
        {
            visit(field);
        }
        for (Ast.Method method: ast.getMethods())
        {
            visit(method);
        }
        requireAssignable(Environment.Type.INTEGER, scope.lookupFunction("main", 0).getReturnType());

        return null;
    }

    @Override
    public Void visit(Ast.Field ast)
    {
        visit(ast.getValue().get());
        ast.setVariable(scope.defineVariable(ast.getName(), ast.getName(), ast.getVariable().getType(), Environment.NIL));
        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast) // TODO
    {
        visit(ast.getExpression());
        if (!(ast.getExpression() instanceof Ast.Expr.Function))
        {
            throw new RuntimeException();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast) // TODO
    {
        if (!(ast.getReceiver() instanceof Ast.Expr.Access))
        {
            throw new RuntimeException();

        }
        throw new UnsupportedOperationException();
    }

    @Override
    public Void visit(Ast.Stmt.If ast)
    {
        visit(ast.getCondition());
        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());
        if (ast.getThenStatements().isEmpty())
        {
            throw new RuntimeException();
        }
        try
        {
            scope = new Scope(scope);
            for (Ast.Stmt stmt :  ast.getThenStatements())
            {
                visit(stmt);
            }
        }
        finally
        {
            scope = scope.getParent();
        }
        try
        {
            scope = new Scope(scope);
            for (Ast.Stmt stmt :  ast.getElseStatements())
            {
                visit(stmt);
            }
        }
        finally
        {
            scope = scope.getParent();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast)
    {
        visit(ast.getValue());
        requireAssignable(Environment.Type.INTEGER_ITERABLE, ast.getValue().getType());
        if (ast.getStatements().isEmpty())
        {
            throw new RuntimeException();
        }
        try
        {
            scope = new Scope(scope);
            scope.defineVariable(ast.getName(), ast.getName(), Environment.Type.INTEGER, Environment.NIL);
            for (Ast.Stmt stmt :  ast.getStatements())
            {
                visit(stmt);
            }
        }
        finally
        {
            scope = scope.getParent();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast)  // from lecture
    {
        visit(ast.getCondition());
        requireAssignable(Environment.Type.BOOLEAN, ast.getCondition().getType());
        try
        {
            scope = new Scope(scope);
            for (Ast.Stmt stmt :  ast.getStatements())
            {
                visit(stmt);
            }
        }
        finally
        {
            scope = scope.getParent();
        }
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Literal ast)
    {
        if (ast.getLiteral() instanceof Boolean)
        {
            ast.setType(Environment.Type.BOOLEAN);
            return null;
        }
        if (ast.getLiteral() instanceof Character )
        {
            ast.setType(Environment.Type.CHARACTER);
            return null;
        }
        if (ast.getLiteral() instanceof String)
        {
            ast.setType(Environment.Type.STRING);
            return null;
        }
        if(ast.getLiteral() == null)
        {
            ast.setType(Environment.Type.NIL);
            return null;
        }
        if (ast.getLiteral() instanceof BigInteger)
        {
            BigInteger max = BigInteger.valueOf(Integer.MAX_VALUE);
            BigInteger min = BigInteger.valueOf(Integer.MIN_VALUE);
            if (((BigInteger) ast.getLiteral()).compareTo(min) < 0 || ((BigInteger) ast.getLiteral()).compareTo(max) > 0)
            {
                throw new RuntimeException();
            }
            ast.setType(Environment.Type.INTEGER);
            return null;
        }
        if (ast.getLiteral() instanceof BigDecimal)
        {
            if (!Double.isFinite(((BigDecimal) ast.getLiteral()).doubleValue())) // if the value is out of range
            {
                throw new RuntimeException();
            }
            ast.setType(Environment.Type.DECIMAL);
            return null;
        }
        throw new RuntimeException();
    }

    @Override
    public Void visit(Ast.Expr.Group ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Binary ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Access ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        throw new UnsupportedOperationException();  // TODO
    }

    public static void requireAssignable(Environment.Type target, Environment.Type type)
    {
        if (target.equals(type)) // types are the same; assignment can be performed
        {
            return;
        }
        if (target.equals(Environment.Type.ANY))
        {
            return;
        }
        if (target.equals(Environment.Type.COMPARABLE))
        {
            if (type.equals(Environment.Type.INTEGER)
                    || type.equals(Environment.Type.DECIMAL)
                    || type.equals(Environment.Type.CHARACTER)
                    || type.equals(Environment.Type.STRING))
            {
                return;
            }
        }
        throw new RuntimeException();
    }

}
