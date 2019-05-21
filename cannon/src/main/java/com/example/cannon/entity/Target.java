package com.example.cannon.entity;

import com.example.cannon.game.Game;
import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import static com.example.cannon.Utils.drawCircle;

/** An entity representing a target. */
public class Target extends GameEntity implements Drawable, Damageable {
    private final static double RADIUS = 15;
    private final static double MAX_HEALTH = 100;
    private final static double HEALTH_BAR_WIDTH = 40;
    private final static double HEALTH_BAR_HEIGHT = 7;
    @NonNull
    private final Point2D position;
    private double currentHealth = MAX_HEALTH;

    public Target(@NonNull Point2D position) {
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
        drawHealthBar(graphicsContext);
    }

    private void drawHealthBar(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        var center = position.add(new Point2D(0, -RADIUS - 10));
        var upperLeft = center.add(new Point2D(-HEALTH_BAR_WIDTH / 2, -HEALTH_BAR_HEIGHT / 2));
        graphicsContext.setFill(Color.BLACK);
        graphicsContext.fillRect(upperLeft.getX(), upperLeft.getY(), HEALTH_BAR_WIDTH, HEALTH_BAR_HEIGHT);
        graphicsContext.setFill(Color.RED);
        graphicsContext.fillRect(upperLeft.getX(), upperLeft.getY(), HEALTH_BAR_WIDTH * currentHealth / MAX_HEALTH, HEALTH_BAR_HEIGHT);
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
    public int drawingLayer() {
        return 0;
    }

    @Override
    public void update() {
        // nothing to do here yet
    }

    @Override
    public void dealDamage(double damage) {
        currentHealth -= damage;
        if (currentHealth < 0) {
            currentHealth = 0;
            getWorld().finishGame(Game.FinishReason.TARGET_DESTROYED);
        }
    }
}
