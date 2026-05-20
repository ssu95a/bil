package ru.inversion.bil;

public class BilSyntaxException extends BilException {

    public BilSyntaxException(String message) {
        super(message);
    }

    public BilSyntaxException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public ErrorType getType() {
        return ErrorType.Syntax;
    }
}
