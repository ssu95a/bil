package ru.inversion.bil;

import javax.script.*;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

public class BilScriptEngine extends AbstractScriptEngine {

    /** */
    private final BilScriptEngineFactory factory;

    /** */
    private final Map<String, FunctionDef > functions = new HashMap<>();

    /** */
    private final FormMaker formMaker;

    public BilScriptEngine( BilScriptEngineFactory factory )
    {
        this.factory = factory;
        this.context   = new SimpleScriptContext();
        this.formMaker = new FormMaker(this.context);

        initBuiltInFunctions();

        initializeContext();
    }

    /** */
    private void initializeContext()
    {
        // Инициализируем контекст по умолчанию
        if( context.getBindings(ScriptContext.ENGINE_SCOPE) == null )
            context.setBindings(createBindings(), ScriptContext.ENGINE_SCOPE );

        if (context.getBindings(ScriptContext.GLOBAL_SCOPE) == null)
            context.setBindings(createBindings(), ScriptContext.GLOBAL_SCOPE);

        if( context.getWriter() == null )
            context.setWriter( new java.io.PrintWriter(System.out));

        if( context.getReader() == null)
            context.setReader( new java.io.InputStreamReader(System.in));

        if (context.getErrorWriter() == null)
            context.setErrorWriter(new java.io.PrintWriter(System.err));

        // FormMaker в глобальный контекст
        context.getBindings( ScriptContext.GLOBAL_SCOPE ).put( "_FM", formMaker );
        context.getBindings( ScriptContext.ENGINE_SCOPE ).put( "_FM", formMaker );
    }

    /** */
    private void initBuiltInFunctions()
    {
        functions.put("typeof", (ctx, args) -> {

            if( args.length != 1)
                throw new RuntimeException("typeof() expects exactly 1 argument");

            Value<?> value = args[0];

            return Value.ofString( value.type().name().toLowerCase() );
        });

        functions.put("rangeof", (ctx, args) -> {
            return Value.ofInt( args.length == 0 ? 1 : args.length );
        });

        functions.put("round", new FunctionDef() {
            @Override
            public Value<?> execute( ScriptContext ctx, Value[] args ) {

                if( args.length < 1 || args.length > 2 )
                    throw new IllegalArgumentException( "round() expects 1 or 2 arguments" );

                Value<?> value = args[0];
                int scale = 0; // null означает "по умолчанию"

                if( args.length == 2 )
                {
                    // Второй аргумент - количество знаков после запятой
                    if( args[1].type() != Value.Type.INT )
                        throw new RuntimeException("Second argument to round() must be integer");

                    scale = args[1].asInt();

                    if( scale < 0 )
                        throw new RuntimeException("Scale cannot be negative");
                }
                else if( value.type() == Value.Type.MONEY )
                    scale = 2;

                return Value.round( value, scale );
            }
        });

        functions.put("format", (ctx, args) -> {

            if( args.length < 1 )
                throw new RuntimeException("format() expects at least 1 argument");

            final String format = args[0].toString();
            Object[] formatArgs = new Object[args.length - 1];

            for( int i = 1; i < args.length; i++ )
                 formatArgs[i-1] = args[i].toJavaObject();

            try {
                return Value.ofString( String.format(format, formatArgs) );
            } catch (Exception e) {
                throw new RuntimeException("Format error: " + e.getMessage());
            }
        });

        functions.put("print", (ctx, args) -> {

            try {

                if( args.length == 0 )
                    return Value.VOID;

                // Автоопределение: если первый аргумент - строка с % и есть другие аргументы
                if( args.length > 1 && args[0].type() == Value.Type.STRING && args[0].asString().indexOf('%') > 0 )
                {
                    final String format = args[0].toString();
                    final Object[] formatArgs = new Object[args.length - 1];

                    for( int i = 1; i < args.length; i++ )
                    {
                        formatArgs[i - 1] = args[i].toJavaObject();
                    }

                    final String result = String.format(format, formatArgs);

                    ctx.getWriter().write(result);

                }
                else
                {
                    for( Value<?> arg : args)
                    {
                        Object output = arg.toJavaObject();
                        ctx.getWriter().write(output != null ? output.toString() : "null");
                    }
                }

                ctx.getWriter().flush();

            } catch (Exception e) {
                throw new RuntimeException("Print error: " + e.getMessage());
            }
            return Value.VOID;
        });
    }

    /** */
    @Override
    public Object eval( String script, ScriptContext context) throws ScriptException {

        try {
            // Используем переданный контекст или дефолтный
            ScriptContext evalContext = context != null ? context : this.context;

            org.antlr.v4.runtime.CharStream input = org.antlr.v4.runtime.CharStreams.fromString(script);
            ru.inversion.bil.antlr.BilLexer lexer = new ru.inversion.bil.antlr.BilLexer(input);
            org.antlr.v4.runtime.CommonTokenStream tokens = new org.antlr.v4.runtime.CommonTokenStream(lexer);
            ru.inversion.bil.antlr.BilParser parser = new ru.inversion.bil.antlr.BilParser(tokens);

            BilVisitorImpl visitor = new BilVisitorImpl(evalContext, functions);
            Value result = visitor.visitProgram(parser.program());
            return result != null ? result.toJavaObject() : null;

        }catch( Exception e ) {
            throw new ScriptException(e);
        }
    }

    @Override
    public Object eval(Reader reader, ScriptContext context) throws ScriptException {
        try {
            return eval(readReader(reader), context);
        } catch (IOException e) {
            throw new ScriptException(e);
        }
    }

    private String readReader(Reader reader) throws IOException
    {
        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        int read;
        while ((read = reader.read(buffer)) != -1) {
            sb.append(buffer, 0, read);
        }
        return sb.toString();
    }

    @Override
    public Bindings createBindings() {
        return new SimpleBindings();
    }

    @Override
    public ScriptEngineFactory getFactory() {
        return factory;
    }

    /** */
    public interface FunctionDef {
        Value execute( ScriptContext ctx, Value[] args);
    }

    /** */
    public void registerFunction( String name, FunctionDef function ) {
        functions.put(name, function);
    }

    /** */
    public FunctionDef getFunction( String name) {
        return functions.get(name);
    }

    /** */
    public FunctionDef removeFunction( String name ) {
        return functions.remove(name);
    }

}