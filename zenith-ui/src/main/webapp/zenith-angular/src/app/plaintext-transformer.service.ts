import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { HttpClient } from "@angular/common/http";
import { TransformerResponse } from "./models/TransformerResponse";
import { environment } from "../environments/environment";
import { ZenithTransformer } from "./models/ZenithTransformer";
import { FormGroup } from "@angular/forms";

const ENDPOINT_URL = environment.apiUrlBase + '/transformers/plaintext';

@Injectable({
  providedIn: 'root'
})
export class PlaintextTransformerService {
  private appliedTransformers$ = new BehaviorSubject<ZenithTransformer[]>([]);

  constructor(
    private http: HttpClient
  ) {}

  updateAppliedTransformers(appliedTransformers: ZenithTransformer[]): void {
    appliedTransformers.forEach((transformer) => {
      transformer.form.form = new FormGroup({});
    });

    return this.appliedTransformers$.next(appliedTransformers);
  }

  getAppliedTransformersAsObservable(): Observable<ZenithTransformer[]> {
    return this.appliedTransformers$.asObservable();
  }

  getTransformers() {
    return this.http.get<TransformerResponse>(ENDPOINT_URL);
  }
}
