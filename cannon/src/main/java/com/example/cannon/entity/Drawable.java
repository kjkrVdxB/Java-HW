package com.example.cannon.entity;

import javafx.scene.canvas.GraphicsContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/** A class for entities that should be drawn on the canvas. */
public interface Drawable {
    /**
     * Draw the entity using the given graphics context. Upon completion the graphicsContext state should be the same as
     * before the call. It's methods save()/restore() can be used for that, but check the documentation for
     * GraphicsContext for subtleties.
     */
    void draw(@NonNull GraphicsContext graphicsContext);

    /** Relative distance from the screen when drawing. Lower means below others/farther. */
    int getDrawingLayer();
}
