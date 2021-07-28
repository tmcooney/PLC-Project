package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Arrays;

public final class Generator implements Ast.Visitor<Void> {

    private final PrintWriter writer;
    private int indent = 0;

    public Generator(PrintWriter writer) {
        this.writer = writer;
    }

    private void print(Object... objects) {
        for (Object object : objects) {
            if (object instanceof Ast) {
                visit((Ast) object);
            } else {
                writer.write(object.toString());
            }
        }
    }

    private void newline(int indent) {
        writer.println();
        for (int i = 0; i < indent; i++) {
            writer.write("    ");
        }
    }

    @Override
    public Void visit(Ast.Source ast) {
        Ast.Method method = new Ast.Method("null", Arrays.asList(), Arrays.asList());
        visit(method);
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        print(ast.getVariable().getType().getJvmName(),
                " ",
                ast.getVariable().getJvmName());

        if (ast.getValue().isPresent()){
            print(" = ", ast.getValue().get());
        }
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
        System.out.println();
        print(ast.getFunction().getReturnType().getJvmName(), " ", ast.getName(), "(");
        for (int i = 0; i < ast.getParameters().size(); i++)
        {
            print(ast.getParameterTypeNames().get(i), ": ", ast.getParameters().get(i));
            if (!(i == ast.getParameters().size() - 1))
            {
                print(", ");
            }
        }
        print(") {");
        if (!ast.getStatements().isEmpty())
        {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++){
                if (i != 0){
                    newline(indent);
                }
                print(ast.getStatements().get(i));
            }
            newline(--indent);
        }
        print("}");


        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Stmt.Expression ast)
    {
        print(ast.getExpression(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Declaration ast) // from lecture
    {

        print(ast.getVariable().getType().getJvmName(),
                " ",
                ast.getVariable().getJvmName());

        if (ast.getValue().isPresent()){
            print(" = ", ast.getValue().get());
        }
        print(";");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Assignment ast)
    {

        print(ast.getReceiver(), " = ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast)
    {
        print("if (", ast.getCondition(), ") {");
        if (!ast.getThenStatements().isEmpty())
        {
            newline(++indent);
            for (int i = 0; i < ast.getThenStatements().size(); i++){
                if (i != 0){
                    newline(indent);
                }
                print(ast.getThenStatements().get(i));
            }
            newline(--indent);
        }
        print("}");
        if (!ast.getElseStatements().isEmpty())
        {
            print(" else {");
            newline(++indent);
            for (int i = 0; i < ast.getElseStatements().size(); i++){
                if (i != 0){
                    newline(indent);
                }
                print(ast.getElseStatements().get(i));
            }
            newline(--indent);
            print("}");
        }
        //Ast.Stmt.For forStmt = new Ast.Stmt.For("name", ast.getCondition(), null); // FIXME delete this
        //visit(forStmt); // FIXME delete this too
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast)
    {

        print("for (",
                ast.getValue().getType().getJvmName(),
                " ",
                ast.getName(),
                " : ",
                ast.getValue(),
                ") {");

        if (!ast.getStatements().isEmpty())
        {
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++){
                if (i != 0){
                    newline(indent);
                }
                print(ast.getStatements().get(i));
            }
            newline(--indent);
        }

        print("}");

        return null;
    }

    @Override
    public Void visit(Ast.Stmt.While ast) // from lecture
    {
        print("while (", ast.getCondition(), ") {");

        if (!ast.getStatements().isEmpty()){
            newline(++indent);
            for (int i = 0; i < ast.getStatements().size(); i++){
                if (i != 0){
                    newline(indent);
                }
                print(ast.getStatements().get(i));
            }
            newline(--indent);
        }

        print("}");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.Return ast)
    {
        print("return ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast)
    { //TODO

        if (ast.getLiteral() instanceof String)
        {
            print("\"", ast.getLiteral(), "\"");
            return null;
        }
        if (ast.getLiteral() instanceof Character)
        {
            print("\'", ast.getLiteral(), "\'");
            return null;
        }
        if (ast.getLiteral() instanceof BigDecimal) // FIXME use BigDecimal(String) constructor to know what precision is
        {
            print(ast.getLiteral());
            return null;
        }
        print(ast.getLiteral());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Group ast)
    {
        print("(",
                ast.getExpression(),
                ")");
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Binary ast)
    {
        print(ast.getLeft(), " ");
        if (ast.getOperator().equals("AND"))
        {
            print("&&");
        }
        else if (ast.getOperator().equals("OR"))
        {
            print("||");
        }
        else
        {
            print(ast.getOperator());
        }
        print(" ", ast.getRight());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Access ast)
    {
        if (ast.getReceiver().isPresent())
        {
            print(ast.getReceiver(), ".");
        }
        print(ast.getName());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast)
    {
        if (ast.getReceiver().isPresent())
        {
            print(ast.getReceiver().get(), ".");
        }

        print(ast.getFunction().getJvmName(), "(");
        for (int i = 0; i < ast.getArguments().size(); i++)
        {
            print(ast.getArguments().get(i));
            if (!(i == ast.getArguments().size() - 1))
            {
                print(", ");
            }
        }
        print(")");
        return null;
    }

}
