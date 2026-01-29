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

import {Component, effect, OnDestroy} from '@angular/core';
import { UntypedFormBuilder } from '@angular/forms';
import { Validators } from '@angular/forms';
import { ConfigurationService } from "../configuration.service";
import { SelectOption } from "../models/SelectOption";
import { SimulatedAnnealingConfiguration } from "../models/SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "../models/GeneticAlgorithmConfiguration";
import {Subscription} from "rxjs";
import { IntroductionService } from "../introduction.service";

const INTEGER_PATTERN = '^[0-9]+$';
const DECIMAL_PATTERN = '^[0-9]+(.[0-9]+)?$';

@Component({
    selector: 'app-settings',
    templateUrl: './settings.component.html',
    styleUrls: ['./settings.component.css'],
    standalone: false
})
export class SettingsComponent implements OnDestroy {
  showIntro = this.introductionService.showIntroSettings;
  generalSettingsFormValueChangesSubscription: Subscription;
  optimizer = this.configurationService.selectedOptimizer;
  optimizerNames: SelectOption[] = ConfigurationService.OPTIMIZER_NAMES;
  availableFitnessFunctions = this.configurationService.availableFitnessFunctions;
  populationNames: SelectOption[] = ConfigurationService.POPULATION_NAMES;
  breederNames: SelectOption[] = ConfigurationService.BREEDER_NAMES;
  crossoverOperatorNames: SelectOption[] = ConfigurationService.CROSSOVER_OPERATOR_NAMES;
  mutationOperatorNames: SelectOption[] = ConfigurationService.MUTATION_OPERATOR_NAMES;
  selectorNames: SelectOption[] = ConfigurationService.SELECTOR_NAMES;
  selectedFitnessFunction = this.configurationService.selectedFitnessFunction;
  samplerIterationsValidators = [Validators.min(1), Validators.max(100000)];
  samplerIterationsValidationMessage = 'Must be a number between 1 and 100000';

  simulatedAnnealingFormGroup = this.fb.group({
    samplerIterations: [null, this.samplerIterationsValidators],
    annealingTemperatureMin: [null, [Validators.pattern(DECIMAL_PATTERN)]],
    annealingTemperatureMax: [null, [Validators.pattern(DECIMAL_PATTERN)]],
  });

  geneticAlgorithmFormGroup = this.fb.group({
    populationSize: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    numberOfGenerations: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    elitism: [null, [Validators.min(0), Validators.pattern(INTEGER_PATTERN)]],
    populationName: [null],
    latticeRows: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    latticeColumns: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    latticeWrapAround: [null],
    latticeRadius: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    breederName: [null],
    crossoverOperatorName: [null],
    mutationOperatorName: [null],
    mutationRate: [null, [Validators.min(0.0), Validators.max(1.0), Validators.pattern(DECIMAL_PATTERN)]],
    maxMutationsPerIndividual: [null, [Validators.min(0), Validators.pattern(INTEGER_PATTERN)]],
    selectorName: [null],
    tournamentSelectorAccuracy: [null, [Validators.min(0.0), Validators.max(1.0), Validators.pattern(DECIMAL_PATTERN)]],
    tournamentSize: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    minPopulations: [null, [Validators.min(0), Validators.pattern(INTEGER_PATTERN)]],
    speciationEvents: [null, [Validators.min(0), Validators.pattern(INTEGER_PATTERN)]],
    speciationFactor: [null, [Validators.min(0), Validators.pattern(INTEGER_PATTERN)]],
    extinctionCycles: [null, [Validators.min(0), Validators.pattern(INTEGER_PATTERN)]]
  });

  generalSettingsForm = this.fb.group({
    optimizer: [this.optimizerNames[0], [Validators.required]],
    fitnessFunction: [null, [Validators.required]],
    simulatedAnnealingConfiguration: this.simulatedAnnealingFormGroup,
    geneticAlgorithmConfiguration: this.geneticAlgorithmFormGroup
  });

