import { Component } from '@angular/core';
import { animate, animateChild, group, query, style, transition, trigger } from "@angular/animations";
import { RouterOutlet } from "@angular/router";

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
export class AppComponent {
  title = 'zenith-angular';

  getState(outlet) {
    return outlet.activatedRouteData.state;
  }
}
