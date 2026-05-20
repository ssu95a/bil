package ru.inversion.bil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class TestFunctionScript {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("bil");

        if (engine != null) {
            try {
                StringWriter output = new StringWriter();
                engine.getContext().setWriter(output);

                String functionScript = "// Скрипт 1\n" +
                        "map testMap = {};\n" +
                        "testMap.put(\"from_script1\", \"value\");\n" +
                        "int testCounter = 42;\n" +
                        "\n" +
                        "// Скрипт 2  \n" +
                        "_FM.println(\"testMap из скрипта 1: \" + testMap.get(\"from_script1\"));\n" +
                        "_FM.println(\"testCounter из скрипта 1: \" + testCounter);\n" +
                        "\n" +
                        "testMap.put(\"from_script2\", \"value2\");\n" +
                        "testCounter = 100;\n" +
                        "\n" +
                        "// Скрипт 3\n" +
                        "_FM.println(\"testMap после скрипта 2: \" + testMap.toString());\n" +
                        "_FM.println(\"testCounter после скрипта 2: \" + testCounter);";
//                        "// Попытка доступа к внутренним переменным должна вызвать ошибку\n" +
//                        "_FM.println(\"Проверка internalVar: \" + (typeof(internalVar) == \"NULL\" ? \"не найдена\" : \"найдена\"));\n" +
//                        "_FM.println(\"Проверка internalMap: \" + (typeof(internalMap) == \"NULL\" ? \"не найдена\" : \"найдена\"));";

                System.out.println("Executing function test script...");
                System.out.println("==========================================");

                engine.eval(functionScript);

                System.out.println("==========================================");
                System.out.println("Function test output:");
                System.out.println(output.toString());
                System.out.println("==========================================");

            } catch (ScriptException e) {
                System.err.println("Script error: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.println("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Bil engine not found");
        }
    }
}