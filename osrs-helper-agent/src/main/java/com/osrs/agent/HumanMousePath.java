package com.osrs.agent;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class HumanMousePath {
    private static final Random random = new Random();

    /**
     * Generate a human-like path between two points using simple jitter and linear interpolation.
     * Replace with Bezier or spline for more realism if needed.
     */
    public static List<Point> generate(Point start, Point end, int steps) {
        List<Point> path = new ArrayList<>();
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            int x = (int) (start.x + t * (end.x - start.x) + randomJitter());
            int y = (int) (start.y + t * (end.y - start.y) + randomJitter());
            path.add(new Point(x, y));
        }
        path.add(end);
        return path;
    }

    /**
     * Generate a human-like path between two points using a quadratic Bezier curve with jitter.
     * This is more advanced than linear interpolation and produces smoother, more human-like movement.
     */
    public static List<Point> generateBezier(Point start, Point end, int steps) {
        List<Point> path = new ArrayList<>();
        // Control point: somewhere between start and end, offset for curve
        int ctrlX = (start.x + end.x) / 2 + random.nextInt(40) - 20;
        int ctrlY = (start.y + end.y) / 2 + random.nextInt(40) - 20;
        for (int i = 1; i <= steps; i++) {
            double t = i / (double) steps;
            double oneMinusT = 1 - t;
            int x = (int) (
                oneMinusT * oneMinusT * start.x +
                2 * oneMinusT * t * ctrlX +
                t * t * end.x + randomJitter()
            );
            int y = (int) (
                oneMinusT * oneMinusT * start.y +
                2 * oneMinusT * t * ctrlY +
                t * t * end.y + randomJitter()
            );
            path.add(new Point(x, y));
        }
        path.add(end);
        return path;
    }

    private static int randomJitter() {
        return random.nextInt(3) - 1; // -1, 0, or 1 pixel jitter
    }
}