  compareByName = (o1: SelectOption | null, o2: SelectOption | null): boolean => {
    if (o1 == null || o2 == null) {
      return o1 === o2;
    }

    return o1.name === o2.name;
  }

  constructor(private fb: UntypedFormBuilder,
              private configurationService: ConfigurationService,
              private introductionService: IntroductionService) {
    effect(() => {
      if (this.showIntro()) {
        setTimeout(() => {
          this.introductionService.startIntroSettings();
          this.introductionService.updateShowIntroSettings(false);
        }, 0);
      }
    });

    effect(() => {
      if (!this.optimizer()) {
        return;
      }

      if (this.generalSettingsForm.get('optimizer').value !== this.optimizer()) {
        const optimizerToUse = ConfigurationService.OPTIMIZER_NAMES.find(name => name.name === this.optimizer().name);
        this.generalSettingsForm.patchValue({ optimizer: optimizerToUse }, { emitEvent: false });
      }
    });

    effect(() => {
      this.generalSettingsForm.patchValue({ fitnessFunction: this.selectedFitnessFunction() }, { emitEvent: false });
    });

    effect(() => {
      const simulatedAnnealingConfiguration = this.configurationService.simulatedAnnealingConfiguration();
      if (!simulatedAnnealingConfiguration) {
        return;
      }

      const patch = {
        samplerIterations: simulatedAnnealingConfiguration.samplerIterations,
        annealingTemperatureMin: simulatedAnnealingConfiguration.annealingTemperatureMin,
        annealingTemperatureMax: simulatedAnnealingConfiguration.annealingTemperatureMax,
      };

      if (JSON.stringify(this.simulatedAnnealingFormGroup.value) !== JSON.stringify(patch)) {
        this.simulatedAnnealingFormGroup.patchValue(patch, { emitEvent: false });
        this.generalSettingsForm.patchValue({ simulatedAnnealingConfiguration: this.simulatedAnnealingFormGroup }, { emitEvent: false });
      }
    });

    effect(() => {
      const geneticAlgorithmConfiguration = this.configurationService.geneticAlgorithmConfiguration();
      if (!geneticAlgorithmConfiguration) {
        return;
      }

      const patch = {
        populationSize: geneticAlgorithmConfiguration.populationSize,
        numberOfGenerations: geneticAlgorithmConfiguration.numberOfGenerations,
        elitism: geneticAlgorithmConfiguration.elitism,
        populationName: { name: geneticAlgorithmConfiguration.populationName },
        latticeRows: geneticAlgorithmConfiguration.latticeRows,
        latticeColumns: geneticAlgorithmConfiguration.latticeColumns,
        latticeWrapAround: geneticAlgorithmConfiguration.latticeWrapAround,
        latticeRadius: geneticAlgorithmConfiguration.latticeRadius,
        breederName: { name: geneticAlgorithmConfiguration.breederName },
        crossoverOperatorName: { name: geneticAlgorithmConfiguration.crossoverOperatorName },
        mutationOperatorName: { name: geneticAlgorithmConfiguration.mutationOperatorName },
        mutationRate: geneticAlgorithmConfiguration.mutationRate,
        maxMutationsPerIndividual: geneticAlgorithmConfiguration.maxMutationsPerIndividual,
        selectorName: { name: geneticAlgorithmConfiguration.selectorName },
        tournamentSelectorAccuracy: geneticAlgorithmConfiguration.tournamentSelectorAccuracy,
        tournamentSize: geneticAlgorithmConfiguration.tournamentSize,
        minPopulations: geneticAlgorithmConfiguration.minPopulations,
        speciationEvents: geneticAlgorithmConfiguration.speciationEvents,
        speciationFactor: geneticAlgorithmConfiguration.speciationFactor,
        extinctionCycles: geneticAlgorithmConfiguration.extinctionCycles
      };

      if (JSON.stringify(this.geneticAlgorithmFormGroup.value) !== JSON.stringify(patch)) {
        this.geneticAlgorithmFormGroup.patchValue(patch, { emitEvent: false });
        this.generalSettingsForm.patchValue({ geneticAlgorithmConfiguration: this.geneticAlgorithmFormGroup }, { emitEvent: false });
      }
    });

    this.onFormChange();
  }

