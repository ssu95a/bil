package ru.inversion.bil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class FunctionTestScript {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("bil");

        if (engine != null) {
            try {
                StringWriter output = new StringWriter();
                engine.getContext().setWriter(output);

                String functionScript = "// Тестирование встроенных функций\n" +
                        "println(\"Testing built-in functions:\");\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Функция print\n" +
                        "print(\"Print without newline: \");\n" +
                        "print(\"multiple \");\n" +
                        "print(\"calls \");\n" +
                        "println(\"with println at the end\");\n" +
                        "\n" +
                        "// Функция println\n" +
                        "println(\"This is on line 1\");\n" +
                        "println(\"This is on line 2\");\n" +
                        "println(\"This is on line 3\");\n" +
                        "\n" +
                        "// Комбинированный вывод\n" +
                        "int num = 42;\n" +
                        "string text = \"answer\";\n" +
                        "bool truth = true;\n" +
                        "\n" +
                        "print(\"The \");\n" +
                        "print(text);\n" +
                        "print(\" is \");\n" +
                        "print(num);\n" +
                        "print(\" and this is \");\n" +
                        "println(truth);\n" +
                        "\n" +
                        "println(\"\");\n" +
                        "println(\"Function test completed!\");";

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