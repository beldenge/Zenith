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

import {Component, effect} from '@angular/core';
import { FormComponent } from "../models/FormComponent";
import { UntypedFormGroup } from "@angular/forms";
import { SamplePlaintextTransformationRequest } from "../models/SamplePlaintextTransformationRequest";
import { ConfigurationService } from "../configuration.service";
import { IntroductionService } from "../introduction.service";
import { SolutionService } from "../solution.service";
import { TransformerUtil } from "../util/transformer-util";
import {PlaintextService} from "../plaintext.service";

@Component({
    selector: 'app-plaintext-transformers',
    templateUrl: './plaintext-transformers.component.html',
    styleUrls: ['./plaintext-transformers.component.css'],
    standalone: false
})
export class PlaintextTransformersComponent {
  showIntro = this.introductionService.showIntroPlaintextTransformers;
  public hoverClasses: string[] = [];
  sample = this.configurationService.samplePlaintext;
  transformedSample: string;

  availableTransformers = this.configurationService.availablePlaintextTransformers;

  availableTransformersOptions = {
    group: {
      name: 'clone-group',
      pull: 'clone',
      put: false
    },
    sort: false
  };

  appliedTransformersLocal: FormComponent[] = [];
  appliedTransformersSignal = this.configurationService.appliedPlaintextTransformers;

  onAppliedTransformersChange = (event: any) => {
    this.solutionService.updateSolution(null);

    const transformationRequest: SamplePlaintextTransformationRequest = {
      plaintext: this.sample(),
      plaintextTransformers: []
    };

    let satisfied = !!this.sample();

    for (let i = 0; i < this.appliedTransformersLocal.length; i ++) {
      const transformer = this.appliedTransformersLocal[i];

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
      if (!this.appliedTransformersLocal?.length) {
        this.transformedSample = null;
      } else {
        this.plaintextService.transformSample(transformationRequest).subscribe((response: any) => {
          this.transformedSample = response.plaintext;
        });
      }

      if (!event?.skipUpdate) {
        this.configurationService.updateAppliedPlaintextTransformers(this.appliedTransformersLocal);
      }
    }
  }

  appliedTransformersOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChange,
    onEnd: this.onAppliedTransformersChange
  };

  constructor(private configurationService: ConfigurationService,
              private introductionService: IntroductionService,
              private solutionService: SolutionService,
              private plaintextService: PlaintextService) {
    effect(() => {
      if (this.showIntro()) {
        setTimeout(() => {
          this.introductionService.startIntroPlaintextTransformers();
          this.introductionService.updateShowIntroPlaintextTransformers(false);
        }, 0);
      }
    });

    effect(() => {
      if (!TransformerUtil.transformersAreEqual(this.appliedTransformersLocal, this.appliedTransformersSignal())) {
        this.appliedTransformersLocal = this.appliedTransformersSignal();

        if (this.appliedTransformersLocal?.length) {
          this.onAppliedTransformersChange({skipUpdate: true});
        }
      }
    });
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

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformersLocal.length) {
      this.appliedTransformersLocal.splice(transformerIndex, 1);
    }

    this.onAppliedTransformersChange(null);
  }
}
