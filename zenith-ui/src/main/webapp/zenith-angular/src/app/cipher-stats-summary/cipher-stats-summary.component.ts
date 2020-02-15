import { Component, Input, OnInit } from '@angular/core';
import { Cipher } from "../models/Cipher";
import {CipherService} from "../cipher.service";
import {CipherStatisticsService} from "../cipher-statistics.service";

@Component({
  selector: 'app-cipher-stats-summary',
  templateUrl: './cipher-stats-summary.component.html',
  styleUrls: ['./cipher-stats-summary.component.css']
})
export class CipherStatsSummaryComponent implements OnInit {
  multiplicity: number = null;
  entropy: number = null;
  indexOfCoincidence: number = null;
  chiSquared: number = null;
  bigramRepeats: number = null;
  cycleScore: number = null;

  constructor(private cipherService: CipherService, private statisticsService: CipherStatisticsService) { }

  ngOnInit() {
    this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      if (selectedCipher == null) {
        return;
      }

      this.multiplicity = null;
      this.entropy = null;
      this.indexOfCoincidence = null;
      this.chiSquared = null;
      this.bigramRepeats = null;
      this.cycleScore = null;

      this.statisticsService.getMultiplicity(selectedCipher.name).subscribe((response) => {
        this.multiplicity = response.value;
      });

      this.statisticsService.getEntropy(selectedCipher.name).subscribe((response) => {
        this.entropy = response.value;
      });

      this.statisticsService.getIndexOfCoincidence(selectedCipher.name).subscribe((response) => {
        this.indexOfCoincidence = response.value;
      });

      this.statisticsService.getChiSquared(selectedCipher.name).subscribe((response) => {
        this.chiSquared = response.value;
      });

      this.statisticsService.getBigramRepeats(selectedCipher.name).subscribe((response) => {
        this.bigramRepeats = response.value;
      });

      this.statisticsService.getCycleScore(selectedCipher.name).subscribe((response) => {
        this.cycleScore = response.value;
      });
    });
  }
}
