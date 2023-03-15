package projekt.delivery.routing;

import org.jetbrains.annotations.Nullable;
import projekt.base.Location;

import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.tudalgo.algoutils.student.Student.crash;

class NodeImpl implements Region.Node {

    protected final Set<Location> connections;
    protected final Region region;
    protected final String name;
    protected final Location location;

    /**
     * Creates a new {@link NodeImpl} instance.
     * @param region The {@link Region} this {@link NodeImpl} belongs to.
     * @param name The name of this {@link NodeImpl}.
     * @param location The {@link Location} of this {@link EdgeImpl}.
     * @param connections All {@link Location}s this {@link NeighborhoodImpl} has an {@link Region.Edge} to.
     */
    NodeImpl(
        Region region,
        String name,
        Location location,
        Set<Location> connections
    ) {
        this.region = region;
        this.name = name;
        this.location = location;
        this.connections = connections;
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
    public Location getLocation() {
        return location;
    }

    public Set<Location> getConnections() {
        return connections;
    }

    @Override
    public @Nullable Region.Edge getEdge(Region.Node other) {
        return other.getRegion().getEdge(this.location, other.getLocation());
    }

    @Override
    public Set<Region.Node> getAdjacentNodes() {
        return connections.stream().map(x -> this.getRegion().getNode(x)).collect(Collectors.toSet());
    }

    @Override
    public Set<Region.Edge> getAdjacentEdges() {
        return connections.stream().map(x -> this.getRegion().getEdge(this.location, x)).collect(Collectors.toSet());
    }

    @Override
    public int compareTo(Region.Node o) {
        return location.compareTo(o.getLocation());
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof NodeImpl castO) {
            return (o == this || (Objects.equals(this.name, castO.name) && Objects.equals(this.location, castO.location) && Objects.equals(this.connections, castO.connections)));
        }
        else {
            return false;
        }  
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, connections);
    }

    @Override
    public String toString() {
        return "NodeImpl(name='%s', location='%s', connections='%s')".formatted(name.toString(), location.toString(), connections.toString());
    }
}
