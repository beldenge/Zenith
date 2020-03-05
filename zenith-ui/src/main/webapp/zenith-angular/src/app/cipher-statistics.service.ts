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

const ENDPOINT_URL = environment.apiUrlBase + '/statistics';

@Injectable({
  providedIn: 'root'
})
export class CipherStatisticsService {
  constructor(private http: HttpClient) { }

  getUniqueSymbols(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/uniqueSymbols');
  }

  getMultiplicity(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/multiplicity');
  }

  getEntropy(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/entropy');
  }

  getIndexOfCoincidence(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/indexOfCoincidence');
  }

  getBigramRepeats(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/bigramRepeats');
  }

  getCycleScore(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/cycleScore');
  }
}
