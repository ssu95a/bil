package ru.inversion.bil;

import org.antlr.v4.runtime.ParserRuleContext;
import ru.inversion.bil.antlr.BilParser;
import ru.inversion.bil.antlr.BilBaseVisitor;
import ru.inversion.utils.TriFunction;
import ru.inversion.utils.U;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.SimpleBindings;
import javax.script.SimpleScriptContext;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class BilVisitorImpl extends BilBaseVisitor<Value<?>> {

    /** */
    private ScriptContext scriptContext;

    /** */
    //private final Map<String,BilScriptEngine.FunctionDef > functions;
    private Map<String, TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?> >> functions;

    /** */
    public BilVisitorImpl( ScriptContext scriptContext) {

        this.scriptContext = scriptContext;

// Инициализируем bindings если они null
//        if( scriptContext.getBindings( ScriptContext.ENGINE_SCOPE ) == null )
//            scriptContext.setBindings( new SimpleBindings(), ScriptContext.ENGINE_SCOPE );
    }

    private Map<String, TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?> >> functions() {

        Map<String, TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?> >> fs =
                (Map<String, TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?> >>)
                scriptContext.getAttribute("__functions__", ScriptContext.GLOBAL_SCOPE );
        if( fs == null ) {
            fs = BuiltInFunctionsHelper.initFunctions();
            scriptContext.setAttribute("__functions__", fs, ScriptContext.GLOBAL_SCOPE );
        }
        return fs;
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
    public Value<?> visitParameter( BilParser.ParameterContext ctx) {

        String paramName = ctx.ID().getText();

        // Инициализируем параметр значением по умолчанию
        String typeName = ctx.type().getText();
        Value.Type paramType = Value.types.get(typeName);
        Value<?> defaultValue = ValueHelper.createDefaultValue(paramType);

        getEngineBindings().put(paramName, defaultValue);

        return Value.VOID;
    }

    /** */
    @Override
    public Value<?> visitProgram( BilParser.ProgramContext ctx )
    {
        Value<?> result = Value.VOID;

        for( BilParser.StatementContext stmt : ctx.statement() )
             result = visit(stmt);

        if( ctx.returnExpression() != null )
            result = visit( ctx.returnExpression() );

        return result;
    }

    /** */
    @Override
    public Value<Void> visitVariableDeclaration( BilParser.VariableDeclarationContext ctx )
    {
        Value.Type declaredType = Value.types.get( ctx.type().getText() );

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
        }
        return Value.VOID;
    }

    @Override
    public Value<?> visitMethodCallExpr( BilParser.MethodCallExprContext ctx )
    {
        return visitMethodCall(ctx.methodCall());
    }

    @Override
    public Value<?> visitMethodCall( BilParser.MethodCallContext ctx)
    {
        final String objectName = ctx.ID(0).getText();
        final String methodName = ctx.ID(1).getText();
        final List<Value<?>> args = new ArrayList<>();

        if( ctx.argumentList() != null )
        {
            for( BilParser.ExpressionContext expr : ctx.argumentList().expression()) {
                 args.add(visit(expr));
            }
        }

        Object target = getEngineBindings().get(objectName);
        if( target == null )
            target = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE).get(objectName);

        if( target == null ) {

            if( getEngineBindings().containsKey(objectName) )
                BilException.throwArgumentException(  methodName, objectName + " value is null", ctx);

            BilException.throwRuntimeException("Object not found: " + objectName, ctx);
        }
        return invokeObjectMethod(objectName, methodName, args, ctx );
    }


    /** */
    private Value<?> invokeObjectMethod( String objectName, String methodName, List<Value<?>> args, ParserRuleContext ctx  )  {

        // Получаем объект из контекста
        Object target = getEngineBindings().get(objectName);

        if( target == null )
            target = scriptContext.getBindings( ScriptContext.GLOBAL_SCOPE ).get(objectName);

        if( target == null )
            BilException.throwRuntimeException( "Object not found: " + objectName, ctx);

        if( target instanceof Value )
        {
            Value<?> v = (Value<?>)target;
            switch ( v.type() ) {
                case NULL:
                    return v;
                case STRING:
                    return StringObjHelper.invokeStringMethod( (Value<String>) v, methodName, args, ctx);
                case ARRAY:
                    return ArrayHelper.invokeArrayMethod((Value<List<Value<?>>>)v, methodName, args, ctx);
                case MAP:
                    return MapHelper.invokeMapMethod(( Value<Map<Value<?>, Value<?>>>) v, methodName, args, ctx);
                default:
                    BilException.throwFunctionException(methodName, "Bad call " + methodName + " for " + objectName, ctx);
            }
        }
        else
        {
            if( target instanceof FormMaker ) {
                FormMaker.invokeMethod((FormMaker) target, methodName, args, ctx);
                return Value.VOID;
            }

            if( target instanceof String )
                return StringObjHelper.invokeStringMethod( Value.ofString(target.toString() ), methodName, args, ctx);

            if( target instanceof List )
                return ArrayHelper.invokeArrayMethod( Value.ofArray((List<Value<?>>) target), methodName, args, ctx);

            if( target instanceof Map )
                return MapHelper.invokeMapMethod( Value.ofMap((Map) target), methodName, args, ctx);

            // Рефлексивный вызов для других объектов
            try {
                // Преобразуем аргументы в Java объекты
                Object[] javaArgs = args.stream().map(Value::toJavaObject).toArray();

                // Ищем подходящий метод
                java.lang.reflect.Method method = findMethod(target.getClass(), methodName, javaArgs);

                // Вызываем метод
                Object result = method.invoke(target, javaArgs);

                // Если метод void, возвращаем VOID
                if( method.getReturnType() == void.class )
                    return Value.VOID;

                return Value.fromObject( result );

            } catch( Exception e ) {
                BilException.throwFunctionException( objectName + "." + methodName, "Error calling method " + objectName + "." + methodName + ": " + e.getMessage(), ctx, e );
            }
        }
        //stub
        return Value.VOID;
    }

    /** */
    private java.lang.reflect.Method findMethod(Class<?> clazz, String methodName, Object[] args) {
        for (java.lang.reflect.Method method : clazz.getMethods()) {
            if (method.getName().equals(methodName) && isCompatible(method, args)) {
                return method;
            }
        }
        throw new RuntimeException("Method not found: " + methodName + " with compatible parameters");
    }

    /** */
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
        final List<Value<?>> args = new ArrayList<>();

        if( ctx.argumentList() != null )
        {
            for( BilParser.ExpressionContext expr : ctx.argumentList().expression()) {
                 Value<?> argValue = visit(expr);
                 args.add(argValue);
            }
        }

        TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?>> function = functions().get(funcName);

        if( function == null )
            BilException.throwFunctionException( funcName, "Function not found: " + funcName, ctx );

        return function.apply( scriptContext, args, ctx );
    }


    /** */
    @Override
    public Value<?> visitMultiplicativeExpr( BilParser.MultiplicativeExprContext ctx )
    {
        final Value<?> left = visit(ctx.expression(0));
        final Value<?> right= visit(ctx.expression(1));
        char op = ctx.op.getText().charAt(0);
        return Value.mathOperator( left, right, op );
    }

    /** */
    @Override
    public Value<?> visitAdditiveExpr( BilParser.AdditiveExprContext ctx )
    {
        final Value<?>  left = visit(ctx.expression(0));
        final Value<?> right = visit(ctx.expression(1));
        char op = ctx.op.getText().charAt(0);
        return Value.mathOperator( left, right, op );
    }

    @Override
    public Value<?> visitVariableExpr(BilParser.VariableExprContext ctx) {

        final String varName = ctx.ID().getText();

        // Специальные функции
        if( U.in( varName, "typeof", "rangeof", "print", "concat") ) {
            return super.visitVariableExpr(ctx);
        }

// 2. Если не нашли, ищем в GLOBAL_SCOPE (PoluchName, ACCA)
        Object value = getEngineBindings().get(varName);

        if( value == null )
        {
            Bindings globalBindings = scriptContext.getBindings(ScriptContext.GLOBAL_SCOPE);
            if( globalBindings != null )
                value = globalBindings.get(varName);
        }

        if( value == null )
            return Value.Null;

        if( value instanceof Value )
            return (Value<?>) value;

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
    public Value<String> visitStringExpr(BilParser.StringExprContext ctx)
    {
        final String text    = ctx.STRING().getText();
        final String content = text.substring(1, text.length() - 1);

        if( content.indexOf('\\') == -1 )
            return Value.ofString(content);

        return Value.ofString( StringObjHelper.unescapeString(content) );
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

        return ValueHelper.compare( left, right, op, ctx );
    }

    @Override
    public Value<Boolean> visitEqualityExpr( BilParser.EqualityExprContext ctx ) {

        Value<?> left  = visit(ctx.expression(0));
        Value<?> right = visit(ctx.expression(1));

        final String op = ctx.op.getText();

        return ValueHelper.compare( left, right, op, ctx );
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

    @Override
    public Value<String> visitCharExpr(BilParser.CharExprContext ctx) {
        final String text = ctx.CHAR().getText();
        // Извлекаем символ из 'a' -> "a"
        final String content = text.substring(1, text.length() - 1);

        // Быстрая проверка
        if( content.indexOf('\\') == -1 )
            return Value.ofString(content);

        return Value.ofString( StringObjHelper.unescapeString( content) );
    }

    // Increments & Decrements

    private Bindings findWritableBindings(ScriptContext ctx, String varName) {
        Bindings engineBindings = ctx.getBindings(ScriptContext.ENGINE_SCOPE);

        if (engineBindings != null && engineBindings.containsKey(varName)) {
            return engineBindings;
        }

        if (ctx instanceof FunctionContext) {
            return findWritableBindings(((FunctionContext) ctx).getParent(), varName);
        }

        Bindings globalBindings = ctx.getBindings(ScriptContext.GLOBAL_SCOPE);

        if (globalBindings != null && globalBindings.containsKey(varName)) {
            return globalBindings;
        }

        return null;
    }

    private void updateVariable(BilParser.ExpressionContext expr, Value<Integer> newValue) {

        if( !(expr instanceof BilParser.VariableExprContext) )
            BilException.throwRuntimeException( "++/-- can only be applied to variable", expr );

        String varName = ((BilParser.VariableExprContext) expr).ID().getText();

        Bindings bindings = findWritableBindings(scriptContext, varName);

        if( bindings == null )
            BilException.throwRuntimeException( "Variable not found: " + varName, expr );

        Object currentValue = bindings.get(varName);

        if (currentValue instanceof Value) {
            bindings.put(varName, newValue);
        } else {
            bindings.put(varName, newValue.toJavaObject());
        }
    }

    @Override
    public Value<Integer> visitPostIncrementExpr(BilParser.PostIncrementExprContext ctx) {
        Value<?> value = visit(ctx.expression());

        // ТОЛЬКО ДЛЯ INT
        if (value.type() != Value.Type.INT) {
            throw new RuntimeException("++ can only be applied to integers");
        }

        int current = value.asInt();
        int newValue = current + 1;
        updateVariable(ctx.expression(), Value.ofInt(newValue));
        return Value.ofInt(current); // Возвращаем старое значение (пост-инкремент)
    }

    @Override
    public Value<Integer> visitPostDecrementExpr(BilParser.PostDecrementExprContext ctx) {
        Value<?> value = visit(ctx.expression());

        // ТОЛЬКО ДЛЯ INT
        if (value.type() != Value.Type.INT) {
            throw new RuntimeException("-- can only be applied to integers");
        }

        int current = value.asInt();
        int newValue = current - 1;
        updateVariable(ctx.expression(), Value.ofInt(newValue));
        return Value.ofInt(current); // Возвращаем старое значение (пост-декремент)
    }

    @Override
    public Value<Integer> visitPreDecrementExpr(BilParser.PreDecrementExprContext ctx) {
        Value<?> value = visit(ctx.expression());

        // ТОЛЬКО ДЛЯ INT
        if (value.type() != Value.Type.INT) {
            throw new RuntimeException("-- can only be applied to integers");
        }

        int newValue = value.asInt() - 1;
        updateVariable(ctx.expression(), Value.ofInt(newValue));
        return Value.ofInt(newValue); // Возвращаем новое значение (пре-декремент)
    }

    @Override
    public Value<Integer> visitPreIncrementExpr(BilParser.PreIncrementExprContext ctx) {
        Value<?> value = visit(ctx.expression());

        // ТОЛЬКО ДЛЯ INT
        if (value.type() != Value.Type.INT) {
            throw new RuntimeException("++ can only be applied to integers");
        }

        int newValue = value.asInt() + 1;
        updateVariable(ctx.expression(), Value.ofInt(newValue));
        return Value.ofInt(newValue); // Возвращаем новое значение (пре-инкремент)
    }


    //
    @Override
    public Value<?> visitIfStatement(BilParser.IfStatementContext ctx) {
        Value<?> condition = visit(ctx.expression());
        if (condition.isTruthy()) {
            return visit(ctx.statement(0));
        } else if (ctx.statement().size() > 1) {
            return visit(ctx.statement(1));
        }
        return Value.VOID;
    }

    @Override
    public Value<?> visitWhileStatement(BilParser.WhileStatementContext ctx) {
        Value<?> result = Value.VOID;
        while (visit(ctx.expression()).isTruthy()) {
            result = visit(ctx.statement());
        }
        return result;
    }

    /** */
    @Override
    public Value<?> visitForStatement(BilParser.ForStatementContext ctx) {
        Value<?> result = Value.VOID;

        // Инициализация
        if (ctx.variableDeclaration() != null) {
            visit(ctx.variableDeclaration());
        } else if (ctx.assignment() != null) {
            visit(ctx.assignment());
        }

        // Условие и обновление
        BilParser.ExpressionContext condition = null;
        BilParser.ExpressionContext update = null;

        if(!ctx.expression().isEmpty() ) {
            condition = ctx.expression(0);
        }

        if( ctx.expression().size() > 1 ) {
            update = ctx.expression(1);
        }

        // Цикл
        while (condition == null || visit(condition).isTruthy()) {
            // Выполняем statement (может быть пустым)
            if (ctx.statement() != null) {
                result = visit(ctx.statement());
            }

            // Обновление
            if (update != null) {
                visit(update);
            }
        }

        return result;
    }

    // Добавить поле в класс
    private boolean returnEncountered = false;

    @Override
    public Value<?> visitReturnStatement(BilParser.ReturnStatementContext ctx) {
        returnEncountered = true;
        if (ctx.expression() != null) {
            return visit(ctx.expression());
        }
        return Value.VOID;
    }

    @Override
    public Value<?> visitBlock(BilParser.BlockContext ctx) {
        returnEncountered = false;
        Value<?> result = Value.VOID;
        for (BilParser.StatementContext stmt : ctx.statement()) {
            result = visit(stmt);
            if (returnEncountered) {
                return result;
            }
        }
        return result;
    }

    @Override
    public Value<?> visitFunctionDeclaration( BilParser.FunctionDeclarationContext ctx ) {

        final String funcName = ctx.ID().getText();

        functions().put(funcName, new TriFunction<ScriptContext, List<Value<?>>, ParserRuleContext, Value<?> >() {
            @Override
            public Value<?> apply(ScriptContext scriptContext, List<Value<?>> args, ParserRuleContext unused) {

                // Создаем временный контекст для функции
                final FunctionContext functionContext = new FunctionContext( scriptContext );
                final boolean savedReturnEncountered = BilVisitorImpl.this.returnEncountered;

                // Устанавливаем параметры функции
                final List<BilParser.ParameterContext> params = ctx.parameterList() == null ? Collections.emptyList() : ctx.parameterList().parameter();

                for( int i = 0; i < params.size(); i++ )
                {
                    String paramName = params.get(i).ID().getText();
                    Value<?> argValue = i < args.size() ? args.get(i) : Value.Null;
                    functionContext.setAttribute( paramName, argValue, ScriptContext.ENGINE_SCOPE );
                }

                try {

                    // Временно подменяем контекст на functionContext
                    BilVisitorImpl.this.scriptContext     = functionContext;
                    BilVisitorImpl.this.returnEncountered = false;

                    // Выполняем тело функции
                    return visit(ctx.block());

                } finally  {
                    // Восстанавливаем оригинальный контекст
                    BilVisitorImpl.this.scriptContext     = functionContext.getParent();
                    BilVisitorImpl.this.returnEncountered = savedReturnEncountered;
                }
            }
        });

        return Value.VOID;
    }

    private Bindings findBindingsForAssignment( ScriptContext ctx, String name )
    {
        Bindings engine = ctx.getBindings(ScriptContext.ENGINE_SCOPE);
        if( engine != null && engine.containsKey(name) )
            return engine;

        if( ctx instanceof FunctionContext )
            return findBindingsForAssignment(((FunctionContext) ctx).getParent(), name);

        Bindings global = ctx.getBindings(ScriptContext.GLOBAL_SCOPE);
        if (global != null && global.containsKey(name)) {
            return global;
        }

        return null;
    }

    @Override
    public Value<?> visitVariableAssignment(BilParser.VariableAssignmentContext ctx) {

        final String varName = ctx.ID().getText();
        Value<?> value = visit(ctx.expression());

        Bindings targetBindings = findBindingsForAssignment(scriptContext, varName);

        if( targetBindings == null )
        {
            // Для строгого C-like поведения лучше бросать ошибку.
            // Для мягкой обратной совместимости можно оставить создание в текущем scope.
            getEngineBindings().put(varName, value);

            return value;
        }

        Object currentValue = targetBindings.get(varName);

        if( currentValue instanceof Value )
        {
            Value<?> currentValueObj = (Value<?>) currentValue;
            value = ValueHelper.convert(value, currentValueObj.type(), ctx);
            targetBindings.put(varName, value);
        }
        else if (currentValue != null) {
            Value.Type currentType = ValueHelper.typeOfJavaObj(currentValue);
            value = ValueHelper.convert(value, currentType, ctx);
            targetBindings.put(varName, value.toJavaObject());
        } else {
            targetBindings.put(varName, value);
        }

        return value;
    }

    /** */
    @Override
    public Value<?> visitArrayElementAssignment(BilParser.ArrayElementAssignmentContext ctx) {

        BilParser.ArrayIndexAccessContext arrayAccess = ctx.arrayIndexAccess();
        Value<?> arrayValue = visit(arrayAccess.expression(0));
        Value<?> indexValue = visit(arrayAccess.expression(1));
        Value<?> value      = visit(ctx.expression());

        if( arrayValue.type() != Value.Type.ARRAY )
            BilException.throwTypeException( "visitArrayElementAssignment", "Expected array type for element assignment", ctx);

        if (indexValue.type() != Value.Type.INT)
            BilException.throwTypeException( "visitArrayElementAssignment", "Array index must be integer", ctx);

        List<Value<?>> array = arrayValue.asArray();
        int index = indexValue.asInt();

        if (index < 0 || index >= array.size()) {
            BilException.throwRuntimeException("Array index out of bounds: " + index, ctx);
        }

        // Устанавливаем значение в массив
        // Массив уже является Value объектом, поэтому изменения сохраняются!
        array.set(index, value);
        return value;
    }

    /** */
    @Override
    public Value<?> visitArrayIndexAccess(BilParser.ArrayIndexAccessContext ctx)
    {
        final Value<?> containerValue = visit( ctx.expression(0) );
        final Value<?> keyValue       = visit( ctx.expression(1) );

        if( containerValue.type() == Value.Type.ARRAY )
        {
            if( keyValue.type() != Value.Type.INT )
                BilException.throwTypeException( "", "Array index must be integer", ctx);

            List<Value<?>> array = containerValue.asArray();
            int index = keyValue.asInt();

            if( index < 0 || index >= array.size() )
                BilException.throwRuntimeException("Array index out of bounds: " + index, ctx);

            return array.get(index);

        }
        else
            if (containerValue.type() == Value.Type.MAP)
            {
                Map<Value<?>, Value<?>> map = containerValue.asMap();
                return map.computeIfAbsent( keyValue, k -> Value.Null );
            }

        BilException.throwTypeException( "","Expected array or map type for index access", ctx );

        return Value.Null;
    }

    @Override
    public Value<?> visitMapLiteral(BilParser.MapLiteralContext ctx) {

        Map<Value<?>, Value<?>> map = new HashMap<>();

        if (ctx.keyValueList() != null)
        {
            for (BilParser.KeyValuePairContext kvp : ctx.keyValueList().keyValuePair()) {
                Value<?> key    = visit(kvp.expression(0));
                Value<?> value = visit(kvp.expression(1));
                map.put(key, value);
            }
        }

        return Value.ofMap(map);
    }

    @Override
    public Value<?> visitMapLiteralExpr(BilParser.MapLiteralExprContext ctx) {
        return visitMapLiteral(ctx.mapLiteral());
    }

    /** */
    @Override
    public Value<?> visitStatement(BilParser.StatementContext ctx) {
        if (ctx.variableDeclaration() != null) {
            return visit(ctx.variableDeclaration());
        } else if (ctx.assignment() != null) {
            // Вместо visitAssignment(ctx.assignment()) - ANTLR сам вызовет нужный метод
            return visit(ctx.assignment());
        } else if (ctx.functionCall() != null) {
            return visit(ctx.functionCall());
        } else if (ctx.functionDeclaration() != null) {
            return visit(ctx.functionDeclaration());
        } else if (ctx.ifStatement() != null) {
            return visit(ctx.ifStatement());
        } else if (ctx.whileStatement() != null) {
            return visit(ctx.whileStatement());
        } else if (ctx.forStatement() != null) {
            return visit(ctx.forStatement());
        } else if (ctx.returnStatement() != null) {
            return visit(ctx.returnStatement());
        } else if (ctx.block() != null) {
            return visit(ctx.block());
        } else if (ctx.expression() != null) {
            return visit(ctx.expression());
        } else {
            // Пустой statement ';'
            return Value.VOID;
        }
    }}