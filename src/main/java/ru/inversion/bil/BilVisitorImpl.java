package ru.inversion.bil;

import ru.inversion.bil.antlr.BilParser;
import ru.inversion.bil.antlr.BilBaseVisitor;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BilVisitorImpl extends BilBaseVisitor<Value<?>> {

    private final ScriptContext scriptContext;

    private final Map<String,BilScriptEngine.FunctionDef > functions;
    private final Map<String, Value.Type> variableTypes = new HashMap<>();
    /** */
    public BilVisitorImpl( ScriptContext scriptContext, Map<String, BilScriptEngine.FunctionDef> functions) {

        this.scriptContext = scriptContext;
        this.functions     = functions;

        // Инициализируем bindings если они null
        if( scriptContext.getBindings( ScriptContext.ENGINE_SCOPE ) == null )
            scriptContext.setBindings( new SimpleBindings(), ScriptContext.ENGINE_SCOPE );
    }

    /** */
    private Bindings getEngineBindings() {
        Bindings bindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        if (bindings == null) {
            bindings = new SimpleBindings();
            scriptContext.setBindings(bindings, ScriptContext.ENGINE_SCOPE);
        }
        return bindings;
    }

    @Override
    public Value<?> visitProgram(BilParser.ProgramContext ctx) {

        Value<?> result = Value.VOID;
        for( BilParser.StatementContext stmt : ctx.statement() )
            result = visit(stmt);

        if( ctx.returnExpression() != null )
            result = visit(ctx.returnExpression());

        return result;
    }

    /** */
    @Override
    public Value<Void> visitVariableDeclaration( BilParser.VariableDeclarationContext ctx )
    {
        final String varName = ctx.ID().getText();
        Value<?> value = Value.Null;
        Value.Type declaredType = Value.types.get( ctx.type().getText() );

<<<<<<< HEAD
        if( ctx.expression() != null ) {
            value = visit(ctx.expression());
            value = ValueHelper.convert( value, declaredType );
=======
        // Обрабатываем все объявления
        for( int i = 0; i < ctx.ID().size(); i++ )
        {
            String varName = ctx.ID(i).getText();

            // Если переменная уже есть в контексте
            // не инициализируем!
            // т.к. может передаваться между вызовами
            if( getEngineBindings().containsKey(varName) )
                continue;

            Value<?> value = Value.Null;

            // Если есть выражение для этой переменной
            if( ctx.expression().size() > i )
            {
                value = visit(ctx.expression(i));

                // Для литерала мапы не нужно преобразование типа
                if( value.type() != declaredType && !(ctx.expression(i) instanceof BilParser.MapLiteralExprContext ))
                {
                    value = ValueHelper.convert( value, declaredType, ctx);
                }

                //value = ValueHelper.convert( value, declaredType, ctx);
            }
            else
                value = ValueHelper.createDefaultValue( declaredType );

            getEngineBindings().put( varName, value );
>>>>>>> be3fc7b (SUPXXI-22856 при повторной иницицализации переменной, значение не присваивается, если переменная уже в контексте)
        }

        variableTypes.put( varName, declaredType );
        getEngineBindings().put( varName, value.toJavaObject() );

        return Value.VOID;
    }

    /** */
    @Override
    public Value<?> visitAssignment( BilParser.AssignmentContext ctx ) {

        final String varName = ctx.ID().getText();
        Value<?> value = visit(ctx.expression());

// Проверяем, была ли переменная объявлена
        Value.Type varType = variableTypes.get( varName );
        if( varType == null)
            throw new RuntimeException("Variable not declared: " + varName);

        // АВТОМАТИЧЕСКОЕ ПРИВЕДЕНИЕ ТИПОВ
        value = ValueHelper.convert( value, varType );

        getEngineBindings().put( varName, value.toJavaObject() );

        return value;
    }

    @Override
    public Value<?> visitMethodCallExpr( BilParser.MethodCallExprContext ctx )
    {
        return visitMethodCall(ctx.methodCall());
    }

    @Override
    public Value<?> visitMethodCall(BilParser.MethodCallContext ctx) {

        final String objectName = ctx.ID(0).getText();
        final String methodName = ctx.ID(1).getText();
        final  List<Value<?>> args = new ArrayList<>();

        if (ctx.argumentList() != null)
        {
            for( BilParser.ExpressionContext expr : ctx.argumentList().expression()) {
                 args.add(visit(expr));
            }
        }

        return invokeObjectMethod( objectName, methodName, args) ;
    }

    /** */
    private Value<?> invokeObjectMethod( String objectName, String methodName, List<Value<?>> args )
    {
        // Получаем объект из контекста
        Object target = getEngineBindings().get(objectName);
        if (target == null)
            target = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE).get(objectName);

        if( target == null )
            throw new RuntimeException("Object not found: " + objectName);

        try {
            // Преобразуем аргументы в Java объекты
            Object[] javaArgs = args.stream().map(Value::toJavaObject).toArray();

            // Ищем подходящий метод
            java.lang.reflect.Method method = findMethod( target.getClass(), methodName, javaArgs );

            // Вызываем метод
            Object result = method.invoke(target, javaArgs);

            // Если метод void, возвращаем VOID
            if (method.getReturnType() == void.class) {
                return Value.VOID;
            }

            return Value.fromObject(result);

        } catch (Exception e) {
            throw new RuntimeException("Error calling method " + objectName + "." + methodName + ": " + e.getMessage(), e);
        }
    }

    private java.lang.reflect.Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        for (java.lang.reflect.Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && isCompatible(method, args)) {
                return method;
            }
        }
        throw new RuntimeException("Method not found: " + methodName + " with compatible parameters");
    }

    private boolean isCompatible(java.lang.reflect.Method method, Object[] args) {
        Class<?>[] paramTypes = method.getParameterTypes();
        if (paramTypes.length != args.length) {
            return false;
        }

        for (int i = 0; i < paramTypes.length; i++) {
            if (args[i] != null && !paramTypes[i].isAssignableFrom(args[i].getClass())) {
                // Проверяем автоупаковку и преобразование типов
                if (paramTypes[i] == String.class) {
                    // Все можно преобразовать в строку
                    continue;
                }
                if (paramTypes[i] == int.class && args[i] instanceof Integer) {
                    continue;
                }
                if (paramTypes[i] == double.class && args[i] instanceof Double) {
                    continue;
                }
                if (paramTypes[i] == boolean.class && args[i] instanceof Boolean) {
                    continue;
                }
                return false;
            }
        }
        return true;
    }


    /** */
    @Override
    public Value<?> visitFunctionCall( BilParser.FunctionCallContext ctx )
    {
        final String funcName = ctx.ID().getText();
        List<Value<?>> args = new ArrayList<>();

        if( ctx.argumentList() != null )
            args = ctx.argumentList().expression().stream().map(this::visit).collect(Collectors.toList());

        if( "typeof".equals(funcName) )
        {
            if( args.size() != 1 )
                throw new RuntimeException("typeof() expects exactly 1 argument");

            Value<?> argument = args.get(0);

            return Value.ofString( argument.type().name().toLowerCase() );
        }

        if( "rangeof".equals(funcName) )
        {
            if( args.size() <= 1 )
                return Value.ofInt(1);

            return Value.ofInt( args.size() );
        }

        final BilScriptEngine.FunctionDef function = functions.get(funcName);
        if( function == null)
            throw new RuntimeException("Function not found: " + funcName);

        return function.execute(scriptContext, args.toArray(new Value[0]));
    }

    /** */
    @Override
    public Value<?> visitMultiplicativeExpr(BilParser.MultiplicativeExprContext ctx) {
        final Value<?> left = visit(ctx.expression(0));
        final Value<?> right= visit(ctx.expression(1));
        char op = ctx.op.getText().charAt(0);
        return Value.mathOperator( left, right, op );
    }

    @Override
    public Value<?> visitAdditiveExpr(BilParser.AdditiveExprContext ctx) {
        final Value<?> left = visit(ctx.expression(0));
        final Value<?> right= visit(ctx.expression(1));
        char op = ctx.op.getText().charAt(0);
        return Value.mathOperator( left, right, op );
    }

    @Override
    public Value visitVariableExpr( BilParser.VariableExprContext ctx ) {

        final String varName = ctx.ID().getText();

        // Если это вызов typeof, обрабатываем особо
        if( "typeof".equals(varName) ) {
            // Это будет обработано в visitFunctionCall
            return super.visitVariableExpr(ctx);
        }
        else if( "rangeof".equals(varName) ) {
            // Это будет обработано в visitFunctionCall
            return super.visitVariableExpr(ctx);
        }

        Object value = getEngineBindings().get(varName);

        if (value == null) {
            // Для необъявленных переменных возвращаем NULL вместо исключения
            return Value.Null;
        }

        return Value.fromObject(value);
    }

    @Override
    public Value<? extends Number> visitNumberExpr(BilParser.NumberExprContext ctx) {
        final String numText = ctx.NUMBER().getText();
        if (numText.contains(".")) {
            return Value.ofMoney(numText);
        } else {
            return Value.ofInt(numText);
        }
    }

    @Override
    public Value<?> visitUnaryMinusExpr(BilParser.UnaryMinusExprContext ctx) {

        final Value<?> value = visit( ctx.expression() );

        switch(value.type() ) {
            case INT:   return Value.ofInt  ( -value.asInt() );
            case FLOAT: return Value.ofFloat( -value.asFloat() );
            case MONEY: return Value.ofMoney( value.asMoney().negate() );
            default:
                throw new RuntimeException("Unary minus not supported for type: " + value.type());
        }
    }

    /** */
    @Override
    public Value<?> visitUnaryPlusExpr(BilParser.UnaryPlusExprContext ctx) {
        return visit(ctx.expression());
    }


    @Override
    public Value<String> visitStringExpr(BilParser.StringExprContext ctx) {
        final String text = ctx.STRING().getText();
        return Value.ofString( text.substring(1, text.length() - 1) );
    }

    @Override
    public Value<Boolean> visitBoolExpr( BilParser.BoolExprContext ctx ) {
        return Value.ofBool( ctx.BOOL().getText() );
    }

    @Override
    public Value<?> visitNullExpr(BilParser.NullExprContext ctx) {
        return Value.Null;
    }

    @Override
    public Value visitLogicalAndExpr(BilParser.LogicalAndExprContext ctx) {
        Value left  = visit(ctx.expression(0));
        Value right = visit(ctx.expression(1));
        return Value.ofBool( left.isTruthy() && right.isTruthy() );
    }

    @Override
    public Value<?> visitLogicalOrExpr(BilParser.LogicalOrExprContext ctx) {
        Value<?> left  = visit( ctx.expression(0) );
        Value<?> right = visit( ctx.expression(1) );
        return Value.ofBool( left.isTruthy() || right.isTruthy() );
    }

    /** */
    @Override
    public Value<?> visitRelationalExpr(BilParser.RelationalExprContext ctx)
    {
        Value<?> left  = visit( ctx.expression(0) );
        Value<?> right = visit( ctx.expression(1) );

        final String op = ctx.op.getText();

        return ValueHelper.compare( left, right, op );
    }

    @Override
    public Value<Boolean> visitEqualityExpr( BilParser.EqualityExprContext ctx ) {

        Value<?> left  = visit(ctx.expression(0));
        Value<?> right = visit(ctx.expression(1));

        final String op = ctx.op.getText();

        return ValueHelper.compare( left, right, op );
    }

    @Override
    public Value<?> visitParenExpr(BilParser.ParenExprContext ctx) {
        return visit(ctx.expression());
    }

    @Override
    public Value<?> visitFunctionCallExpr(BilParser.FunctionCallExprContext ctx) {
        return visitFunctionCall( ctx.functionCall() );
    }

    /** */
    @Override
    public Value< LocalDate > visitDateExpr( BilParser.DateExprContext ctx ) {
        return Value.ofDate(ctx.DATE().getText());
    }

    /** */
    @Override
    public Value<LocalTime> visitTimeExpr( BilParser.TimeExprContext ctx) {
        return Value.ofTime( ctx.TIME().getText() );
    }
}