  ngOnDestroy() {
    this.generalSettingsFormValueChangesSubscription?.unsubscribe();
  }

  // For some reason this doesn't even get called if we don't specify the event parameter
  onFitnessFunctionChange(event) {
    this.configurationService.updateSelectedFitnessFunction(this.generalSettingsForm.get('fitnessFunction').value);
  }

  onFormChange() {
    this.generalSettingsFormValueChangesSubscription = this.generalSettingsForm.valueChanges.subscribe(val => {
      if (!this.generalSettingsForm.valid) {
        return;
      }

      this.configurationService.updateSelectedOptimizer(this.generalSettingsForm.get('optimizer').value);
      this.configurationService.updateSelectedFitnessFunction(this.generalSettingsForm.get('fitnessFunction').value);

      if (this.generalSettingsForm.get('optimizer').value === this.optimizerNames[0]) {
        const configuration: SimulatedAnnealingConfiguration = {
          samplerIterations: this.simulatedAnnealingFormGroup.get('samplerIterations').value,
          annealingTemperatureMin: this.simulatedAnnealingFormGroup.get('annealingTemperatureMin').value,
          annealingTemperatureMax: this.simulatedAnnealingFormGroup.get('annealingTemperatureMax').value
        };

        this.configurationService.updateSimulatedAnnealingConfiguration(configuration);
      } else {
        const configuration: GeneticAlgorithmConfiguration = {
          populationSize: this.geneticAlgorithmFormGroup.get('populationSize').value,
          numberOfGenerations: this.geneticAlgorithmFormGroup.get('numberOfGenerations').value,
          elitism: this.geneticAlgorithmFormGroup.get('elitism').value,
          populationName: this.geneticAlgorithmFormGroup.get('populationName').value.name,
          latticeRows: this.geneticAlgorithmFormGroup.get('latticeRows').value,
          latticeColumns: this.geneticAlgorithmFormGroup.get('latticeColumns').value,
          latticeWrapAround: this.geneticAlgorithmFormGroup.get('latticeWrapAround').value,
          latticeRadius: this.geneticAlgorithmFormGroup.get('latticeRadius').value,
          breederName: this.geneticAlgorithmFormGroup.get('breederName').value.name,
          crossoverOperatorName: this.geneticAlgorithmFormGroup.get('crossoverOperatorName').value.name,
          mutationOperatorName: this.geneticAlgorithmFormGroup.get('mutationOperatorName').value.name,
          mutationRate: this.geneticAlgorithmFormGroup.get('mutationRate').value,
          maxMutationsPerIndividual: this.geneticAlgorithmFormGroup.get('maxMutationsPerIndividual').value,
          selectorName: this.geneticAlgorithmFormGroup.get('selectorName').value.name,
          tournamentSelectorAccuracy: this.geneticAlgorithmFormGroup.get('tournamentSelectorAccuracy').value,
          tournamentSize: this.geneticAlgorithmFormGroup.get('tournamentSize').value,
          minPopulations: this.geneticAlgorithmFormGroup.get('minPopulations').value,
          speciationEvents: this.geneticAlgorithmFormGroup.get('speciationEvents').value,
          speciationFactor: this.geneticAlgorithmFormGroup.get('speciationFactor').value,
          extinctionCycles: this.geneticAlgorithmFormGroup.get('extinctionCycles').value
        };

        this.configurationService.updateGeneticAlgorithmConfiguration(configuration);
      }
    });
  }

  restore() {
    this.configurationService.restoreGeneralSettings();
  }
}
