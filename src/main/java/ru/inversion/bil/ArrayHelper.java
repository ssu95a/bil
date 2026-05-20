package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class ArrayHelper {

    public static Value<?> invokeArrayMethod( Value<List<Value<?>>> arrayValue, String methodName, List<Value<?>> args, ParserRuleContext ctx) {

        final List<Value<?>> array = arrayValue.asArray();

        switch( methodName )
        {
            case "length":
            case "size":
                ArgsValidator.checkNoArguments( args, "Array." + methodName, ctx );
                return Value.ofInt( array.size() );
            case "get":
                ArgsValidator.checkArgumentCount( args, 1, "Array.get", ctx);
                ArgsValidator.checkArgumentType ( args.get(0), Value.Type.INT, "Array.get", ctx);
                int index = args.get(0).asInt();
                if (index < 0 || index >= array.size()) {
                    BilException.throwRuntimeException("Array index out of bounds: " + index, ctx);
                }
                return array.get(index);
            case "set":
                ArgsValidator.checkArgumentCount(args, 2, "Array.set", ctx);
                ArgsValidator.checkArgumentType(args.get(0), Value.Type.INT, "Array.set", ctx);
                int setIndex = args.get(0).asInt();
                if (setIndex < 0 || setIndex >= array.size()) {
                    BilException.throwRuntimeException("Array index out of bounds: " + setIndex, ctx);
                }
                array.set(setIndex, args.get(1));
                return Value.VOID;
            case "add":
                ArgsValidator.checkArgumentCount(args, 1, "Array.add", ctx);
                array.add(args.get(0));
                return Value.VOID;
            case "remove":
                ArgsValidator.checkArgumentCount(args, 1, "Array.remove", ctx);
                ArgsValidator.checkArgumentType(args.get(0), Value.Type.INT, "Array.remove", ctx);
                int removeIndex = args.get(0).asInt();
                if (removeIndex < 0 || removeIndex >= array.size()) {
                    BilException.throwRuntimeException("Array index out of bounds: " + removeIndex, ctx);
                }
                return array.remove(removeIndex);
            case "clear":
                ArgsValidator.checkNoArguments(args, "Array.clear", ctx);
                array.clear();
                return Value.VOID;
            case "isEmpty":
                ArgsValidator.checkNoArguments(args, "Array.isEmpty", ctx);
                return Value.ofBool(array.isEmpty());
            case "contains":
                ArgsValidator.checkArgumentCount(args, 1, "Array.contains", ctx);
                return Value.ofBool(array.contains(args.get(0)));

            case "indexOf":
                ArgsValidator.checkArgumentCount(args, 1, "Array.indexOf", ctx);
                for (int i = 0; i < array.size(); i++) {
                    if (array.get(i).equals(args.get(0))) {
                        return Value.ofInt(i);
                    }
                }
                return Value.ofInt(-1);
            default: BilException.throwFunctionException("Array." + methodName, "Unknown array method: " + methodName, ctx);
                return Value.Null;
        }
    }
}
