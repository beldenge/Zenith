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
import { Cipher } from "./models/Cipher";
import { CiphertextTransformationRequest } from "./models/CiphertextTransformationRequest";
import {Apollo, gql} from "apollo-angular";
import {map} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class CipherService {
  constructor(private apollo: Apollo) {}

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
    }).pipe(map((response: any) => response.data.transformCipher));
  }
}
