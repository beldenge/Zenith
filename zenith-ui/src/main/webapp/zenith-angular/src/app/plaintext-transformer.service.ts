import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { TransformerResponse } from "./models/TransformerResponse";
import { environment } from "../environments/environment";
import { ZenithTransformer } from "./models/ZenithTransformer";
import { SamplePlaintextTransformationRequest } from "./models/SamplePlaintextTransformationRequest";
import { SolutionResponse } from "./models/SolutionResponse";

const ENDPOINT_URL = environment.apiUrlBase + '/transformers/plaintext';
const SAMPLE_ENDPOINT_URL = environment.apiUrlBase + '/plaintext-samples';

@Injectable({
  providedIn: 'root'
})
export class PlaintextTransformerService {
  private appliedTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);

  constructor(
    private http: HttpClient
  ) {}

  updateAppliedTransformers(appliedTransformers: ZenithTransformer[]): void {
    return this.appliedTransformers$.next(appliedTransformers);
  }

  getAppliedTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.appliedTransformers$.asObservable();
  }

  getTransformers() {
    return this.http.get<TransformerResponse>(ENDPOINT_URL);
  }

  transformSample(request: SamplePlaintextTransformationRequest) {
    return this.http.post<SolutionResponse>(SAMPLE_ENDPOINT_URL, request);
  }
}
