package ru.inversion.bil;

public class BilArgumentException extends BilRuntimeException {

    private final String functionName;

    public BilArgumentException( String fn, String message, int lineNum, int columnNum ) {
        super( message, lineNum, columnNum);
        this.functionName = fn == null ? "<bil>" : fn;
    }

    public BilArgumentException( String fn, String message, int lineNum, int columnNum, Throwable cause) {
        super(message, lineNum, columnNum, cause);
        this.functionName = fn == null ? "<bil>" : fn;
    }

    /** */
    @Override
    public ErrorType getType() {
        return ErrorType.Argument;
    }

}
