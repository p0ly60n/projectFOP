package projekt.base;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.Objects;

import static org.tudalgo.algoutils.student.Student.crash;

/**
 * A tuple for the x- and y-coordinates of a point.
 */
@SuppressWarnings("ClassCanBeRecord")
public final class Location implements Comparable<Location> {

    private final static Comparator<Location> COMPARATOR =
        Comparator.comparing(Location::getX).thenComparing(Location::getY);

    private final int x;
    private final int y;

    /**
     * Instantiates a new {@link Location} object using {@code x} and {@code y} as coordinates.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     */
    public Location(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Returns the x-coordinate of this location.
     *
     * @return the x-coordinate
     */
    public int getX() {
        return x;
    }

    /**
     * Returns the y-coordinate of this location.
     *
     * @return the y-coordinate
     */
    public int getY() {
        return y;
    }

    /**
     * Adds the coordinates of this location and the other location and returns a new
     * {@link Location} object with the resulting coordinates.
     *
     * @param other the other {@link Location} object to get the second set of coordinates from
     * @return a new {@link Location} object with the sum of coordinates from both locations
     */
    public Location add(Location other) {
        return new Location(x + other.x, y + other.y);
    }

    /**
     * Subtracts the coordinates of this location from the other location and returns a new
     * {@link Location} object with the resulting coordinates.
     *
     * @param other the other {@link Location} object to get the second set of coordinates from
     * @return a new {@link Location} object with the difference of coordinates from both locations
     */
    public Location subtract(Location other) {
        return new Location(x - other.x, y - other.y);
    }

    @Override
    public int compareTo(@NotNull Location o) {
        if (o.getX() == this.getX() && o.getY() == this.getY()) {
            return 0;
        }
        else if (this.getX() < o.getX() || (this.getX() == o.getX() && this.getY() < o.getY())) {
            return -1;
        }
        else {
            return 1;
        }
    }

    @Override
    public int hashCode() {
        // calculate the hash of the two numbers by using the cantor pairing function, which is only defined for positive
        // decimal values
        int mappedX = (x < 0) ? (2 * (-x) + 1) :  2 * x; // map negative x to odd positive number number
        int mappedY = (y < 0) ? (2 * (-y) + 1) :  2 * y; // map negative y to odd positive number number
        return ((mappedX + mappedY) * (mappedX + mappedY + 1)) / 2 + mappedY;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Location castLocation) {
            return (castLocation.getX() == this.getX() && castLocation.getY() == this.getY());
        }
        else {
            return false;
        }
    }

    @Override
    public String toString() {
        return "(%d,%d)".formatted(this.getX(), this.getY());
    }
}
