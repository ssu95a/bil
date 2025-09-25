package ru.inversion.bil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.io.StringWriter;

public class ComplexTestScript {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("bil");

        if (engine != null) {
            try {
                StringWriter output = new StringWriter();
                engine.getContext().setWriter(output);

                String complexScript = "// ===== ТЕСТИРОВАНИЕ ВСЕХ ЧИСЛЕННЫХ ТИПОВ BIL =====\n" +
                        "\n" +
                        "print(\"Testing all numeric types in Bil:\");\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 1. ЦЕЛЫЕ ЧИСЛА (INT)\n" +
                        "int intVar1 = 42;\n" +
                        "int intVar2 = -100;\n" +
                        "int intVar3 = 0;\n" +
                        "int intVar4 = 999999;\n" +
                        "\n" +
                        "print(\"=== INTEGER TESTS ===\");\n" +
                        "print(\"intVar1 = \" + intVar1 + \", type: \" + typeof(intVar1));\n" +
                        "print(\"intVar2 = \" + intVar2 + \", type: \" + typeof(intVar2)); \n" +
                        "print(\"intVar3 = \" + intVar3 + \", type: \" + typeof(intVar3));\n" +
                        "print(\"intVar4 = \" + intVar4 + \", type: \" + typeof(intVar4));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 2. ДРОБНЫЕ ЧИСЛА (FLOAT)\n" +
                        "float floatVar1 = 3.14159;\n" +
                        "float floatVar2 = -2.71828;\n" +
                        "float floatVar3 = 0.0;\n" +
                        "float floatVar4 = 123.456789;\n" +
                        "\n" +
                        "print(\"=== FLOAT TESTS ===\");\n" +
                        "print(\"floatVar1 = \" + floatVar1 + \", type: \" + typeof(floatVar1));\n" +
                        "print(\"floatVar2 = \" + floatVar2 + \", type: \" + typeof(floatVar2));\n" +
                        "print(\"floatVar3 = \" + floatVar3 + \", type: \" + typeof(floatVar3));\n" +
                        "print(\"floatVar4 = \" + floatVar4 + \", type: \" + typeof(floatVar4));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 3. ДЕНЬГИ (MONEY)\n" +
                        "money moneyVar1 = 110.32;\n" +
                        "money moneyVar2 = 0.01;\n" +
                        "money moneyVar3 = 999999.999;\n" +
                        "money moneyVar4 = -50.25;\n" +
                        "\n" +
                        "print(\"=== MONEY TESTS ===\");\n" +
                        "print(\"moneyVar1 = \" + moneyVar1 + \", type: \" + typeof(moneyVar1));\n" +
                        "print(\"moneyVar2 = \" + moneyVar2 + \", type: \" + typeof(moneyVar2));\n" +
                        "print(\"moneyVar3 = \" + moneyVar3 + \", type: \" + typeof(moneyVar3));\n" +
                        "print(\"moneyVar4 = \" + moneyVar4 + \", type: \" + typeof(moneyVar4));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 4. АВТОМАТИЧЕСКОЕ ПРЕОБРАЗОВАНИЕ ТИПОВ\n" +
                        "print(\"=== AUTOMATIC TYPE CONVERSION ===\");\n" +
                        "money moneyFromInt = 100;\n" +
                        "money moneyFromFloat = 50.75;\n" +
                        "int intFromMoney = 200;\n" +
                        "float floatFromMoney = 75.5;\n" +
                        "\n" +
                        "print(\"moneyFromInt = \" + moneyFromInt + \", type: \" + typeof(moneyFromInt));\n" +
                        "print(\"moneyFromFloat = \" + moneyFromFloat + \", type: \" + typeof(moneyFromFloat));\n" +
                        "print(\"intFromMoney = \" + intFromMoney + \", type: \" + typeof(intFromMoney));\n" +
                        "print(\"floatFromMoney = \" + floatFromMoney + \", type: \" + typeof(floatFromMoney));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 5. АРИФМЕТИЧЕСКИЕ ОПЕРАЦИИ\n" +
                        "print(\"=== ARITHMETIC OPERATIONS ===\");\n" +
                        "int intSum = intVar1 + intVar4;\n" +
                        "float floatSum = floatVar1 + floatVar4;\n" +
                        "money moneySum = moneyVar1 + moneyVar3;\n" +
                        "\n" +
                        "print(\"intSum = \" + intSum + \", type: \" + typeof(intSum));\n" +
                        "print(\"floatSum = \" + floatSum + \", type: \" + typeof(floatSum));\n" +
                        "print(\"moneySum = \" + moneySum + \", type: \" + typeof(moneySum));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 6. СМЕШАННЫЕ ВЫЧИСЛЕНИЯ\n" +
                        "print(\"=== MIXED CALCULATIONS ===\");\n" +
                        "money mixed1 = intVar1 + moneyVar1;\n" +
                        "float mixed2 = floatVar1 + intVar1;\n" +
                        "money mixed3 = moneyVar1 * 2;\n" +
                        "int mixed4 = 100 + 50;\n" +
                        "\n" +
                        "print(\"mixed1 = \" + mixed1 + \", type: \" + typeof(mixed1));\n" +
                        "print(\"mixed2 = \" + mixed2 + \", type: \" + typeof(mixed2));\n" +
                        "print(\"mixed3 = \" + mixed3 + \", type: \" + typeof(mixed3));\n" +
                        "print(\"mixed4 = \" + mixed4 + \", type: \" + typeof(mixed4));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 7. ВЫРАЖЕНИЯ С РАЗНЫМИ ТИПАМИ\n" +
                        "print(\"=== EXPRESSIONS WITH DIFFERENT TYPES ===\");\n" +
                        "money complex1 = (100 + 50) * 2;\n" +
                        "float complex2 = 3.14 * 10;\n" +
                        "int complex3 = 100 / 3;\n" +
                        "money complex4 = 123.456789;\n" +
                        "\n" +
                        "print(\"complex1 = \" + complex1 + \", type: \" + typeof(complex1));\n" +
                        "print(\"complex2 = \" + complex2 + \", type: \" + typeof(complex2));\n" +
                        "print(\"complex3 = \" + complex3 + \", type: \" + typeof(complex3));\n" +
                        "print(\"complex4 = \" + complex4 + \", type: \" + typeof(complex4));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 8. ФУНКЦИЯ ОКРУГЛЕНИЯ\n" +
                        "print(\"=== ROUND FUNCTION TESTS ===\");\n" +
                        "money preciseMoney = 123.456789;\n" +
                        "float preciseFloat = 7.891234;\n" +
                        "\n" +
                        "print(\"round(123.456789) = \" + round(preciseMoney) + \", type: \" + typeof(round(preciseMoney)));\n" +
                        "print(\"round(123.456789, 2) = \" + round(preciseMoney, 2) + \", type: \" + typeof(round(preciseMoney, 2)));\n" +
                        "print(\"round(7.891234) = \" + round(preciseFloat) + \", type: \" + typeof(round(preciseFloat)));\n" +
                        "print(\"round(7.891234, 3) = \" + round(preciseFloat, 3) + \", type: \" + typeof(round(preciseFloat, 3)));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "// 9. ПРОВЕРКА ГРАНИЧНЫХ ЗНАЧЕНИЙ (ИСПРАВЛЕНО)\n" +
                        "print(\"=== BOUNDARY VALUES ===\");\n" +
                        "int maxInt = 2147483647;\n" +
                        "int minInt = -2147483647;\n" +
                        "money largeMoney = 999999.999999;\n" +
                        "money smallMoney = 0.000001;\n" +
                        "\n" +
                        "print(\"maxInt = \" + maxInt + \", type: \" + typeof(maxInt));\n" +
                        "print(\"minInt = \" + minInt + \", type: \" + typeof(minInt));\n" +
                        "print(\"largeMoney = \" + largeMoney + \", type: \" + typeof(largeMoney));\n" +
                        "print(\"smallMoney = \" + smallMoney + \", type: \" + typeof(smallMoney));\n" +
                        "print(\"\");\n" +
                        "\n" +
                        "print(\"=== ALL NUMERIC TESTS COMPLETED ===\");";

                System.out.print("Executing complex test script...");
                System.out.print("==========================================");

                engine.eval(complexScript);

                System.out.print("==========================================");
                System.out.print("Script output:");
                System.out.print(output.toString());
                System.out.print("==========================================");
                System.out.print("Complex test completed!");

            } catch (ScriptException e) {
                System.err.print("Script error: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                System.err.print("Unexpected error: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.print("Bil engine not found");
        }
    }
}