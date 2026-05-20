package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;

import java.util.List;

public class StringObjHelper {

    /** */
    static public Value<?> invokeStringMethod( Value<String> stringValue, String methodName, List<Value<?>> args, ParserRuleContext ctx ) throws BilException
    {
        final String str = stringValue.toString();

        switch( methodName )
        {
            case "len":
                // s.len() -> длина строки
                if( !args.isEmpty() )
                    BilException.throwArgumentException( "String.len","String.len() expects no arguments", ctx );

                return Value.ofInt( str.length() );

            case "at":
                // s.at(index) -> символ по индексу
                if( args.size() != 1 )
                    BilException.throwArgumentException("String.at","at() expects 1 argument", ctx );

                if( args.get(0).type() != Value.Type.INT )
                    BilException.throwArgumentException("String.at","Index must be integer", ctx );

                int index = args.get(0).asInt();
                if( index < 0 || index >= str.length())
                    BilException.throwArgumentException("String.at","Index bust be > 0 and < length", ctx );

                return Value.ofString( String.valueOf( str.charAt(index) ) );

            case "sub":
                // s.sub(start, length) -> подстрока
                if (args.size() != 2) throw new RuntimeException("sub() expects 2 arguments");
                if (args.get(0).type() != Value.Type.INT || args.get(1).type() != Value.Type.INT) {
                    BilException.throwArgumentException("string.sub","string.sub(): arguments must be integers", ctx );
                }
                int start  = args.get(0).asInt();
                int length = args.get(1).asInt();
                if (start < 0 || start >= str.length() || length < 0) {
                    BilException.throwArgumentException("string.sub","string.sub(): index bust be > 0 and < length. bad index " + start + ", len " + length, ctx );
                }
                int end = Math.min(start + length, str.length());
                return Value.ofString(str.substring(start, end));

            case "isEmpty":
                // s.isEmpty() -> проверка на пустоту
                if (!args.isEmpty()) throw new RuntimeException("isEmpty() expects no arguments");
                return Value.ofBool(str.isEmpty());

            case "trim":
                // s.trim() -> убрать пробелы
                if (!args.isEmpty()) throw new RuntimeException("trim() expects no arguments");
                return Value.ofString(str.trim());

            case "toUpper":
                // s.toUpper() -> верхний регистр
                if (!args.isEmpty()) throw new RuntimeException("toUpper() expects no arguments");
                return Value.ofString(str.toUpperCase());

            case "toLower":
                // s.toLower() -> нижний регистр
                if (!args.isEmpty()) throw new RuntimeException("toLower() expects no arguments");
                return Value.ofString(str.toLowerCase());

            case "contains":
                // s.contains(substr) -> проверка содержания подстроки
                if (args.size() != 1) throw new RuntimeException("contains() expects 1 argument");
                return Value.ofBool(str.contains(args.get(0).toString()));

            case "indexOf":
                // s.indexOf(substr) -> поиск подстроки
                if (args.size() != 1) throw new RuntimeException("indexOf() expects 1 argument");
                return Value.ofInt(str.indexOf(args.get(0).toString()));

            case "replace":
                // s.replace(old, new) -> замена подстроки
                if (args.size() != 2) throw new RuntimeException("replace() expects 2 arguments");
                return Value.ofString(str.replace(
                        args.get(0).toString(),
                        args.get(1).toString()
                ));
            default:
                throw new BilFunctionException( methodName, "Unknown string method: " + methodName, -1, -1 );
        }
    }

    /** Раскрывает escape-последовательности с десятичными кодами ASCII */
    public static String unescapeString(String escaped) {

        final StringBuilder result = new StringBuilder();

        for( int i = 0; i < escaped.length(); i++ )
        {
            char c = escaped.charAt(i);

            if( c == '\\' && i + 1 < escaped.length() ) {
                i++; // пропускаем обратный слеш

                char next = escaped.charAt(i);

                // Проверяем специальные символы
                switch (next) {
                    case '"':  result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    case '/':  result.append('/'); break;
                    case 'b':  result.append('\b'); break;  // backspace
                    case 'f':  result.append('\f'); break;  // form feed
                    case 'n':  result.append('\n'); break;  // new line
                    case 'r':  result.append('\r'); break;  // carriage return
                    case 't':  result.append('\t'); break;  // tab
                    default:
                        // Проверяем цифру - десятичный код ASCII
                        if (next >= '0' && next <= '9') {
                            // Считываем до 3 десятичных цифр
                            StringBuilder digits = new StringBuilder();
                            digits.append(next);

                            for( int j = 1; j < 3 && i + j < escaped.length(); j++)
                            {
                                char digit = escaped.charAt(i + j);
                                if (digit >= '0' && digit <= '9') {
                                    digits.append(digit);
                                } else {
                                    break;
                                }
                            }

                            try {
                                int asciiCode = Integer.parseInt(digits.toString());
                                if (asciiCode <= 255) {
                                    result.append((char) asciiCode);
                                    i += digits.length() - 1; // пропускаем считанные цифры
                                } else {
                                    // Код больше 255 - оставляем как есть
                                    result.append("\\").append(digits);
                                    i += digits.length() - 1;
                                }
                            } catch (NumberFormatException e) {
                                result.append("\\").append(digits); // оставляем как есть
                                i += digits.length() - 1;
                            }
                        } else {
                            // Неизвестная escape-последовательность
                            result.append("\\").append(next);
                        }
                        break;
                }
            }
            else
            {
                result.append(c);
            }
        }
        return result.toString();
    }
}
