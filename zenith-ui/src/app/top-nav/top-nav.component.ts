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

import {Component, OnDestroy, OnInit} from '@angular/core';
import { Cipher } from "../models/Cipher";
import { ApplicationConfiguration } from "../models/ApplicationConfiguration";
import { SidebarService } from "../sidebar.service";
import { SafeUrl } from "@angular/platform-browser";
import {SolutionService} from "../solution.service";
import {ConfigurationService} from "../configuration.service";
import {CipherService} from "../cipher.service";
import {Subscription} from "rxjs";
import {MatSnackBar} from "@angular/material/snack-bar";

@Component({
    selector: 'app-top-nav',
    templateUrl: './top-nav.component.html',
    styleUrls: ['./top-nav.component.css'],
    standalone: false
})
export class TopNavComponent implements OnInit, OnDestroy {
  configFilename = 'zenith.json';
  selectHasFocus = false;
  exportUri: SafeUrl;
  ciphers: Cipher[];
  selectedCipher: Cipher;
  isRunning = false;
  selectedCipherSubscription: Subscription;
  ciphersSubscription: Subscription;
  isRunningSubscription: Subscription;

  constructor(private snackBar: MatSnackBar,
              private cipherService: CipherService,
              private configurationService: ConfigurationService,
              private solutionService: SolutionService,
              private sidebarService: SidebarService) {}

  ngOnInit(): void {
    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      this.selectedCipher = selectedCipher;
    });

    this.ciphersSubscription = this.cipherService.getCiphersAsObservable().subscribe(ciphers => {
      this.ciphers = ciphers;
    });

    this.isRunningSubscription = this.solutionService.getRunStateAsObservable().subscribe(runState => {
      this.isRunning = runState;
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
    this.isRunningSubscription.unsubscribe();
    this.ciphersSubscription.unsubscribe();
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

  onSidebarToggleClick() {
    if (document.body.classList.contains('sidebar-toggled')) {
      this.sidebarService.updateSidebarToggle(false);
    } else {
      this.sidebarService.updateSidebarToggle(true);
    }
  }

  byName(c1: Cipher, c2: Cipher): boolean {
    return c1 && c2 ? c1.name === c2.name : c1 === c2;
  }

  onCipherSelect(element: HTMLElement) {
    element.blur();
    this.solutionService.updateSolution(null);
    this.solutionService.updateProgressPercentage(0);
    delete this.selectedCipher.transformed;
    this.configurationService.updateAppliedCiphertextTransformers([]);
    this.configurationService.updateAppliedPlaintextTransformers([])
    this.cipherService.updateSelectedCipher(this.selectedCipher);
  }

  setExportUri() {
    this.exportUri = this.configurationService.getExportUri();
  }

  clickInput(input: HTMLInputElement) {
    input.click();
  }

  importConfiguration(event) {
    const input = event.target;

    if (!input.value.endsWith(this.configFilename)) {
      this.snackBar.open('Error: invalid filename.  Expected ' + this.configFilename + '.', '', {
        duration: 5000,
        verticalPosition: 'top'
      });

      return;
    }

    const reader = new FileReader();

    const self = this;

    reader.onload = () => {
      const text = reader.result.toString();
      const configuration: ApplicationConfiguration = Object.assign(new ApplicationConfiguration(), JSON.parse(text));
      self.configurationService.import(configuration);

      this.snackBar.open('Configuration imported successfully.', '', {
        duration: 2000,
        verticalPosition: 'top'
      });
    };

    reader.onerror = () => {
      this.snackBar.open('Error: could not load configuration.', '', {
        duration: 5000,
        verticalPosition: 'top'
      });
    };

    reader.readAsText(input.files[0]);
  }
}
