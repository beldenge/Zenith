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

import { AfterViewInit, Component, ElementRef, HostListener, OnDestroy, OnInit } from '@angular/core';
import { animate, animateChild, group, query, style, transition, trigger } from "@angular/animations";
import { CipherService } from "./cipher.service";
import { IntroductionService } from "./introduction.service";
import { NavigationEnd, Router } from "@angular/router";
import { environment } from "../environments/environment";
import { ConfigurationService } from "./configuration.service";
import { Subscription } from "rxjs";
import { LocalStorageKeys } from "./models/LocalStorageKeys";
import { SidebarService } from "./sidebar.service";

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
export class AppComponent implements OnInit, OnDestroy, AfterViewInit {
  title = 'zenith-angular';
  trackingEnabled = false;
  enableTrackingSubscription: Subscription;
  pageTransitionsEnabled = false;
  enablePageTransitionsSubscription: Subscription;
  googleAnalyticsInitialized = false;

  constructor(private elementRef: ElementRef,
              private cipherService: CipherService,
              private introductionService: IntroductionService,
              private configurationService: ConfigurationService,
              private router: Router,
              private sidebarService: SidebarService) {
    this.router.events.subscribe(event => {
      if(this.trackingEnabled && event instanceof NavigationEnd) {
        gtag('config', environment.googleAnalyticsTrackingId, { 'page_path': event.urlAfterRedirects });
      }
    });
  }

  getState(outlet) {
    return this.pageTransitionsEnabled ? outlet.activatedRouteData.state : null;
  }

  ngOnInit() {
    if (!localStorage.getItem(LocalStorageKeys.SKIP_INTRO)) {
      localStorage.setItem(LocalStorageKeys.SKIP_INTRO, 'true');
      this.introductionService.startIntro();
    }

    let skipInitGA = true;

    this.enableTrackingSubscription = this.configurationService.getEnableTrackingAsObservable().subscribe((enabled) => {
      this.trackingEnabled = enabled;

      if (!skipInitGA) {
        this.initGoogleAnalytics();
      }

      skipInitGA = false;
    });

    let enableTracking = localStorage.getItem(LocalStorageKeys.ENABLE_TRACKING);

    if (enableTracking !== null) {
      this.configurationService.updateEnableTracking(enableTracking === 'true');
    }

    this.enablePageTransitionsSubscription = this.configurationService.getEnablePageTransitionsAsObservable().subscribe((enabled) => {
      this.pageTransitionsEnabled = enabled;
    });

    let enablePageTransitions = localStorage.getItem(LocalStorageKeys.ENABLE_PAGE_TRANSITIONS);

    if (enablePageTransitions) {
      this.configurationService.updateEnablePageTransitions(enablePageTransitions === 'true');
    }
  }

  ngAfterViewInit() {
    this.initGoogleAnalytics();
  }

  ngOnDestroy() {
    this.enableTrackingSubscription.unsubscribe();
    this.enablePageTransitionsSubscription.unsubscribe();
  }

  initGoogleAnalytics() {
    if(this.trackingEnabled && !this.googleAnalyticsInitialized) {
      let s = document.createElement("script");
      s.type = "text/javascript";
      s.src = "https://www.googletagmanager.com/gtag/js?id=UA-159370258-1";
      this.elementRef.nativeElement.appendChild(s);
      this.googleAnalyticsInitialized = true;
    }
  }

  @HostListener('window:resize', ['$event'])
  onResize(event) {
    if (event.target.innerWidth >= 768) {
      this.sidebarService.updateSidebarToggle(true);
    }
  }
}
