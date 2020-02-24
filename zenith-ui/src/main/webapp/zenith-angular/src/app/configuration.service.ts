import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { SimulatedAnnealingConfiguration } from "./models/SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "./models/GeneticAlgorithmConfiguration";
import { SelectOption } from "./models/SelectOption";
import { ApplicationConfiguration } from "./models/ApplicationConfiguration";
import { ZenithTransformer } from "./models/ZenithTransformer";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";
import {FormGroup} from "@angular/forms";

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

  private epochs$ = new BehaviorSubject<number>(1);
  private appliedCiphertextTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);
  private appliedPlaintextTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);
  private selectedOptimizer$ = new BehaviorSubject<SelectOption>(ConfigurationService.OPTIMIZER_NAMES[0]);
  private simulatedAnnealingConfiguration$ = new BehaviorSubject<SimulatedAnnealingConfiguration>(ConfigurationService.SIMULATED_ANNEALING_DEFAULTS);
  private geneticAlgorithmConfiguration$ = new BehaviorSubject<GeneticAlgorithmConfiguration>(ConfigurationService.GENETIC_ALGORITHM_DEFAULTS);

  constructor(private sanitizer: DomSanitizer) {}

  getEpochsAsObservable(): Observable<number> {
    return this.epochs$.asObservable();
  }

  updateEpochs(epochs: number) {
    this.epochs$.next(epochs);
  }

  getAppliedCiphertextTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.appliedCiphertextTransformers$.asObservable();
  }

  updateAppliedCiphertextTransformers(appliedTransformers: ZenithTransformer[]): void {
    return this.appliedCiphertextTransformers$.next(appliedTransformers);
  }

  getAppliedPlaintextTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.appliedPlaintextTransformers$.asObservable();
  }

  updateAppliedPlaintextTransformers(appliedTransformers: ZenithTransformer[]): void {
    return this.appliedPlaintextTransformers$.next(appliedTransformers);
  }

  getSelectedOptimizerAsObservable(): Observable<SelectOption> {
    return this.selectedOptimizer$.asObservable();
  }

  updateSelectedOptimizer(optimizer: SelectOption) {
    this.selectedOptimizer$.next(optimizer);
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

  restoreGeneralSettings() {
    this.updateSelectedOptimizer(ConfigurationService.OPTIMIZER_NAMES[0]);
    this.updateSimulatedAnnealingConfiguration(ConfigurationService.SIMULATED_ANNEALING_DEFAULTS);
    this.updateGeneticAlgorithmConfiguration(ConfigurationService.GENETIC_ALGORITHM_DEFAULTS);
  }

  import(configuration: ApplicationConfiguration) {
    this.updateEpochs(configuration.epochs);

    if (configuration.appliedCiphertextTransformers) {
      configuration.appliedCiphertextTransformers.forEach(transformer => {
        transformer.form.form = new FormGroup({});
      });
    }

    this.updateAppliedCiphertextTransformers(configuration.appliedCiphertextTransformers);

    if (configuration.appliedPlaintextTransformers) {
      configuration.appliedPlaintextTransformers.forEach(transformer => {
        transformer.form.form = new FormGroup({});
      });
    }

    this.updateAppliedPlaintextTransformers(configuration.appliedPlaintextTransformers);
    this.updateSelectedOptimizer(configuration.selectedOptimizer);
    this.updateSimulatedAnnealingConfiguration(configuration.simulatedAnnealingConfiguration);
    this.updateGeneticAlgorithmConfiguration(configuration.geneticAlgorithmConfiguration);
  }

  getExportUri(): SafeUrl {
    let configuration = new ApplicationConfiguration();

    configuration.epochs = this.epochs$.getValue();

    if (this.appliedCiphertextTransformers$.getValue()) {
      // The FormGroup attribute is causing cyclic references when serializing to JSON, so we have to manually instantiate the transformers to skip copying the FormGroup
      configuration.appliedCiphertextTransformers = [];

      this.appliedCiphertextTransformers$.getValue().forEach(transformer => {
        configuration.appliedCiphertextTransformers.push({
          name: transformer.name,
          displayName: transformer.displayName,
          form: {
            model: transformer.form.model,
            fields: transformer.form.fields
          },
          order: transformer.order
        });
      });
    }

    if (this.appliedPlaintextTransformers$.getValue()) {
      // The FormGroup attribute is causing cyclic references when serializing to JSON, so we have to manually instantiate the transformers to skip copying the FormGroup
      configuration.appliedPlaintextTransformers = [];

      this.appliedPlaintextTransformers$.getValue().forEach(transformer => {
        configuration.appliedPlaintextTransformers.push({
          name: transformer.name,
          displayName: transformer.displayName,
          form: {
            model: transformer.form.model,
            fields: transformer.form.fields
          },
          order: transformer.order
        });
      });
    }

    configuration.selectedOptimizer = this.selectedOptimizer$.getValue();
    configuration.simulatedAnnealingConfiguration = this.simulatedAnnealingConfiguration$.getValue();
    configuration.geneticAlgorithmConfiguration = this.geneticAlgorithmConfiguration$.getValue();

    let configAsPrettyString = JSON.stringify(configuration, null, 2);
    return this.sanitizer.bypassSecurityTrustUrl('data:text/json;charset=UTF-8,' + encodeURIComponent(configAsPrettyString));
  }
}
