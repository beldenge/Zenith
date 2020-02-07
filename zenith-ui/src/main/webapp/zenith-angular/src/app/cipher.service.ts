import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { CipherResponse } from "./models/CipherResponse";
import { BehaviorSubject, Observable } from "rxjs";
import { Cipher } from "./models/Cipher";
import { TransformationRequest } from "./models/TransformationRequest";

@Injectable({
  providedIn: 'root'
})
export class CipherService {
  private selectedCipher$ = new BehaviorSubject<Cipher>(null);
  private ciphers$ = new BehaviorSubject<Cipher[]>(null);

  getSelectedCipherAsObservable(): Observable<Cipher> {
    return this.selectedCipher$.asObservable();
  }

  updateSelectedCipher(cipher: Cipher): void {
    return this.selectedCipher$.next(cipher);
  }

  getCiphersAsObservable(): Observable<Cipher[]> {
    return this.ciphers$.asObservable();
  }

  updateCiphers(ciphers: Cipher[]): void {
    return this.ciphers$.next(ciphers);
  }

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

  transformCipher(transformationRequest: TransformationRequest) {
    const headers = new HttpHeaders({
      'Content-Type': 'application/json',
      'Accept': 'application/json'
    });

    return this.http.post<CipherResponse>('http://localhost:8080/api/ciphers', transformationRequest, { headers: headers });
  }
}
