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

import { AfterViewInit, Component, OnDestroy, OnInit } from '@angular/core';
import { IntroductionService } from "../introduction.service";
import { UntypedFormBuilder } from "@angular/forms";
import { ConfigurationService } from "../configuration.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'app-help',
  templateUrl: './help.component.html',
  styleUrls: ['./help.component.css']
})
export class HelpComponent implements OnInit, OnDestroy, AfterViewInit {
  // Workaround for angular component issue #13870
  disableAnimation = true;
  enableTrackingSubscription: Subscription;
  enablePageTransitionsSubscription: Subscription;

  applicationSettingsForm = this.fb.group({
    enableTracking: [true],
    enablePageTransitions: [true]
  });

  constructor(private fb: UntypedFormBuilder, private introductionService: IntroductionService, private configurationService: ConfigurationService) { }

  ngOnInit() {
    this.enableTrackingSubscription = this.configurationService.getEnableTrackingAsObservable().subscribe(enabled => {
      if (this.applicationSettingsForm.get('enableTracking').value !== enabled) {
        this.applicationSettingsForm.patchValue({ 'enableTracking': enabled });
      }
    });

    this.enablePageTransitionsSubscription = this.configurationService.getEnablePageTransitionsAsObservable().subscribe(enabled => {
      if (this.applicationSettingsForm.get('enablePageTransitions').value !== enabled) {
        this.applicationSettingsForm.patchValue({ 'enablePageTransitions': enabled });
      }
    });
  }

  ngOnDestroy() {
    this.enableTrackingSubscription.unsubscribe();
    this.enablePageTransitionsSubscription.unsubscribe();
  }

  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);
  }

  replayIntroduction() {
    this.introductionService.startIntro();
  }

  onTrackingToggleChange() {
    this.configurationService.updateEnableTracking(this.applicationSettingsForm.get('enableTracking').value);
  }

  onPageTransitionToggleChange() {
    this.configurationService.updateEnablePageTransitions(this.applicationSettingsForm.get('enablePageTransitions').value);
  }
}
