package com.example.cannon.entity;

import com.example.cannon.game.GameEntity;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.checkerframework.checker.nullness.qual.NonNull;

import java.util.ArrayList;
import java.util.List;

import static com.example.cannon.CannonApplication.HEIGHT;
import static com.example.cannon.CannonApplication.WIDTH;
import static java.lang.Math.abs;
import static java.lang.Math.sqrt;

/** A class representing ground in the world. */
public class Terrain extends GameEntity implements Drawable {
    private static final double SUBDIVIDE_EPSILON = 1;
    private static final int TERRAIN_DRAWING_LAYER = -1;
    /** Vertices of the ground path. 'x' coordinates should increase. */
    @NonNull
    private final List<Point2D> vertices;

    public Terrain(@NonNull List<Point2D> vertices) {
        this.vertices = new ArrayList<>(vertices);
        for (int i = 0; i * SUBDIVIDE_EPSILON < WIDTH; ++i) {
            trySubdivide(i * SUBDIVIDE_EPSILON);
        }
    }

    @NonNull
    public List<Point2D> getVertices() {
        return vertices;
    }

    /** Try to create intermediary point, but only if there is no vertexes nearby. */
    private void trySubdivide(double x) {
        int l = vertexRightBefore(x);
        if (x - vertices.get(l).getX() < SUBDIVIDE_EPSILON || vertices.get(l + 1).getX() - x < SUBDIVIDE_EPSILON) {
            return;
        }
        vertices.add(l + 1, new Point2D(x, getHeight(vertices.get(l), vertices.get(l + 1), x)));
    }

    /** Returns the index of the closest (according to X coordinate) vertex in terrain to the left of 'x'. */
    private int vertexRightBefore(double x) {
        int l = 0, r = vertices.size();
        while (r - l > 1) {
            int m = (l + r) / 2;
            if (vertices.get(m).getX() < x) {
                l = m;
            } else {
                r = m;
            }
        }
        return l;
    }

    /** Returns the index of the closest (according to X coordinate) vertex in terrain to the right of 'x'. */
    private int vertexRightAfter(double x) {
        return vertexRightBefore(x) + 1;
    }

    /** Returns the height at the given 'x' coordinate. */
    public double getHeight(double x) {
        int l = vertexRightBefore(x);
        return getHeight(vertices.get(l), vertices.get(l + 1), x);
    }

    /** Get height on the segment between 'a' and 'b'. */
    private double getHeight(Point2D a, Point2D b, double x) {
        assert a.getX() <= x && b.getX() >= x;
        return (x - a.getX()) / (b.getX() - a.getX()) * (b.getY() - a.getY()) + a.getY();
    }

    // Math utils specific for terrain. */

    private double slope(Point2D a, Point2D b) {
        return (b.getY() - a.getY()) / (b.getX() - a.getX());
    }

    private double sinBySlope(double slope) {
        return slope / sqrt(1 + slope * slope);
    }

    private double cosBySlope(double slope) {
        return 1 / sqrt(1 + slope * slope);
    }

    /** Move from start to the right (if 'distance' is positive) for 'distance'. */
    private Point2D moveOnLine(Point2D a, Point2D b, Point2D start, double distance) {
        double slope = slope(a, b);
        return new Point2D(start.getX() + distance * cosBySlope(slope), start.getY() + distance * sinBySlope(slope));
    }

    /** Checks that the 'point' is under the terrain. */
    public boolean isPointUnder(@NonNull Point2D point) {
        if (point.getX() < vertices.get(0).getX() || point.getX() >= vertices.get(vertices.size() - 1).getX()) {
            return false;
        }
        var height = getHeight(point.getX());
        return height < point.getY();
    }

    /** Move the point along the terrain for distance (to the right if 'distance' is positive) */
    public Point2D move(Point2D start, double distance) {
        int l = vertexRightAfter(start.getX());
        distance -= vertices.get(l).distance(start);
        boolean right = distance > 0;
        distance = abs(distance);
        while ((right ? l + 1 < vertices.size() : l >= 1) && distance > 0) {
            int next = right ? l + 1 : l - 1;
            distance -= vertices.get(next).distance(vertices.get(l));
            l = next;
        }
        if (distance < 0) {
            return right ? moveOnLine(vertices.get(l - 1), vertices.get(l), vertices.get(l), distance)
                         : moveOnLine(vertices.get(l), vertices.get(l + 1), vertices.get(l), -distance);
        } else {
            return vertices.get(l);
        }
    }

    @Override
    public void draw(@NonNull GraphicsContext graphicsContext) {
        graphicsContext.save();
        graphicsContext.setFill(Color.rgb(60, 40, 10));
        graphicsContext.beginPath();
        graphicsContext.moveTo(vertices.get(0).getX(), vertices.get(0).getY());
        for (int i = 1; i < vertices.size(); ++i) {
            graphicsContext.lineTo(vertices.get(i).getX(), vertices.get(i).getY());
        }
        graphicsContext.lineTo(WIDTH, HEIGHT);
        graphicsContext.lineTo(0, HEIGHT);
        graphicsContext.closePath();
        graphicsContext.fill();
        graphicsContext.restore();
    }

    @Override
    public int getDrawingLayer() {
        return TERRAIN_DRAWING_LAYER;
    }

    @Override
    public void update() {
        // nothing to do here yet
    }
}
