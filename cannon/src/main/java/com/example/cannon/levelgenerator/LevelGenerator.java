package com.example.cannon.levelgenerator;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static java.lang.Math.sin;

public class LevelGenerator {
    public static void main(String[] args) throws IOException {
        var printer = new PrintWriter("test.layout", StandardCharsets.UTF_8);
        int n = 401;
        double width = 800;
        printer.println(n);
        double cannonX = 0, cannonY = 0;
        double targetX = 0, targetY = 0;
        for (int i = 0; i < n; ++i) {
            double x = width / (n - 1) * i;
            double y = f(x);
            printer.print(x);
            printer.print(" ");
            printer.println(y);
            if (i == 50) {
                cannonX = x;
                cannonY = y;
            }
            if (i == 350) {
                targetX = x;
                targetY = y;
            }
        }
        printer.print(-Math.PI / 4);
        printer.print(" ");
        printer.print(cannonX);
        printer.print(" ");
        printer.println(cannonY);
        printer.print(targetX);
        printer.print(" ");
        printer.println(targetY);
        printer.close();
    }

    private static double f(double x) {
        return sin(x / 20) * (50 + x * (800 - x) / 1600) + 400;
    }
}
