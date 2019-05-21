package com.example.cannon.game;

import com.example.cannon.entity.Cannon;
import com.example.cannon.entity.Drawable;
import com.example.cannon.entity.Target;
import com.example.cannon.entity.Terrain;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.example.cannon.CannonApplication.HEIGHT;
import static com.example.cannon.CannonApplication.WIDTH;
import static com.example.cannon.Utils.getTimeElapsedSeconds;

public class World {
    /** Gravity on the X axis is of course zero, but just in case. */
    /** Y 'axis' increases towards the bottom of the screen, so the Y component is positive is positive. */
    @NonNull
    public static final Point2D GRAVITY = new Point2D(0, 500);

    @NonNull
    private Terrain terrain;
    @NonNull
    private Cannon cannon;
    private long currentTime;
    private long previousTime;
    @NonNull
    private Target target;
    @NonNull
    private final List<@NonNull GameEntity> entities = new ArrayList<>();
    @NonNull
    private final List<@NonNull GameEntity> newEntities = new ArrayList<>();
    @NonNull
    private final List<@NonNull GameEntity> entitiesToDelete = new ArrayList<>();
    @NonNull
    private final Game game;

    /** Create a world for the given game. */
    public World(@NonNull Game game, @NonNull WorldProvider provider) {
        this.game = game;
        this.target = provider.getTarget();
        this.cannon = provider.getCannon();
        this.terrain = provider.getTerrain();
        register(target);
        register(cannon);
        register(terrain);
        currentTime = System.nanoTime();
        update(currentTime);
    }
    /**
     * Update the world.
     *
     * @param timeNano time of the update in nanoseconds
     */
    public void update(long timeNano) {
        previousTime = currentTime;
        currentTime = timeNano;
        entityUpdateLoop();
    }

    /** Update entities, deleting the ones that unregistered and updating the newly registered ones too. */
    private void entityUpdateLoop() {
        List<@NonNull GameEntity> entitiesToProcess = new ArrayList<>(entities);
        do {
            entitiesToProcess.addAll(newEntities);
            entities.addAll(newEntities);
            newEntities.clear();
            entities.removeAll(entitiesToDelete);
            entitiesToDelete.clear();
            for (var entity: entitiesToProcess) {
                entity.update();
            }
            entitiesToProcess.clear();
        } while (!newEntities.isEmpty() || !entitiesToDelete.isEmpty());
    }

    /** Draw this world on the canvas. */
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.rgb(157, 227, 250));
        graphicsContext.fillRect(0, 0, WIDTH, HEIGHT);
        graphicsContext.restore();

        // Draw the entities that are 'Drawable'
        entities.stream()
                .filter(e -> e instanceof Drawable)
                .map(e -> (Drawable)e)
                .sorted(Comparator.comparing(Drawable::drawingLayer))
                .forEach(d -> d.draw(graphicsContext));
    }

    /** Get the time of the previous update in nanoseconds. */
    public long getPreviousTime() {
        return previousTime;
    }

    /** Get current time in nanoseconds. */
    public long getCurrentTime() {
        return currentTime;
    }

    /** Get the tam since last update in seconds. */
    public double getLastUpdateTimeElapsedSeconds() {
        return getTimeElapsedSeconds(previousTime, currentTime);
    }

    @NonNull
    public Cannon getCannon() {
        return cannon;
    }

    @NonNull
    public Terrain getTerrain() {
        return terrain;
    }

    @NonNull
    public Target getTarget() {
        return target;
    }

    /** Register the entity for receiving updates. */
    void register(GameEntity entity) {
        entity.setWorld(this);
        newEntities.add(entity);
    }

    /** Unregister the entity from receiving updates. */
    void unregister(GameEntity entity) {
        entitiesToDelete.add(entity);
    }

    /**
     * Finish the game. Objects in the world don't have access to game so that they can't fire key
     * presses for example.
     */
    public void finishGame(Game.@NonNull FinishReason reason) {
        game.finish(reason);
    }

    public interface WorldProvider {
        Terrain getTerrain();
        Cannon getCannon();
        Target getTarget();
    }
}
