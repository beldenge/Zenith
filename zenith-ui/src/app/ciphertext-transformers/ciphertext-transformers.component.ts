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
import { FormComponent } from "../models/FormComponent";
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { Subscription } from "rxjs";
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
export class CiphertextTransformersComponent implements OnInit, OnDestroy {
  showIntroCiphertextTransformersSubscription: Subscription;
  cipher: Cipher;
  public hoverClasses: string[] = [];
  selectedCipherSubscription: Subscription;
  appliedCiphertextTransformersSubscription: Subscription;

  availableTransformers: FormComponent[] = [];

  availableTransformersOptions = {
    group: {
      name: 'clone-group',
      pull: 'clone',
      put: false
    },
    sort: false
  };

  appliedTransformers: FormComponent[] = [];

  onAppliedTransformersChange = (event: any) => {
    this.solutionService.updateSolution(null);

    if (!this.cipher) {
      return;
    }

    const transformationRequest: CiphertextTransformationRequest = {
      cipher: this.cipher,
      steps: []
    };

    let satisfied = true;

    for (let i = 0; i < this.appliedTransformers.length; i ++) {
      const transformer = this.appliedTransformers[i];

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
      if (!this.appliedTransformers.length) {
        delete this.cipher.transformed;
        this.cipherService.updateSelectedCipher(this.cipher);
      } else {
        this.cipherService.transformCipher(transformationRequest).subscribe(cipherResponse => {
          this.cipher.transformed = cipherResponse.ciphers[0];
          this.cipherService.updateSelectedCipher(this.cipher);
        });
      }

      if (!event || !event.skipUpdate) {
        this.configurationService.updateAppliedCiphertextTransformers(this.appliedTransformers);
      }
    }
  }

  appliedTransformersOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChange,
    onRemove: this.onAppliedTransformersChange,
    onEnd: this.onAppliedTransformersChange
  };

  constructor(private cipherService: CipherService,
              private configurationService: ConfigurationService,
              private introductionService: IntroductionService,
              private solutionService: SolutionService) {}

  ngOnInit(): void {
    this.configurationService.getAvailableCiphertextTransformersAsObservable().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse;
    });

    this.appliedCiphertextTransformersSubscription = this.configurationService.getAppliedCiphertextTransformersAsObservable().subscribe(appliedTransformers => {
      if (!TransformerUtil.transformersAreEqual(this.appliedTransformers, appliedTransformers)) {
        this.appliedTransformers = appliedTransformers;

        if (this.appliedTransformers.length) {
          this.onAppliedTransformersChange({ skipUpdate: true });
        }
      }
    });

    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(cipher => {
        this.cipher = cipher;
    });

    this.showIntroCiphertextTransformersSubscription = this.introductionService.getShowIntroCiphertextTransformersAsObservable().subscribe(showIntro => {
      if (showIntro) {
        setTimeout(() => {
          this.introductionService.startIntroCiphertextTransformers();
          this.introductionService.updateShowIntroCiphertextTransformers(false);
        }, 0);
      }
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
    this.appliedCiphertextTransformersSubscription.unsubscribe();
    this.showIntroCiphertextTransformersSubscription.unsubscribe();
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
  };

  removeTransformer(transformerIndex: number): void {
    this.hoverClasses = [];

    if (transformerIndex >= 0 && transformerIndex < this.appliedTransformers.length) {
      this.appliedTransformers.splice(transformerIndex, 1);
    }

    this.onAppliedTransformersChange(null);
  }
}
