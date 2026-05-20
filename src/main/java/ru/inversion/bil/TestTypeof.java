package ru.inversion.bil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class TestTypeof {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("bil");

        if (engine != null) {
            try {
                StringWriter output = new StringWriter();
                engine.getContext().setWriter(output);

                String testScript = "// Тестирование функции typeof()\n" +
                        "print(\"Testing typeof() function:\");\n" +
                        "print(1+\"||\");\n" +
                        "\n" +
                        "// Типы литералов\n" +
                        "println(\"typeof(42): \" + typeof(42));\n" +
                        "println(\"typeof(3.14): \" + typeof(3.14));\n" +
                        "println(\"typeof(\\\"hello\\\"): \" + typeof(\"hello\"));\n" +
                        "println(\"typeof(true): \" + typeof(true));\n" +
                        "println(\"typeof(null): \" + typeof(null));\n" +
                        "println(\"typeof(123.45m): \" + typeof(123.45m));\n" +
                        "println(\"typeof(2024-01-15): \" + typeof(2024-01-15));\n" +
                        "println(\"typeof(14:30:00): \" + typeof(14:30:00));\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Типы переменных\n" +
                        "int x = 10;\n" +
                        "float y = 20.5;\n" +
                        "string name = \"test\";\n" +
                        "bool flag = false;\n" +
                        "money price = 99.99m;\n" +
                        "date today = 2024-01-15;\n" +
                        "time now = 14:30:00;\n" +
                        "\n" +
                        "println(\"typeof(x): \" + typeof(x));\n" +
                        "println(\"typeof(y): \" + typeof(y));\n" +
                        "println(\"typeof(name): \" + typeof(name));\n" +
                        "println(\"typeof(flag): \" + typeof(flag));\n" +
                        "println(\"typeof(price): \" + typeof(price));\n" +
                        "println(\"typeof(today): \" + typeof(today));\n" +
                        "println(\"typeof(now): \" + typeof(now));\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Необъявленная переменная\n" +
                        "println(\"typeof(undeclared): \" + typeof(undeclared));\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Выражения\n" +
                        "println(\"typeof(x + y): \" + typeof(x + y));\n" +
                        "println(\"typeof(\\\"text\\\" + 123): \" + typeof(\"text\" + 123));\n" +
                        "println(\"typeof(x > y): \" + typeof(x > y));\n" +
                        "\n" +
                        "println(\"\\nTypeof test completed!\");";

                System.out.println("Executing typeof test script...");
                System.out.println("==========================================");

                engine.eval(testScript);

                System.out.println("==========================================");
                System.out.println("Typeof test output:");
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