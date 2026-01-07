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

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { Subscription } from "rxjs";
import { CipherStatisticsService } from "../cipher-statistics.service";
import { MatTableDataSource } from "@angular/material/table";
import { NgramStatisticsResponse } from "../models/NgramStatisticsResponse";
import { MatExpansionPanel } from "@angular/material/expansion";

@Component({
    selector: 'app-cipher-ngram-stats',
    templateUrl: './cipher-ngram-stats.component.html',
    styleUrls: ['./cipher-ngram-stats.component.css'],
    standalone: false
})
export class CipherNgramStatsComponent implements OnInit, OnDestroy {
  selectedCipher: Cipher;
  selectedCipherSubscription: Subscription;
  ngramsDataSources: MatTableDataSource<any>[][] = [];
  headerTextOpened = 'Hide ngram statistics';
  headerTextClosed = 'Show ngram statistics';
  headerText = this.headerTextClosed;
  shown = false;

  @ViewChild(MatExpansionPanel, { static: true }) matExpansionPanelElement: MatExpansionPanel;

  constructor(private cipherService: CipherService,
              private cipherStatisticsService: CipherStatisticsService) {}

  ngOnInit(): void {
    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      this.selectedCipher = selectedCipher;
      this.matExpansionPanelElement.close();
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
  }

  onCollapse() {
    this.shown = false;
    this.headerText = this.headerTextClosed;
  }

  onExpand(): void {
    this.shown = true;
    this.headerText = this.headerTextOpened;
    this.onMore();
  }

  onMore(): void {
    this.cipherStatisticsService.getNgramStatistics(this.selectedCipher, this.ngramsDataSources?.length).subscribe((response: NgramStatisticsResponse) => {
      const sortFunction = (a, b) => a.count < b.count ? 1 : (a.count > b.count ? -1 : 0);
      this.ngramsDataSources.push([]);
      this.ngramsDataSources[0].push(new MatTableDataSource(response.firstNGramCounts.sort(sortFunction)));
      this.ngramsDataSources[0].push(new MatTableDataSource(response.secondNGramCounts.sort(sortFunction)));
      this.ngramsDataSources[0].push(new MatTableDataSource(response.thirdNGramCounts.sort(sortFunction)));
    });
  }
}
