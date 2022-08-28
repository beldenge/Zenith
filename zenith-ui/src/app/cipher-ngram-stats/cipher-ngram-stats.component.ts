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

import { AfterViewInit, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
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
  styleUrls: ['./cipher-ngram-stats.component.css']
})
export class CipherNgramStatsComponent implements OnInit, OnDestroy, AfterViewInit {
  // Workaround for angular component issue #13870
  disableAnimation = true;
  selectedCipher: Cipher;
  selectedCipherSubscription: Subscription;
  unigramsDataSource: MatTableDataSource<any>;
  bigramsDataSource: MatTableDataSource<any>;
  trigramsDataSource: MatTableDataSource<any>;
  quadrigramsDataSource: MatTableDataSource<any>;
  pentagramsDataSource: MatTableDataSource<any>;
  hexagramsDataSource: MatTableDataSource<any>;
  headerTextOpened = "Hide ngram statistics";
  headerTextClosed = "Show ngram statistics";
  headerText = this.headerTextClosed;
  shown: boolean = false;

  @ViewChild(MatExpansionPanel, { static: true }) matExpansionPanelElement: MatExpansionPanel;

  constructor(private cipherService: CipherService, private cipherStatisticsService: CipherStatisticsService) { }

  ngOnInit(): void {
    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      this.selectedCipher = selectedCipher;
      this.matExpansionPanelElement.close();
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
  }

  ngAfterViewInit(): void {
    // timeout required to avoid the dreaded 'ExpressionChangedAfterItHasBeenCheckedError'
    setTimeout(() => this.disableAnimation = false);
  }

  onCollapse() {
    this.shown = false;
    this.headerText = this.headerTextClosed;
  }

  onExpand(): void {
    this.cipherStatisticsService.getNgramStatistics(this.selectedCipher).subscribe((response: NgramStatisticsResponse) => {
      let sortFunction = (a, b) => a.count < b.count ? 1 : (a.count > b.count ? -1 : 0);
      this.unigramsDataSource = new MatTableDataSource(response.unigramCounts.sort(sortFunction));
      this.bigramsDataSource = new MatTableDataSource(response.bigramCounts.sort(sortFunction));
      this.trigramsDataSource = new MatTableDataSource(response.trigramCounts.sort(sortFunction));
      this.quadrigramsDataSource = new MatTableDataSource(response.quadrigramCounts.sort(sortFunction));
      this.pentagramsDataSource = new MatTableDataSource(response.pentagramCounts.sort(sortFunction));
      this.hexagramsDataSource = new MatTableDataSource(response.hexagramCounts.sort(sortFunction));
      this.shown = true;
      this.headerText = this.headerTextOpened;
    });
  }
}
