package projekt.gui.scene;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import projekt.delivery.archetype.ProblemArchetype;
import projekt.delivery.archetype.ProblemGroup;
import projekt.delivery.archetype.ProblemGroupImpl;
import projekt.delivery.rating.AmountDeliveredRater;
import projekt.delivery.rating.InTimeRater;
import projekt.delivery.rating.RatingCriteria;
import projekt.delivery.rating.TravelDistanceRater;
import projekt.delivery.routing.Region;
import projekt.delivery.service.BasicDeliveryService;
import projekt.delivery.service.BogoDeliveryService;
import projekt.delivery.service.DeliveryService;
import projekt.delivery.service.OurDeliveryService;
import projekt.delivery.simulation.SimulationConfig;
import projekt.gui.controller.MainMenuSceneController;
import projekt.gui.scene.tableObjects.problemArchetypeEntrys;
import projekt.io.IOHelper;
import projekt.runner.RunnerImpl;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class MainMenuScene extends MenuScene<MainMenuSceneController> {

    private int simulationRuns = 1;
    private DeliveryService.Factory deliveryServiceFactory;
    private ProblemArchetype selectedProblemArchetype;
    private final Insets preferredPadding = new Insets(20, 20, 20, 20);

    public MainMenuScene() {
        super(new MainMenuSceneController(), "Delivery Service Simulation");
    }

    @Override
    public void initComponents() {
        root.setCenter(createOptionsVBox());
        root.setRight(createProblemsVBox());
    }

    /**
     * Initializes this {@link MainMenuScene} with the {@link ProblemArchetype} presets in the resource dir.
     */
    public void init() {
        super.init(IOHelper.readProblems());
    }

    private VBox createCenterBox(){
        VBox centerBox = new VBox();
        centerBox.getChildren().addAll(createProblemsVBox(), createOptionsVBox());
        return centerBox;
    }

    private VBox createProblemsVBox() {
        VBox problemVBox = new VBox();
        problemVBox.setPrefSize(200, 100);
        problemVBox.setAlignment(Pos.CENTER);
        problemVBox.setSpacing(10);
        problemVBox.setPadding(preferredPadding);

        HBox labelHBox = new HBox();
        Label label = new Label("Problem Archetypes:");
        labelHBox.getChildren().addAll(label);

        HBox choiceBoxHBox = new HBox();
        ChoiceBox<ProblemArchetype> choiceBox = new ChoiceBox<>();

        for (ProblemArchetype problem : problems){
            choiceBox.getItems().add(problem);
        }
        choiceBox.setConverter(new StringConverter<ProblemArchetype>() {
            @Override
            public String toString(ProblemArchetype object) {
                return object.toString();
            }

            @Override
            public ProblemArchetype fromString(String string) {
                throw new UnsupportedOperationException();
            }
        });

        choiceBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) ->
            updateProblemTable(choiceBox.getItems().get((Integer) newValue)));

        choiceBox.getSelectionModel().select(0);

        choiceBoxHBox.getChildren().addAll(choiceBox);


        problemVBox.getChildren().addAll(labelHBox, choiceBoxHBox, problemTable);

        return problemVBox;
    }
    private TableView<problemArchetypeEntrys> problemTable;
    private void updateProblemTable(ProblemArchetype archetype){
        if (problemTable == null)
            problemTable = createArchetypeTable(archetype);
        DecimalFormat twoDForm = new DecimalFormat("#.###");
        String d = twoDForm.format(9.1342);


        problemTable.getItems().removeIf(v->true);

        problemTable.getItems().add(new problemArchetypeEntrys("Raters", " ", " "));

        AmountDeliveredRater.Factory fact = (AmountDeliveredRater.Factory) archetype.raterFactoryMap().get(RatingCriteria.AMOUNT_DELIVERED);
        problemTable.getItems().add(new problemArchetypeEntrys("DeliveredRatio", twoDForm.format(fact.factor), ""));

        InTimeRater.Factory fact2 = (InTimeRater.Factory) archetype.raterFactoryMap().get(RatingCriteria.IN_TIME);
        problemTable.getItems().add(new problemArchetypeEntrys("TimeRater", String.valueOf(fact2.ignoredTicksOff), String.valueOf(fact2.maxTicksOff)));

        TravelDistanceRater.Factory fact3 = (TravelDistanceRater.Factory) archetype.raterFactoryMap().get(RatingCriteria.TRAVEL_DISTANCE);
        problemTable.getItems().add(new problemArchetypeEntrys("DistanceRater", twoDForm.format(fact3.factor), " "));

        problemTable.getItems().add(new problemArchetypeEntrys(" ", " ", " "));

        problemTable.getItems().add(new problemArchetypeEntrys("Type", "Amount", "Capacity"));

        /*
        problemTable.getItems().add(new problemArchetypeEntrys("Nodes total",
            String.valueOf(archetype.vehicleManager().getRegion().getNodes().size()), " "));
        problemTable.getItems().add(new problemArchetypeEntrys("Vehicles total",
            String.valueOf(archetype.vehicleManager().getAllVehicles().size()), " "));
*/
        problemTable.getItems().add(new problemArchetypeEntrys("Restaurants",
            String.valueOf(archetype.vehicleManager().getRegion().getNodes().stream().
                filter(n -> n instanceof Region.Restaurant).toList().size()), " "));

        int rest = 0;
        for (Region.Restaurant restaurant : archetype.vehicleManager().getRegion().getNodes().stream().filter(Region.Restaurant.class::isInstance).map(Region.Restaurant.class::cast).toList()){
            rest++;
            AtomicInteger num = new AtomicInteger(0);
            AtomicReference<Double> average = new AtomicReference<>(0.0);
            archetype.vehicleManager().getAllVehicles().stream().filter(v -> v.getStartingNode().getComponent().equals(restaurant)).
                forEach(v -> {
                    num.set(num.get() + 1);
                    average.set((average.get() + v.getCapacity()) / num.get());});
            problemTable.getItems().add(new problemArchetypeEntrys("Vehicles at Restaurant " + rest,
                String.valueOf(archetype.vehicleManager().getAllVehicles().stream().
                    filter(v -> v.getStartingNode().getComponent().equals(restaurant)).toList().size()),
                twoDForm.format(average.get())
            ));
        }



        problemTable.getItems().add(new problemArchetypeEntrys("Neighborhoods",
            String.valueOf(archetype.vehicleManager().getRegion().getNodes().stream().
                filter(n -> n instanceof Region.Neighborhood).toList().size()), " "));

        problemTable.getItems().add(new problemArchetypeEntrys("Forests",
            String.valueOf(archetype.vehicleManager().getRegion().getNodes().stream().
                filter(n -> !(n instanceof Region.Neighborhood) && !(n instanceof Region.Restaurant)).toList().size()), " "));

        problemTable.getItems().add(new problemArchetypeEntrys("Edges",
            String.valueOf(archetype.vehicleManager().getRegion().getEdges().size()), " "));

        problemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    private TableView<problemArchetypeEntrys> createArchetypeTable(ProblemArchetype archetype){
        problemTable = new TableView<>();
        problemTable.setMinWidth(250);

        TableColumn<problemArchetypeEntrys, String> title = new TableColumn<>("  ");
        title.setCellValueFactory(new PropertyValueFactory<>("title"));
        //title.setPrefWidth(350);
        title.setMinWidth(100);

        TableColumn<problemArchetypeEntrys, String> param1 = new TableColumn<>("  ");
        param1.setCellValueFactory(new PropertyValueFactory<>("param1"));

        TableColumn<problemArchetypeEntrys, String> param2 = new TableColumn<>("  ");
        param2.setCellValueFactory(new PropertyValueFactory<>("param2"));

        problemTable.getColumns().add(title);
        problemTable.getColumns().add(param1);
        problemTable.getColumns().add(param2);

        return problemTable;
    }



    private VBox createOptionsVBox() {
        VBox optionsVbox = new VBox();
        optionsVbox.setPrefSize(200, 100);
        optionsVbox.setAlignment(Pos.CENTER);
        optionsVbox.setSpacing(10);
        optionsVbox.setPadding(preferredPadding);

        optionsVbox.getChildren().addAll(
            createStartSimulationButton(),
            createSimulationRunsHBox(),
            createDeliveryServiceChoiceBox()
            //TODO H11.2
        );

        optionsVbox.getChildren().stream()
            .filter(Button.class::isInstance)
            .map(Button.class::cast)
            .forEach(button -> {
                button.setPrefSize(200, 50);
                button.setMaxWidth(Double.MAX_VALUE);
            });

        return optionsVbox;
    }

    private Button createStartSimulationButton() {
        Button startSimulationButton = new Button("Start Simulation");
        startSimulationButton.setOnAction((e) -> {
            if (problems.size() == 0) {
                throw new IllegalArgumentException("No problems selected");
            }

            //store the SimulationScene
            AtomicReference<SimulationScene> simulationScene = new AtomicReference<>();
            //Execute the GUIRunner in a separate Thread to prevent it from blocking the GUI
            new Thread(() -> {
                ProblemGroup problemGroup = new ProblemGroupImpl(problems, problems.get(0).raterFactoryMap().keySet().stream().toList());
                new RunnerImpl().run(
                    problemGroup,
                    new SimulationConfig(20),
                    simulationRuns,
                    deliveryServiceFactory,
                    (simulation, problem, i) -> {
                        //CountDownLatch to check if the SimulationScene got created
                        CountDownLatch countDownLatch = new CountDownLatch(1);
                        //execute the scene switching on the javafx application thread
                        Platform.runLater(() -> {
                            //switch to the SimulationScene and set everything up
                            SimulationScene scene = (SimulationScene) SceneSwitcher.loadScene(SceneSwitcher.SceneType.SIMULATION, getController().getStage());
                            scene.init(simulation, problem, i, simulationRuns);
                            simulation.addListener(scene);
                            simulationScene.set(scene);
                            countDownLatch.countDown();
                        });

                        try {
                            //wait for the SimulationScene to be set
                            countDownLatch.await();
                        } catch (InterruptedException exc) {
                            throw new RuntimeException(exc);
                        }
                    },
                    (simulation, problem) -> {
                        //remove the scene from the list of listeners
                        simulation.removeListener(simulationScene.get());

                        //check if gui got stopped
                        return simulationScene.get().isClosed();
                    },
                    result -> {
                        //execute the scene switching on the javafx thread
                        Platform.runLater(() -> {
                            RaterScene raterScene = (RaterScene) SceneSwitcher.loadScene(SceneSwitcher.SceneType.RATING, getController().getStage());
                            raterScene.init(problemGroup.problems(), result);
                        });
                    });
            }).start();
        });

        return startSimulationButton;
    }

    private HBox createSimulationRunsHBox() {
        HBox simulationRunsHBox = new HBox();
        simulationRunsHBox.setMaxWidth(200);

        Label simulationRunsLabel = new Label("Simulation Runs:");
        TextField simulationRunsTextField = createPositiveIntegerTextField(value -> simulationRuns = value, 1);
        simulationRunsTextField.setMaxWidth(50);

        simulationRunsHBox.getChildren().addAll(simulationRunsLabel, createIntermediateRegion(0), simulationRunsTextField);

        return simulationRunsHBox;
    }

    private VBox createDeliveryServiceChoiceBox() {
        VBox deliveryServiceVBox = new VBox();
        deliveryServiceVBox.setMaxWidth(200);
        deliveryServiceVBox.setSpacing(10);

        HBox labelHBox = new HBox();
        Label label = new Label("Delivery Service:");
        labelHBox.getChildren().addAll(label);

        HBox choiceBoxHBox = new HBox();
        ChoiceBox<DeliveryService.Factory> choiceBox = new ChoiceBox<>();

        choiceBox.getItems().setAll(
            DeliveryService.BASIC,
            DeliveryService.OUR,
            DeliveryService.BOGO
        );
        choiceBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(DeliveryService.Factory deliveryService) {
                if (deliveryService instanceof BasicDeliveryService.Factory) {
                    return "Basic Delivery Service";
                }
                if (deliveryService instanceof OurDeliveryService.Factory) {
                    return "Our Delivery Service";
                }
                if (deliveryService instanceof BogoDeliveryService.Factory) {
                    return "Bogo Delivery Service";
                }

                return "Delivery Service";
            }

            @Override
            public DeliveryService.Factory fromString(String distanceCalculator) {
                throw new UnsupportedOperationException();
            }
        });

        choiceBox.getSelectionModel().selectedIndexProperty().addListener((obs, oldValue, newValue) ->
            deliveryServiceFactory = choiceBox.getItems().get((Integer) newValue));

        choiceBox.getSelectionModel().select(0);

        choiceBoxHBox.getChildren().addAll(choiceBox);

        deliveryServiceVBox.getChildren().addAll(label, choiceBox);

        return deliveryServiceVBox;
    }

    @Override
    public void initReturnButton() {
        ((HBox) root.getBottom()).getChildren().remove(returnButton);
    }

    @Override
    public MainMenuSceneController getController() {
        return controller;
    }
}
