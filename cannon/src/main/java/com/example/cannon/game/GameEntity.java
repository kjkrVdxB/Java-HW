package com.example.cannon.game;

import org.checkerframework.checker.nullness.qual.NonNull;

/** Superclass for all entities that need to be updated in the world. */
public abstract class GameEntity {
    private World world;

    protected GameEntity() {
    }

    /** Get the world for this entity. For use in ancestors. */
    @NonNull
    final protected World getWorld() {
        return world;
    }

    /** Set the world for this entity. For use in World. */
    final void setWorld(@NonNull World world) {
        this.world = world;
    }

    /** Update this entity */
    public abstract void update();

    /** Remove self from the world. */
    final protected void disappear() {
        world.unregister(this);
    }

    /** TODO: use this to order updates. */
    protected int updatePriority() { return 0; }

    /** Spawn another entity in the world */
    final protected void spawn(GameEntity entity) {
        world.register(entity);
    }
}
