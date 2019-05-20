package com.example.cannon.game;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Superclass for all entities that need to be updated in the world. */
public abstract class GameEntity {
    @NonNull
    private final World world;

    /** Create an entity for the given world */
    protected GameEntity(@NonNull World world) {
        this.world = world;
    }

    /** Get the world for this entity. For use in ancestors */
    @NonNull
    protected World getWorld() {
        return world;
    }

    /** Update this entity */
    public abstract void update();

    /** Remove self from the world. */
    protected void disappear() {
        world.unregister(this);
    }

    /** Spawn another entity in the world */
    protected void spawn(GameEntity entity) {
        world.register(entity);
    }
}
