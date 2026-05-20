package ru.inversion.bil;

/** */
public class BilTypeException extends BilRuntimeException {

    private final String functionName;

    /** */
    public BilTypeException( String fn, String message, int lineNum, int columnNum) {
        super(message, lineNum, columnNum);
        functionName = fn;
    }

    /** */
    public BilTypeException(String fn, String message, int lineNum, int columnNum, Throwable cause ) {
        super(message, lineNum, columnNum, cause);
        functionName = fn;
    }

    @Override
    public ErrorType getType() {
        return ErrorType.Type;
    }
}
