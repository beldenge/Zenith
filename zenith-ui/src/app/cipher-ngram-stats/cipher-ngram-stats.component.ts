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

import {Component, effect, ViewChild} from '@angular/core';
import { CipherStatisticsService } from "../cipher-statistics.service";
import { MatTableDataSource } from "@angular/material/table";
import { MatExpansionPanel } from "@angular/material/expansion";
import {ConfigurationService} from "../configuration.service";

@Component({
    selector: 'app-cipher-ngram-stats',
    templateUrl: './cipher-ngram-stats.component.html',
    styleUrls: ['./cipher-ngram-stats.component.css'],
    standalone: false
})
export class CipherNgramStatsComponent {
  selectedCipher = this.configurationService.selectedCipher;
  ngramsDataSources: MatTableDataSource<any>[][] = [];
  headerTextOpened = 'Hide ngram statistics';
  headerTextClosed = 'Show ngram statistics';

  @ViewChild(MatExpansionPanel, { static: true }) matExpansionPanelElement: MatExpansionPanel;

  constructor(private cipherStatisticsService: CipherStatisticsService,
              private configurationService: ConfigurationService) {
    effect(() => {
      if (!!this.selectedCipher()) {
        this.matExpansionPanelElement.close();
        // BUG FIX: Clear cached ngram data when cipher changes.
        // Previously, old cipher's data would be shown briefly when expanding
        // the panel for a new cipher, since ngramsDataSources was not reset.
        this.ngramsDataSources = [];
      }
    });
  }

  onCollapse() {
    // No-op: expansion panel state drives the UI directly in the template.
  }

  onExpand(): void {
    // BUG FIX: Avoid mutating view-bound state during MatExpansionPanel change detection.
    if (!this.ngramsDataSources?.length) {
      setTimeout(() => this.onMore(), 0);
    }
  }

  onMore(): void {
    const statsPage = this.ngramsDataSources?.length;
    this.cipherStatisticsService.getNgramStatistics(this.selectedCipher(), statsPage).subscribe((response) => {
      const sortFunction = (a, b) => a.count < b.count ? 1 : (a.count > b.count ? -1 : 0);
      this.ngramsDataSources.push([]);
      this.ngramsDataSources[statsPage].push(new MatTableDataSource([...response.firstNGramCounts].sort(sortFunction)));
      this.ngramsDataSources[statsPage].push(new MatTableDataSource([...response.secondNGramCounts].sort(sortFunction)));
      this.ngramsDataSources[statsPage].push(new MatTableDataSource([...response.thirdNGramCounts].sort(sortFunction)));
    });
  }
}
