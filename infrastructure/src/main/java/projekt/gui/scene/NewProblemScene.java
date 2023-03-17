package projekt.gui.scene;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import projekt.delivery.archetype.ProblemArchetype;
import projekt.delivery.archetype.ProblemArchetypeImpl;
import projekt.delivery.generator.OrderGenerator;
import projekt.delivery.rating.*;
import projekt.delivery.routing.Region;
import projekt.delivery.routing.Vehicle;
import projekt.delivery.routing.VehicleManager;
import projekt.gui.controller.MainMenuSceneController;
import projekt.gui.controller.NewProblemSceneController;
import projekt.gui.scene.tableObjects.problemArchetypeEntrys;
import projekt.io.IOHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class NewProblemScene extends MenuScene<NewProblemSceneController> {
    private ProblemArchetype problem;

    private final Insets preferredPadding = new Insets(20, 20, 20, 20);
    /**
     * Creates a new {@link NewProblemScene}.
     *
     * @param controller  The {@link NewProblemSceneController} of this {@link MainMenuScene}.
     * @param title       The title of this {@link MainMenuScene}.
     * @param styleSheets The styleSheets to apply.
     */
    public NewProblemScene(NewProblemSceneController controller, String title, String... styleSheets) {
        super(controller, title, styleSheets);
    }
    public NewProblemScene() {
        super(new NewProblemSceneController(), "Delivery Service Simulation");
    }
    public void init(ArrayList<ProblemArchetype> problems, ProblemArchetype problem){
        super.init(problems);
        if (problem == null){
            //ToDo problem mit nullähnlichen Values initiieren
        }
        System.out.println(problem.name());
        this.problem = problem;
        orderGeneratorFactory= problem.orderGeneratorFactory();
        vehicleManager = problem.vehicleManager();
        raterFactoryMap = problem.raterFactoryMap();
        simulationLength = problem.simulationLength();
        initComponents();
    }
    private OrderGenerator.Factory orderGeneratorFactory ;
    private VehicleManager vehicleManager;
    private Map<RatingCriteria, Rater.Factory> raterFactoryMap;
    private long simulationLength;
    private void updateProblem(){
        problem = new ProblemArchetypeImpl(orderGeneratorFactory,
            vehicleManager,
            raterFactoryMap,
            simulationLength,
            name.getText());
    }

    @Override
    public void initComponents() {
        if (problem != null)
            root.setRight(initTable());

    }




    private VBox createAdderBoxes(){
        return new VBox();
    }
    private HBox createRestaurantBox() {
        Label X = new Label("X");
        Label Y = new Label("Y");
        TextField XT = new TextField();
        TextField YT = new TextField();
        XT.setPrefWidth(30);
        YT.setPrefWidth(30);
        return new HBox(X, XT, Y, YT);

    }
    private TableView<problemArchetypeEntrys> problemTable;
    private TextField name;
    private VBox initTable(){
        Label nameLabel = new Label("Name:   ");
        name = new TextField(problem.name());
        name.setEditable(true);
        HBox nameBox = new HBox(nameLabel, name);
        nameBox.setAlignment(Pos.CENTER);


        VBox problemVBox = new VBox(nameBox, createArchetypeTable());
        problemVBox.setPrefSize(200, 100);
        problemVBox.setAlignment(Pos.CENTER);
        problemVBox.setSpacing(10);
        problemVBox.setPadding(preferredPadding);
        updateProblemTable(problem);
        return problemVBox;
    }
    private void updateProblemTable(ProblemArchetype archetype){
        if (problemTable == null)
            problemTable = createArchetypeTable();
        DecimalFormat twoDForm = new DecimalFormat("#.##");


        problemTable.getItems().removeIf(v->true);

        problemTable.getItems().add(new problemArchetypeEntrys("Raters", " ", " "));

        AmountDeliveredRater.Factory fact = (AmountDeliveredRater.Factory) archetype.raterFactoryMap().get(RatingCriteria.AMOUNT_DELIVERED);
        problemTable.getItems().add(new problemArchetypeEntrys("DeliveredRatio", twoDForm.format(fact.factor), ""));

        InTimeRater.Factory fact2 = (InTimeRater.Factory) archetype.raterFactoryMap().get(RatingCriteria.IN_TIME);
        problemTable.getItems().add(new problemArchetypeEntrys("TimeRater", String.valueOf(fact2.ignoredTicksOff), String.valueOf(fact2.maxTicksOff)));

        TravelDistanceRater.Factory fact3 = (TravelDistanceRater.Factory) archetype.raterFactoryMap().get(RatingCriteria.TRAVEL_DISTANCE);
        problemTable.getItems().add(new problemArchetypeEntrys("DistanceRater", twoDForm.format(fact3.factor), " "));

        problemTable.getItems().add(new problemArchetypeEntrys(" ", " ", " "));


        /*
        problemTable.getItems().add(new problemArchetypeEntrys("Nodes total",
            String.valueOf(archetype.vehicleManager().getRegion().getNodes().size()), " "));
        problemTable.getItems().add(new problemArchetypeEntrys("Vehicles total",
            String.valueOf(archetype.vehicleManager().getAllVehicles().size()), " "));
*/
        problemTable.getItems().add(new problemArchetypeEntrys(archetype.vehicleManager().getRegion().getNodes().stream().
            filter(n -> n instanceof Region.Restaurant).toList().size() + " Restaurants",
            " X ",
            " Y "));

        int rest = 0;
        for (Region.Restaurant restaurant : archetype.vehicleManager().getRegion().getNodes().stream().filter(Region.Restaurant.class::isInstance).map(Region.Restaurant.class::cast).toList()) {
            problemTable.getItems().add(new problemArchetypeEntrys(" - (" + rest + ") " + restaurant.getName(),
                String.valueOf(restaurant.getLocation().getX()),
                String.valueOf(restaurant.getLocation().getY())));
            rest++;
        }
        rest = 0;
        for (Region.Restaurant restaurant : archetype.vehicleManager().getRegion().getNodes().stream().filter(Region.Restaurant.class::isInstance).map(Region.Restaurant.class::cast).toList()){
            AtomicInteger num = new AtomicInteger(0);
            AtomicReference<Double> average = new AtomicReference<>(0.0);
            archetype.vehicleManager().getAllVehicles().stream().filter(v -> v.getStartingNode().getComponent().equals(restaurant)).
                forEach(v -> {
                    num.set(num.get() + 1);
                    average.set((average.get() + v.getCapacity()) / num.get());});
            problemTable.getItems().add(new problemArchetypeEntrys("Vehicles at Restaurant " + rest,
                String.valueOf(archetype.vehicleManager().getAllVehicles().stream().
                    filter(v -> v.getStartingNode().getComponent().equals(restaurant)).toList().size()),
                "Load")//twoDForm.format(average.get())
            );
            rest++;

            for (Vehicle vehicle : archetype.vehicleManager().getAllVehicles().stream()
                .filter(v -> v.getStartingNode().getComponent().equals(restaurant)).toList()){
                problemTable.getItems().add(new problemArchetypeEntrys(
                    "ID: " + vehicle.getId(),
                    " ",
                    twoDForm.format(vehicle.getCapacity()))
                );
            }
        }



        problemTable.getItems().add(new problemArchetypeEntrys(" ", " ", " "));
        problemTable.getItems().add(new problemArchetypeEntrys(
            archetype.vehicleManager().getRegion().getNodes().stream().
                filter(n -> n instanceof Region.Neighborhood).toList().size() + "  Neighborhoods",
            " X ",
            " Y "));

        for (Region.Neighborhood neighborhood : archetype.vehicleManager().getRegion().getNodes().stream().filter(Region.Neighborhood.class::isInstance).map(Region.Neighborhood.class::cast).toList()) {
            problemTable.getItems().add(new problemArchetypeEntrys(" - " + neighborhood.getName(),
                String.valueOf(neighborhood.getLocation().getX()),
                String.valueOf(neighborhood.getLocation().getY())));
        }

        problemTable.getItems().add(new problemArchetypeEntrys(" ", " ", " "));
        problemTable.getItems().add(new problemArchetypeEntrys(
            archetype.vehicleManager().getRegion().getNodes().stream().filter(n -> !(n instanceof Region.Neighborhood) && !(n instanceof Region.Restaurant)).toList().size() + "  Forests",
            " X ",
            " Y "));
        for (Region.Node node : archetype.vehicleManager().getRegion().getNodes().stream()
            .filter(nod -> !(nod instanceof Region.Restaurant) && !(nod instanceof Region.Neighborhood))
            .map(Region.Node.class::cast).toList()) {
            problemTable.getItems().add(new problemArchetypeEntrys(" - " + node.getName(),
                String.valueOf(node.getLocation().getX()),
                String.valueOf(node.getLocation().getY())));
        }

        problemTable.getItems().add(new problemArchetypeEntrys(" ", " ", " "));
        problemTable.getItems().add(new problemArchetypeEntrys(
            archetype.vehicleManager().getRegion().getEdges().size() + "  Edges",
            "(X1,Y1)",
            "(X2,Y2)"));
        for (Region.Edge edge : archetype.vehicleManager().getRegion().getEdges()) {
            problemTable.getItems().add(new problemArchetypeEntrys(" - " + edge.getName(),
                "(" + edge.getNodeA().getLocation().getX() + ", "+edge.getNodeA().getLocation().getY() + ")",
                "(" + edge.getNodeB().getLocation().getX() + ", "+edge.getNodeB().getLocation().getY() + ")"));
        }

        problemTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }
    private TableView<problemArchetypeEntrys> createArchetypeTable(){
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

    @Override
    public void initReturnButton() {
        returnButton.setText("Save and Return");
        returnButton.setOnAction(e -> {
            updateProblem();
            if (problem != null) {
                IOHelper.writeProblem(problem);
            }
            MainMenuScene scene = (MainMenuScene) SceneSwitcher.loadScene(SceneSwitcher.SceneType.MAIN_MENU, getController().getStage());
            scene.init(new ArrayList<>(problems));
        });
    }
    @Override
    public NewProblemSceneController getController(){
        return controller;
    }
}
