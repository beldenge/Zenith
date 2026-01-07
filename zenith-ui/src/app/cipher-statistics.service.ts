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
import { Cipher } from "./models/Cipher";

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
  ngramStatisticsObservables = {};

  constructor(private http: HttpClient) { }

  getUniqueSymbols(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.uniqueSymbolsObservables.hasOwnProperty(cipher.name)) {
      this.uniqueSymbolsObservables[cipher.name] = this.http.post<NumberResponse>(ENDPOINT_URL + '/uniqueSymbols', cipher).pipe(shareReplay(1));
    }

    return this.uniqueSymbolsObservables[cipher.name];
  }

  getMultiplicity(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.multiplicityObservables.hasOwnProperty(cipher.name)) {
      this.multiplicityObservables[cipher.name] = this.http.post<NumberResponse>(ENDPOINT_URL + '/multiplicity', cipher).pipe(shareReplay(1));
    }

    return this.multiplicityObservables[cipher.name];
  }

  getEntropy(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.entropyObservables.hasOwnProperty(cipher.name)) {
      this.entropyObservables[cipher.name] = this.http.post<NumberResponse>(ENDPOINT_URL + '/entropy', cipher).pipe(shareReplay(1));
    }

    return this.entropyObservables[cipher.name];
  }

  getIndexOfCoincidence(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.indexOfCoincidenceObservables.hasOwnProperty(cipher.name)) {
      this.indexOfCoincidenceObservables[cipher.name] = this.http.post<NumberResponse>(ENDPOINT_URL + '/indexOfCoincidence', cipher).pipe(shareReplay(1));
    }

    return this.indexOfCoincidenceObservables[cipher.name];
  }

  getBigramRepeats(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.bigramRepeatsObservables.hasOwnProperty(cipher.name)) {
      this.bigramRepeatsObservables[cipher.name] = this.http.post<NumberResponse>(ENDPOINT_URL + '/bigramRepeats', cipher).pipe(shareReplay(1));
    }

    return this.bigramRepeatsObservables[cipher.name];
  }

  getCycleScore(cipher: Cipher) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.cycleScoreObservables.hasOwnProperty(cipher.name)) {
      this.cycleScoreObservables[cipher.name] = this.http.post<NumberResponse>(ENDPOINT_URL + '/cycleScore', cipher).pipe(shareReplay(1));
    }

    return this.cycleScoreObservables[cipher.name];
  }

  getNgramStatistics(cipher: Cipher, statsPage: number) {
    cipher = cipher.transformed ? cipher.transformed : cipher;

    if (!this.ngramStatisticsObservables.hasOwnProperty(cipher.name)) {
      const request = {
        ...cipher,
        statsPage
      };

      this.ngramStatisticsObservables[cipher.name] = this.http.post<NumberResponse>(ENDPOINT_URL + '/ngrams', request).pipe();
    }

    return this.ngramStatisticsObservables[cipher.name];
  }
}
