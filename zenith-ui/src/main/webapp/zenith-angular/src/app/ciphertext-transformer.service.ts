import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { TransformerResponse } from "./models/TransformerResponse";
import { ZenithTransformer } from "./models/ZenithTransformer";
import { BehaviorSubject, Observable } from "rxjs";
import { environment } from "../environments/environment";

const ENDPOINT_URL = environment.apiUrlBase + '/transformers/ciphertext';

@Injectable({
  providedIn: 'root'
})
export class CiphertextTransformerService {
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
}
