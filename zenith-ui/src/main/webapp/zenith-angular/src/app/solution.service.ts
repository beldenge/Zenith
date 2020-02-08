import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Cipher } from "./models/Cipher";
import { SolutionResponse } from "./models/SolutionResponse";
import { SolutionRequest } from "./models/SolutionRequest";

@Injectable({
  providedIn: 'root'
})
export class SolutionService {
  constructor(
    private http: HttpClient
  ) {}

  solve(cipher: Cipher, epochs: number) {
    let request = new SolutionRequest(cipher.rows, cipher.columns, cipher.ciphertext, epochs);

    return this.http.post<SolutionResponse>('http://localhost:8080/api/solutions', request);
  }
}
