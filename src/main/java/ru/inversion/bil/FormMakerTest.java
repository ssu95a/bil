package ru.inversion.bil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class FormMakerTest {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("bil");

        if (engine != null) {
            try {
                StringWriter output = new StringWriter();
                engine.getContext().setWriter(output);

                String testScript = "// Тестирование FormMaker методов\n" +
                        "println(\"Testing FormMaker methods:\");\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Метод print\n" +
                        "_FM.print(\"This is \");\n" +
                        "_FM.print(\"FormMaker \");\n" +
                        "_FM.print(\"print method!\");\n" +
                        "println(\"\");\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Метод println\n" +
                        "_FM.println(\"This is FormMaker println method!\");\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Метод line\n" +
                        "_FM.line();\n" +
                        "println(\"Above is a line from FormMaker\");\n" +
                        "_FM.line();\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Метод report с разными аргументами\n" +
                        "_FM.report(\"Simple report\");\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "_FM.report(\"First message\", \"Second message\", \"Third message\");\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "_FM.report(\"Number: \" + 42, \"Boolean: \" + true, \"Null: \" + null);\n" +
                        "println(\"\");\n" +
                        "\n" +
                        "// Комбинированное использование\n" +
                        "_FM.print(\"Starting \");\n" +
                        "_FM.println(\"report generation...\");\n" +
                        "_FM.line();\n" +
                        "_FM.report(\"Report item 1\", \"Report item 2\", \"Report item 3\");\n" +
                        "_FM.line();\n" +
                        "_FM.println(\"Report generation completed!\");\n" +
                        "\n" +
                        "println(\"\\nFormMaker test completed!\");";

                System.out.println("Executing FormMaker test script...");
                System.out.println("==========================================");

                engine.eval(testScript);

                System.out.println("==========================================");
                System.out.println("FormMaker test output:");
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