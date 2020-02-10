import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Cipher } from "./models/Cipher";
import { SolutionResponse } from "./models/SolutionResponse";
import { SolutionRequest } from "./models/SolutionRequest";
import { environment } from "../environments/environment";

const ENDPOINT_URL = environment.apiUrlBase + '/solutions';

@Injectable({
  providedIn: 'root'
})
export class SolutionService {
  constructor(
    private http: HttpClient
  ) {}

  solve(cipher: Cipher, epochs: number) {
    let request = new SolutionRequest(cipher.rows, cipher.columns, cipher.ciphertext, epochs);

    return this.http.post<SolutionResponse>(ENDPOINT_URL, request);
  }
}
