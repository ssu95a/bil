package ru.inversion.bil;

import ru.inversion.utils.S;
import ru.inversion.utils.U;
import ru.inversion.utils.converter.TypeConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/** */
public class Value<T> implements Cloneable {

    public enum Type {

        INT, FLOAT, STRING, BOOL, NULL, VOID, MONEY, DATE, TIME,
        ARRAY, MAP;

        public boolean isNumeric() {
            return U.in( this, INT, FLOAT, MONEY );
        }

        /** Определяет, является ли тип примитивным (передается по значению) */
        public boolean isPrimitive() {
            return U.in(this, INT, FLOAT, BOOL, MONEY, DATE, TIME);
        }

        /** Определяет, является ли тип объектным (передается по ссылке) */
        public boolean isReference() {
            return U.in(this, STRING, ARRAY, MAP);
        }
    }

    private final Type type;
    private final T value;

    public Value(Type type, T value) {
        this.type = type;
        this.value = value;
    }

    public Type type() {
        return type;
    }

    public T value() {
        return value;
    }

    /** */
    public int asInt()
    {
        if( type == Type.NULL )
            throw new NullPointerException("Cannot convert NULL to Integer");

        if( value instanceof Number )
            return ((Number)value).intValue();

        throw new ClassCastException("Cannot cast " + value.getClass() + " to Integer");
    }

    /** */
    public double asFloat() {

        if( type == Type.NULL )
            throw new NullPointerException("Cannot convert NULL to float");

        if( value instanceof Number )
            return ((Number)value).doubleValue();

        throw new ClassCastException("Cannot cast " + value.getClass() + " to Double");
    }

    /** */
    public String asString()
    {
        if( type == Type.NULL )
            return S.EMPTY_STRING;//throw new NullPointerException("Cannot convert NULL to string");
        //return (String) value;
        return value.toString();
    }

    /** */
    public boolean asBool() {
        if( type == Type.NULL )
            throw new NullPointerException("Cannot convert NULL to boolean");
        return (Boolean) value;
    }

    public BigDecimal asMoney() {
        if (type == Type.NULL) throw new NullPointerException("Cannot convert NULL to money");
        return (BigDecimal) value;
    }

    public LocalDate asDate() {
        if (type == Type.NULL) throw new NullPointerException("Cannot convert NULL to date");
        return (LocalDate) value;
    }

    public LocalTime asTime() {
        if (type == Type.NULL) throw new NullPointerException("Cannot convert NULL to time");
        return (LocalTime) value;
    }

    @SuppressWarnings("unchecked")
    public List<Value<?>> asArray() {
        if (type == Type.ARRAY) return (List<Value<?>>) value;
        throw new ClassCastException("Not an array: " + type);
    }

    @SuppressWarnings("unchecked")
    public Map<Value<?>, Value<?>> asMap() {
        if (type == Type.MAP) return (Map<Value<?>, Value<?>>) value;
        throw new ClassCastException("Not a map: " + type);
    }

    public boolean isTruthy() {
        switch (type) {
            case NULL: return false;
            case BOOL: return asBool();
            case INT:  return asInt() != 0;
            case FLOAT:  return asFloat() != 0.0;
            case STRING: return !asString().isEmpty();
            case MONEY:  return asMoney().compareTo(BigDecimal.ZERO) != 0;
            case DATE:   return true; // Дата всегда истинна если не NULL
            case TIME:   return true; // Время всегда истинно если не NULL
            case ARRAY:  return !asArray().isEmpty();
            case MAP:    return !asMap().isEmpty();
            case VOID:   return false;
            default: return true;
        }
    }

    public Object toJavaObject() {
        switch (type) {
            case INT:    return asInt();
            case FLOAT:  return asFloat();
            case STRING: return asString();
            case BOOL:   return asBool();
            case MONEY:  return asMoney();
            case DATE:   return asDate();
            case TIME:   return asTime();
            case ARRAY:  return asArray().stream().map(Value::toJavaObject).collect(Collectors.toList());
            case MAP:    return asMap().entrySet().stream().collect(Collectors.toMap(e -> e.getKey().toJavaObject(),e -> e.getValue().toJavaObject()));
            case NULL:   return null;
            case VOID:   return null;
            default:
                return value;
        }
    }

