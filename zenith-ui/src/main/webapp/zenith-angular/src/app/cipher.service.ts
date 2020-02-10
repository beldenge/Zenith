import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { CipherResponse } from "./models/CipherResponse";
import { BehaviorSubject, Observable } from "rxjs";
import { Cipher } from "./models/Cipher";
import { TransformationRequest } from "./models/TransformationRequest";
import { CipherRequest } from "./models/CipherRequest";
import { environment } from "../environments/environment";

const ENDPOINT_URL = environment.apiUrlBase + '/ciphers';

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
    return this.http.get<CipherResponse>(ENDPOINT_URL);
  }

  transformCipher(cipherName: string, transformationRequest: TransformationRequest) {
    return this.http.post<CipherResponse>(ENDPOINT_URL + '/' + cipherName, transformationRequest);
  }

  createCipher(cipherRequest: CipherRequest) {
    return this.http.post<void>(ENDPOINT_URL, cipherRequest);
  }

  updateCipher(cipherName: string, cipherRequest: CipherRequest) {
    return this.http.put<void>(ENDPOINT_URL + '/' + cipherName, cipherRequest);
  }

  deleteCipher(cipherName: string) {
    return this.http.delete<void>(ENDPOINT_URL + '/' + cipherName);
  }
}
