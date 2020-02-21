import { Injectable } from '@angular/core';
import { Router } from "@angular/router";
import * as introJs from "intro.js/intro";
import { BehaviorSubject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class IntroductionService {
  showIntro = new BehaviorSubject(false);
  intro: introJs = new introJs();
  intro2: introJs = new introJs();

  constructor(private router: Router) {
  }

  startIntro(): void {
    this.showIntro.next(true);
  }

  startIntroDashboard(): void {
    this.intro.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: true,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      doneLabel: 'Next page',
      disableInteraction: true,
      overlayOpacity: 0.5,
      steps: [{
        intro: 'Welcome to Project Zenith!  How about a tour?'
      }, {
        element: '#cipher_select',
        intro: 'Select a Cipher here',
        position: 'right'
      }, {
        element: '#cipher_statistics_summary',
        intro: 'See summary statistics here',
        position: 'bottom'
      }, {
        element: '#solve_button',
        intro: 'Click to solve',
        position: 'left'
      }, {
        element: '#nav_manage_ciphers',
        intro: 'Manage ciphers here',
        position: 'right',
        highlightClass: 'introjs-nav-item-highlighted'
      }]
    });

    let self = this;
    this.intro.oncomplete(function() {
      self.router.navigate(['/ciphers']);
    });

    this.intro.start();
  }

  startIntroManageCiphers(): void {
    this.intro2.setOptions({
      exitOnOverlayClick: false,
      hidePrev: true,
      hideNext: true,
      showStepNumbers: false,
      showBullets: false,
      showProgress: false,
      doneLabel: 'Next page',
      disableInteraction: true,
      overlayOpacity: 0.5,
      steps: [{
        element: '#table-container',
        intro: 'Available ciphers display in this table',
        position: 'top'
      }]
    });

    let self = this;
    this.intro2.oncomplete(function() {
      self.router.navigate(['/settings']);
    });

    this.intro2.start();
  }

  getShowIntroAsObservable() {
    return this.showIntro.asObservable();
  }
}