    /**
     * Создает копию значения.
     * Для примитивных типов - глубокая копия.
     * Для объектных типов - передается ссылка на тот же объект.
     */
    @SuppressWarnings("unchecked")
    public Value<T> clone() {
        try {

            Value<T> cloned = (Value<T>) super.clone();

            // Для объектных типов, которые должны передаваться по ссылке,
            // не клонируем внутренний объект
            if( type.isReference() )
                return cloned;

            // Для примитивных типов создаем новые объекты если нужно
            switch (type) {
                case MONEY:
                    return new Value<>(type, (T) new BigDecimal(value.toString()));
                case DATE:
                    return new Value<>(type, (T) LocalDate.from((LocalDate) value));
                case TIME:
                    return new Value<>(type, (T) LocalTime.from((LocalTime) value));
                case INT:
                case FLOAT:
                case BOOL:
                case STRING:
                    // Эти типы иммутабельны или примитивны - можно использовать тот же объект
                    return cloned;
                case ARRAY:
                case MAP:
                default:
                    return cloned;
            }
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("Clone not supported for Value", e);
        }
    }

    /**
     * Создает копию значения согласно семантике передачи параметров.
     * Примитивы копируются, объекты передаются по ссылке.
     */
    public Value<T> makeCopy() {
        if( type.isPrimitive() || type == Value.Type.NULL || type == Value.Type.VOID ) {
            return clone(); // Глубокая копия для примитивов
        } else {
            return this;    // Та же ссылка для объектов
        }
    }

    // static zone

    public static Value<?> fromObject(Object obj) {
        if( obj == null )
            return Null;
        if( obj instanceof Value )
            return (Value<?>)obj;

        if (obj instanceof Integer   ) return ofInt((Integer) obj);
        if (obj instanceof Double    ) return ofFloat((Double) obj);
        if (obj instanceof String    ) return ofString((String) obj);
        if (obj instanceof Boolean   ) return ofBool((Boolean) obj);
        if (obj instanceof BigDecimal) return ofMoney((BigDecimal) obj);
        if (obj instanceof LocalDate ) return ofDate((LocalDate) obj);
        if (obj instanceof LocalTime ) return ofTime((LocalTime) obj);
        if (obj instanceof List      ) {
            return ofArray((List)obj );//.stream().map(Value::fromObject).collect(Collectors.toList()));
        }
        if (obj instanceof Map) {
//            Map<Value<?>, Value<?>> map = new HashMap<>();
//            ((Map<?, ?>) obj).forEach((k, v) -> {
//                map.put(Value.fromObject(k), Value.fromObject(v));
//            });
            return ofMap((Map)obj);
        }
        throw new BilTypeException("Value.fromObject", "Unsupported type: " + obj.getClass(), -1, -1 );
    }

    @Override
    public String toString() {
        if (type == Type.NULL) return "null";
        if (type == Type.VOID) return "void";
        return value.toString();
    }

    @Override
    public boolean equals(Object o) {

        if( this == o )
            return true;

        if( o == null || getClass() != o.getClass() )
            return false;

        final Value<?> v = (Value<?>) o;
        return type == v.type && U.equals( value, v.value );
    }

    @Override
    public int hashCode() {
        return Objects.hash( type, value );
    }

    public static Value<Integer> ofInt(int value) {
        return new Value<>(Type.INT, value);
    }

    public static Value<Integer> ofInt(String value) {
        return new Value<>(Type.INT, Integer.parseInt(value));
    }

    public static Value<Double> ofFloat(double value) {
        return new Value<>(Type.FLOAT, value);
    }

    public static Value<String> ofString(String value) {
        return new Value<>(Type.STRING, value);
    }

    // Boolean

