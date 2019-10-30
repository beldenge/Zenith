import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CipherResponse } from "./models/CipherResponse";

@Injectable({
  providedIn: 'root'
})
export class CipherService {
  constructor(
    private http: HttpClient
  ) {}

  getCiphers() {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.get<CipherResponse>('http://localhost:8080/api/ciphers', { headers: headers });
  }
}
