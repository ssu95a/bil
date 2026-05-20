package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MapHelper {

    public static Value<?> invokeMapMethod( Value<Map<Value<?>, Value<?>>> mapValue, String methodName, List<Value<?>> args, ParserRuleContext ctx )
    {
        final Map<Value<?>, Value<?>> map = mapValue.asMap();

        switch (methodName) {
            case "size":
            case "length":
                ArgsValidator.checkNoArguments(args, "Map." + methodName, ctx);
                return Value.ofInt(map.size());

            case "get":
                ArgsValidator.checkArgumentCount(args, 1, "Map.get", ctx);
                Value<?> key = args.get(0);
                Value<?> value = map.get(key);
                return value != null ? value : Value.Null;

            case "put":
                ArgsValidator.checkArgumentCount(args, 2, "Map.put", ctx);
                map.put(args.get(0), args.get(1));
                return Value.VOID;

            case "remove":
                ArgsValidator.checkArgumentCount(args, 1, "Map.remove", ctx);
                Value<?> removed = map.remove(args.get(0));
                return removed != null ? removed : Value.Null;

            case "containsKey":
                ArgsValidator.checkArgumentCount(args, 1, "Map.containsKey", ctx);
                return Value.ofBool(map.containsKey(args.get(0)));

            case "containsValue":
                ArgsValidator.checkArgumentCount(args, 1, "Map.containsValue", ctx);
                return Value.ofBool(map.containsValue(args.get(0)));

            case "clear":
                ArgsValidator.checkNoArguments(args, "Map.clear", ctx);
                map.clear();
                return Value.VOID;

            case "isEmpty":
                ArgsValidator.checkNoArguments(args, "Map.isEmpty", ctx);
                return Value.ofBool(map.isEmpty());

            case "keys":
            case "keySet":
                ArgsValidator.checkNoArguments(args, "Map." + methodName, ctx);
                List<Value<?>> keys = new ArrayList<>(map.keySet());
                return Value.ofArray(keys);

            case "values":
                ArgsValidator.checkNoArguments(args, "Map.values", ctx);
                List<Value<?>> values = new ArrayList<>(map.values());
                return Value.ofArray(values);

            case "toString":
                ArgsValidator.checkNoArguments(args, "Map.toString", ctx);
                StringBuilder sb = new StringBuilder("{");
                boolean first = true;
                for (Map.Entry<Value<?>, Value<?>> entry : map.entrySet()) {
                    if (!first) sb.append(", ");
                    first = false;
                    sb.append(entry.getKey()).append(": ").append(entry.getValue());
                }
                sb.append("}");
                return Value.ofString(sb.toString());

            default:
                BilException.throwFunctionException( "Map." + methodName, "Unknown map method: " + methodName, ctx);
                return Value.Null;
        }
    }
}
