package projekt.gui.pane;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Text;
import projekt.delivery.archetype.ProblemArchetype;
import projekt.delivery.routing.Vehicle;
import projekt.delivery.simulation.Simulation;
import projekt.delivery.simulation.SimulationConfig;

import java.util.Collection;
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
    }
    private void swapVehicle(int id){
        //ToDo Implement
        if (vehicles.size() == 0){
        } else chosenVehicle = vehicles.stream().filter(v -> v.getId() == id).toList().get(0);

        vehicleIDTextField.setText(String.valueOf(chosenVehicle.getId()));
        vehicleLocationTextField.setText(chosenVehicle.getOccupied().getComponent().getName());

    }
    private Vehicle chosenVehicle;
    private TextField vehicleTextField;
    private TextField vehicleIDTextField;
    private TextField vehicleLocationTextField;
    private ListView<String> vehicleOrdersListView;


    private void initComponents(ProblemArchetype problem, int run, int simulationRuns, MapPane mapPane) {

        Button previousButton = new Button("Previous");
        previousButton.setOnAction(e -> swapVehicle(0));

        Button nextButton = new Button("Next");
        nextButton.setOnAction(e -> swapVehicle(0));

        vehicleTextField = new TextField("Test");



        tickIntervalSlider.setValue(simulationConfig.getMillisecondsPerTick());
        tickIntervalSlider.setMin(20);
        tickIntervalSlider.setMax(2000);
        tickIntervalSlider.setMajorTickUnit(1);
        tickIntervalSlider.setSnapToTicks(true);
        tickIntervalSlider.valueProperty().addListener((obs, oldValue, newValue) -> {
            simulationConfig.setMillisecondsPerTick(newValue.intValue());
            updateText();
        });
        VBox sliderBox = new VBox(tickIntervalSlider, tickIntervalSliderLabel);

        Label problemLabel = new Label("Simulating Problem: %s".formatted(problem.name()));
        Label runLabel = new Label("Run: %d/%d".formatted(run + 1, simulationRuns));
        VBox labels = new VBox(problemLabel, runLabel, tickLabel);

        Region intermediateRegion = new Region();
        intermediateRegion.setMinWidth(0);
        HBox.setHgrow(intermediateRegion, Priority.ALWAYS);

        HBox box = new HBox(previousButton, vehicleTextField, nextButton, sliderBox, intermediateRegion, labels);
        box.setPadding(new Insets(0, 10, 0, 10));
        box.setSpacing(10);

        setCenter(box);
    }

    public void updateTickLabel(long tick) {
        tickLabel.setText("Tick: %d/%d".formatted(tick, simulationLength));
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

