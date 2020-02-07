import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { CiphertextTransformerResponse } from "./models/CiphertextTransformerResponse";
import { CiphertextTransformer } from "./models/CiphertextTransformer";
import {BehaviorSubject, Observable} from "rxjs";
import {Cipher} from "./models/Cipher";

@Injectable({
  providedIn: 'root'
})
export class TransformerService {
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
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.get<CiphertextTransformerResponse>('http://localhost:8080/api/transformers', { headers: headers });
  }
}
