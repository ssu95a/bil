package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class ArgsValidator {

    /** */
    public static void checkArgumentCount( List<Value<?>> args, int expected, String functionName, ParserRuleContext ctx) {

        if( args.size() != expected )
            BilException.throwArgumentException( functionName,"Expected " + expected + " arguments, but got " + args.size(), ctx);
    }

    /** */
    public static void checkNoArguments( List<Value<?>> args, String functionName, ParserRuleContext ctx) {
        if(!args.isEmpty() )
            BilException.throwArgumentException(functionName, "Expected no arguments", ctx);
    }

    /** */
    public static void checkArgumentType( Value<?> arg, Value.Type expected, String functionName, ParserRuleContext ctx ) {
        if( arg.type() != expected )
            BilException.throwTypeException( functionName, "Argument must be " + expected + ", but got " + arg.type(), ctx);
    }
}