package projekt.runner;

import projekt.delivery.archetype.ProblemArchetype;
import projekt.delivery.archetype.ProblemGroup;
import projekt.delivery.rating.RatingCriteria;
import projekt.delivery.service.DeliveryService;
import projekt.delivery.simulation.BasicDeliverySimulation;
import projekt.delivery.simulation.Simulation;
import projekt.delivery.simulation.SimulationConfig;
import projekt.runner.handler.ResultHandler;
import projekt.runner.handler.SimulationFinishedHandler;
import projekt.runner.handler.SimulationSetupHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.tudalgo.algoutils.student.Student.crash;

public class RunnerImpl implements Runner {

    @Override
    public void run(ProblemGroup problemGroup,
                    SimulationConfig simulationConfig,
                    int simulationRuns,
                    DeliveryService.Factory deliveryServiceFactory,
                    SimulationSetupHandler simulationSetupHandler,
                    SimulationFinishedHandler simulationFinishedHandler,
                    ResultHandler resultHandler) {

        Map<ProblemArchetype, Simulation> simulationMap = createSimulations(problemGroup,simulationConfig, deliveryServiceFactory);
        Map<RatingCriteria, Integer> runsAtCriterion = new HashMap<>();
        Map<RatingCriteria, Double> average = new HashMap<>();
        Arrays.stream(RatingCriteria.values()).forEach(r -> {
            runsAtCriterion.put(r, 0);
            average.put(r, 0.0);
        });

        boolean stopSimulations = false;
        for (int i=0;i<simulationRuns && !stopSimulations; i++){
            for (ProblemArchetype archetype: problemGroup.problems()){
                if (stopSimulations) break;
                simulationSetupHandler.accept(simulationMap.get(archetype), archetype, i);
                long maxTicks = 500; //ToDo Was sind die MaxTicks?
                simulationMap.get(archetype).runSimulation(maxTicks);
                if (simulationFinishedHandler.accept(simulationMap.get(archetype), archetype)) stopSimulations = true;
                Arrays.stream(RatingCriteria.values())
                    .forEach(r -> {
                        double rating = simulationMap.get(archetype).getRatingForCriterion(r);
                        double av = average.get(r);
                        int runs = runsAtCriterion.get(r) + 1;
                        average.remove(r);
                        runsAtCriterion.remove(r);
                        runsAtCriterion.put(r, runs);
                        average.put(r, (av+rating) / runs);
                    });
            }
        }
        resultHandler.accept(average);
    }

    @Override
    public Map<ProblemArchetype, Simulation> createSimulations(ProblemGroup problemGroup,
                                                                SimulationConfig simulationConfig,
                                                                DeliveryService.Factory deliveryServiceFactory) {
        Map<ProblemArchetype, Simulation> returner = new HashMap<>();
        for (ProblemArchetype archetype: problemGroup.problems()){
            BasicDeliverySimulation deliverySimulation = new BasicDeliverySimulation(
                simulationConfig,
                archetype.raterFactoryMap(),
                deliveryServiceFactory.create(archetype.vehicleManager()),
                archetype.orderGeneratorFactory()
                );
            returner.put(archetype, deliverySimulation);
        }
        return returner;
    }

}
