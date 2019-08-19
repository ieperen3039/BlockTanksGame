package NG.DataStructures.Generic;

import org.joml.Vector2fc;
import org.joml.Vector2i;

/**
 * @author Geert van Ieperen created on 4-2-2019.
 */
public enum Direction {
    POSITIVE_X, POSITIVE_Y, NEGATIVE_X, NEGATIVE_Y, NONE;

    public Direction inverse() {
        switch (this) {
            case POSITIVE_X:
                return NEGATIVE_X;
            case POSITIVE_Y:
                return NEGATIVE_Y;
            case NEGATIVE_X:
                return POSITIVE_X;
            case NEGATIVE_Y:
                return POSITIVE_Y;
        }
        return NONE;
    }

    public static Direction get(Vector2fc direction) {
        return get(direction.x(), direction.y());
    }

    public static Direction get(float x, float y) {
        if (x == 0 && y == 0) {
            return NONE;

        } else if (Math.abs(x) > Math.abs(y)) {
            if (x > 0) {
                return POSITIVE_X;
            } else {
                return NEGATIVE_X;
            }

        } else {
            if (y > 0) {
                return POSITIVE_Y;
            } else {
                return NEGATIVE_Y;
            }
        }
    }

    public Vector2i toVector() {
        switch (this) {
            case POSITIVE_X:
                return new Vector2i(1, 0);
            case POSITIVE_Y:
                return new Vector2i(0, 1);
            case NEGATIVE_X:
                return new Vector2i(-1, 0);
            case NEGATIVE_Y:
                return new Vector2i(0, -1);
        }

        return new Vector2i(0, 0);
    }
}
