/*
 * Copyright 2017-2026 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

import {Injectable, Signal, signal, WritableSignal} from '@angular/core';
import { SimulatedAnnealingConfiguration } from "./models/SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "./models/GeneticAlgorithmConfiguration";
import { SelectOption } from "./models/SelectOption";
import { ApplicationConfiguration } from "./models/ApplicationConfiguration";
import { FormComponent } from "./models/FormComponent";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";
import { UntypedFormGroup} from "@angular/forms";
import { LocalStorageKeys } from "./models/LocalStorageKeys";
import { FitnessFunctionService } from "./fitness-function.service";
import { Cipher } from "./models/Cipher";
import {map} from "rxjs/operators";
import {Apollo, gql} from "apollo-angular";
import { firstValueFrom } from "rxjs";
import {TransformerService} from "./transformer.service";

interface GetCiphersQuery {
  ciphers: Cipher[];
}

@Injectable({
  providedIn: 'root'
})
export class ConfigurationService {
  static readonly DEFAULT_SAMPLE_PLAINTEXT = 'thetomatoisaplantinthenightshadefamilyxxxx';

  static readonly OPTIMIZER_NAMES: SelectOption[] = [{
    name: 'SimulatedAnnealing',
    displayName: 'Simulated Annealing'
  }, {
    name: 'GeneticAlgorithmSolution',
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

  static readonly CROSSOVER_OPERATOR_NAMES: SelectOption[] = [{
    name: 'UniformCrossoverOperator',
    displayName: 'Uniform'
  }, {
    name: 'SinglePointCrossoverOperator',
    displayName: 'Single-Point'
  }];

  static readonly MUTATION_OPERATOR_NAMES: SelectOption[] = [{
    name: 'PointMutationOperator',
    displayName: 'Point'
  }, {
    name: 'MultipleMutationOperator',
    displayName: 'Multiple'
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

  private epochsInternal: WritableSignal<number> = signal(1);
  public epochs: Signal<number> = this.epochsInternal.asReadonly();
  private availableCiphertextTransformersInternal: WritableSignal<FormComponent[]> = signal([]);
  public availableCiphertextTransformers: Signal<FormComponent[]> = this.availableCiphertextTransformersInternal.asReadonly();
  private appliedCiphertextTransformersInternal: WritableSignal<FormComponent[]> = signal([]);
  public appliedCiphertextTransformers: Signal<FormComponent[]> = this.appliedCiphertextTransformersInternal.asReadonly();
  private availablePlaintextTransformersInternal: WritableSignal<FormComponent[]> = signal([]);
  public availablePlaintextTransformers: Signal<FormComponent[]> = this.availablePlaintextTransformersInternal.asReadonly();
  private appliedPlaintextTransformersLocal: WritableSignal<FormComponent[]> = signal([]);
  public appliedPlaintextTransformers: Signal<FormComponent[]> = this.appliedPlaintextTransformersLocal.asReadonly();
  private samplePlaintextInternal: WritableSignal<string> = signal(ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT);
  public samplePlaintext: Signal<string> = this.samplePlaintextInternal.asReadonly();
  private selectedOptimizerInternal: WritableSignal<SelectOption> = signal(null);
  public selectedOptimizer: Signal<SelectOption> = this.selectedOptimizerInternal.asReadonly();
  private availableFitnessFunctionsInternal: WritableSignal<FormComponent[]> = signal([]);
  public availableFitnessFunctions: Signal<FormComponent[]> = this.availableFitnessFunctionsInternal.asReadonly();
  private selectedFitnessFunctionInternal: WritableSignal<FormComponent> = signal(null);
  public selectedFitnessFunction: Signal<FormComponent> = this.selectedFitnessFunctionInternal.asReadonly();
  private simulatedAnnealingConfigurationInternal: WritableSignal<SimulatedAnnealingConfiguration> = signal(null);
  public simulatedAnnealingConfiguration = this.simulatedAnnealingConfigurationInternal.asReadonly();
  private geneticAlgorithmConfigurationInternal: WritableSignal<GeneticAlgorithmConfiguration> = signal(null);
  public geneticAlgorithmConfiguration: Signal<GeneticAlgorithmConfiguration> = this.geneticAlgorithmConfigurationInternal.asReadonly();
  private ciphersInternal: WritableSignal<Cipher[]> = signal([]);
  public ciphers: Signal<Cipher[]> = this.ciphersInternal.asReadonly();
  private selectedCipherInternal: WritableSignal<Cipher> = signal(null);
  public selectedCipher: Signal<Cipher> = this.selectedCipherInternal.asReadonly();

  constructor(private sanitizer: DomSanitizer,
              private fitnessFunctionService: FitnessFunctionService,
              private apollo: Apollo,
              private transformerService: TransformerService) {
    this.loadInitialData();
  }

  private loadConfiguration() {
    return this.apollo.query<ApplicationConfiguration>({ query: gql`
        query configuration {
          configuration {
            selectedCipher
            epochs
            appliedCiphertextTransformers {
              name
              displayName
              form {
                model,
                fields {
                  key,
                  type,
                  props {
                    label,
                    placeholder,
                    required,
                    type,
                    rows,
                    cols,
                    max,
                    min,
                    maxLength,
                    minLength,
                    pattern
                  }
                  defaultValue
                }
              }
              order
              helpText
            }
            appliedPlaintextTransformers {
              name
              displayName
              form {
                model,
                fields {
                  key,
                  type,
                  props {
                    label,
                    placeholder,
                    required,
                    type,
                    rows,
                    cols,
                    max,
                    min,
                    maxLength,
                    minLength,
                    pattern
                  }
                  defaultValue
                }
              }
              order
              helpText
            }
            selectedOptimizer {
              name
              displayName
            }
            selectedFitnessFunction {
              name
              displayName
              form {
                model,
                fields {
                  key,
                  type,
                  props {
                    label,
                    placeholder,
                    required,
                    type,
                    rows,
                    cols,
                    max,
                    min,
                    maxLength,
                    minLength,
                    pattern
                  }
                  defaultValue
                }
              }
              order
              helpText
            }
            simulatedAnnealingConfiguration {
              samplerIterations
              annealingTemperatureMin
              annealingTemperatureMax
            }
            geneticAlgorithmConfiguration {
              populationSize
              numberOfGenerations
              elitism
              populationName
              latticeRows
              latticeColumns
              latticeWrapAround
              latticeRadius
              breederName
              crossoverOperatorName
              mutationOperatorName
              mutationRate
              maxMutationsPerIndividual
              selectorName
              tournamentSelectorAccuracy
              tournamentSize
              minPopulations
              speciationEvents
              speciationFactor
              extinctionCycles
            }
            ciphers {
              name
              rows
              columns
              readOnly
              ciphertext
              knownSolutionKey
            }
          }
        }
      `
    }).pipe(map((response: any) => response.data.configuration));
  }

  private async loadInitialData() {
    const [fitnessFunctionResponse, plaintextTransformerResponse, ciphertextTransformerResponse, ciphersData] = await Promise.all([
      this.fitnessFunctionService.getFitnessFunctions(),
      this.transformerService.getPlaintextTransformers(),
      this.transformerService.getCiphertextTransformers(),
      firstValueFrom(this.apollo.query<GetCiphersQuery>({ query: gql`
        query GetCiphers {
          ciphers {
            name,
            rows,
            columns,
            readOnly,
            ciphertext,
            knownSolutionKey
          }
        }
      `
      }).pipe(map((response: any) => response.data.ciphers)))
    ]);

    this.updateCiphers(ciphersData, true);

    const availableFitnessFunctions = [...fitnessFunctionResponse].sort((t1, t2) => {
      return t1.order - t2.order;
    });

    this.availableFitnessFunctionsInternal.update(() => availableFitnessFunctions);

    const availablePlaintextTransformers = [...plaintextTransformerResponse].sort((t1, t2) => {
      return t1.order - t2.order;
    });

    this.availablePlaintextTransformersInternal.update(() => availablePlaintextTransformers);

    const availableCiphertextTransformers = [...ciphertextTransformerResponse].sort((t1, t2) => {
      return t1.order - t2.order;
    });

    this.availableCiphertextTransformersInternal.update(() => availableCiphertextTransformers);

    if (!localStorage.getItem(LocalStorageKeys.APPLICATION_CONFIGURATION)) {
      this.loadConfiguration().subscribe(configurationResponse => {
        localStorage.setItem(LocalStorageKeys.APPLICATION_CONFIGURATION, JSON.stringify(configurationResponse));

        this.import({...configurationResponse});
      });
    } else {
      this.import(JSON.parse(localStorage.getItem(LocalStorageKeys.APPLICATION_CONFIGURATION)));
    }
  }

  updateSelectedCipher(selectedCipher: Cipher, skipSave?: boolean) {
    this.selectedCipherInternal.update(() => selectedCipher);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateEpochs(epochs: number, skipSave?: boolean) {
    this.epochsInternal.update(() => epochs);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateAppliedCiphertextTransformers(appliedTransformers: FormComponent[], skipSave?: boolean): void {
    this.appliedCiphertextTransformersInternal.update(() => appliedTransformers);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateAppliedPlaintextTransformers(appliedTransformers: FormComponent[], skipSave?: boolean): void {
    this.appliedPlaintextTransformersLocal.update(() => appliedTransformers);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateSamplePlaintext(sample: string): void {
    this.samplePlaintextInternal.update(() => sample);
  }

  updateSelectedOptimizer(optimizer: SelectOption, skipSave?: boolean) {
    this.selectedOptimizerInternal.update(() => optimizer);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateSelectedFitnessFunction(fitnessFunction: FormComponent, skipSave?: boolean) {
    this.selectedFitnessFunctionInternal.update(() => fitnessFunction);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateSimulatedAnnealingConfiguration(configuration: SimulatedAnnealingConfiguration, skipSave?: boolean) {
    this.simulatedAnnealingConfigurationInternal.update(() => configuration);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateGeneticAlgorithmConfiguration(configuration: GeneticAlgorithmConfiguration, skipSave?: boolean) {
    this.geneticAlgorithmConfigurationInternal.update(() => configuration);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  updateCiphers(ciphers: Cipher[], skipSave?: boolean) {
    if (this.selectedCipherInternal() && !ciphers.find(cipher => cipher.name === this.selectedCipherInternal().name)) {
      // If the selected cipher has been deleted, pick a different one
      this.updateSelectedCipher(ciphers[0]);
    }

    this.ciphersInternal.update(() => ciphers);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  restoreGeneralSettings() {
    this.loadConfiguration().subscribe(configurationResponse => {
      this.updateSelectedOptimizer(configurationResponse.selectedOptimizer, true);
      this.updateSelectedFitnessFunction(this.availableFitnessFunctions().find(fitnessFunction => fitnessFunction.name === configurationResponse.selectedFitnessFunction.name), true);
      this.updateSimulatedAnnealingConfiguration(configurationResponse.simulatedAnnealingConfiguration, true);
      this.updateGeneticAlgorithmConfiguration(configurationResponse.geneticAlgorithmConfiguration, true);
      this.saveConfigurationToLocalStorage();
    });
  }

  import(configuration: ApplicationConfiguration) {
    this.updateEpochs(configuration.epochs, true);

    if (configuration.appliedCiphertextTransformers) {
      configuration.appliedCiphertextTransformers.forEach(transformer => {
        if (transformer.form) {
          transformer.form.form = new UntypedFormGroup({});
        }
      });
    } else {
      configuration.appliedCiphertextTransformers = [];
    }

    this.updateAppliedCiphertextTransformers(configuration.appliedCiphertextTransformers, true);

    if (configuration.appliedPlaintextTransformers) {
      configuration.appliedPlaintextTransformers.forEach(transformer => {
        if (transformer.form) {
          transformer.form.form = new UntypedFormGroup({});
        }
      });
    } else {
      configuration.appliedPlaintextTransformers = [];
    }

    this.updateAppliedPlaintextTransformers(configuration.appliedPlaintextTransformers, true);
    this.updateSelectedOptimizer(configuration.selectedOptimizer, true);

    const selectedFitnessFunctionMatch = this.availableFitnessFunctions().find(fitnessFunction =>
      fitnessFunction.name === configuration.selectedFitnessFunction.name
    );

    if (selectedFitnessFunctionMatch.form) {
      selectedFitnessFunctionMatch.form.model = configuration.selectedFitnessFunction.form.model;
      selectedFitnessFunctionMatch.form.fields = configuration.selectedFitnessFunction.form.fields;
    }

    this.updateSelectedFitnessFunction(selectedFitnessFunctionMatch, true);
    this.updateSimulatedAnnealingConfiguration(configuration.simulatedAnnealingConfiguration, true);
    this.updateGeneticAlgorithmConfiguration(configuration.geneticAlgorithmConfiguration, true);
    this.updateSelectedCipher(this.ciphersInternal().find(cipher => cipher.name === configuration.selectedCipher), true);
    this.saveConfigurationToLocalStorage();
  }

  buildConfigurationAsString(): string {
    const configuration = new ApplicationConfiguration();

    configuration.epochs = this.epochsInternal();

    if (this.appliedCiphertextTransformersInternal()) {
      // The FormGroup attribute is causing cyclic references when serializing to JSON,
      // so we have to manually instantiate the transformers to skip copying the FormGroup
      configuration.appliedCiphertextTransformers = [];

      this.appliedCiphertextTransformersInternal().forEach(transformer => {
        configuration.appliedCiphertextTransformers.push({
          name: transformer.name,
          displayName: transformer.displayName,
          form: transformer.form ? {
            model: transformer.form.model,
            fields: transformer.form.fields
          } : null,
          order: transformer.order,
          helpText: transformer.helpText
        });
      });
    }

    if (this.appliedPlaintextTransformersLocal()) {
      // The FormGroup attribute is causing cyclic references when serializing to JSON,
      // so we have to manually instantiate the transformers to skip copying the FormGroup
      configuration.appliedPlaintextTransformers = [];

      this.appliedPlaintextTransformersLocal().forEach(transformer => {
        configuration.appliedPlaintextTransformers.push({
          name: transformer.name,
          displayName: transformer.displayName,
          form: transformer.form ? {
            model: transformer.form.model,
            fields: transformer.form.fields
          } : null,
          order: transformer.order,
          helpText: transformer.helpText
        });
      });
    }

    const fitnessFunction = this.selectedFitnessFunctionInternal();

    configuration.selectedFitnessFunction = {
      name: fitnessFunction.name,
      displayName: fitnessFunction.displayName,
      form: fitnessFunction.form ? {
        model: fitnessFunction.form.model,
        fields: fitnessFunction.form.fields
      } : null,
      order: fitnessFunction.order,
      helpText: fitnessFunction.helpText
    };

    configuration.selectedOptimizer = this.selectedOptimizerInternal();
    configuration.simulatedAnnealingConfiguration = this.simulatedAnnealingConfigurationInternal();
    configuration.geneticAlgorithmConfiguration = this.geneticAlgorithmConfigurationInternal();
    configuration.selectedCipher = this.selectedCipherInternal().name;

    return JSON.stringify(configuration, null, 2);
  }

  saveConfigurationToLocalStorage() {
    localStorage.setItem(LocalStorageKeys.APPLICATION_CONFIGURATION, this.buildConfigurationAsString());
  }

  getExportUri(): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl('data:text/json;charset=UTF-8,' + encodeURIComponent(this.buildConfigurationAsString()));
  }
}
