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
import { animate, style, transition, trigger } from "@angular/animations";
import { ZenithTransformer } from "../models/ZenithTransformer";
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { Subscription } from "rxjs";
import { FormGroup } from "@angular/forms";
import { ConfigurationService } from "../configuration.service";
import { IntroductionService } from "../introduction.service";
import { CiphertextTransformationRequest } from "../models/CiphertextTransformationRequest";

@Component({
  selector: 'app-ciphertext-transformers',
  templateUrl: './ciphertext-transformers.component.html',
  styleUrls: ['./ciphertext-transformers.component.css'],
  animations: [
    // the fade-in/fade-out animation.
    trigger('simpleFadeAnimation', [
      transition(':leave',
        animate(300, style({ opacity: 0 })))
    ])
  ]
})
export class CiphertextTransformersComponent implements OnInit, OnDestroy {
  showIntroCiphertextTransformersSubscription: Subscription;
  cipher: Cipher;
  public hoverClasses: string[] = [];
  selectedCipherSubscription: Subscription;
  appliedCiphertextTransformersSubscription: Subscription;

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
    if (!this.cipher) {
      return;
    }

    let transformationRequest: CiphertextTransformationRequest = {
      cipher: this.cipher,
      steps: []
    };

    let satisfied = true;

    this.appliedTransformers.forEach(transformer => {
      if (transformer.form && ((event && event.isNew) || !transformer.form.form.valid)) {
        satisfied = false;
        return;
      }

      transformationRequest.steps.push({
        transformerName: transformer.name,
        data: transformer.form ? transformer.form.model : null
      });
    });

    if (satisfied) {
      this.cipherService.transformCipher(transformationRequest).subscribe(cipherResponse => {
        this.cipherService.updateSelectedCipher(cipherResponse.ciphers[0]);
      });

      if (!event || !event.skipUpdate) {
        this.configurationService.updateAppliedCiphertextTransformers(this.appliedTransformers);
      }
    }
  };

  // On adding of a new item to the Sortable list, the event fires before the formly form is initialized, so we cannot rely on validation alone
  onAppliedTransformersChangeNew = (event: any) => {
    this.onAppliedTransformersChange({ isNew: true });
  };

  appliedTransformersOptions = {
    group: 'clone-group',
    onAdd: this.onAppliedTransformersChangeNew,
    onRemove: this.onAppliedTransformersChange,
    onMove: this.onAppliedTransformersChange
  };

  constructor(private cipherService: CipherService, private configurationService: ConfigurationService, private introductionService: IntroductionService) {}

  ngOnInit(): void {
    this.configurationService.getAvailableCiphertextTransformersAsObservable().subscribe(transformerResponse => {
      this.availableTransformers = transformerResponse;
    });

    this.configurationService.getAppliedCiphertextTransformersAsObservable().subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
      this.onAppliedTransformersChange({ skipUpdate: true });
    });

    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(cipher => {
      this.cipher = cipher;
    });

    this.appliedCiphertextTransformersSubscription = this.configurationService.getAppliedCiphertextTransformersAsObservable().subscribe(appliedTransformers => {
      this.appliedTransformers = appliedTransformers;
    });

    this.showIntroCiphertextTransformersSubscription = this.introductionService.getShowIntroCiphertextTransformersAsObservable().subscribe(showIntro => {
      if (showIntro) {
        setTimeout(() => {
          this.introductionService.startIntroCiphertextTransformers();
          this.introductionService.updateShowIntroCiphertextTransformers(false);
        }, 500);
      }
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
    this.appliedCiphertextTransformersSubscription.unsubscribe();
    this.showIntroCiphertextTransformersSubscription.unsubscribe();
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
