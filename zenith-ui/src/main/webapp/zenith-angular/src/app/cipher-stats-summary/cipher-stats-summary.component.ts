import { Component, OnDestroy, OnInit } from '@angular/core';
import { Cipher } from "../models/Cipher";
import { CipherService } from "../cipher.service";
import { CipherStatisticsService } from "../cipher-statistics.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'app-cipher-stats-summary',
  templateUrl: './cipher-stats-summary.component.html',
  styleUrls: ['./cipher-stats-summary.component.css']
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

      if (this.selectedCipher && this.selectedCipher.ciphertext === selectedCipher.ciphertext) {
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

      this.statisticsService.getUniqueSymbols(selectedCipher.name).subscribe((response) => {
        this.uniqueSymbols = response.value;
      });

      this.statisticsService.getMultiplicity(selectedCipher.name).subscribe((response) => {
        this.multiplicity = response.value;
      });

      this.statisticsService.getEntropy(selectedCipher.name).subscribe((response) => {
        this.entropy = response.value;
      });

      this.statisticsService.getIndexOfCoincidence(selectedCipher.name).subscribe((response) => {
        this.indexOfCoincidence = response.value;
      });

      this.statisticsService.getBigramRepeats(selectedCipher.name).subscribe((response) => {
        this.bigramRepeats = response.value;
      });

      this.statisticsService.getCycleScore(selectedCipher.name).subscribe((response) => {
        this.cycleScore = response.value;
      });
    });
  }

  ngOnDestroy() {
    this.selectedCipherSubscription.unsubscribe();
  }
}
