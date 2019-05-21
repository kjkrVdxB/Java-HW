package com.example.cannon;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import org.checkerframework.checker.nullness.qual.NonNull;

import static java.lang.Math.*;

public class Utils {
    public static void drawCircle(@NonNull GraphicsContext graphicsContext, @NonNull Point2D position, double radius) {
         graphicsContext.fillOval(position.getX() - radius, position.getY() - radius, radius * 2, radius * 2);
    }

    @NonNull
    public static Point2D vectorByAngle(double angle) {
        return new Point2D(cos(angle), sin(angle));
    }

    public static double angleRadians(@NonNull Point2D point) {
        return atan2(point.getY(), point.getX());
    }

    @NonNull
    public static Point2D rotate(@NonNull Point2D point, double angle) {
        return vectorByAngle(angleRadians(point) + angle);
    }

    public static double getTimeElapsedSeconds(long start, long end) {
        return secondsFromNano(end - start);
    }

    public static double secondsFromNano(long nano) {
        return nano / 1000000000.0;
    }

    public static long nanoFromSeconds(double seconds) {
        return (long)(seconds * 1000000000);
    }
}
