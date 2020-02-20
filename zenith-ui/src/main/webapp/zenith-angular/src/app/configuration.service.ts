import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { SimulatedAnnealingConfiguration } from "./models/SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "./models/GeneticAlgorithmConfiguration";
import {SelectOption} from "./models/SelectOption";

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {
  static readonly OPTIMIZER_NAMES: SelectOption[] = [{
    name: 'SimulatedAnnealingSolutionOptimizer',
    displayName: 'Simulated Annealing'
  }, {
    name: 'GeneticAlgorithmSolutionOptimizer',
    displayName: 'Genetic Algorithm'
  }];

  static readonly POPULATION_NAMES: SelectOption[] = [{
    name: 'StandardPopulation',
    displayName: 'Standard'
  }, {
    name: 'LatticePopulation',
    displayName: 'Lattice'
  }];

  static readonly BREEDER_NAMES: SelectOption[] = [{
    name: 'RandomCipherKeyBreeder',
    displayName: 'Random'
  }, {
    name: 'ProbabilisticCipherKeyBreeder',
    displayName: 'Probabilistic'
  }];

  static readonly CROSSOVER_ALGORITHM_NAMES: SelectOption[] = [{
    name: 'GeneWiseCrossoverAlgorithm',
    displayName: 'Gene-Wise'
  }, {
    name: 'RandomSinglePointCrossoverAlgorithm',
    displayName: 'Random Single-Point'
  }];

  static readonly MUTATION_ALGORITHM_NAMES: SelectOption[] = [{
    name: 'StandardMutationAlgorithm',
    displayName: 'Standard'
  }, {
    name: 'MultipleMutationAlgorithm',
    displayName: 'Multiple'
  }, {
    name: 'MandatorySingleMutationAlgorithm',
    displayName: 'Mandatory Single'
  }];

  static readonly SELECTOR_NAMES: SelectOption[] = [{
    name: 'TournamentSelector',
    displayName: 'Tournament'
  }, {
    name: 'RouletteSelector',
    displayName: 'Roulette'
  }, {
    name: 'RandomSelector',
    displayName: 'Random'
  }];

  static readonly SIMULATED_ANNEALING_DEFAULTS: SimulatedAnnealingConfiguration = {
    samplerIterations: 5000,
    annealingTemperatureMin: 2.75,
    annealingTemperatureMax: 5
  };

  static readonly GENETIC_ALGORITHM_DEFAULTS: GeneticAlgorithmConfiguration = {
    populationSize: 10000,
    numberOfGenerations: 1000,
    elitism: 1,
    populationName: ConfigurationService.POPULATION_NAMES[0].name,
    latticeRows: 100,
    latticeColumns: 100,
    latticeWrapAround: true,
    latticeRadius: 1,
    breederName: ConfigurationService.BREEDER_NAMES[0].name,
    crossoverAlgorithmName: ConfigurationService.CROSSOVER_ALGORITHM_NAMES[0].name,
    mutationAlgorithmName: ConfigurationService.MUTATION_ALGORITHM_NAMES[0].name,
    mutationRate: 0.05,
    maxMutationsPerIndividual: 5,
    selectorName: ConfigurationService.SELECTOR_NAMES[0].name,
    tournamentSelectorAccuracy: 0.75,
    tournamentSize: 5
  };

  private selectedOptimizer = new BehaviorSubject<SelectOption>(ConfigurationService.OPTIMIZER_NAMES[0]);
  private simulatedAnnealingConfiguration$ = new BehaviorSubject<SimulatedAnnealingConfiguration>(ConfigurationService.SIMULATED_ANNEALING_DEFAULTS);
  private geneticAlgorithmConfiguration$ = new BehaviorSubject<GeneticAlgorithmConfiguration>(ConfigurationService.GENETIC_ALGORITHM_DEFAULTS);

  constructor() {}

  getSelectedOptimizerAsObservable(): Observable<SelectOption> {
    return this.selectedOptimizer.asObservable();
  }

  updateSelectedOptimizer(optimizer: SelectOption) {
    this.selectedOptimizer.next(optimizer);
  }

  getSimulatedAnnealingConfigurationAsObservable(): Observable<SimulatedAnnealingConfiguration> {
    return this.simulatedAnnealingConfiguration$.asObservable();
  }

  updateSimulatedAnnealingConfiguration(configuration: SimulatedAnnealingConfiguration) {
    this.simulatedAnnealingConfiguration$.next(configuration);
  }

  getGeneticAlgorithmConfigurationAsObservable(): Observable<GeneticAlgorithmConfiguration> {
    return this.geneticAlgorithmConfiguration$.asObservable();
  }

  updateGeneticAlgorithmConfiguration(configuration: GeneticAlgorithmConfiguration) {
    this.geneticAlgorithmConfiguration$.next(configuration);
  }

  restore() {
    this.updateSelectedOptimizer(ConfigurationService.OPTIMIZER_NAMES[0]);
    this.updateSimulatedAnnealingConfiguration(ConfigurationService.SIMULATED_ANNEALING_DEFAULTS);
    this.updateGeneticAlgorithmConfiguration(ConfigurationService.GENETIC_ALGORITHM_DEFAULTS);
  }
}
