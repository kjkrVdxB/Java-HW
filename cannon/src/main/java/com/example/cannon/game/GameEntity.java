package com.example.cannon.game;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Superclass for all entities that need to be updated in the world. */
public abstract class GameEntity {
    private World world;

    protected GameEntity() {
    }

    /** Get the world for this entity. For use in ancestors. */
    @NonNull
    protected World getWorld() {
        return world;
    }

    /** Set the world for this entity. For use in World. */
    void setWorld(@NonNull World world) {
        this.world = world;
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
