package projekt.delivery.service;

import projekt.delivery.event.ArrivedAtNeighborhoodEvent;
import projekt.delivery.event.Event;
import projekt.delivery.routing.ConfirmedOrder;
import projekt.delivery.routing.Region;
import projekt.delivery.routing.Vehicle;
import projekt.delivery.routing.VehicleManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.tudalgo.algoutils.student.Student.crash;

/**
 * A very simple delivery service that distributes orders to compatible vehicles in a FIFO manner.
 */
public class BasicDeliveryService extends AbstractDeliveryService {

    // List of orders that have not yet been loaded onto delivery vehicles
    protected final List<ConfirmedOrder> pendingOrders = new ArrayList<>();

    public BasicDeliveryService(
        VehicleManager vehicleManager
    ) {
        super(vehicleManager);
    }

    @Override
    protected List<Event> tick(long currentTick, List<ConfirmedOrder> newOrders) {
        List<Event> eventsOfTick = vehicleManager.tick(currentTick);
        //Orders laden und sortieren
        pendingOrders.addAll(newOrders);
        pendingOrders.sort((i,j) -> (int) (i.getDeliveryInterval().end() - j.getDeliveryInterval().end()));

        //Bei jedem Restaurant alle Vehicles durchgehen
        for (VehicleManager.OccupiedRestaurant restaurant : pendingOrders.stream().map(ConfirmedOrder::getRestaurant).distinct().toList()){
            for (Vehicle vehicle : vehicleManager.getVehicles().stream()
                .filter(o -> o.getOccupied() == restaurant).toList()){
                //Vehicle mit den Orders vollladen
                for (ConfirmedOrder pending : pendingOrders.stream().filter(o -> o.getRestaurant() == restaurant).toList()){
                    if (vehicle.getCapacity() - vehicle.getCurrentWeight() > pending.getWeight()){
                        pendingOrders.remove(pending);
                        restaurant.loadOrder(vehicle, pending, currentTick);
                    }
                }
            }
        }
        //Bei jedem Vehicle
        for (Vehicle vehicle : vehicleManager.getVehicles().stream().toList()){
            //eine Liste aller Ziellocations anlegen
            List<VehicleManager.OccupiedNeighborhood> destinations = vehicle.getOrders().stream()
                .map(o -> vehicleManager.getOccupiedNeighborhood(vehicleManager.getRegion().getNode(o.getLocation()))).distinct().toList();

            //Für jeder Ziellocation alle Orders an die Location auf das Vehicel packen und später delivern
            for (VehicleManager.OccupiedNeighborhood destination : destinations){
                List<ConfirmedOrder> list = vehicle.getOrders().stream().filter(o -> o.getLocation() == destination.getComponent().getLocation()).toList();
                vehicle.moveQueued(destination.getComponent(),
                    (v, t) -> list.forEach(conOrder -> destination.deliverOrder(v, conOrder, t)));
            }
            //Zurück zum Restaurant
            if (destinations.size()>0)
                vehicle.moveQueued((Region.Node) vehicle.getOccupied().getComponent());
        }

        return  eventsOfTick;
    }

    @Override
    public List<ConfirmedOrder> getPendingOrders() {
        return pendingOrders;
    }

    @Override
    public void reset() {
        super.reset();
        pendingOrders.clear();
    }

    public interface Factory extends DeliveryService.Factory {

        BasicDeliveryService create(VehicleManager vehicleManager);
    }
}
