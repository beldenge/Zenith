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
import { HttpClient } from '@angular/common/http';
import { CipherResponse } from "./models/CipherResponse";
import { BehaviorSubject, Observable } from "rxjs";
import { Cipher } from "./models/Cipher";
import { CiphertextTransformationRequest } from "./models/CiphertextTransformationRequest";
import { CipherRequest } from "./models/CipherRequest";
import { environment } from "../environments/environment";

const ENDPOINT_URL = environment.apiUrlBase + '/ciphers';

@Injectable({
  providedIn: 'root'
})
export class CipherService {
  private selectedCipher$ = new BehaviorSubject<Cipher>(null);
  private ciphers$ = new BehaviorSubject<Cipher[]>(null);

  constructor(private http: HttpClient) {}

  getSelectedCipherAsObservable(): Observable<Cipher> {
    return this.selectedCipher$.asObservable();
  }

  updateSelectedCipher(cipher: Cipher): void {
    return this.selectedCipher$.next(cipher);
  }

  getCiphersAsObservable(): Observable<Cipher[]> {
    return this.ciphers$.asObservable();
  }

  updateCiphers(ciphers: Cipher[]): void {
    return this.ciphers$.next(ciphers);
  }

  getCiphers() {
    return this.http.get<CipherResponse>(ENDPOINT_URL);
  }

  transformCipher(cipherName: string, transformationRequest: CiphertextTransformationRequest) {
    return this.http.post<CipherResponse>(ENDPOINT_URL + '/' + cipherName, transformationRequest);
  }

  createCipher(cipherRequest: CipherRequest) {
    return this.http.post<void>(ENDPOINT_URL, cipherRequest);
  }

  updateCipher(cipherName: string, cipherRequest: CipherRequest) {
    return this.http.put<void>(ENDPOINT_URL + '/' + cipherName, cipherRequest);
  }

  deleteCipher(cipherName: string) {
    return this.http.delete<void>(ENDPOINT_URL + '/' + cipherName);
  }
}
