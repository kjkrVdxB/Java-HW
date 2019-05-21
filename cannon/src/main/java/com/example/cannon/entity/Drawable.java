package com.example.cannon.entity;

import javafx.scene.canvas.GraphicsContext;
import org.checkerframework.checker.nullness.qual.NonNull;

/** A class for entities that should be drawn on the canvas. */
public interface Drawable {
    /** Draw the entity on the given canvas. Should save and restore the context state. */
    void draw(@NonNull GraphicsContext graphicsContext);

    /** Relative distance from the screen when drawing. Lower means below others/farther. */
    int getDrawingLayer();
}
