package ru.inversion.bil;

public class BilFunctionException extends BilRuntimeException {

    private final String functionName;

    public BilFunctionException(String functionName, String message, int lineNum, int columnNum) {
        super(message, lineNum, columnNum);
        this.functionName = functionName;
    }

    public BilFunctionException( String functionName, String message, int lineNum, int columnNum, Throwable cause ) {
        super( message, lineNum, columnNum, cause );
        this.functionName = functionName;
    }

    /** */
    @Override
    public ErrorType getType() {
        return ErrorType.Function;
    }

    /** */
    public String getFunctionName() {
        return functionName;
    }
}
