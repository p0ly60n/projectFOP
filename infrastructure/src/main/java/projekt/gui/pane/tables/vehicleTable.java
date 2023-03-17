package projekt.gui.pane.tables;

import projekt.delivery.routing.Region;

public class vehicleTable {
    public vehicleTable(Region.Node node1, long orderTick1, long deliveryTick1){
        this.node = node1;
        this.orderTick = orderTick1;
        this.deliveryTick = deliveryTick1;
    }
    private Region.Node node;
    private long orderTick;
    private long deliveryTick;

    public long getOrderTick() {
        return orderTick;
    }

    public long getDeliveryTick() {
        return deliveryTick;
    }

    public String getNode() {
        return node.getName();
    }
}
