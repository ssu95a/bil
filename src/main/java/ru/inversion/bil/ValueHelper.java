package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;
import ru.inversion.utils.S;
import ru.inversion.utils.converter.IConverter;
import ru.inversion.utils.converter.TypeConverter;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.function.BiFunction;

import static ru.inversion.bil.Value.Type.*;

/** */
public class ValueHelper {

    public enum MathOperatorEnum {

        add ('+'),
        sub ('-'),
        mlt ('*'),
        div ('/'),
        mod ('%'),
        and ('&'),
        or  ('|'),
        xor ('^');

        final char symb;

        MathOperatorEnum( char symb ) {
            this.symb = symb;
        }

        /** */
        public static MathOperatorEnum of( char ch )
        {
            switch(ch) {
                case '+': return add;
                case '-': return sub;
                case '*': return mlt;
                case '/': return div;
                case '%': return mod;
                case '&': return and;
                case '|': return or;
                case '^': return xor;
            }
            throw new IllegalArgumentException("Оператор '" + ch +  "' не поддерживается в BIL");
        }
    }

    public enum ComparisonOperatorEnum {

        EQUAL                ("=="),
        NOT_EQUAL            ("!="),
        LESS_THAN            ("<" ),
        GREATER_THAN         (">"),
        LESS_THAN_OR_EQUAL   ("<="),
        GREATER_THAN_OR_EQUAL(">=");

        private final String symbol;

        ComparisonOperatorEnum(String symbol) {
            this.symbol = symbol;
        }

        public String symbol() {
            return symbol;
        }

        @Override
        public String toString() {
            return symbol;
        }

