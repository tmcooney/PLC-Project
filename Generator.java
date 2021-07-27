package plc.project;

import java.io.PrintWriter;
import java.math.BigDecimal;

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
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Field ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Method ast) {
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
    public Void visit(Ast.Stmt.Assignment ast) {

        print(ast.getReceiver(), " = ", ast.getValue(), ";");
        return null;
    }

    @Override
    public Void visit(Ast.Stmt.If ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Stmt.For ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
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
    public Void visit(Ast.Stmt.Return ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

    @Override
    public Void visit(Ast.Expr.Literal ast) { //TODO

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
    public Void visit(Ast.Expr.Group ast) {
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
    public Void visit(Ast.Expr.Access ast) {
        if (ast.getReceiver().isPresent())
        {
            print(ast.getReceiver(), ".");
        }
        print(ast.getName());
        return null;
    }

    @Override
    public Void visit(Ast.Expr.Function ast) {
        throw new UnsupportedOperationException(); //TODO
        //return null;
    }

}
