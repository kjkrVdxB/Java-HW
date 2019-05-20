package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import com.example.cannon.game.World;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.example.cannon.Utils.drawCircle;

/** An entity representing a target. */
public class Target extends GameEntity implements Drawable {
    private final static double RADIUS = 20;
    @NonNull
    private final Point2D position;

    public Target(@NonNull World world, @NonNull Point2D position) {
        super(world);
        this.position = new Point2D(position.getX(), position.getY() - RADIUS);
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.RED);
        drawCircle(graphicsContext, position, RADIUS);
        graphicsContext.setFill(Color.WHITE);
        drawCircle(graphicsContext, position, RADIUS / 3 * 2);
        graphicsContext.setFill(Color.RED);
        drawCircle(graphicsContext, position, RADIUS / 3);
        graphicsContext.restore();
    }

    double getRadius() {
        return RADIUS;
    }

    @NonNull
    Point2D getPosition() {
        return position;
    }

    @Override
    public int layer() {
        return 0;
    }

    @Override
    public void update() {
        // nothing to do here??
    }
}
