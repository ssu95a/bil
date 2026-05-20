package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;
import ru.inversion.utils.IExceptionInfo;

/** */
public abstract class BilException extends RuntimeException implements IExceptionInfo {

    /** */
    public enum ErrorType {
        Syntax, Runtime, Type, Function, Argument
    }

    final private int lineNum, columnNum;

    public BilException( String message ) {
        super( Tags.PRODUCT_LABEL + message );
        lineNum = -1; columnNum = -1;
    }

    public BilException( Throwable cause ) {
        super( cause );
        lineNum = -1; columnNum = -1;
    }

    public BilException( String message, Throwable cause ) {
        super( Tags.PRODUCT_LABEL + message, cause );
        lineNum = -1; columnNum = -1;
    }

    /** */
    public BilException( String message, int lineNum, int columnNum ) {
        super(Tags.PRODUCT_LABEL + message, null );
        this.lineNum = lineNum; this.columnNum = columnNum;
    }

    /** */
    public BilException( String message, int lineNum, int columnNum, Throwable cause ) {
        super( Tags.PRODUCT_LABEL + message, cause );
        this.lineNum = lineNum; this.columnNum = columnNum;
    }

    /** */
    public int getLineNumber() {
        return lineNum;
    }

    /** */
    public int getColumnNumber() {
        return columnNum;
    }

    /** */
    public abstract ErrorType getType();

    @Override
    public String getCategory() {
        return "Bil";
    }

    /** */
    @Override
    public String getDetailedMessage() {
        if( getLineNumber() != -1 ) {
            if( getColumnNumber() != -1 )
                return String.format("Ошибочная строка %d, в позиции %d", getLineNumber(), getColumnNumber() );
            else
                return String.format("Ошибочная строка %d", getLineNumber() );
        }
        return null;
    }

    // static zone
    // Вспомогательные методы для получения позиции
    private static int getLine(ParserRuleContext ctx) {
        return ctx != null && ctx.getStart() != null ? ctx.getStart().getLine() : -1;
    }
    private static int getColumn(ParserRuleContext ctx) {
        return ctx != null && ctx.getStart() != null ? ctx.getStart().getCharPositionInLine() : -1;
    }

    /** */
    public static void throwTypeException( String functionName, String message, ParserRuleContext ctx ) {
        throw new BilTypeException(functionName, message, getLine(ctx), getColumn(ctx) );
    }

    /** */
    public static void throwTypeException( String functionName, String message, ParserRuleContext ctx, Throwable cause) {
        throw new BilTypeException( functionName, message, getLine(ctx), getColumn(ctx), cause );
    }

    /** */
    public static void throwArgumentException(String functionName, String message, ParserRuleContext ctx) {
        throw new BilArgumentException(functionName, message, getLine(ctx), getColumn(ctx));
    }

    /** */
    public static void throwArgumentException(String functionName, String message, ParserRuleContext ctx, Throwable cause) {
        throw new BilArgumentException(functionName, message, getLine(ctx), getColumn(ctx), cause);
    }

    /** */
    public static void throwRuntimeException(String message, ParserRuleContext ctx) {
        throw new BilRuntimeException( message, getLine(ctx), getColumn(ctx));
    }

    /** */
    public static void throwRuntimeException(String message, ParserRuleContext ctx, Throwable cause) {
        throw new BilRuntimeException(message, getLine(ctx), getColumn(ctx), cause);
    }

    /** */
    public static void throwFunctionException(String functionName, String message, ParserRuleContext ctx) {
        throw new BilFunctionException(functionName, message, getLine(ctx), getColumn(ctx));
    }
    /** */
    public static void throwFunctionException(String functionName, String message, ParserRuleContext ctx, Throwable cause ) {
        throw new BilFunctionException(functionName, message, getLine(ctx), getColumn(ctx), cause );
    }
}
