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

import { Injectable } from '@angular/core';
import { environment } from "../environments/environment";
import { HttpClient } from "@angular/common/http";
import { NumberResponse } from "./models/NumberResponse";
import { shareReplay } from "rxjs/operators";

const ENDPOINT_URL = environment.apiUrlBase + '/statistics';

@Injectable({
  providedIn: 'root'
})
export class CipherStatisticsService {
  uniqueSymbolsObservables = {};
  multiplicityObservables = {};
  entropyObservables = {};
  indexOfCoincidenceObservables = {};
  bigramRepeatsObservables = {};
  cycleScoreObservables = {};
  constructor(private http: HttpClient) { }

  getUniqueSymbols(cipherName: string) {
    if (!this.uniqueSymbolsObservables.hasOwnProperty(cipherName)) {
      this.uniqueSymbolsObservables[cipherName] = this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/uniqueSymbols').pipe(shareReplay(1));
    }

    return this.uniqueSymbolsObservables[cipherName];
  }

  getMultiplicity(cipherName: string) {
    if (!this.multiplicityObservables.hasOwnProperty(cipherName)) {
      this.multiplicityObservables[cipherName] = this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/multiplicity').pipe(shareReplay(1));
    }

    return this.multiplicityObservables[cipherName];
  }

  getEntropy(cipherName: string) {
    if (!this.entropyObservables.hasOwnProperty(cipherName)) {
      this.entropyObservables[cipherName] = this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/entropy').pipe(shareReplay(1));
    }

    return this.entropyObservables[cipherName];
  }

  getIndexOfCoincidence(cipherName: string) {
    if (!this.indexOfCoincidenceObservables.hasOwnProperty(cipherName)) {
      this.indexOfCoincidenceObservables[cipherName] = this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/indexOfCoincidence').pipe(shareReplay(1));
    }

    return this.indexOfCoincidenceObservables[cipherName];
  }

  getBigramRepeats(cipherName: string) {
    if (!this.bigramRepeatsObservables.hasOwnProperty(cipherName)) {
      this.bigramRepeatsObservables[cipherName] = this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/bigramRepeats').pipe(shareReplay(1));
    }

    return this.bigramRepeatsObservables[cipherName];
  }

  getCycleScore(cipherName: string) {
    if (!this.cycleScoreObservables.hasOwnProperty(cipherName)) {
      this.cycleScoreObservables[cipherName] = this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/cycleScore').pipe(shareReplay(1));
    }

    return this.cycleScoreObservables[cipherName];
  }
}
