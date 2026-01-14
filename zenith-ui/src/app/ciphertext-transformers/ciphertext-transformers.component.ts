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
import { CipherService } from "../cipher.service";
import { UntypedFormGroup } from "@angular/forms";
import { ConfigurationService } from "../configuration.service";
import { IntroductionService } from "../introduction.service";
import { CiphertextTransformationRequest } from "../models/CiphertextTransformationRequest";
import { SolutionService } from "../solution.service";
import { TransformerUtil } from "../util/transformer-util";

@Component({
    selector: 'app-ciphertext-transformers',
    templateUrl: './ciphertext-transformers.component.html',
    styleUrls: ['./ciphertext-transformers.component.css'],
    standalone: false
})
export class CiphertextTransformersComponent {
  showIntro = this.introductionService.showIntroCiphertextTransformers;
  cipher = this.configurationService.selectedCipher;
  public hoverClasses: string[] = [];
  availableTransformers = this.configurationService.availableCiphertextTransformers;

  availableTransformersOptions = {
    group: {
      name: 'clone-group',
      pull: 'clone',
      put: false
    },
    sort: false
  };

  appliedTransformersLocal: FormComponent[] = [];
  appliedTransformersSignal = this.configurationService.appliedCiphertextTransformers;

  onAppliedTransformersChange = (event: any) => {
    this.solutionService.updateSolution(null);

    const localCipher = {...this.cipher()};
    if (!localCipher) {
      return;
    }

    const transformationRequest: CiphertextTransformationRequest = {
      cipher: {
        name: localCipher.name,
        rows: localCipher.rows,
        columns: localCipher.columns,
        ciphertext: localCipher.ciphertext
      },
      steps: []
    };

    let satisfied = true;

    for (let i = 0; i < this.appliedTransformersLocal.length; i ++) {
      const transformer = this.appliedTransformersLocal[i];

      if (transformer.form && ((event && event.type === 'add' && event.newIndex === i) || !transformer.form.form.valid)) {
        satisfied = false;
        return;
      }

      transformationRequest.steps.push({
        transformerName: transformer.name,
        data: transformer.form ? transformer.form.model : null
      });
    }

    if (satisfied) {
      if (!this.appliedTransformersLocal.length) {
        delete localCipher.transformed;
        this.configurationService.updateSelectedCipher(localCipher);
      } else {
        this.cipherService.transformCipher(transformationRequest).subscribe((cipherResponse: any) => {
          localCipher.transformed = cipherResponse;
          this.configurationService.updateSelectedCipher(localCipher);
        });
      }

      if (!event?.skipUpdate) {
        this.configurationService.updateAppliedCiphertextTransformers(this.appliedTransformersLocal);
      }
    }
  }

  appliedTransformersOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChange,
    onEnd: this.onAppliedTransformersChange
  };

  constructor(private cipherService: CipherService,
              private configurationService: ConfigurationService,
              private introductionService: IntroductionService,
              private solutionService: SolutionService) {
    effect(() => {
      if (this.showIntro()) {
        setTimeout(() => {
          this.introductionService.startIntroCiphertextTransformers();
          this.introductionService.updateShowIntroCiphertextTransformers(false);
        }, 0);
      }
    });

    effect(() => {
      if (!TransformerUtil.transformersAreEqual(this.appliedTransformersLocal, this.appliedTransformersSignal())) {
        this.appliedTransformersLocal = this.appliedTransformersSignal();

        if (this.appliedTransformersLocal?.length) {
          this.onAppliedTransformersChange({ skipUpdate: true });
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
