package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;
import ru.inversion.utils.IObservableMap;
import ru.inversion.utils.ObservableMap;
import ru.inversion.utils.TriFunction;

import javax.script.ScriptContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class BuiltInFunctionsHelper {

    /** */
    public static Map<String, TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?> >> initFunctions( )
    {
        final Map<String, TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?> > > functions = new HashMap<>();

        functions.put("typeof", (scriptCtx, args, execCtx ) -> {

            ArgsValidator.checkArgumentCount( args,1, "typeof()", execCtx );

            if( args.size() != 1 )
                BilException.throwArgumentException( "typeof()","typeof() expects exactly 1 argument", execCtx );

            Value<?> value = args.get(0);

            return Value.ofString( value.type().name().toLowerCase() );
        });

        functions.put( "rangeof", (ctx, args, execCtx) -> Value.ofInt(args.isEmpty() ? 1 : args.size() ) );

        functions.put( "round",  (scriptCtx, args, execCtx )-> {

            ArgsValidator.checkArgumentCount( args,1, "round", execCtx );
            if( args.isEmpty() || args.size() > 2 )
                BilException.throwArgumentException("round", "round() expects 1 or 2 arguments", execCtx );

            Value<?> value = args.get(0);
            int scale = 0; // null означает "по умолчанию"

            if (args.size() == 2)
            {
                // Второй аргумент - количество знаков после запятой
                if (args.get(1).type() != Value.Type.INT)
                    BilException.throwArgumentException("round", "Second argument to round() must be integer", execCtx );

                scale = args.get(1).asInt();

                if( scale < 0 )
                    BilException.throwArgumentException("round", "Scale cannot be negative", execCtx );

            } else if (value.type() == Value.Type.MONEY)
                scale = 2;

            return Value.round( value, scale);
        });

        functions.put("format", (scriptCtx, args, execCtx ) -> {

            if( args.isEmpty() )
                BilException.throwArgumentException("format", "format() expects at least 1 argument", execCtx );

            final String format = args.get(0).toString();
            Object[] formatArgs = new Object[args.size() - 1];

            for( int i = 1; i < args.size(); i++ )
                formatArgs[i-1] = args.get(i).toJavaObject();

            try {
                return Value.ofString( String.format(format, formatArgs) );
            } catch (Exception e) {
                BilException.throwFunctionException( "format", "Format error: " + e.getMessage(), execCtx, e );
            }
            return Value.Null;
        });

        functions.put("print", (scriptCtx, args, execCtx ) -> {

            try {

                if( args.isEmpty() )
                    return Value.VOID;

                // Автоопределение: если первый аргумент - строка с % и есть другие аргументы
                if( args.size() > 1 && args.get(0).type() == Value.Type.STRING && args.get(0).asString().indexOf('%') > 0 )
                {
                    final String format = args.get(0).toString();
                    final Object[] formatArgs = new Object[args.size() - 1];

                    for( int i = 1; i < args.size(); i++ )
                    {
                        formatArgs[i - 1] = args.get(i).toJavaObject();
                    }

                    final String result = String.format( format, formatArgs );

                    scriptCtx.getWriter().write(result);
                }
                else
                {
                    for( Value<?> arg : args )
                    {
                        Object output = arg.toJavaObject();
                        scriptCtx.getWriter().write( output != null ? output.toString() : "null" );
                    }
                }

                scriptCtx.getWriter().flush();

            } catch( Exception e ) {
                BilException.throwFunctionException( "print", "call Print error: " + e.getMessage(), execCtx, e );
            }
            return Value.VOID;
        });

        functions.put( "concat", (scriptCtx, args, execCtx ) -> {

            try {

                if( args.isEmpty() )
                    return Value.EmptyString;

                final String[] values = new String[args.size()];
                int   length = 0;
                int   i = 0;
                for( Value<?> v : args )
                {
                    if( v != null && v.type() != Value.Type.NULL )
                    {
                        values[i] = v.asString();
                        length += values[i].length();
                        i++;
                    }
                    else
                        values[i++] = null;
                }

                final StringBuilder sb = new StringBuilder(length);

                for( String s : values )
                     sb.append(s);

                return Value.ofString( sb.toString() );

            } catch( Exception e ) {
                BilException.throwFunctionException( "concat", "call concat error: " + e.getMessage(), execCtx, e );
            }
            return Value.VOID;
        });

        return functions;
    }

}
