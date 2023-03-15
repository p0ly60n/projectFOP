package projekt.delivery.routing;

import org.jetbrains.annotations.Nullable;

import projekt.base.DistanceCalculator;
import projekt.base.EuclideanDistanceCalculator;
import projekt.base.Location;

import java.util.*;

import static org.tudalgo.algoutils.student.Student.crash;

class RegionImpl implements Region {

    private final Map<Location, NodeImpl> nodes = new HashMap<>();
    private final Map<Location, Map<Location, EdgeImpl>> edges = new HashMap<>();
    private final List<EdgeImpl> allEdges = new ArrayList<>();
    private final DistanceCalculator distanceCalculator;

    /**
     * Creates a new, empty {@link RegionImpl} instance using a {@link EuclideanDistanceCalculator}.
     */
    public RegionImpl() {
        this(new EuclideanDistanceCalculator());
    }

    /**
     * Creates a new, empty {@link RegionImpl} instance using the given {@link DistanceCalculator}.
     */
    public RegionImpl(DistanceCalculator distanceCalculator) {
        this.distanceCalculator = distanceCalculator;
    }

    @Override
    public @Nullable Node getNode(Location location) {
        return nodes.get(location);
    }

    @Override
    public @Nullable Edge getEdge(Location locationA, Location locationB) {
        if (locationA.compareTo(locationB) <= 0) {
            return edges.get(locationA).get(locationB);
        }
        else {
            return edges.get(locationB).get(locationA);
        }
    }

    @Override
    public Collection<Node> getNodes() {
       return Collections.unmodifiableCollection(nodes.values());
    }

    @Override
    public Collection<Edge> getEdges() {
        return Collections.unmodifiableCollection(allEdges);
    }

    @Override
    public DistanceCalculator getDistanceCalculator() {
        return distanceCalculator;
    }

    /**
     * Adds the given {@link NodeImpl} to this {@link RegionImpl}.
     * @param node the {@link NodeImpl} to add.
     */
    void putNode(NodeImpl node) {
        //TODO: reihenefolge geändert
        if (!this.equals(node.getRegion())) {
            throw new IllegalArgumentException("Node %s has incorrect region".formatted(node.toString()));
        }
        else {
            nodes.put(node.getLocation(), node);
        }
    }

    /**
     * Adds the given {@link EdgeImpl} to this {@link RegionImpl}.
     * @param edge the {@link EdgeImpl} to add.
     */
    void putEdge(EdgeImpl edge) {
        if (edge.getNodeA() == null) {
            throw new IllegalArgumentException("NodeA %s is not part of the region".formatted(edge.getLocationA().toString()));
        }
        if (edge.getNodeB() == null) {
            throw new IllegalArgumentException("NodeB %s is not part of the region".formatted(edge.getLocationB().toString()));
        }
        // check if it is within region
        if (!edge.getRegion().equals(this) || !edge.getNodeA().getRegion().equals(this) || !edge.getNodeB().getRegion().equals(this)) {
            throw new IllegalArgumentException("Edge %s has incorrect region".formatted(edge.toString()));
        }

        // when no errors occured, the real stuff may happen
        allEdges.add(edge);

        if (edge.getLocationA().compareTo(edge.getLocationB()) <= 0) {
            edges.put(edge.getLocationA(), new HashMap<>());
            edges.get(edge.getLocationA()).put(edge.getLocationB(), edge);
        }
        else {
            edges.put(edge.getLocationB(), new HashMap<>());
            edges.get(edge.getLocationB()).put(edge.getLocationA(), edge);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof RegionImpl castO) {
            if (o == this || (Objects.equals(castO.nodes, this.nodes) && Objects.equals(castO.edges, this.edges))) {
                return true;
            }
            else {
                return false;
            }
        }
        else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges);
    }
}
