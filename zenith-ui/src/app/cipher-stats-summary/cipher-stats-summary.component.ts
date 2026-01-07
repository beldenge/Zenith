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
import { CipherService } from "../cipher.service";
import { CipherStatisticsService } from "../cipher-statistics.service";
import { Subscription } from "rxjs";

@Component({
    selector: 'app-cipher-stats-summary',
    templateUrl: './cipher-stats-summary.component.html',
    styleUrls: ['./cipher-stats-summary.component.css'],
    standalone: false
})
export class CipherStatsSummaryComponent implements OnInit, OnDestroy {
  uniqueSymbols: number = null;
  multiplicity: number = null;
  entropy: number = null;
  indexOfCoincidence: number = null;
  bigramRepeats: number = null;
  cycleScore: number = null;
  selectedCipher: Cipher;
  selectedCipherSubscription: Subscription;

  constructor(private cipherService: CipherService, private statisticsService: CipherStatisticsService) { }

  ngOnInit() {
    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      if (selectedCipher == null) {
        return;
      }

      let skipStatistics = false;

      if (this.selectedCipher && this.selectedCipher.name === selectedCipher.name) {
        skipStatistics = true;
      }

      this.selectedCipher = selectedCipher;

      if (skipStatistics) {
        return;
      }

      this.uniqueSymbols = null;
      this.multiplicity = null;
      this.entropy = null;
      this.indexOfCoincidence = null;
      this.bigramRepeats = null;
      this.cycleScore = null;

      this.statisticsService.getUniqueSymbols(selectedCipher).subscribe((response) => {
        this.uniqueSymbols = response.value;
      });

      this.statisticsService.getMultiplicity(selectedCipher).subscribe((response) => {
        this.multiplicity = response.value;
      });

      this.statisticsService.getEntropy(selectedCipher).subscribe((response) => {
        this.entropy = response.value;
      });

      this.statisticsService.getIndexOfCoincidence(selectedCipher).subscribe((response) => {
        this.indexOfCoincidence = response.value;
      });

      this.statisticsService.getBigramRepeats(selectedCipher).subscribe((response) => {
        this.bigramRepeats = response.value;
      });

      this.statisticsService.getCycleScore(selectedCipher).subscribe((response) => {
        this.cycleScore = response.value;
      });
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
  }
}
