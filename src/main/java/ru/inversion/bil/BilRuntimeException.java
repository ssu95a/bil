package ru.inversion.bil;

public class BilRuntimeException extends BilException{

    public BilRuntimeException(String message, int lineNum, int columnNum) {
        super(message, lineNum, columnNum);
    }

    public BilRuntimeException(String message, int lineNum, int columnNum, Throwable cause) {
        super(message, lineNum, columnNum, cause);
    }

    @Override
    public ErrorType getType() {
        return ErrorType.Runtime;
    }
}
