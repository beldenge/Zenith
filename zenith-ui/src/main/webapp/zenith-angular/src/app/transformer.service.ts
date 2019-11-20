import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import { CiphertextTransformerResponse } from "./models/CiphertextTransformerResponse";

@Injectable({
  providedIn: 'root'
})
export class TransformerService {
  constructor(
    private http: HttpClient
  ) {}

  getTransformers() {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.get<CiphertextTransformerResponse>('http://localhost:8080/api/transformers', { headers: headers });
  }
}
