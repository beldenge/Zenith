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

import {Component, effect, OnDestroy, OnInit} from '@angular/core';
import { UntypedFormBuilder, Validators } from "@angular/forms";
import { SolutionRequest } from "../models/SolutionRequest";
import { SolutionUpdate } from "../models/SolutionUpdate";
import { TransformationStep } from "../models/TransformationStep";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ConfigurationService } from "../configuration.service";
import { IntroductionService } from "../introduction.service";
import { Subscription } from "rxjs";
import { SolutionService } from "../solution.service";
import { SolutionRequestFitnessFunction } from "../models/SolutionRequestFitnessFunction";
import { LocalStorageKeys } from "../models/LocalStorageKeys";

@Component({
    selector: 'app-dashboard',
    templateUrl: './dashboard.component.html',
    styleUrls: ['./dashboard.component.css'],
    standalone: false
})
export class DashboardComponent implements OnInit, OnDestroy {
  showApplicationDownloadInfo = false;
  showIntro = this.introductionService.showIntroDashboard;
  solutionSubscription?: Subscription;
  selectedCipher = this.configurationService.selectedCipher;
  isRunning = this.solutionService.runState;
  epochsValidators = [Validators.min(1), Validators.max(100)];
  epochsValidationMessage = 'Must be a number between 1 and 100';
  hyperparametersForm = this.fb.group({
    epochs: [null, this.epochsValidators]
  });
  appliedPlaintextTransformers = this.configurationService.appliedPlaintextTransformers;
  optimizer = this.configurationService.selectedOptimizer;
  fitnessFunction = this.configurationService.selectedFitnessFunction;
  geneticAlgorithmConfiguration = this.configurationService.geneticAlgorithmConfiguration;
  simulatedAnnealingConfiguration = this.configurationService.simulatedAnnealingConfiguration;
  epochs = this.configurationService.epochs;

  constructor(private fb: UntypedFormBuilder,
              private snackBar: MatSnackBar,
              private configurationService: ConfigurationService,
              private introductionService: IntroductionService,
              public solutionService: SolutionService) {
    effect(() => {
      if (this.showIntro()) {
        this.introductionService.startIntroDashboard();
        this.introductionService.updateShowIntroDashboard(false);
      }
    });

    effect(() => {
      if (this.hyperparametersForm.get('epochs').value !== this.epochs()) {
        this.hyperparametersForm.patchValue({ epochs: this.epochs() });
      }
    });
  }

  ngOnInit() {
    const showApplicationDownloadInfoLocal = localStorage.getItem(LocalStorageKeys.SHOW_APPLICATION_DOWNLOAD_INFO);

    this.showApplicationDownloadInfo = !showApplicationDownloadInfoLocal || showApplicationDownloadInfoLocal === 'true';
  }

  ngOnDestroy() {
    this.solutionSubscription?.unsubscribe();
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

    if (this.isRunning()) {
      return;
    }

    let request: SolutionRequest;

    const selectedCipherLocal = this.selectedCipher();
    if (!selectedCipherLocal.transformed) {
      request = new SolutionRequest(
        selectedCipherLocal.rows,
        selectedCipherLocal.columns,
        selectedCipherLocal.ciphertext,
        this.hyperparametersForm.get('epochs').value
      );
    } else {
      request = new SolutionRequest(
        selectedCipherLocal.transformed.rows,
        selectedCipherLocal.transformed.columns,
        selectedCipherLocal.transformed.ciphertext,
        this.hyperparametersForm.get('epochs').value
      );
    }

    if (this.optimizer().name === 'SimulatedAnnealing') {
      request.simulatedAnnealingConfiguration = this.simulatedAnnealingConfiguration();
    } else {
      request.geneticAlgorithmConfiguration = this.geneticAlgorithmConfiguration();
    }

    request.fitnessFunction = new SolutionRequestFitnessFunction(this.fitnessFunction().name, this.fitnessFunction().form ? this.fitnessFunction().form.model : null);

    let allValid = true;

    if (this.appliedPlaintextTransformers().length) {
      const plaintextTransformers = [];

      this.appliedPlaintextTransformers().forEach((transformer) => {
        plaintextTransformers.push(new TransformationStep(transformer.name, transformer.form.model));

        allValid = allValid && transformer.form.form.valid;
      });

      if (allValid) {
        request.plaintextTransformers = plaintextTransformers;
      }
    }

    if (!allValid) {
      this.snackBar.open('Errors exist in plaintext transformers.  Please correct them before solving.', '', {
        duration: 2000,
        verticalPosition: 'top'
      });

      return;
    }

    this.solutionService.updateSolution(null);
    this.solutionService.updateRunState(true);
    this.solutionService.updateProgressPercentage(0);

    this.solutionService.solveSolution(request).subscribe({
      next: (requestId: string) => {
        // Don't manually unsubscribe on SOLUTION/ERROR - let the server complete the stream
        // via tryEmitComplete() to avoid race condition causing "WebSocket session has been closed" errors
        this.solutionSubscription = this.solutionService.solutionUpdates(requestId).subscribe({
          next: (update: SolutionUpdate) => {
            this.solutionService.handleSolutionUpdate(update);
            if (update.type === 'ERROR') {
              this.solverError();
            }
          },
          error: () => {
            this.solverError();
          }
        });
      },
      error: () => {
        this.solverError();
      }
    });
  }

  solverError() {
    this.solutionService.updateRunState(false);
    this.solutionService.updateProgressPercentage(0);

    this.snackBar.open('Error: unable to complete solve due to server error.', '', {
      duration: 5000,
      verticalPosition: 'top'
    });
  }

  disableApplicationDownloadInfo() {
    this.showApplicationDownloadInfo = false;
    localStorage.setItem(LocalStorageKeys.SHOW_APPLICATION_DOWNLOAD_INFO, 'false');
  }
}
