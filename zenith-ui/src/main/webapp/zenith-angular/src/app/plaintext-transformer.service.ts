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
    return this.http.get<TransformerResponse>(ENDPOINT_URL);
  }

  transformSample(request: SamplePlaintextTransformationRequest) {
    return this.http.post<SolutionResponse>(SAMPLE_ENDPOINT_URL, request);
  }
}
