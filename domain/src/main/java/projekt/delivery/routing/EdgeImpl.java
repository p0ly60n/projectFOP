package projekt.delivery.routing;

import org.jetbrains.annotations.NotNull;
import projekt.base.Location;

import static org.tudalgo.algoutils.student.Student.crash;

import java.util.Comparator;
import java.util.Objects;

/**
 * Represents a weighted edge in a graph.
 */
@SuppressWarnings("ClassCanBeRecord")
class EdgeImpl implements Region.Edge {

    private final Region region;
    private final String name;
    private final Location locationA;
    private final Location locationB;
    private final long duration;

    /**
     * Creates a new {@link EdgeImpl} instance.
     * @param region The {@link Region} this {@link EdgeImpl} belongs to.
     * @param name The name of this {@link EdgeImpl}.
     * @param locationA The start of this {@link EdgeImpl}.
     * @param locationB The end of this {@link EdgeImpl}.
     * @param duration The length of this {@link EdgeImpl}.
     */
    EdgeImpl(
        Region region,
        String name,
        Location locationA,
        Location locationB,
        long duration
    ) {
        this.region = region;
        this.name = name;
        // locations must be in ascending order
        if (locationA.compareTo(locationB) > 0) {
            throw new IllegalArgumentException(String.format("locationA %s must be <= locationB %s", locationA, locationB));
        }
        this.locationA = locationA;
        this.locationB = locationB;
        this.duration = duration;
    }

    /**
     * Returns the start of this {@link EdgeImpl}.
     * @return The start of this {@link EdgeImpl}.
     */
    public Location getLocationA() {
        return locationA;
    }

    /**
     * Returns the end of this {@link EdgeImpl}.
     * @return The end of this {@link EdgeImpl}.
     */
    public Location getLocationB() {
        return locationB;
    }

    @Override
    public Region getRegion() {
        return region;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public Region.Node getNodeA() {
        Region.Node nodeA = region.getNode(locationA);
        return (nodeA != null) ? nodeA : null;
    }

    @Override
    public Region.Node getNodeB() {
        Region.Node nodeB = region.getNode(locationB);
        return (nodeB != null) ? nodeB : null;
    }

    @Override
    public int compareTo(Region.@NotNull Edge o) {
        // how did I even do that? this took so much time to understand xD
        Comparator<Region.Edge> compNodeA = Comparator.comparing(Region.Edge::getNodeA);
        Comparator<Region.Edge> compNodeB = Comparator.comparing(Region.Edge::getNodeB);
        return compNodeA.thenComparing(compNodeB).compare(this, o);
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof EdgeImpl castO) {
            return (this == o || (Objects.equals(this.name, castO.name) && Objects.equals(this.locationA, castO.locationA) 
                    && Objects.equals(this.locationB, castO.locationB) && Objects.equals(this.duration, castO.duration)));
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, locationA, locationB, duration);
    }

    @Override
    public String toString() {
        return "EdgeImpl(name='%s', locationA='%s', locationB='%s', duration='%d')".formatted(name.toString(), locationA.toString(), locationB.toString(), duration);
    }
}
