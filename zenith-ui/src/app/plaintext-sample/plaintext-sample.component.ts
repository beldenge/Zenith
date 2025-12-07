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

import { AfterContentInit, Component, Input, OnInit } from '@angular/core';
import { ConfigurationService } from "../configuration.service";
import { Subscription } from "rxjs";

@Component({
    selector: 'app-plaintext-sample',
    templateUrl: './plaintext-sample.component.html',
    styleUrls: ['./plaintext-sample.component.css'],
    standalone: false
})
export class PlaintextSampleComponent implements OnInit, AfterContentInit  {
  @Input() transformedSample: string;
  sample: string = ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT;
  samplePlaintextSubscription: Subscription;

  constructor(private configurationService: ConfigurationService) { }

  ngOnInit() {
    this.samplePlaintextSubscription = this.configurationService.getSamplePlaintextAsObservable().subscribe(sample => {
      this.sample = sample;
    });
  }

  ngAfterContentInit() {
    // We just want the initial value, then don't take updates anymore
    this.samplePlaintextSubscription.unsubscribe();
  }

  reset(sampleEditor: HTMLElement) {
    sampleEditor.textContent = ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT;
    this.configurationService.updateSamplePlaintext(ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT);
  }

  edit(sampleEditor: HTMLElement) {
    this.configurationService.updateSamplePlaintext(sampleEditor.textContent);
  }
}
