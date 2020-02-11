import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { CiphertextTransformerResponse } from "./models/CiphertextTransformerResponse";
import { CiphertextTransformer } from "./models/CiphertextTransformer";
import { BehaviorSubject, Observable } from "rxjs";
import { environment } from "../environments/environment";

const ENDPOINT_URL = environment.apiUrlBase + '/transformers';

@Injectable({
  providedIn: 'root'
})
export class CiphertextTransformerService {
  private appliedTransformers$ = new BehaviorSubject<CiphertextTransformer[]>([]);

  constructor(
    private http: HttpClient
  ) {}

  updateAppliedTransformers(appliedTransformers: CiphertextTransformer[]): void {
    return this.appliedTransformers$.next(appliedTransformers);
  }

  getAppliedTransformersAsObservable(): Observable<CiphertextTransformer[]> {
    return this.appliedTransformers$.asObservable();
  }

  getTransformers() {
    return this.http.get<CiphertextTransformerResponse>(ENDPOINT_URL);
  }
}
