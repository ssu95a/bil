package ru.inversion.bil;

import ru.inversion.utils.U;
import ru.inversion.utils.converter.TypeConverter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/** */
public class Value<T> {

    public enum Type {

        INT, FLOAT, STRING, BOOL, NULL, VOID, MONEY, DATE, TIME;

        public boolean isNumeric() {
            return U.in( this, INT, FLOAT, MONEY );
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
            throw new NullPointerException("Cannot convert NULL to string");
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
            case NULL:   return null;
            case VOID:   return null;
            default:
                return value;
        }
    }

    public static Value fromObject(Object obj) {
        if( obj == null )
            return Null;

        if (obj instanceof Integer   ) return ofInt((Integer) obj);
        if (obj instanceof Double    ) return ofFloat((Double) obj);
        if (obj instanceof String    ) return ofString((String) obj);
        if (obj instanceof Boolean   ) return ofBool((Boolean) obj);
        if (obj instanceof BigDecimal) return ofMoney((BigDecimal) obj);
        if (obj instanceof LocalDate ) return ofDate((LocalDate) obj);
        if (obj instanceof LocalTime ) return ofTime((LocalTime) obj);

        throw new RuntimeException("Unsupported type: " + obj.getClass());
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
    public static Value<BigDecimal> ofMoney(String value) {
        return new Value<>( Type.MONEY, TypeConverter.convert(value, BigDecimal.class) );
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

    public static Value<Object>  Null = new Value<>( Type.NULL, null );
    public static Value<Void>    VOID  = new Value<>( Type.VOID, null );
    public static Value<Boolean> True  = new Value<>( Type.BOOL, true );
    public static Value<Boolean> False = new Value<>( Type.BOOL, false);

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
        types = Collections.unmodifiableMap(t);
    }

}