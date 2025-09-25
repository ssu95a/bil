package ru.inversion.bil;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

public class Runner {
    public static void main(String[] args) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("bil");

        if (engine != null) {
            try {
                // Простейший скрипт для начала
                String simpleScript = "int x = 10; int y = 9; print(x+y);";

                System.out.println("Executing simple script...");
                engine.eval(simpleScript);
                System.out.println("Simple script executed successfully!");

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