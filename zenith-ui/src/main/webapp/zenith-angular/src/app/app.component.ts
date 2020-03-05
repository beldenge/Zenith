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

import { Component, OnInit } from '@angular/core';
import { animate, animateChild, group, query, style, transition, trigger } from "@angular/animations";
import { CipherService } from "./cipher.service";
import { IntroductionService } from "./introduction.service";
import { NavigationEnd, Router } from "@angular/router";
import { environment } from "../environments/environment";

declare let gtag: Function;

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css'],
  animations: [
    trigger('routeAnimations', [
      transition('* => *', [
        style({ position: 'relative' }),
        query(':enter, :leave', [
          style({
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%'
          })
        ], { optional: true }),
        query(':enter', [
          style({ left: '-100%'})
        ], { optional: true }),
        query(':leave', animateChild(), { optional: true }),
        group([
          query(':leave', [
            animate('200ms ease-out', style({ left: '100%'}))
          ], { optional: true }),
          query(':enter', [
            animate('300ms ease-out', style({ left: '0%'}))
          ], { optional: true })
        ]),
        query(':enter', animateChild(), { optional: true }),
      ])
    ])
  ]
})
export class AppComponent implements OnInit {
  title = 'zenith-angular';

  constructor(private cipherService: CipherService, private introductionService: IntroductionService, private router: Router) {
    this.router.events.subscribe(event => {
      if(event instanceof NavigationEnd) {
        gtag('config', environment.googleAnalyticsTrackingId, { 'page_path': event.urlAfterRedirects });
      }
    });
  }

  getState(outlet) {
    return outlet.activatedRouteData.state;
  }

  ngOnInit() {
    this.cipherService.getCiphers().subscribe(cipherResponse => {
      let ciphers = cipherResponse.ciphers;
      this.cipherService.updateCiphers(ciphers);

      this.cipherService.updateSelectedCipher(ciphers[0]);

      if(localStorage.getItem('selected_cipher_name')) {
        let selectedCipherName = localStorage.getItem('selected_cipher_name');

        let selectedCipher = ciphers.find(cipher => {
          return cipher.name === selectedCipherName;
        });

        this.cipherService.updateSelectedCipher(selectedCipher);
      }
    });

    if (!localStorage.getItem('skip_intro')) {
      localStorage.setItem('skip_intro', 'true');
      this.introductionService.startIntro();
    }
  }
}
