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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from "rxjs";
import { PlaintextTransformerService } from "../plaintext-transformer.service";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { UntypedFormGroup } from "@angular/forms";
import { SamplePlaintextTransformationRequest } from "../models/SamplePlaintextTransformationRequest";
import { ConfigurationService } from "../configuration.service";
import { IntroductionService } from "../introduction.service";
import { SolutionService } from "../solution.service";
import { TransformerUtil } from "../util/transformer-util";

@Component({
    selector: 'app-plaintext-transformers',
    templateUrl: './plaintext-transformers.component.html',
    styleUrls: ['./plaintext-transformers.component.css'],
    standalone: false
})
export class PlaintextTransformersComponent implements OnInit, OnDestroy {
  showIntroPlaintextTransformersSubscription: Subscription;
  public hoverClasses: string[] = [];
  sample: string;
  transformedSample: string;
  appliedPlaintextTransformersSubscription: Subscription;
  samplePlaintextSubscription: Subscription;

  availableTransformers: ZenithTransformer[] = [];

  availableTransformersOptions = {
    group: {
      name: 'clone-group',
      pull: 'clone',
      put: false
    },
    sort: false
  };

  appliedTransformers: ZenithTransformer[] = [];

  onAppliedTransformersChange = (event: any) => {
    this.solutionService.updateSolution(null);

    const transformationRequest: SamplePlaintextTransformationRequest = {
      plaintext: this.sample,
      plaintextTransformers: []
    };

    let satisfied = !!this.sample;

    for (let i = 0; i < this.appliedTransformers.length; i ++) {
      const transformer = this.appliedTransformers[i];

      if (transformer.form && ((event && event.type === 'add' && event.newIndex === i) || !transformer.form.form.valid)) {
        satisfied = false;
        return;
      }

      transformationRequest.plaintextTransformers.push({
        transformerName: transformer.name,
        data: transformer.form ? transformer.form.model : null
      });
    }

    if (satisfied) {
      if (!this.appliedTransformers || !this.appliedTransformers.length) {
        this.transformedSample = null;
      } else {
        this.transformerService.transformSample(transformationRequest).subscribe(response => {
          this.transformedSample = response.plaintext;
        });
      }

      if (!event || !event.skipUpdate) {
        this.configurationService.updateAppliedPlaintextTransformers(this.appliedTransformers);
      }
    }
  };

  appliedTransformersOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChange,
    onRemove: this.onAppliedTransformersChange,
    onEnd: this.onAppliedTransformersChange
  };

  constructor(private transformerService: PlaintextTransformerService,
              private configurationService: ConfigurationService,
              private introductionService: IntroductionService,
              private solutionService: SolutionService) {}

  ngOnInit(): void {
    this.configurationService.getAvailablePlaintextTransformersAsObservable().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse;
    });

    this.appliedPlaintextTransformersSubscription = this.configurationService.getAppliedPlaintextTransformersAsObservable().subscribe(appliedTransformers => {
      if (!TransformerUtil.transformersAreEqual(this.appliedTransformers, appliedTransformers)) {
        this.appliedTransformers = appliedTransformers;
        this.onAppliedTransformersChange({ skipUpdate: true });
      }
    });

    this.samplePlaintextSubscription = this.configurationService.getSamplePlaintextAsObservable().subscribe(sample => {
      if (!this.sample || this.sample !== sample) {
        this.sample = sample;
        this.onAppliedTransformersChange(null);
      }
    });

    this.showIntroPlaintextTransformersSubscription = this.introductionService.getShowIntroPlaintextTransformersAsObservable().subscribe(showIntro => {
      if (showIntro) {
        setTimeout(() => {
          this.introductionService.startIntroPlaintextTransformers();
          this.introductionService.updateShowIntroPlaintextTransformers(false);
        }, 500);
      }
    });
  }

  ngOnDestroy() {
    this.appliedPlaintextTransformersSubscription.unsubscribe();
    this.samplePlaintextSubscription.unsubscribe();
    this.showIntroPlaintextTransformersSubscription.unsubscribe();
  }

  cloneTransformer = (item) => {
    const clone = {
      name: item.name,
      displayName: item.displayName,
      form: item.form ? JSON.parse(JSON.stringify(item.form)) : null
    };

    if (clone.form) {
      clone.form.form = new UntypedFormGroup({});
    }

    return clone;
  }

  removeTransformer(transformerIndex: number): void {
    this.hoverClasses = [];

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformers.length) {
      this.appliedTransformers.splice(transformerIndex, 1);
    }

    this.onAppliedTransformersChange(null);
  }
}
