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
    private final Terrain terrain = new Terrain(this, List.of(new Point2D(0, 400), new Point2D(300, 300), new Point2D(500, 500), new Point2D(WIDTH, 400)));
    @NonNull
    private final Cannon cannon = new Cannon(this, Math.PI / 4, terrain.getVertices().get(0));
    private long currentTime;
    private long previousTime;
    @NonNull
    private final Target target = new Target(this, terrain.getVertices().get(2));
    @NonNull
    private final List<@NonNull GameEntity> entities = new ArrayList<>();
    @NonNull
    private final List<@NonNull GameEntity> newEntities = new ArrayList<>();
    @NonNull
    private final List<@NonNull GameEntity> entitiesToDelete = new ArrayList<>();
    @NonNull
    private final Game game;

    /** Create a world for the given game. */
    public World(@NonNull Game game) {
        this.game = game;
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
        graphicsContext.setFill(Color.LAVENDER);
        graphicsContext.fillRect(0, 0, WIDTH, HEIGHT);
        graphicsContext.restore();

        // Draw the entities that are 'Drawable'
        entities.stream()
                .filter(e -> e instanceof Drawable)
                .map(e -> (Drawable)e)
                .sorted(Comparator.comparing(Drawable::layer))
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
    public void finishGame() {
        game.finish();
    }
}
