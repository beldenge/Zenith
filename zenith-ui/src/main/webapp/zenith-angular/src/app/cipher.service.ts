import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CipherResponse } from "./models/CipherResponse";
import { BehaviorSubject, Observable } from "rxjs";
import { Cipher } from "./models/Cipher";
import { TransformationRequest } from "./models/TransformationRequest";
import { CipherRequest } from "./models/CipherRequest";

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
    return this.http.get<CipherResponse>('http://localhost:8080/api/ciphers');
  }

  transformCipher(cipherName: string, transformationRequest: TransformationRequest) {
    return this.http.post<CipherResponse>('http://localhost:8080/api/ciphers/' + cipherName, transformationRequest);
  }

  createCipher(cipherRequest: CipherRequest) {
    return this.http.post<void>('http://localhost:8080/api/ciphers', cipherRequest);
  }

  updateCipher(cipherName: string, cipherRequest: CipherRequest) {
    return this.http.put<void>('http://localhost:8080/api/ciphers/' + cipherName, cipherRequest);
  }

  deleteCipher(cipherName: string) {
    return this.http.delete<void>('http://localhost:8080/api/ciphers/' + cipherName);
  }
}
