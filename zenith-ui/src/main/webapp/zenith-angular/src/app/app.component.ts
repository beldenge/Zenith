import { Component, OnInit } from '@angular/core';
import { animate, animateChild, group, query, style, transition, trigger } from "@angular/animations";
import { CipherService } from "./cipher.service";
import { IntroductionService } from "./introduction.service";
import { Router } from "@angular/router";

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

  constructor(private cipherService: CipherService, private introductionService: IntroductionService, private router: Router) {}

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