        // Метод для получения оператора по символу
        public static ComparisonOperatorEnum of( String symbol) {
            for( ComparisonOperatorEnum op : values()) {
                if (op.symbol.equals(symbol)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("Unknown comparison operator: " + symbol);
        }
    }

    /** */
    private static class Key {
        
        final private Value.Type t1;
        final private Value.Type t2;

        public Key( Value.Type t1, Value.Type t2 ) {
            this.t1 = t1;
            this.t2 = t2;
        }
        @Override
        public boolean equals( Object obj ) {
            Key p = (Key) obj;
            return this.t1 == p.t1 && this.t2 == p.t2;
        }
        @Override
        public int hashCode() {
            return Objects.hash( t1, t2 );
        }
        public static Key of( Value.Type t1, Value.Type t2 )
        {
            return new Key(t1,t2);
        }
        public static Key of( Value<?> v1, Value<?> v2 )
        {
            return new Key(v1.type(),v2.type());
        }
    }
    
    /** */
    final static private Map< Key, List> mathOperMap = new HashMap<>();

    /** */
    final static private Map< Key, IConverter> typeConvertors = new HashMap<>();

    static {

        MathContext ROUND_CONTEXT = new MathContext(2, RoundingMode.HALF_EVEN);

        final List< BiFunction< Value< Integer >, Value< Integer >, Value<Integer> > > i
            = Arrays.asList(
            ( v1, v2 ) -> Value.ofInt(v1.value() + v2.value()), // 0 add
            ( v1, v2 ) -> Value.ofInt(v1.value() - v2.value()), // 1 sub
            ( v1, v2 ) -> Value.ofInt(v1.value() * v2.value()), // 2 mlt
            ( v1, v2 ) -> Value.ofInt(v1.value() / v2.value()), // 3 div
            ( v1, v2 ) -> Value.ofInt(v1.value() % v2.value()), // 4 mod
            ( v1, v2 ) -> Value.ofInt(v1.value() & v2.value()), // 5 l_and
            ( v1, v2 ) -> Value.ofInt(v1.value() | v2.value()), // 6 l_or
            ( v1, v2 ) -> Value.ofInt(v1.value() ^ v2.value())  // 7 l_xor
        );
        mathOperMap.put( new Key(INT, INT), i );

        final List< BiFunction<Value<Double>,Value<Double>,Value<Double>>> d = Arrays.asList (
            ( v1, v2 ) -> Value.ofFloat( v1.value() + v2.value() ),
            ( v1, v2 ) -> Value.ofFloat( v1.value() - v2.value() ),
            ( v1, v2 ) -> Value.ofFloat( v1.value() * v2.value() ),
            ( v1, v2 ) -> Value.ofFloat( v1.value() / v2.value() )
        );
        mathOperMap.put( new Key(Value.Type.FLOAT, Value.Type.FLOAT), d );

        final List< BiFunction<Value<Double>,Value<Integer>,Value<Double>>> di = Arrays.asList (
            ( v1, v2 ) -> Value.ofFloat( v1.value() + v2.value() ),
            ( v1, v2 ) -> Value.ofFloat( v1.value() - v2.value() ),
            ( v1, v2 ) -> Value.ofFloat( v1.value() * v2.value() ),
            ( v1, v2 ) -> Value.ofFloat( v1.value() / v2.value() )
        );
        mathOperMap.put( new Key(Value.Type.FLOAT, INT), di );

        final List< BiFunction<Value<BigDecimal>,Value<BigDecimal>,Value<BigDecimal>>> m = Arrays.asList (
            ( v1, v2 ) -> Value.ofMoney( v1.value().add     ( v2.value() ) ),
            ( v1, v2 ) -> Value.ofMoney( v1.value().subtract( v2.value() ) ),
            ( v1, v2 ) -> Value.ofMoney( v1.value().multiply( v2.value() ) ),
            ( v1, v2 ) -> Value.ofMoney( v1.value().divide  ( v2.value(), ROUND_CONTEXT) )
        );
        mathOperMap.put( new Key(Value.Type.MONEY, Value.Type.MONEY), m );

        final List< BiFunction<Value<BigDecimal>,Value<Integer>,Value<BigDecimal>>> mi = Arrays.asList (
            ( v1, v2 ) -> Value.ofMoney( v1.value().add     ( BigDecimal.valueOf( v2.value()) ) ),
            ( v1, v2 ) -> Value.ofMoney( v1.value().subtract( BigDecimal.valueOf( v2.value()) ) ),
            ( v1, v2 ) -> Value.ofMoney( v1.value().multiply( BigDecimal.valueOf( v2.value()) ) ),
            ( v1, v2 ) -> Value.ofMoney( v1.value().divide  ( BigDecimal.valueOf( v2.value()), ROUND_CONTEXT ) )
        );
        mathOperMap.put( new Key(Value.Type.MONEY, INT), mi );

        initConvertors();
    }

    /** */
    static Value<?> exec( Value<?> left, Value<?> right, char operator )
    {
        if( left.type() == Value.Type.NULL && right.type() == Value.Type.NULL )
            return Value.Null;

        if( (left.type() == Value.Type.STRING || right.type() == Value.Type.STRING ) && operator == '+' ) {

            if( right.type() == Value.Type.NULL )
                return left;
            if( left.type() == Value.Type.NULL )
                return right;

            return Value.ofString( left.value().toString() + right.value().toString() );
        }

        final MathOperatorEnum o = MathOperatorEnum.of(operator);
        boolean reverse = false;

        List<BiFunction< Value<?>, Value<?>, Value<?> >> impls = mathOperMap.get( Key.of(left, right) );
        if( impls == null ) {
            impls = mathOperMap.get( Key.of(right, left ) );
            reverse = true;
        }

        if( impls == null )
            throw new IllegalStateException( "Нет определенных правил вычисления для оператора '" + operator + "', для типов " + left.type() + ", " + right.type() );

        if( impls.size() >= o.ordinal() + 1 ) {
            if(reverse)
                return impls.get(o.ordinal()).apply(right, left ); //!!! right

            return impls.get(o.ordinal()).apply(left,right );
        }
        throw new IllegalArgumentException("Операция '" + operator + "' не поддерживается для типов " + left.type() + ", " + right.type() );
    }


    /** */
    static void initConvertors()
    {
        typeConvertors.put( Key.of( INT, MONEY ), new IConverter< Value<Integer>, Value<BigDecimal> >() {
            //int -> money
            @Override public Value<BigDecimal> to( Value<Integer> v ) { return Value.ofMoney(TypeConverter.convert(v.value(),BigDecimal.class)); }
            //money -> int
            @Override public Value<Integer> from( Value<BigDecimal> v ) { return Value.ofInt( v.value().intValue()); }
        });
        typeConvertors.put( Key.of( INT, FLOAT ), new IConverter< Value<Integer>, Value<Double> >() {
            //int -> money
            @Override public Value<Double> to( Value<Integer> v ) { return Value.ofFloat( v.value().doubleValue()); }
            //money -> int
            @Override public Value<Integer> from( Value<Double> v ) { return Value.ofInt( v.value().intValue()); }
        });
        typeConvertors.put( Key.of( INT, STRING ), new IConverter< Value<Integer>, Value<String> >() {
            //int -> money
            @Override public Value<String> to( Value<Integer> v ) { return Value.ofString(TypeConverter.convert(v.value(),String.class)); }
            //money -> int
            @Override public Value<Integer> from( Value<String> v ) { return Value.ofInt( TypeConverter.convert(v.value(),Integer.class)); }
        });
        typeConvertors.put( Key.of( FLOAT, MONEY ), new IConverter< Value<Double>, Value<BigDecimal> >() {
            //int -> money
            @Override public Value<BigDecimal> to( Value<Double> v ) { return Value.ofMoney( new BigDecimal(v.value())); }
            //money -> int
            @Override public Value<Double> from( Value<BigDecimal> v ) { return Value.ofFloat( v.value().doubleValue()); }
        });
    }

    /* */
    private static class ReverseConverter implements IConverter<Value<?>,Value<?>> {
        private final IConverter<Value<?>,Value<?>> converter;
        public ReverseConverter( IConverter<Value<?>,Value<?>> converter) { this.converter = converter; }
        @Override
        public Value< ? > to( Value< ? > value ) { return converter.from(value); }
        @Override
        public Value< ? > from( Value< ? > value ) { return converter.to(value);}
    }


    private static IConverter<Value<?>,Value<?>> getConverter( Value.Type to, Value.Type from )
    {
        final Key key = Key.of(to,from);
        IConverter<Value<?>,Value<?>> c = typeConvertors.get(key);
        if( c == null )
        {
            c = typeConvertors.get( Key.of(from,to)); // from, to !!! );
            if( c != null ) {
                c = new ReverseConverter(c);
                typeConvertors.put( key, c );
            }
        }
        return c;
    }


    /** */
    public static Value<?> convert( Value<?> v, Value.Type typeTo, ParserRuleContext ctx )
    {
        if( v == null || v.type() == NULL || typeTo == NULL )
            return Value.Null;

        if( v.type() == typeTo )
            return v;

        final IConverter<Value<?>,Value<?>> c = getConverter( v.type(), typeTo );

        if( c == null )
            throw new BilTypeException( "convert", Tags.PRODUCT_LABEL + "Нет правила(конвертора) для преобразования типа " + v.type() + " в " + typeTo, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine() );

        return c.to(v);
    }


    /** */
    private static int compareValues(Value<?> left, Value<?> right, ParserRuleContext ctx) {
        
        // Реализация сравнения значений различных типов
        if( left.type() == Value.Type.INT && right.type() == Value.Type.INT )
            return Integer.compare( left.asInt(), right.asInt() );
        //
        if( left.type() == Value.Type.FLOAT || right.type() == Value.Type.FLOAT )
            return Double.compare(left.asFloat(), right.asFloat());
        //
        if (left.type() == Value.Type.MONEY || right.type() == Value.Type.MONEY) {
            return left.asMoney().compareTo(right.asMoney());
        }
        if (left.type() == Value.Type.STRING && right.type() == Value.Type.STRING) {
            return left.asString().compareTo(right.asString());
        }

        throw new BilTypeException( "ValueHelper.compareValues", Tags.PRODUCT_LABEL + "Cannot compare " + left.type() + " and " + right.type(), ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine() );
    }
    
    /** */
    private static boolean compareImpl(  Value<?> left, Value<?> right, String operator, ParserRuleContext ctx )
    {
        ComparisonOperatorEnum op = ComparisonOperatorEnum.of(operator);

        switch (op) {
            case EQUAL: 
                return left.equals(right);
            case NOT_EQUAL:
                return !left.equals(right);
            case LESS_THAN:
                return compareValues(left, right, ctx) < 0;
            case GREATER_THAN:
                return compareValues(left, right, ctx) > 0;
            case LESS_THAN_OR_EQUAL:
                return compareValues(left, right, ctx) <= 0;
            case GREATER_THAN_OR_EQUAL:
                return compareValues(left, right, ctx) >= 0;
            default:
                throw new BilTypeException( "ValueHelper.compareImpl", Tags.PRODUCT_LABEL + "Unknown operator: " + op, ctx.getStart().getLine(), ctx.getStart().getCharPositionInLine() );
        }
    }

    /** */
    public static Value<Boolean> compare( Value<?> left, Value<?> right, String operator, ParserRuleContext ctx )
    {
        if( left.type() == Value.Type.NULL || right.type() == Value.Type.NULL )
            return Value.False;

        return Value.ofBool( compareImpl( left,right, operator, ctx ) );
    }

    /** */
    public static Value<?> createDefaultValue( Value.Type type)
    {
        switch( type ) {
            case INT:    return Value.ofInt(0);
            case FLOAT:  return Value.ofFloat(0.0);
            case STRING: return Value.ofString(S.EMPTY_STRING);
            case BOOL:   return Value.False;
            case MONEY:  return Value.ofMoney(BigDecimal.ZERO);
            case DATE:   return Value.ofDate(java.time.LocalDate.now());
            case TIME:   return Value.ofTime(java.time.LocalTime.now());
            case ARRAY:  return Value.ofArray( new ArrayList<>());
            case MAP:    return Value.ofMap  ( new HashMap<>()  );
            case NULL:   return Value.Null;
            default:     return Value.Null;
        }
    }

    /** Определяет тип из значения Java объекта */
    public static  Value.Type typeOfJavaObj (Object value) {
        if (value == null)
            return Value.Type.NULL;
        if (value instanceof Integer)
            return Value.Type.INT;
        if (value instanceof Double)
            return Value.Type.FLOAT;
        if (value instanceof String)
            return Value.Type.STRING;
        if (value instanceof Boolean)
            return Value.Type.BOOL;
        if (value instanceof BigDecimal)
            return Value.Type.MONEY;
        if (value instanceof LocalDate)
            return Value.Type.DATE;
        if (value instanceof LocalTime)
            return Value.Type.TIME;
        if (value instanceof List)
            return Value.Type.ARRAY;
        if (value instanceof Map)
            return Value.Type.MAP;

        throw new RuntimeException("Unknown value type: " + value.getClass());
    }

}
