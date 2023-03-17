package projekt.gui.pane;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import projekt.delivery.archetype.ProblemArchetype;
import projekt.delivery.routing.Vehicle;
import projekt.delivery.simulation.Simulation;
import projekt.delivery.simulation.SimulationConfig;
import projekt.gui.pane.tables.vehicleTable;

import java.util.Collection;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

public class VehiclePane extends BorderPane {

    private final Simulation simulation;
    private final SimulationConfig simulationConfig;

    private final Button singleStepButton = new Button("Single step");
    private final Slider tickIntervalSlider = new Slider();
    private final Label tickIntervalSliderLabel = new Label();
    private final Label tickLabel = new Label();

    private final long simulationLength;

    private final ProblemArchetype problem;

    public VehiclePane(Simulation simulation, ProblemArchetype problem, int run, int simulationRuns, long simulationLength, MapPane mapPane) {
        this.simulationLength = simulationLength;
        this.simulation = simulation;
        this.simulationConfig = simulation.getSimulationConfig();
        this.problem = problem;
        initComponents(problem, run, simulationRuns, mapPane);
        updateText();
        setPadding(new Insets(5));
    }
    private Collection<Vehicle> vehicles;
    public void updateVehicleInformation(){
        vehicles = problem.vehicleManager().getVehicles();
        swapVehicle();
    }
    private void swapVehicle(){
        if (vehicles.size() == 0){
        } else chosenVehicle = vehicles.stream().filter(v -> v.getId() == IDInt.get()).toList().get(0);

        vehicleIDTextField.setText("Vehicle ID: " + chosenVehicle.getId());
        vehicleLocationTextField.setText(chosenVehicle.getOccupied().getComponent().getName());
        //chosenVehicle.getOrders().stream().forEach( o -> vehicleOrdersListView.getItems().add(o.));

        vehicleOrdersTable.getItems().removeIf(v->true);

        chosenVehicle.getOrders().forEach(o -> vehicleOrdersTable.getItems()
            .add(new vehicleTable(
                problem.vehicleManager().getRegion().getNode(o.getLocation()),
                o.getDeliveryInterval().start(),
                o.getDeliveryInterval().end())));
        vehicleOrdersTable.setMaxHeight(19*(chosenVehicle.getOrders().size()+2.5));
        vehicleOrdersTable.setMinHeight(vehicleOrdersTable.getMaxHeight());

    }
    private Vehicle chosenVehicle;
    private TextField vehicleTextField;
    private TextField vehicleIDTextField;
    private TextField vehicleLocationTextField;
    private TableView<vehicleTable> vehicleOrdersTable;

    private AtomicInteger IDInt;


    private void initComponents(ProblemArchetype problem, int run, int simulationRuns, MapPane mapPane) {

        vehicleTextField = new TextField("Test");
        vehicleIDTextField = new TextField();
        vehicleLocationTextField = new TextField();
        vehicleOrdersTable = new TableView<>();
        vehicleOrdersTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        IDInt = new AtomicInteger(0);

        Button previousButton = new Button("Previous");
        previousButton.setOnAction(e -> {
            IDInt.set(IDInt.get() - 1);
            if (IDInt.get() < 0) IDInt.set(vehicles.size()-1);
            swapVehicle();
        });

        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> {
            IDInt.set(IDInt.get() + 1);
            if (IDInt.get() > vehicles.size()-1) IDInt.set(0);
            swapVehicle();
        });

        VBox IDLocationBox = new VBox(vehicleIDTextField, vehicleLocationTextField);

        TableColumn<vehicleTable, String> node = new TableColumn<>("Target Location");
        node.setCellValueFactory(new PropertyValueFactory<>("node"));
        node.setMinWidth(100);
        TableColumn<vehicleTable, String> orderTick = new TableColumn<>("Ordered");
        orderTick.setCellValueFactory(new PropertyValueFactory<>("orderTick"));
        orderTick.setMinWidth(75);
        TableColumn<vehicleTable, String> deliveryTick = new TableColumn<>("Scheduled");
        deliveryTick.setCellValueFactory(new PropertyValueFactory<>("deliveryTick"));
        deliveryTick.setMinWidth(75);

        vehicleOrdersTable.getColumns().add(node);
        vehicleOrdersTable.getColumns().add(orderTick);
        vehicleOrdersTable.getColumns().add(deliveryTick);
        vehicleOrdersTable.setMaxHeight(96);

        Region intermediateRegion = new Region();
        intermediateRegion.setMinWidth(0);
        HBox.setHgrow(intermediateRegion, Priority.ALWAYS);

        HBox box = new HBox(previousButton, IDLocationBox, nextButton, vehicleOrdersTable, intermediateRegion);
        box.setPadding(new Insets(0, 10, 0, 10));
        box.setSpacing(10);

        setCenter(box);
    }

    private void updateText() {
        tickIntervalSliderLabel.setText(
            "   Tick interval: %d ms %s".formatted(
                (int) tickIntervalSlider.getValue(),
                simulationConfig.isPaused() ? "(paused)" : ""));
    }

    private void togglePaused() {
        simulationConfig.setPaused(!simulationConfig.isPaused());
        singleStepButton.setDisable(!singleStepButton.isDisabled());
        updateText();
    }
}

