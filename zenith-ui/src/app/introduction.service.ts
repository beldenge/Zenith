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
import { Router } from '@angular/router';
import introJs from 'intro.js';

@Injectable({
  providedIn: 'root'
})
export class IntroductionService {
  private showIntroDashboardInternal: WritableSignal<boolean> = signal(false);
  public showIntroDashboard: Signal<boolean> = this.showIntroDashboardInternal.asReadonly();
  private showIntroManageCiphersInternal: WritableSignal<boolean> = signal(false);
  public showIntroManageCiphers: Signal<boolean> = this.showIntroManageCiphersInternal.asReadonly();
  private showIntroSettingsInternal: WritableSignal<boolean> = signal(false);
  public showIntroSettings: Signal<boolean> = this.showIntroSettingsInternal.asReadonly();
  private showIntroCiphertextTransformersInternal: WritableSignal<boolean> = signal(false);
  public showIntroCiphertextTransformers: Signal<boolean> = this.showIntroCiphertextTransformersInternal.asReadonly();
  private showIntroPlaintextTransformersInternal: WritableSignal<boolean> = signal(false);
  public showIntroPlaintextTransformers: Signal<boolean> = this.showIntroPlaintextTransformersInternal.asReadonly();

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
    this.showIntroDashboardInternal.update(() => true);
    this.showIntroManageCiphersInternal.update(() => true);
    this.showIntroSettingsInternal.update(() => true);
    this.showIntroCiphertextTransformersInternal.update(() => true);
    this.showIntroPlaintextTransformersInternal.update(() => true);
    this.router.navigate(['/dashboard']);
  }

  stopIntro(): void {
    this.showIntroDashboardInternal.update(() => false);
    this.showIntroManageCiphersInternal.update(() => false);
    this.showIntroSettingsInternal.update(() => false);
    this.showIntroCiphertextTransformersInternal.update(() => false);
    this.showIntroPlaintextTransformersInternal.update(() => false);
  }

  startIntroDashboard(): void {
    this.introDashboard = this.buildTour({
      doneLabel: 'Take me there!',
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
    }, () => this.router.navigate(['/ciphers']));
  }

  startIntroManageCiphers(): void {
    this.introManageCiphers = this.buildTour({
      doneLabel: 'Take me there!',
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
    }, () => this.router.navigate(['/settings']));
  }

  startIntroSettings(): void {
    this.introSettings = this.buildTour({
      doneLabel: 'Take me there!',
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
    }, () => this.router.navigate(['/transformers/ciphertext']));
  }

  startIntroCiphertextTransformers(): void {
    this.introCiphertextTransformers = this.buildTour({
      doneLabel: 'Take me there!',
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
    }, () => this.router.navigate(['/transformers/plaintext']));
  }

  startIntroPlaintextTransformers(): void {
    this.introPlaintextTransformers = this.buildTour({
      doneLabel: 'Finish tour',
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
    }, () => this.router.navigate(['/dashboard']));
  }

  private buildTour(options: any, onComplete: () => void) {
    const tour = introJs.tour();
    tour.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: false,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      disableInteraction: true,
      overlayOpacity: 0.5,
      ...options
    });

    let allowStop = true;
    tour.onComplete((_, reason) => {
      if (reason === 'skip') {
        return;
      }
      allowStop = false;
      tour.resetCurrentStep();
      setTimeout(() => onComplete(), 0);
    });

    const stopIntroIfAllowed = () => {
      tour.resetCurrentStep();
      if (allowStop) {
        this.stopIntro();
      }
    };

    tour.onExit(stopIntroIfAllowed);
    tour.onSkip(stopIntroIfAllowed);
    tour.resetCurrentStep();
    tour.refresh(true);
    tour.start();
    return tour;
  }

  updateShowIntroDashboard(showIntro: boolean) {
    this.showIntroDashboardInternal.update(() => showIntro);
  }

  updateShowIntroManageCiphers(showIntro: boolean) {
    this.showIntroManageCiphersInternal.update(() => showIntro);
  }

  updateShowIntroSettings(showIntro: boolean) {
    this.showIntroSettingsInternal.update(() => showIntro);
  }

  updateShowIntroCiphertextTransformers(showIntro: boolean) {
    this.showIntroCiphertextTransformersInternal.update(() => showIntro);
  }

  updateShowIntroPlaintextTransformers(showIntro: boolean) {
    this.showIntroPlaintextTransformersInternal.update(() => showIntro);
  }
}
