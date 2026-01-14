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
import { CipherStatisticsService } from "../cipher-statistics.service";
import {ConfigurationService} from "../configuration.service";

@Component({
    selector: 'app-cipher-stats-summary',
    templateUrl: './cipher-stats-summary.component.html',
    styleUrls: ['./cipher-stats-summary.component.css'],
    standalone: false
})
export class CipherStatsSummaryComponent {
  uniqueSymbols: number = null;
  multiplicity: number = null;
  entropy: number = null;
  indexOfCoincidence: number = null;
  bigramRepeats: number = null;
  cycleScore: number = null;
  selectedCipher = this.configurationService.selectedCipher;

  constructor(private statisticsService: CipherStatisticsService,
              private configurationService: ConfigurationService) {
    effect(() => {
      const selected = this.selectedCipher();
      if (selected == null) {
        return;
      }

      this.uniqueSymbols = null;
      this.multiplicity = null;
      this.entropy = null;
      this.indexOfCoincidence = null;
      this.bigramRepeats = null;
      this.cycleScore = null;

      this.statisticsService.getUniqueSymbols(selected).subscribe((response) => {
        this.uniqueSymbols = response.value;
      });

      this.statisticsService.getMultiplicity(selected).subscribe((response) => {
        this.multiplicity = response.value;
      });

      this.statisticsService.getEntropy(selected).subscribe((response) => {
        this.entropy = response.value;
      });

      this.statisticsService.getIndexOfCoincidence(selected).subscribe((response) => {
        this.indexOfCoincidence = response.value;
      });

      this.statisticsService.getBigramRepeats(selected).subscribe((response) => {
        this.bigramRepeats = response.value;
      });

      this.statisticsService.getCycleScore(selected).subscribe((response) => {
        this.cycleScore = response.value;
      });
    });
  }
}
