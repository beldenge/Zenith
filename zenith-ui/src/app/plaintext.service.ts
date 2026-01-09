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
import {Apollo, gql} from "apollo-angular";
import {WordSegmentationResponse} from "./models/WordSegmentationResponse";
import {SamplePlaintextTransformationRequest} from "./models/SamplePlaintextTransformationRequest";
import {SolutionResponse} from "./models/SolutionResponse";

@Injectable({
  providedIn: 'root',
})
export class PlaintextService {
  constructor(private apollo: Apollo) {}

  getWordSegmentation(plaintext: string) {
    return this.apollo.query<WordSegmentationResponse>({
      query: gql`
          query SegmentPlaintext($plaintext: String!) {
            segmentPlaintext(plaintext: $plaintext) {
              probability
              segmentedPlaintext
            }
          }
        `,
      variables: {
        plaintext
      }
    });
  }

  transformSample(request: SamplePlaintextTransformationRequest) {
    return this.apollo.mutate<SolutionResponse>({
      mutation: gql`
        mutation TransformPlaintext($request: PlaintextTransformationRequest!) {
            transformPlaintext(request: $request) {
              plaintext
              scores
            }
          }
        `,
      variables: {
        request
      }
    });
  }
}
