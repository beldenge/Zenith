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
import { BehaviorSubject, Subscription } from "rxjs";
import { BlockifyPipe } from "../blockify.pipe";
import { CipherService } from "../cipher.service";
import { MatTooltip } from "@angular/material/tooltip";
import { SolutionService } from "../solution.service";
import { SolutionResponse } from "../models/SolutionResponse";

const originalTooltipText = 'Copy to clipboard';

@Component({
    selector: 'app-plaintext',
    templateUrl: './plaintext.component.html',
    styleUrls: ['./plaintext.component.css'],
    standalone: false
})
export class PlaintextComponent implements OnInit, OnDestroy {
  cipher: Cipher;
  solution: SolutionResponse;
  tooltipText = new BehaviorSubject<string>(originalTooltipText);
  blockifyPipe = new BlockifyPipe();
  selectedCipherSubscription: Subscription;
  solutionSubscription: Subscription;

  constructor(private cipherService: CipherService, private solutionService: SolutionService) {}

  ngOnInit() {
    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(cipher => {
      this.cipher = cipher;
    });

    this.solutionSubscription = this.solutionService.getSolutionAsObservable().subscribe(solution => {
      this.solution = solution;
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
    this.solutionSubscription.unsubscribe();
  }

  copyPlaintext(tooltip : MatTooltip) {
    const plaintextElement = document.createElement("textarea");
    plaintextElement.id = 'txt';
    plaintextElement.style.position = 'fixed';
    plaintextElement.style.top = '0';
    plaintextElement.style.left = '0';
    plaintextElement.style.opacity = '0';
    plaintextElement.value = this.blockifyPipe.transform(this.solution.plaintext.split(' '), this.cipher.columns).toString();
    document.body.appendChild(plaintextElement);
    plaintextElement.select();
    document.execCommand('copy');

    tooltip.hide();
    this.tooltipText.next('Copied!');
    tooltip.show();
  }

  resetTooltipText() {
    this.tooltipText.next(originalTooltipText);
  }
}
