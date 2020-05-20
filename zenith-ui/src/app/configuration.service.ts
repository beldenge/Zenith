/*
 * Copyright 2017-2020 George Belden
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

import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { SimulatedAnnealingConfiguration } from "./models/SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "./models/GeneticAlgorithmConfiguration";
import { SelectOption } from "./models/SelectOption";
import { ApplicationConfiguration } from "./models/ApplicationConfiguration";
import { ZenithTransformer } from "./models/ZenithTransformer";
import { DomSanitizer, SafeUrl } from "@angular/platform-browser";
import { FormGroup } from "@angular/forms";
import { LocalStorageKeys } from "./models/LocalStorageKeys";
import { ZenithFitnessFunction } from "./models/ZenithFitnessFunction";
import { FitnessFunctionService } from "./fitness-function.service";
import { PlaintextTransformerService } from "./plaintext-transformer.service";
import { CiphertextTransformerService } from "./ciphertext-transformer.service";
import { HttpClient } from "@angular/common/http";
import { Cipher } from "./models/Cipher";
import { environment } from "../environments/environment";
import { debounceTime } from "rxjs/operators";

const ENDPOINT_URL = environment.apiUrlBase + '/configurations';

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

  private enableTracking$ = new BehaviorSubject<boolean>(true);
  private enablePageTransitions$ = new BehaviorSubject<boolean>(true);
  private epochs$ = new BehaviorSubject<number>(1);
  private availableCiphertextTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);
  private appliedCiphertextTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);
  private availablePlaintextTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);
  private appliedPlaintextTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);
  private samplePlaintext$ = new BehaviorSubject<string>(ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT);
  private selectedOptimizer$ = new BehaviorSubject<SelectOption>(null);
  private availableFitnessFunctions$ = new BehaviorSubject<ZenithFitnessFunction[]>([]);
  private selectedFitnessFunction$ = new BehaviorSubject<ZenithFitnessFunction>(null);
  private simulatedAnnealingConfiguration$ = new BehaviorSubject<SimulatedAnnealingConfiguration>(null);
  private geneticAlgorithmConfiguration$ = new BehaviorSubject<GeneticAlgorithmConfiguration>(null);
  private ciphers$ = new BehaviorSubject<Cipher[]>([]);
  private selectedCipher$ = new BehaviorSubject<Cipher>(null);
  private configurationLoadedNotification$ = new BehaviorSubject<boolean>(false);

  constructor(private http: HttpClient,
              private sanitizer: DomSanitizer,
              private fitnessFunctionService: FitnessFunctionService,
              private plaintextTransformerService: PlaintextTransformerService,
              private ciphertextTransformerService: CiphertextTransformerService) {
    let fitnessFunctionsPromise = fitnessFunctionService.getFitnessFunctions().then(fitnessFunctionResponse => {
      let availableFitnessFunctions = fitnessFunctionResponse.fitnessFunctions.sort((t1, t2) => {
        return t1.order - t2.order;
      });

      for (let i = 0; i < availableFitnessFunctions.length; i ++) {
        if (availableFitnessFunctions[i].form) {
          availableFitnessFunctions[i].form.form = new FormGroup({});
        }
      }

      this.updateAvailableFitnessFunctions(availableFitnessFunctions);
    });

    let plaintextTransformerPromise = plaintextTransformerService.getTransformers().then(transformerResponse => {
      let availablePlaintextTransformers = transformerResponse.transformers.sort((t1, t2) => {
        return t1.order - t2.order;
      });

      this.updateAvailablePlaintextTransformers(availablePlaintextTransformers);
    });

    let ciphertextTransformerPromise = ciphertextTransformerService.getTransformers().then(transformerResponse => {
      let availableCiphertextTransformers = transformerResponse.transformers.sort((t1, t2) => {
        return t1.order - t2.order;
      });

      this.updateAvailableCiphertextTransformers(availableCiphertextTransformers);
    });

    Promise.all([fitnessFunctionsPromise, plaintextTransformerPromise, ciphertextTransformerPromise]).then(() => {
      if (!localStorage.getItem(LocalStorageKeys.APPLICATION_CONFIGURATION)) {
        this.http.get<ApplicationConfiguration>(ENDPOINT_URL).subscribe(configurationResponse => {
          localStorage.setItem(LocalStorageKeys.APPLICATION_CONFIGURATION, JSON.stringify(configurationResponse));

          this.import(configurationResponse);
        });
      } else {
        this.import(JSON.parse(localStorage.getItem(LocalStorageKeys.APPLICATION_CONFIGURATION)));
      }

      this.configurationLoadedNotification$.next(true);
    });
  }

  getConfigurationLoadedNotification() {
    return this.configurationLoadedNotification$.asObservable();
  }

  getEnableTrackingAsObservable(): Observable<boolean> {
    return this.enableTracking$.asObservable();
  }

  updateEnableTracking(enabled: boolean) {
    localStorage.setItem(LocalStorageKeys.ENABLE_TRACKING, enabled.toString());
    this.enableTracking$.next(enabled);
  }

  getEnablePageTransitionsAsObservable(): Observable<boolean> {
    return this.enablePageTransitions$.asObservable();
  }

  updateEnablePageTransitions(enabled: boolean) {
    localStorage.setItem(LocalStorageKeys.ENABLE_PAGE_TRANSITIONS, enabled.toString());
    this.enablePageTransitions$.next(enabled);
  }

  getSelectedCipherAsObservable(): Observable<Cipher> {
    return this.selectedCipher$.asObservable();
  }

  updateSelectedCipher(selectedCipher: Cipher, skipSave?: boolean) {
    this.selectedCipher$.next(selectedCipher);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getEpochsAsObservable(): Observable<number> {
    return this.epochs$.asObservable();
  }

  updateEpochs(epochs: number, skipSave?: boolean) {
    this.epochs$.next(epochs);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getAvailableCiphertextTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.availableCiphertextTransformers$.asObservable();
  }

  updateAvailableCiphertextTransformers(appliedTransformers: ZenithTransformer[]): void {
    this.availableCiphertextTransformers$.next(appliedTransformers);
  }

  getAppliedCiphertextTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.appliedCiphertextTransformers$.asObservable();
  }

  updateAppliedCiphertextTransformers(appliedTransformers: ZenithTransformer[], skipSave?: boolean): void {
    this.appliedCiphertextTransformers$.next(appliedTransformers);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getAvailablePlaintextTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.availablePlaintextTransformers$.asObservable();
  }

  updateAvailablePlaintextTransformers(appliedTransformers: ZenithTransformer[]): void {
    this.availablePlaintextTransformers$.next(appliedTransformers);
  }

  getAppliedPlaintextTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.appliedPlaintextTransformers$.asObservable();
  }

  updateAppliedPlaintextTransformers(appliedTransformers: ZenithTransformer[], skipSave?: boolean): void {
    this.appliedPlaintextTransformers$.next(appliedTransformers);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getSamplePlaintextAsObservable(): Observable<string> {
    return this.samplePlaintext$.pipe(debounceTime(500));
  }

  updateSamplePlaintext(appliedTransformers: string): void {
    this.samplePlaintext$.next(appliedTransformers);
  }

  getSelectedOptimizerAsObservable(): Observable<SelectOption> {
    return this.selectedOptimizer$.asObservable();
  }

  updateSelectedOptimizer(optimizer: SelectOption, skipSave?: boolean) {
    this.selectedOptimizer$.next(optimizer);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getAvailableFitnessFunctionsAsObservable(): Observable<ZenithFitnessFunction[]> {
    return this.availableFitnessFunctions$.asObservable();
  }

  updateAvailableFitnessFunctions(fitnessFunctions: ZenithFitnessFunction[]) {
    this.availableFitnessFunctions$.next(fitnessFunctions);
  }

  getSelectedFitnessFunctionAsObservable(): Observable<ZenithFitnessFunction> {
    return this.selectedFitnessFunction$.asObservable();
  }

  updateSelectedFitnessFunction(fitnessFunction: ZenithFitnessFunction, skipSave?: boolean) {
    this.selectedFitnessFunction$.next(fitnessFunction);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getSimulatedAnnealingConfigurationAsObservable(): Observable<SimulatedAnnealingConfiguration> {
    return this.simulatedAnnealingConfiguration$.asObservable();
  }

  updateSimulatedAnnealingConfiguration(configuration: SimulatedAnnealingConfiguration, skipSave?: boolean) {
    this.simulatedAnnealingConfiguration$.next(configuration);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getGeneticAlgorithmConfigurationAsObservable(): Observable<GeneticAlgorithmConfiguration> {
    return this.geneticAlgorithmConfiguration$.asObservable();
  }

  updateGeneticAlgorithmConfiguration(configuration: GeneticAlgorithmConfiguration, skipSave?: boolean) {
    this.geneticAlgorithmConfiguration$.next(configuration);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  getCiphersAsObservable(): Observable<Cipher[]> {
    return this.ciphers$.asObservable();
  }

  updateCiphers(ciphers: Cipher[], skipSave?: boolean) {
    this.ciphers$.next(ciphers);

    if (!skipSave) {
      this.saveConfigurationToLocalStorage();
    }
  }

  restoreGeneralSettings() {
    this.http.get<ApplicationConfiguration>(ENDPOINT_URL).subscribe(configurationResponse => {
      this.updateSelectedOptimizer(configurationResponse.selectedOptimizer, true);
      this.updateSelectedFitnessFunction(configurationResponse.selectedFitnessFunction, true);
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
          transformer.form.form = new FormGroup({});
        }
      });
    } else {
      configuration.appliedCiphertextTransformers = [];
    }

    this.updateAppliedCiphertextTransformers(configuration.appliedCiphertextTransformers, true);

    if (configuration.appliedPlaintextTransformers) {
      configuration.appliedPlaintextTransformers.forEach(transformer => {
        if (transformer.form) {
          transformer.form.form = new FormGroup({});
        }
      });
    } else {
      configuration.appliedPlaintextTransformers = [];
    }

    this.updateAppliedPlaintextTransformers(configuration.appliedPlaintextTransformers, true);
    this.updateSelectedOptimizer(configuration.selectedOptimizer, true);

    let selectedFitnessFunction = this.availableFitnessFunctions$.getValue().find(fitnessFunction =>
      fitnessFunction.name === configuration.selectedFitnessFunction.name
    );

    if (selectedFitnessFunction.form) {
      selectedFitnessFunction.form.model = configuration.selectedFitnessFunction.form.model;
      selectedFitnessFunction.form.fields = configuration.selectedFitnessFunction.form.fields;
    }

    this.updateSelectedFitnessFunction(selectedFitnessFunction, true);
    this.updateSimulatedAnnealingConfiguration(configuration.simulatedAnnealingConfiguration, true);
    this.updateGeneticAlgorithmConfiguration(configuration.geneticAlgorithmConfiguration, true);

    // FIXME: This is a hack to handle the fact that we store ciphertext as space-delimited -- it would be cleaner to store it as an array of chars/strings
    configuration.ciphers.forEach(cipher => cipher.ciphertext = cipher.ciphertext.replace(/ /g , ''));

    this.updateCiphers(configuration.ciphers, true);
    this.updateSelectedCipher(configuration.ciphers.find(cipher => cipher.name === configuration.selectedCipher), true);
    this.saveConfigurationToLocalStorage();
  }

  buildConfigurationAsString(): string {
    let configuration = new ApplicationConfiguration();

    configuration.epochs = this.epochs$.getValue();

    if (this.appliedCiphertextTransformers$.getValue()) {
      // The FormGroup attribute is causing cyclic references when serializing to JSON, so we have to manually instantiate the transformers to skip copying the FormGroup
      configuration.appliedCiphertextTransformers = [];

      this.appliedCiphertextTransformers$.getValue().forEach(transformer => {
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

    if (this.appliedPlaintextTransformers$.getValue()) {
      // The FormGroup attribute is causing cyclic references when serializing to JSON, so we have to manually instantiate the transformers to skip copying the FormGroup
      configuration.appliedPlaintextTransformers = [];

      this.appliedPlaintextTransformers$.getValue().forEach(transformer => {
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

    let fitnessFunction = this.selectedFitnessFunction$.getValue();

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

    configuration.selectedOptimizer = this.selectedOptimizer$.getValue();
    configuration.simulatedAnnealingConfiguration = this.simulatedAnnealingConfiguration$.getValue();
    configuration.geneticAlgorithmConfiguration = this.geneticAlgorithmConfiguration$.getValue();
    configuration.ciphers = this.ciphers$.getValue();
    configuration.selectedCipher = this.selectedCipher$.getValue().name;

    return JSON.stringify(configuration, null, 2);
  }

  saveConfigurationToLocalStorage() {
    localStorage.setItem(LocalStorageKeys.APPLICATION_CONFIGURATION, this.buildConfigurationAsString());
  }

  getExportUri(): SafeUrl {
    return this.sanitizer.bypassSecurityTrustUrl('data:text/json;charset=UTF-8,' + encodeURIComponent(this.buildConfigurationAsString()));
  }
}
