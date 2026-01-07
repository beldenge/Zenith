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
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { UntypedFormBuilder, Validators } from "@angular/forms";
import { WebSocketAPI } from "../websocket.api";
import { SolutionRequest } from "../models/SolutionRequest";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { SolutionRequestTransformer } from "../models/SolutionRequestTransformer";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ConfigurationService } from "../configuration.service";
import { GeneticAlgorithmConfiguration } from "../models/GeneticAlgorithmConfiguration";
import { SimulatedAnnealingConfiguration } from "../models/SimulatedAnnealingConfiguration";
import { SelectOption } from "../models/SelectOption";
import { IntroductionService } from "../introduction.service";
import { Subscription } from "rxjs";
import { SolutionService } from "../solution.service";
import { SolutionResponse } from "../models/SolutionResponse";
import { SolutionRequestFitnessFunction } from "../models/SolutionRequestFitnessFunction";
import { ZenithFitnessFunction } from "../models/ZenithFitnessFunction";
import { LocalStorageKeys } from "../models/LocalStorageKeys";
import { animate, style, transition, trigger } from "@angular/animations";

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css'],
    standalone: false
})
export class DashboardComponent implements OnInit, OnDestroy {
  showApplicationDownloadInfo = false;
  showIntroDashboardSubscription: Subscription;
  webSocketAPI: WebSocketAPI;
  selectedCipher: Cipher;
  isRunning = false;
  progressPercentage = 0;
  isRunningSubscription: Subscription;
  progressPercentageSubscription: Subscription;
  epochsValidationMessageDefault = 'Must be a number greater than zero';
  epochsValidationMessage: string = this.epochsValidationMessageDefault;
  epochsValidatorsDefault = [Validators.min(1), Validators.pattern('^[0-9]*$')];
  hyperparametersForm = this.fb.group({
    epochs: [null, this.epochsValidatorsDefault]
  });
  appliedPlaintextTransformers: ZenithTransformer[] = [];
  optimizer: SelectOption;
  fitnessFunction: ZenithFitnessFunction;
  geneticAlgorithmConfiguration: GeneticAlgorithmConfiguration;
  simulatedAnnealingConfiguration: SimulatedAnnealingConfiguration;
  selectedCipherSubscription: Subscription;
  appliedPlaintextTransformersSubscription: Subscription;
  epochsSubscription: Subscription;
  selectedOptimizerSubscription: Subscription;
  selectedFitnessFunctionSubscription: Subscription;
  simulatedAnnealingConfigurationSubscription: Subscription;
  geneticAlgorithmConfigurationSubscription: Subscription;
  featuresSubscription: Subscription;

  constructor(private fb: UntypedFormBuilder,
              private cipherService: CipherService,
              private _snackBar: MatSnackBar,
              private configurationService: ConfigurationService,
              private introductionService: IntroductionService,
              private solutionService: SolutionService) {
  }

