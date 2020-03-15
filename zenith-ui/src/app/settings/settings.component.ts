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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { JsonPipe } from '@angular/common';
import { Validators } from '@angular/forms';
import { ConfigurationService } from "../configuration.service";
import { SelectOption } from "../models/SelectOption";
import { SimulatedAnnealingConfiguration } from "../models/SimulatedAnnealingConfiguration";
import { GeneticAlgorithmConfiguration } from "../models/GeneticAlgorithmConfiguration";
import { Subscription } from "rxjs";
import { IntroductionService } from "../introduction.service";

const INTEGER_PATTERN: string = "^[0-9]+$";
const DECIMAL_PATTERN: string = "^[0-9]+(.[0-9]+)?$";

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit, OnDestroy {
  showIntroSettingsSubscription: Subscription;
  selectedOptimizerSubscription: Subscription;
  simulatedAnnealingConfigurationSubscription: Subscription;
  geneticAlgorithmConfigurationSubscription: Subscription;
  generalSettingsFormValueChangesSubscription: Subscription;
  optimizerNames: SelectOption[] = ConfigurationService.OPTIMIZER_NAMES;
  populationNames: SelectOption[] = ConfigurationService.POPULATION_NAMES;
  breederNames: SelectOption[] = ConfigurationService.BREEDER_NAMES;
  crossoverAlgorithmNames: SelectOption[] = ConfigurationService.CROSSOVER_ALGORITHM_NAMES;
  mutationAlgorithmNames: SelectOption[] = ConfigurationService.MUTATION_ALGORITHM_NAMES;
  selectorNames: SelectOption[] = ConfigurationService.SELECTOR_NAMES;
  simulatedAnnealingFormGroup = this.fb.group({
    samplerIterations: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    annealingTemperatureMin: [null, [Validators.pattern(DECIMAL_PATTERN)]],
    annealingTemperatureMax: [null, [Validators.pattern(DECIMAL_PATTERN)]],
  });

  geneticAlgorithmFormGroup = this.fb.group({
    populationSize: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    numberOfGenerations: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    elitism: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    populationName: [null],
    latticeRows: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    latticeColumns: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    latticeWrapAround: [null],
    latticeRadius: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]],
    breederName: [null],
    crossoverAlgorithmName: [null],
    mutationAlgorithmName: [null],
    mutationRate: [null, [Validators.min(0.0), Validators.max(1.0), Validators.pattern(DECIMAL_PATTERN)]],
    maxMutationsPerIndividual: [null, [Validators.min(0), Validators.pattern(INTEGER_PATTERN)]],
    selectorName: [null],
    tournamentSelectorAccuracy: [null, [Validators.min(0.0), Validators.max(1.0), Validators.pattern(DECIMAL_PATTERN)]],
    tournamentSize: [null, [Validators.min(1), Validators.pattern(INTEGER_PATTERN)]]
  });

  generalSettingsForm = this.fb.group({
    optimizer: [this.optimizerNames[0], [Validators.required]],
    simulatedAnnealingConfiguration: this.simulatedAnnealingFormGroup,
    geneticAlgorithmConfiguration: this.geneticAlgorithmFormGroup
  });

  constructor(private fb: FormBuilder, private json: JsonPipe, private configurationService: ConfigurationService, private introductionService: IntroductionService) { }

  ngOnInit() {
    this.simulatedAnnealingConfigurationSubscription = this.configurationService.getSimulatedAnnealingConfigurationAsObservable().subscribe(configuration => {
      let patch = {
        samplerIterations: configuration.samplerIterations,
        annealingTemperatureMin: configuration.annealingTemperatureMin,
        annealingTemperatureMax: configuration.annealingTemperatureMax,
      };

      if (JSON.stringify(this.simulatedAnnealingFormGroup.value) !== JSON.stringify(patch)) {
        this.simulatedAnnealingFormGroup.patchValue(patch);
        this.generalSettingsForm.patchValue({ simulatedAnnealingConfiguration: this.simulatedAnnealingFormGroup });
      }
    });

    this.geneticAlgorithmConfigurationSubscription = this.configurationService.getGeneticAlgorithmConfigurationAsObservable().subscribe(configuration => {
      let patch = {
        populationSize: configuration.populationSize,
        numberOfGenerations: configuration.numberOfGenerations,
        elitism: configuration.elitism,
        populationName: this.populationNames.find(name => name.name === configuration.populationName),
        latticeRows: configuration.latticeRows,
        latticeColumns: configuration.latticeColumns,
        latticeWrapAround: configuration.latticeWrapAround,
        latticeRadius: configuration.latticeRadius,
        breederName: this.breederNames.find(name => name.name === configuration.breederName),
        crossoverAlgorithmName: this.crossoverAlgorithmNames.find(name => name.name === configuration.crossoverAlgorithmName),
        mutationAlgorithmName: this.mutationAlgorithmNames.find(name => name.name === configuration.mutationAlgorithmName),
        mutationRate: configuration.mutationRate,
        maxMutationsPerIndividual: configuration.maxMutationsPerIndividual,
        selectorName: this.selectorNames.find(name => name.name === configuration.selectorName),
        tournamentSelectorAccuracy: configuration.tournamentSelectorAccuracy,
        tournamentSize: configuration.tournamentSize
      };

      if (JSON.stringify(this.geneticAlgorithmFormGroup.value) !== JSON.stringify(patch)) {
        this.geneticAlgorithmFormGroup.patchValue(patch);
        this.generalSettingsForm.patchValue({geneticAlgorithmConfiguration: this.geneticAlgorithmFormGroup});
      }
    });

    this.selectedOptimizerSubscription = this.configurationService.getSelectedOptimizerAsObservable().subscribe(optimizer => {
      if (this.generalSettingsForm.get('optimizer').value !== optimizer) {
        let optimizerToUse = ConfigurationService.OPTIMIZER_NAMES.find(name => name.name === optimizer.name);
        this.generalSettingsForm.patchValue({ optimizer: optimizerToUse });
      }
    });

    this.onFormChange();

    this.showIntroSettingsSubscription = this.introductionService.getShowIntroSettingsAsObservable().subscribe(showIntro => {
      if (showIntro) {
        setTimeout(() => {
          this.introductionService.startIntroSettings();
          this.introductionService.updateShowIntroSettings(false);
        }, 500);
      }
    });
  }

  ngOnDestroy() {
    this.showIntroSettingsSubscription.unsubscribe();
    this.simulatedAnnealingConfigurationSubscription.unsubscribe();
    this.geneticAlgorithmConfigurationSubscription.unsubscribe();
    this.selectedOptimizerSubscription.unsubscribe();
    this.generalSettingsFormValueChangesSubscription.unsubscribe();
  }

  onFormChange() {
    this.generalSettingsFormValueChangesSubscription = this.generalSettingsForm.valueChanges.subscribe(val => {
      this.configurationService.updateSelectedOptimizer(this.generalSettingsForm.get('optimizer').value);

      if (this.generalSettingsForm.get('optimizer').value === this.optimizerNames[0]) {
        let configuration: SimulatedAnnealingConfiguration = {
          samplerIterations: this.simulatedAnnealingFormGroup.get('samplerIterations').value,
          annealingTemperatureMin: this.simulatedAnnealingFormGroup.get('annealingTemperatureMin').value,
          annealingTemperatureMax: this.simulatedAnnealingFormGroup.get('annealingTemperatureMax').value
        };

        this.configurationService.updateSimulatedAnnealingConfiguration(configuration);
      } else {
        let configuration: GeneticAlgorithmConfiguration = {
          populationSize: this.geneticAlgorithmFormGroup.get('populationSize').value,
          numberOfGenerations: this.geneticAlgorithmFormGroup.get('numberOfGenerations').value,
          elitism: this.geneticAlgorithmFormGroup.get('elitism').value,
          populationName: this.geneticAlgorithmFormGroup.get('populationName').value.name,
          latticeRows: this.geneticAlgorithmFormGroup.get('latticeRows').value,
          latticeColumns: this.geneticAlgorithmFormGroup.get('latticeColumns').value,
          latticeWrapAround: this.geneticAlgorithmFormGroup.get('latticeWrapAround').value,
          latticeRadius: this.geneticAlgorithmFormGroup.get('latticeRadius').value,
          breederName: this.geneticAlgorithmFormGroup.get('breederName').value.name,
          crossoverAlgorithmName: this.geneticAlgorithmFormGroup.get('crossoverAlgorithmName').value.name,
          mutationAlgorithmName: this.geneticAlgorithmFormGroup.get('mutationAlgorithmName').value.name,
          mutationRate: this.geneticAlgorithmFormGroup.get('mutationRate').value,
          maxMutationsPerIndividual: this.geneticAlgorithmFormGroup.get('maxMutationsPerIndividual').value,
          selectorName: this.geneticAlgorithmFormGroup.get('selectorName').value.name,
          tournamentSelectorAccuracy: this.geneticAlgorithmFormGroup.get('tournamentSelectorAccuracy').value,
          tournamentSize: this.geneticAlgorithmFormGroup.get('tournamentSize').value
        };

        this.configurationService.updateGeneticAlgorithmConfiguration(configuration);
      }
    });
  }

  restore() {
    this.configurationService.restoreGeneralSettings();
  }
}
