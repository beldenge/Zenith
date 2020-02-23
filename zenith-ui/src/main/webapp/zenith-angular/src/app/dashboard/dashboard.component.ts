import {Component, OnDestroy, OnInit} from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { FormBuilder, Validators } from "@angular/forms";
import { WebSocketAPI } from "../websocket.api";
import { SolutionRequest } from "../models/SolutionRequest";
import { PlaintextTransformerService } from "../plaintext-transformer.service";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { SolutionRequestTransformer } from "../models/SolutionRequestTransformer";
import { MatSnackBar } from "@angular/material/snack-bar";
import { ConfigurationService } from "../configuration.service";
import { GeneticAlgorithmConfiguration } from "../models/GeneticAlgorithmConfiguration";
import { SimulatedAnnealingConfiguration } from "../models/SimulatedAnnealingConfiguration";
import { SelectOption } from "../models/SelectOption";
import { IntroductionService } from "../introduction.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit, OnDestroy {
  webSocketAPI: WebSocketAPI;
  ciphers: Cipher[];
  selectedCipher: Cipher;
  solution: string;
  score: number;
  isRunning: boolean = false;
  progressPercentage: number = 0;
  hyperparametersForm = this.fb.group({
    epochs: ['1', [Validators.min(1), Validators.pattern("^[0-9]*$")]]
  });
  selectHasFocus: boolean = false;
  appliedPlaintextTransformers: ZenithTransformer[] = [];
  optimizer: SelectOption;
  geneticAlgorithmConfiguration: GeneticAlgorithmConfiguration;
  simulatedAnnealingConfiguration: SimulatedAnnealingConfiguration;
  showIntroDashboardSubscription: Subscription;

  constructor(private fb: FormBuilder, private cipherService: CipherService, private plaintextTransformerService: PlaintextTransformerService, private _snackBar: MatSnackBar, private configurationService: ConfigurationService, private introductionService: IntroductionService) {
  }

  ngOnInit() {
    this.webSocketAPI = new WebSocketAPI();

    this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      this.selectedCipher = selectedCipher
    });

    this.cipherService.getCiphersAsObservable().subscribe(ciphers => {
      this.ciphers = ciphers
    });

    this.plaintextTransformerService.getAppliedTransformersAsObservable().subscribe(appliedTransformers => {
      this.appliedPlaintextTransformers = appliedTransformers;
    });

    this.configurationService.getSelectedOptimizerAsObservable().subscribe(optimizer => {
      this.optimizer = optimizer;
    });

    this.configurationService.getSimulatedAnnealingConfigurationAsObservable().subscribe(configuration => {
      this.simulatedAnnealingConfiguration = configuration;
    });

    this.configurationService.getGeneticAlgorithmConfigurationAsObservable().subscribe(configuration => {
      this.geneticAlgorithmConfiguration = configuration;
    });

    this.showIntroDashboardSubscription = this.introductionService.getShowIntroDashboardAsObservable().subscribe(showIntro => {
      if (showIntro) {
        this.introductionService.startIntroDashboard();
        this.introductionService.updateShowIntroDashboard(false);
      }
    });
  }

  ngOnDestroy() {
    this.showIntroDashboardSubscription.unsubscribe();
  }

  onMouseDownSelect(element: HTMLElement) {
    this.selectHasFocus = true;
  }

  onMouseOverSelect(element: HTMLElement) {
    if (!this.selectHasFocus) {
      element.focus();
    }
  }

  onMouseOutSelect(element: HTMLElement) {
    if (!this.selectHasFocus) {
      element.blur();
    }
  }

  onFocusOutSelect(element: HTMLElement) {
    this.selectHasFocus = false;
  }

  solve() {
    let request = new SolutionRequest(this.selectedCipher.rows, this.selectedCipher.columns, this.selectedCipher.ciphertext, this.hyperparametersForm.get('epochs').value);

    if (this.optimizer === ConfigurationService.OPTIMIZER_NAMES[0]) {
      request.simulatedAnnealingConfiguration = this.simulatedAnnealingConfiguration;
    } else {
      request.geneticAlgorithmConfiguration = this.geneticAlgorithmConfiguration;
    }

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

    this.progressPercentage = 0;
    this.isRunning = true;
    this.solution = null;

    let self = this;
    this.webSocketAPI.connectAndSend(request, function (response) {
      if (response.headers.type === 'SOLUTION') {
        let json = JSON.parse(response.body);
        self.solution = json.plaintext;
        self.score = json.score;
        self.isRunning = false;
        self.webSocketAPI.disconnect();
        self.progressPercentage = 100;
      } else if (response.headers.type === 'EPOCH_COMPLETE') {
        let responseBody = JSON.parse(response.body);
        self.progressPercentage = (responseBody.epochsCompleted / responseBody.epochsTotal) * 100;
      }
    }, function(error) {
      self.isRunning = false;
    });
  }

  byName(c1: Cipher, c2: Cipher): boolean {
    return c1 && c2 ? c1.name === c2.name : c1 === c2;
  }

  onCipherSelect(element: HTMLElement) {
    element.blur();
    this.solution = null;
    localStorage.setItem('selected_cipher_name', this.selectedCipher.name);
    this.cipherService.updateSelectedCipher(this.selectedCipher);
  }
}
