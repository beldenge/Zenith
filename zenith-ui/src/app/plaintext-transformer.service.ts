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
import { HttpClient } from "@angular/common/http";
import { TransformerResponse } from "./models/TransformerResponse";
import { environment } from "../environments/environment";
import { SamplePlaintextTransformationRequest } from "./models/SamplePlaintextTransformationRequest";
import { SolutionResponse } from "./models/SolutionResponse";

const ENDPOINT_URL = environment.apiUrlBase + '/transformers/plaintext';
const SAMPLE_ENDPOINT_URL = environment.apiUrlBase + '/plaintext-samples';

@Injectable({
  providedIn: 'root'
})
export class PlaintextTransformerService {
  constructor(private http: HttpClient) {}

  getTransformers() {
    return this.http.get<TransformerResponse>(ENDPOINT_URL).toPromise();
  }

  transformSample(request: SamplePlaintextTransformationRequest) {
    return this.http.post<SolutionResponse>(SAMPLE_ENDPOINT_URL, request);
  }
}
