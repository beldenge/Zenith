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

import { Component, OnDestroy, OnInit } from '@angular/core';
import { Subscription } from "rxjs";
import { animate, style, transition, trigger } from "@angular/animations";
import { PlaintextTransformerService } from "../plaintext-transformer.service";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { FormGroup } from "@angular/forms";
import { SamplePlaintextTransformationRequest } from "../models/SamplePlaintextTransformationRequest";
import { ConfigurationService } from "../configuration.service";
import { IntroductionService } from "../introduction.service";

@Component({
  selector: 'app-plaintext-transformers',
  templateUrl: './plaintext-transformers.component.html',
  styleUrls: ['./plaintext-transformers.component.css'],
  animations: [
    // the fade-in/fade-out animation.
    trigger('simpleFadeAnimation', [
      transition(':leave',
        animate(300, style({ opacity: 0 })))
    ])
  ]
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

  // On adding of a new item to the Sortable list, the event fires before the formly form is initialized, so we cannot rely on validation alone
  onAppliedTransformersChangeNew = (event: any) => {
    this.onAppliedTransformersChange({ isNew: true });
  };

  onAppliedTransformersChange = (event: any) => {
    if (!this.appliedTransformers.length) {
      this.transformedSample = null;
      return;
    }

    let transformationRequest: SamplePlaintextTransformationRequest = {
      plaintext: this.sample,
      plaintextTransformers: []
    };

    let satisfied = true;

    this.appliedTransformers.forEach(transformer => {
      if (transformer.form && ((event && event.isNew) || !transformer.form.form.valid)) {
        satisfied = false;
        return;
      }

      transformationRequest.plaintextTransformers.push({
        transformerName: transformer.name,
        data: transformer.form ? transformer.form.model : null
      });
    });

    if (satisfied) {
      this.transformerService.transformSample(transformationRequest).subscribe(response => {
        this.transformedSample = response.plaintext;
      });

      if (!event || !event.skipUpdate) {
        this.configurationService.updateAppliedPlaintextTransformers(this.appliedTransformers);
      }
    }
  };

  appliedTransformersOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChangeNew,
    onRemove: this.onAppliedTransformersChange,
    onMove: this.onAppliedTransformersChange
  };

  constructor(private transformerService: PlaintextTransformerService, private configurationService: ConfigurationService, private introductionService: IntroductionService) {}

  ngOnInit(): void {
    this.transformerService.getTransformers().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse.transformers.sort((t1, t2) => {
        return t1.order - t2.order;
      });
    });

    this.appliedPlaintextTransformersSubscription = this.configurationService.getAppliedPlaintextTransformersAsObservable().subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
      this.onAppliedTransformersChange({ skipUpdate: true });
    });

    this.samplePlaintextSubscription = this.configurationService.getSamplePlaintextAsObservable().subscribe(sample => {
      this.sample = sample;
      this.onAppliedTransformersChange(null);
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
    let clone = {
      name: item.name,
      displayName: item.displayName,
      form: item.form ? JSON.parse(JSON.stringify(item.form)) : null
    };

    if (clone.form) {
      clone.form.form = new FormGroup({});
    }

    return clone;
  };

  removeTransformer(transformerIndex: number): void {
    this.hoverClasses = [];

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformers.length) {
      this.appliedTransformers.splice(transformerIndex, 1);
    }

    this.onAppliedTransformersChange(null);
  }
}
