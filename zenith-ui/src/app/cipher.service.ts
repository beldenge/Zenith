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

import { Injectable } from '@angular/core';
import { Observable } from "rxjs";
import { Cipher } from "./models/Cipher";
import { CiphertextTransformationRequest } from "./models/CiphertextTransformationRequest";
import { ConfigurationService } from "./configuration.service";
import {Apollo, gql} from "apollo-angular";

@Injectable({
  providedIn: 'root'
})
export class CipherService {
  constructor(private configurationService: ConfigurationService,
              private apollo: Apollo) {}

  selected: Cipher;

  getSelectedCipherAsObservable(): Observable<Cipher> {
    return this.configurationService.getSelectedCipherAsObservable();
  }

  updateSelectedCipher(cipher: Cipher): void {
    this.selected = cipher;
    this.configurationService.updateSelectedCipher(cipher);
  }

  getCiphersAsObservable(): Observable<Cipher[]> {
    return this.configurationService.getCiphersAsObservable();
  }

  updateCiphers(ciphers: Cipher[]): void {
    if (this.selected !== undefined && !ciphers.find(cipher => cipher.name === this.selected.name)) {
      // If the selected cipher has been deleted, pick a different one
      this.updateSelectedCipher(ciphers[0]);
    }

    return this.configurationService.updateCiphers(ciphers);
  }

  transformCipher(request: CiphertextTransformationRequest) {
    return this.apollo.mutate<Cipher>({
      mutation: gql`
          mutation TransformCipher($request: CiphertextTransformationRequest!) {
            transformCipher(request: $request) {
              name
              rows
              columns
              readOnly
              ciphertext
              knownSolutionKey
            }
          }
        `,
      variables: {
        request
      }
    });
  }
}
