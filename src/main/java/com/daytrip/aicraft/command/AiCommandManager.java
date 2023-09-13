package com.daytrip.aicraft.command;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

public class AiCommandManager {
    private static final HashMap<String, Method> types = new HashMap<>();
    public static void registerClass(Class<?> cls) {
        for (Method method : cls.getDeclaredMethods()) {
            if (method.isAnnotationPresent(AiCommand.class) && Modifier.isStatic(method.getModifiers())) {
                types.put(method.getAnnotation(AiCommand.class).name(), method);
            }
        }
    }

    public static void execute(String input) {
        String[] parts = input.strip().split(Pattern.quote(" "), 2);
        String command = parts[0].substring(1);
        String rest = parts[1];

        List<Object> arguments = new ArrayList<>();

        for (String arg : rest.split(Pattern.quote(","))) {
            arg = arg.strip();
            System.out.println("Parsing arg: " + arg);
            if (arg.startsWith("\"") && arg.endsWith("\"")) {
                arguments.add(arg.substring(1, arg.length() - 1));
                continue;
            }

            arg = arg.toLowerCase();
            if (arg.equals("true") || arg.equals("false")) {
                arguments.add(Boolean.parseBoolean(arg));
                continue;
            }
            /*if (arg.contains(".")) {*/
            /*    arguments.add(Float.parseFloat(arg));*/
            /*    continue;*/
            /*}*/
            arguments.add(Integer.parseInt(arg));
        }

        Method method = types.get(command);
        if (method == null) {
            throw new IllegalArgumentException("Command doesn't exist! (" + command + ")");
        }

        try {
            method.invoke(null, arguments.toArray());
        } catch (InvocationTargetException | IllegalAccessException e) {
            throw new IllegalArgumentException("Invalid command format!");
        }
    }
}
