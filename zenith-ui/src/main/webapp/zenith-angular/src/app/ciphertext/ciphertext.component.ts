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
import { Cipher } from "../models/Cipher";
import { BehaviorSubject, Observable, Subscription } from "rxjs";
import { BlockifyPipe } from "../blockify.pipe";
import { CipherService } from "../cipher.service";
import { MatTooltip } from "@angular/material/tooltip";

const originalTooltipText = 'Copy to clipboard';

@Component({
  selector: 'app-ciphertext',
  templateUrl: './ciphertext.component.html',
  styleUrls: ['./ciphertext.component.css']
})
export class CiphertextComponent implements OnInit, OnDestroy {
  cipher: Cipher;
  cipher$: Observable<Cipher>;
  tooltipText = new BehaviorSubject<string>(originalTooltipText);
  blockifyPipe = new BlockifyPipe();
  cipherSubscription: Subscription;

  constructor(private cipherService: CipherService) {
    this.cipher$ = cipherService.getSelectedCipherAsObservable();
  }

  ngOnInit() {
    this.cipherSubscription = this.cipher$.subscribe(cipher => {
      this.cipher = cipher;
    });
  }

  ngOnDestroy() {
    this.cipherSubscription.unsubscribe();
  }

  copyCiphertext(tooltip : MatTooltip) {
    var ciphertextElement = document.createElement("textarea");
    ciphertextElement.id = 'txt';
    ciphertextElement.style.position = 'fixed';
    ciphertextElement.style.top = '0';
    ciphertextElement.style.left = '0';
    ciphertextElement.style.opacity = '0';
    ciphertextElement.value = this.blockifyPipe.transform(this.cipher.ciphertext, this.cipher.columns).toString();
    document.body.appendChild(ciphertextElement);
    ciphertextElement.select();
    document.execCommand('copy');

    tooltip.hide();
    this.tooltipText.next('Copied!');
    tooltip.show();
  }

  resetTooltipText() {
    this.tooltipText.next(originalTooltipText);
  }
}
