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
import { Router } from '@angular/router';
import introJs from 'intro.js';
import { BehaviorSubject } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class IntroductionService {
  showIntroDashboard = new BehaviorSubject(false);
  showIntroManageCiphers = new BehaviorSubject(false);
  showIntroSettings = new BehaviorSubject(false);
  showIntroCiphertextTransformers = new BehaviorSubject(false);
  showIntroPlaintextTransformers = new BehaviorSubject(false);

  introDashboard: any;
  introManageCiphers: any;
  introSettings: any;
  introCiphertextTransformers: any;
  introPlaintextTransformers: any;

  constructor(private router: Router) {
    this.introDashboard = introJs.tour();
    this.introManageCiphers = introJs.tour();
    this.introSettings = introJs.tour();
    this.introCiphertextTransformers = introJs.tour();
    this.introPlaintextTransformers = introJs.tour();
  }

  startIntro(): void {
    this.showIntroDashboard.next(true);
    this.showIntroManageCiphers.next(true);
    this.showIntroSettings.next(true);
    this.showIntroCiphertextTransformers.next(true);
    this.showIntroPlaintextTransformers.next(true);
    this.router.navigate(['/dashboard']);
  }

  stopIntro(): void {
    this.showIntroDashboard.next(false);
    this.showIntroManageCiphers.next(false);
    this.showIntroSettings.next(false);
    this.showIntroCiphertextTransformers.next(false);
    this.showIntroPlaintextTransformers.next(false);
  }

  startIntroDashboard(): void {
    this.introDashboard.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: true,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      skipLabel: 'Quit',
      doneLabel: 'Take me there!',
      disableInteraction: true,
      overlayOpacity: 0.5,
      steps: [{
        intro: 'Welcome to Project Zenith!  How about a tour?'
      }, {
        element: '#cipher_select',
        intro: 'Select the Cipher you wish to solve here.',
        position: 'right'
      }, {
        element: '#cipher_statistics_summary',
        intro: 'See summary statistics for the ciphertext.',
        position: 'bottom'
      }, {
        element: '#solve_button',
        intro: 'Run the solver here.',
        position: 'left'
      }, {
        element: '#import_export',
        intro: 'Export your settings to share them or to import them on a different browser or device.',
        position: 'left'
      }, {
        element: '#nav_manage_ciphers',
        intro: 'This page lets you view, add, edit, and delete available ciphers.',
        position: 'right',
        highlightClass: 'introjs-nav-item-highlighted'
      }]
    });

    const self = this;
    this.introDashboard.oncomplete(() => {
      self.router.navigate(['/ciphers']);

      // Prevent the intro on other pages from being halted because of the onexit function below
      self.introDashboard.onexit(() => {});
    });

    this.introDashboard.onexit(() => {
      self.stopIntro();
    });

    this.introDashboard.start();
  }

  startIntroManageCiphers(): void {
    this.introManageCiphers.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: true,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      skipLabel: 'Quit',
      doneLabel: 'Take me there!',
      disableInteraction: true,
      overlayOpacity: 0.5,
      steps: [{
        element: '#create_button',
        intro: 'This button opens a form to create a new cipher.',
        position: 'bottom',
      }, {
        element: '#table_container',
        intro: 'You can filter, view, edit, and delete ciphers within this table.',
        position: 'top',
      }, {
        element: '#nav_settings',
        intro: 'This page lets you modify the solver configuration.',
        position: 'right',
        highlightClass: 'introjs-nav-item-highlighted'
      }]
    });

    const self = this;
    this.introManageCiphers.oncomplete(() => {
      self.router.navigate(['/settings']);

      // Prevent the intro on other pages from being halted because of the onexit function below
      self.introManageCiphers.onexit(() => {});
    });

    this.introManageCiphers.onexit(() => {
      self.stopIntro();
    });

    this.introManageCiphers.start();
  }

  startIntroSettings(): void {
    this.introSettings.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: true,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      skipLabel: 'Quit',
      doneLabel: 'Take me there!',
      disableInteraction: true,
      overlayOpacity: 0.5,
      steps: [{
        element: '#optimizer_form_group',
        intro: 'Choose your preferred optimizer and fitness function.',
        position: 'bottom',
      }, {
        element: '#optimizer_settings_form_group',
        intro: 'Configure the available options for the chosen optimizer.',
        position: 'top',
      }, {
        element: '#restore_button',
        intro: 'Restore the original configuration at any time.',
        position: 'top',
      }, {
        element: '#nav_ciphertext_transformers',
        intro: 'This page lets you manage ciphertext transformers.',
        position: 'right',
        highlightClass: 'introjs-nav-item-highlighted'
      }]
    });

    const self = this;
    this.introSettings.oncomplete(() => {
      self.router.navigate(['/transformers/ciphertext']);

      // Prevent the intro on other pages from being halted because of the onexit function below
      self.introSettings.onexit(() => {});
    });

    this.introSettings.onexit(() => {
      self.stopIntro();
    });

    this.introSettings.start();
  }

  startIntroCiphertextTransformers(): void {
    this.introCiphertextTransformers.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: true,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      skipLabel: 'Quit',
      doneLabel: 'Take me there!',
      disableInteraction: true,
      overlayOpacity: 0.5,
      steps: [{
        element: '#available_ciphertext_transformers_container',
        intro: 'Drag one or more transformers from here to the right to add them to the pipeline.',
        position: 'top',
      }, {
        element: '#ciphertext_transformer_pipeline',
        intro: 'Reorder and delete transformers in the pipeline.',
        position: 'top',
      }, {
        element: '#transformed_ciphertext',
        intro: 'Watch the ciphertext update in realtime.',
        position: 'top',
      }, {
        element: '#nav_plaintext_transformers',
        intro: 'This page lets you manage plaintext transformers.',
        position: 'right',
        highlightClass: 'introjs-nav-item-highlighted'
      }]
    });

    const self = this;
    this.introCiphertextTransformers.oncomplete(() => {
      self.router.navigate(['/transformers/plaintext']);

      // Prevent the intro on other pages from being halted because of the onexit function below
      self.introCiphertextTransformers.onexit(() => {});
    });

    this.introCiphertextTransformers.onexit(() => {
      self.stopIntro();
    });

    this.introCiphertextTransformers.start();
  }

  startIntroPlaintextTransformers(): void {
    this.introPlaintextTransformers.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: true,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      skipLabel: 'Quit',
      doneLabel: 'Finish tour',
      disableInteraction: true,
      overlayOpacity: 0.5,
      steps: [{
        element: '#available_plaintext_transformers_container',
        intro: 'Drag one or more transformers from here to the right to add them to the pipeline.',
        position: 'top',
      }, {
        element: '#plaintext_transformer_pipeline',
        intro: 'Reorder and delete transformers in the pipeline.',
        position: 'top',
      }, {
        element: '#sample_plaintext',
        intro: 'Watch the sample plaintext update in realtime.',
        position: 'top',
      }, {
        element: '#nav_help',
        intro: 'If you ever get stuck or want to replay this intro, you can find help here.',
        position: 'right',
        highlightClass: 'introjs-nav-item-highlighted'
      }]
    });

    const self = this;
    this.introPlaintextTransformers.oncomplete(() => {
      self.router.navigate(['/dashboard']);

      // Prevent the intro on other pages from being halted because of the onexit function below
      self.introPlaintextTransformers.onexit(() => {});
    });

    this.introPlaintextTransformers.onexit(() => {
      self.stopIntro();
    });

    this.introPlaintextTransformers.start();
  }

  getShowIntroDashboardAsObservable() {
    return this.showIntroDashboard.asObservable();
  }

  updateShowIntroDashboard(showIntro: boolean) {
    this.showIntroDashboard.next(showIntro);
  }

  getShowIntroManageCiphersAsObservable() {
    return this.showIntroManageCiphers.asObservable();
  }

  updateShowIntroManageCiphers(showIntro: boolean) {
    this.showIntroManageCiphers.next(showIntro);
  }

  getShowIntroSettingsAsObservable() {
    return this.showIntroSettings.asObservable();
  }

  updateShowIntroSettings(showIntro: boolean) {
    this.showIntroSettings.next(showIntro);
  }

  getShowIntroCiphertextTransformersAsObservable() {
    return this.showIntroCiphertextTransformers.asObservable();
  }

  updateShowIntroCiphertextTransformers(showIntro: boolean) {
    this.showIntroCiphertextTransformers.next(showIntro);
  }

  getShowIntroPlaintextTransformersAsObservable() {
    return this.showIntroPlaintextTransformers.asObservable();
  }

  updateShowIntroPlaintextTransformers(showIntro: boolean) {
    this.showIntroPlaintextTransformers.next(showIntro);
  }
}
