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

    private static int randomJitter() {
        return random.nextInt(3) - 1; // -1, 0, or 1 pixel jitter
    }
}
