package projekt.delivery.routing;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;

import static org.tudalgo.algoutils.student.Student.crash;

class VehicleImpl implements Vehicle {

    private final int id;
    private final double capacity;
    private final List<ConfirmedOrder> orders = new ArrayList<>();
    private final VehicleManagerImpl vehicleManager;
    private final Deque<PathImpl> moveQueue = new LinkedList<>();
    private final VehicleManager.OccupiedRestaurant startingNode;
    private AbstractOccupied<?> occupied;

    public VehicleImpl(
        int id,
        double capacity,
        VehicleManagerImpl vehicleManager,
        VehicleManager.OccupiedRestaurant startingNode) {
        this.id = id;
        this.capacity = capacity;
        this.occupied = (AbstractOccupied<?>) startingNode;
        this.vehicleManager = vehicleManager;
        this.startingNode = startingNode;
    }

    @Override
    public VehicleManager.Occupied<?> getOccupied() {
        return occupied;
    }

    @Override
    public @Nullable VehicleManager.Occupied<?> getPreviousOccupied() {
        AbstractOccupied.VehicleStats stats = occupied.vehicles.get(this);
        return stats == null ? null : stats.previous;
    }

    @Override
    public List<? extends Path> getPaths() {
        return new LinkedList<>(moveQueue);
    }

    void setOccupied(AbstractOccupied<?> occupied) {
        this.occupied = occupied;
    }

    @Override
    public void moveDirect(Region.Node node, BiConsumer<? super Vehicle, Long> arrivalAction) {


        moveQueue.clear();
        if (occupied instanceof Region.Node){
            if (occupied.getComponent() == node)
                throw new IllegalArgumentException();
        }

        if (occupied instanceof Region.Edge){
            Region.Node previousOccupied =
                //Stream aller Fahrzeuge dieser Node
                occupied.getVehicles().stream()
                    //Nur dieses
                .filter(v -> v == this)
                    //Get prev component
                .map(v-> v.getPreviousOccupied().getComponent())
                    //Cast to node
                .filter(c -> c instanceof Region.Node).map(c -> (Region.Node) c)
                    //Stream besteht nur aus dem Element dieses Fahrzeugs, raw conversion
                    .toList().get(0);
            Region.Edge edge = (Region.Edge) occupied.getComponent();
            if (previousOccupied == edge.getNodeA()){
                moveQueue.add(new PathImpl(vehicleManager.getPathCalculator().getPath(edge.getNodeB(), node), arrivalAction));
            }
            else if (previousOccupied == edge.getNodeB()){
                moveQueue.add(new PathImpl(vehicleManager.getPathCalculator().getPath(edge.getNodeA(), node), arrivalAction));
            } throw new RuntimeException("Something went terribly wrong");
        }
        moveQueued(node, arrivalAction);

        /*
        //TODO: fix this shit
        VehicleImpl.PathImpl nextPath = null;
        if (moveQueue.size() > 0) {
            nextPath = moveQueue.getFirst();
        }
        moveQueue.clear();
        if (occupied.getComponent() instanceof Region.Node castOccupiedNode) { // check if component is node
            if (node.equals(castOccupiedNode)) {
                throw new IllegalArgumentException();
            } else {
                moveQueued(node, arrivalAction);
            }
        }
        else { // component is edge, so first a path is run
            Region.Edge edge = (Region.Edge) occupied.getComponent();
            moveQueue.add(new PathImpl(vehicleManager.getPathCalculator().getPath(edge.getNodeB(), node), arrivalAction));
            moveQueued(node, arrivalAction);
        }

         */
    }

    @Override
    public void moveQueued(Region.Node node, BiConsumer<? super Vehicle, Long> arrivalAction) {
        if (node.equals(occupied.getComponent()) && moveQueue.size() == 0) {
            throw new IllegalArgumentException();
        }
        else {
            if (moveQueue.size() == 0) {
                moveQueue.add(new PathImpl(vehicleManager.getPathCalculator().getPath((Region.Node) occupied.getComponent(), node), arrivalAction));
            }
            else {
                try {
                    moveQueue
                        .add(new PathImpl(vehicleManager.getPathCalculator()
                            .getPath(
                                moveQueue.getLast().
                                nodes.getLast(),
                                node),
                            arrivalAction));
                } catch (Exception e){
                    //throw e;
                }

            }
        }
    }

    @Override
    public int getId() {
        return id;
    }

    @Override
    public double getCapacity() {
        return capacity;
    }

    @Override
    public VehicleManager getVehicleManager() {
        return vehicleManager;
    }

    @Override
    public VehicleManager.Occupied<? extends Region.Node> getStartingNode() {
        return startingNode;
    }

    @Override
    public Collection<ConfirmedOrder> getOrders() {
        return orders;
    }

    @Override
    public void reset() {
        occupied = (AbstractOccupied<?>) startingNode;
        moveQueue.clear();
        orders.clear();
    }

    private void checkMoveToNode(Region.Node node) {
        if (occupied.component.equals(node) && moveQueue.isEmpty()) {
            throw new IllegalArgumentException("Vehicle " + getId() + " cannot move to own node " + node);
        }
    }

    void move(long currentTick) {
        final Region region = vehicleManager.getRegion();
        if (moveQueue.isEmpty()) {
            return;
        }
        final PathImpl path = moveQueue.peek();
        if (path.nodes().isEmpty()) {
            moveQueue.pop();
            final @Nullable BiConsumer<? super Vehicle, Long> action = path.arrivalAction();
            if (action == null) {
                move(currentTick);
            } else {
                action.accept(this, currentTick);
            }
        } else {
            Region.Node next = path.nodes().peek();
            if (occupied instanceof OccupiedNodeImpl) {
                vehicleManager.getOccupied(region.getEdge(((OccupiedNodeImpl<?>) occupied).getComponent(), next)).addVehicle(this, currentTick);
            } else if (occupied instanceof OccupiedEdgeImpl) {
                vehicleManager.getOccupied(next).addVehicle(this, currentTick);
                path.nodes().pop();
            } else {
                throw new AssertionError("Component must be either node or component");
            }
        }
    }

    void loadOrder(ConfirmedOrder order) {
        double potentialWeight = getCurrentWeight() + order.getWeight();
        if (potentialWeight <= getCapacity()) {
            orders.add(order);
        }
        else {
            throw new VehicleOverloadedException(this, potentialWeight);
        }
    }

    void unloadOrder(ConfirmedOrder order) {
        orders.remove(order);
    }

    @Override
    public int compareTo(Vehicle o) {
        return Integer.compare(getId(), o.getId());
    }

    @Override
    public String toString() {
        return "VehicleImpl("
            + "id=" + id
            + ", capacity=" + capacity
            + ", orders=" + orders
            + ", component=" + occupied.component
            + ')';
    }

    private record PathImpl(Deque<Region.Node> nodes, BiConsumer<? super Vehicle, Long> arrivalAction) implements Path {

    }
}
