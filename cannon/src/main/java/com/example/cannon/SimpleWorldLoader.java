package com.example.cannon;

import com.example.cannon.entity.Cannon;
import com.example.cannon.entity.Target;
import com.example.cannon.entity.Terrain;
import com.example.cannon.game.World;
import javafx.geometry.Point2D;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

public class SimpleWorldLoader implements World.WorldProvider {
    private Terrain terrain;
    private Cannon cannon;
    private Target target;

    /**
     * Loads a file with a Cannon game instance in the following text format:
     *
     * 'int: number of vertices in terrain'
     * 'double: vertex 1 X coordinate' 'double: vertex 1 Y coordinate'
     * 'double: vertex 2 X coordinate' ...
     * ...
     * 'double: cannon starting angle' 'double: cannon X coordinate' 'double: cannon Y coordinate'
     * 'double: target X coordinate' 'double: target Y coordinate'
     *
     * @throws FileNotFoundException if the file could not be found
     */
    public SimpleWorldLoader(@NonNull String filename) throws FileNotFoundException {
        try (var scanner = new Scanner(new File(filename))){
            var vertices = new ArrayList<Point2D>();
            int verticesNumber = scanner.nextInt();
            for (int i = 0; i < verticesNumber; ++i) {
                vertices.add(new Point2D(scanner.nextDouble(), scanner.nextDouble()));
            }
            terrain = new Terrain(vertices);
            cannon = new Cannon(scanner.nextDouble(), new Point2D(scanner.nextDouble(), scanner.nextDouble()));
            target = new Target(new Point2D(scanner.nextDouble(), scanner.nextDouble()));
        }
    }

    public Terrain getTerrain() {
        return terrain;
    }

    public Cannon getCannon() {
        return cannon;
    }

    public Target getTarget() {
        return target;
    }
}
