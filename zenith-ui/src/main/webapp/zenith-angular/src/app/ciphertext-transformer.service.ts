import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { TransformerResponse } from "./models/TransformerResponse";
import { environment } from "../environments/environment";

const ENDPOINT_URL = environment.apiUrlBase + '/transformers/ciphertext';

@Injectable({
  providedIn: 'root'
})
export class CiphertextTransformerService {
  constructor(private http: HttpClient) {}

  getTransformers() {
    return this.http.get<TransformerResponse>(ENDPOINT_URL);
  }
}