  ngOnInit() {
    this.webSocketAPI = new WebSocketAPI();

    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      this.selectedCipher = selectedCipher;
    });

    this.appliedPlaintextTransformersSubscription = this.configurationService.getAppliedPlaintextTransformersAsObservable().subscribe(appliedTransformers => {
      this.appliedPlaintextTransformers = appliedTransformers;
    });

    this.epochsSubscription = this.configurationService.getEpochsAsObservable().subscribe(epochs => {
      if (this.hyperparametersForm.get('epochs').value !== epochs) {
        this.hyperparametersForm.patchValue({ 'epochs': epochs });
      }
    });

    this.selectedOptimizerSubscription = this.configurationService.getSelectedOptimizerAsObservable().subscribe(optimizer => {
      this.optimizer = optimizer;
    });

    this.selectedFitnessFunctionSubscription = this.configurationService.getSelectedFitnessFunctionAsObservable().subscribe(fitnessFunction => {
      this.fitnessFunction = fitnessFunction;
    });

    this.simulatedAnnealingConfigurationSubscription = this.configurationService.getSimulatedAnnealingConfigurationAsObservable().subscribe(configuration => {
      this.simulatedAnnealingConfiguration = configuration;
    });

    this.geneticAlgorithmConfigurationSubscription = this.configurationService.getGeneticAlgorithmConfigurationAsObservable().subscribe(configuration => {
      this.geneticAlgorithmConfiguration = configuration;
    });

    this.showIntroDashboardSubscription = this.introductionService.getShowIntroDashboardAsObservable().subscribe(showIntro => {
      if (showIntro) {
        this.introductionService.startIntroDashboard();
        this.introductionService.updateShowIntroDashboard(false);
      }
    });

    this.featuresSubscription = this.configurationService.getFeaturesAsObservable().subscribe(featureResponse => {
      if (featureResponse.maxEpochs > 0) {
        this.hyperparametersForm.get('epochs').setValidators(this.epochsValidatorsDefault.concat([Validators.max(featureResponse.maxEpochs)]));
        this.epochsValidationMessage = 'Must be a number between 1 and ' + featureResponse.maxEpochs;
      } else {
        this.hyperparametersForm.get('epochs').setValidators(this.epochsValidatorsDefault);
        this.epochsValidationMessage = this.epochsValidationMessageDefault;
      }
    });

    this.isRunningSubscription = this.solutionService.getRunStateAsObservable().subscribe(runState => {
      this.isRunning = runState;
    });

    this.progressPercentageSubscription = this.solutionService.getProgressPercentageAsObservable().subscribe(progress => {
      this.progressPercentage = progress;
    });

    let showApplicationDownloadInfoLocal = localStorage.getItem(LocalStorageKeys.SHOW_APPLICATION_DOWNLOAD_INFO);

    this.showApplicationDownloadInfo = !showApplicationDownloadInfoLocal || showApplicationDownloadInfoLocal === 'true';
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
    this.appliedPlaintextTransformersSubscription.unsubscribe();
    this.epochsSubscription.unsubscribe();
    this.selectedOptimizerSubscription.unsubscribe();
    this.selectedFitnessFunctionSubscription.unsubscribe();
    this.simulatedAnnealingConfigurationSubscription.unsubscribe();
    this.geneticAlgorithmConfigurationSubscription.unsubscribe();
    this.showIntroDashboardSubscription.unsubscribe();
    this.featuresSubscription.unsubscribe();
    this.isRunningSubscription.unsubscribe();
    this.progressPercentageSubscription.unsubscribe();
  }

  onEpochsChange() {
    if (this.hyperparametersForm.valid) {
      this.solutionService.updateProgressPercentage(0);
      this.configurationService.updateEpochs(this.hyperparametersForm.get('epochs').value);
    }
  }

  solve() {
    if (!this.hyperparametersForm.valid) {
      return;
    }

    if (this.isRunning) {
      return;
    }

    let request: SolutionRequest;

    if (!this.selectedCipher.transformed) {
      request = new SolutionRequest(
        this.selectedCipher.rows,
        this.selectedCipher.columns,
        this.selectedCipher.ciphertext,
        this.hyperparametersForm.get('epochs').value
      );
    } else {
      request = new SolutionRequest(
        this.selectedCipher.transformed.rows,
        this.selectedCipher.transformed.columns,
        this.selectedCipher.transformed.ciphertext,
        this.hyperparametersForm.get('epochs').value
      );
    }

    if (this.optimizer.name === ConfigurationService.OPTIMIZER_NAMES[0].name) {
      request.simulatedAnnealingConfiguration = this.simulatedAnnealingConfiguration;
    } else {
      request.geneticAlgorithmConfiguration = this.geneticAlgorithmConfiguration;
    }

    request.fitnessFunction = new SolutionRequestFitnessFunction(this.fitnessFunction.name, this.fitnessFunction.form ? this.fitnessFunction.form.model : null);

    let allValid = true;

    if (this.appliedPlaintextTransformers.length) {
      let plaintextTransformers = [];

      this.appliedPlaintextTransformers.forEach((transformer) => {
        plaintextTransformers.push(new SolutionRequestTransformer(transformer.name, transformer.form.model));

        allValid = allValid && transformer.form.form.valid;
      });

      if (allValid) {
        request.plaintextTransformers = plaintextTransformers;
      }
    }

    if (!allValid) {
      this._snackBar.open('Errors exist in plaintext transformers.  Please correct them before solving.', '',{
        duration: 2000,
        verticalPosition: 'top'
      });

      return;
    }

    this.solutionService.updateSolution(null);
    this.solutionService.updateRunState(true);
    this.solutionService.updateProgressPercentage(0);

    const self = this;
    this.webSocketAPI.connectAndSend(request, function (response) {
      if (response.headers.type === 'SOLUTION') {
        const json = JSON.parse(response.body);

        self.solutionService.updateSolution(new SolutionResponse(json.plaintext, json.score))

        self.solutionService.updateRunState(false);
        self.solutionService.updateProgressPercentage(100);
        self.webSocketAPI.disconnect();
      } else if (response.headers.type === 'EPOCH_COMPLETE') {
        const responseBody = JSON.parse(response.body);
        self.solutionService.updateProgressPercentage((responseBody.epochsCompleted / responseBody.epochsTotal) * 100);
      } else if (response.headers.type === 'ERROR') {
        self.solverError();
      }
    }, error => {
      self.solverError();
    });
  }

  solverError() {
    this.solutionService.updateRunState(false);
    this.solutionService.updateProgressPercentage(0);

    this._snackBar.open('Error: unable to complete solve due to server error.', '',{
      duration: 5000,
      verticalPosition: 'top'
    });

    this.webSocketAPI.disconnect();
  }

  disableApplicationDownloadInfo() {
    this.showApplicationDownloadInfo = false;
    localStorage.setItem(LocalStorageKeys.SHOW_APPLICATION_DOWNLOAD_INFO, 'false');
  }
}