    public static Value<Boolean> ofBool(boolean value) {
        return value ? True : False;
    }

    /** */
    public static Value<Boolean> ofBool( String v ) {
        return ofBool( TypeConverter.convert( v, Boolean.class ));
    }

    // Money

    public static Value<BigDecimal> ofMoney(BigDecimal value) {
        return new Value<>(Type.MONEY, value);
    }

    /** */
    public static Value<BigDecimal> ofMoney( String value ) {
        return new Value<>( Type.MONEY, TypeConverter.convert( value, BigDecimal.class) );
    }

    /** */
    public static Value<BigDecimal> ofMoney(double value) {
        return new Value<>(Type.MONEY, BigDecimal.valueOf(value));
    }

    // Date

    /** */
    public static Value<LocalDate> ofDate(LocalDate value) {
        return new Value<>(Type.DATE, value);
    }

    /** */
    public static Value<LocalDate> ofDate( String value ) {
        return new Value<>(Type.DATE, LocalDate.parse(value));
    }

    // Time

    public static Value<LocalTime> ofTime( LocalTime value ) {
        return new Value<>(Type.TIME, value);
    }

    public static Value<LocalTime> ofTime(String value) {
        return new Value<>(Type.TIME, LocalTime.parse(value));
    }

    public static Value<Object>  Null  = new Value<>( Type.NULL, null );
    public static Value<Void>    VOID  = new Value<>( Type.VOID, null );
    public static Value<Boolean> True  = new Value<>( Type.BOOL, true );
    public static Value<Boolean> False = new Value<>( Type.BOOL, false);
    public static Value<String>  EmptyString = new Value<>( Type.STRING, S.EMPTY_STRING);

    // Array
    public static Value<List<Value<?>>> ofArray(List<Value<?>> value) {
        return new Value<>( Type.ARRAY, value );
    }
    //
    public static Value<List<Value<?>>> ofArray(Value<?>... values  ) {
        return ofArray( Arrays.asList(values) );
    }
    // Map
    public static Value<Map<Value<?>, Value<?>>> ofMap(Map<Value<?>, Value<?>> value) {
        return new Value<>(Type.MAP, value);
    }

    /** Math operation */
   public static Value<?> mathOperator( Value<?> v1, Value<?> v2, char operator )
    {
        return ValueHelper.exec(v1,v2, operator );
    }

    /** */
    public static Value<?> round( Value<?> value, int scale )
    {
        if( value.type == Type.NULL )
            return Null;

        if( value.type == Type.INT )
            return value;

        if( value.type == Type.FLOAT )
        {
            double d = value.asFloat();

            if( scale == 0)
            {
                return Value.ofInt( (int)Math.round(d) );
            }
            else
            {
                double factor  = Math.pow(10, scale);
                double rounded = Math.round(d * factor) / factor;
                return Value.ofFloat(rounded);
            }
        }

        if( value.type == Type.MONEY )
        {
            BigDecimal dec = value.asMoney();
            return Value.ofMoney(dec.setScale(scale, RoundingMode.HALF_UP));
        }

        throw new RuntimeException("round() can only be applied to numeric types");
    }

    /** */
    static final public Map<String,Type> types;
    static {
        final Map<String,Type> t = new HashMap<String,Type>() {
            @Override
            public Type get( Object key ) {
                if( key == null )
                    return Value.Type.NULL;
                return U.nvl( super.get(key), Value.Type.NULL );
            }
        };
        t.put("int",   Value.Type.INT   );
        t.put("float", Value.Type.FLOAT );
        t.put("string",Value.Type.STRING);
        t.put("bool",  Value.Type.BOOL  );
        t.put("money", Value.Type.MONEY );
        t.put("date",  Value.Type.DATE  );
        t.put("time",  Value.Type.TIME  );
        t.put("void",  Value.Type.VOID  );
        t.put("array", Value.Type.ARRAY );
        t.put("map",   Value.Type.MAP   );
        types = Collections.unmodifiableMap(t);
    }
}