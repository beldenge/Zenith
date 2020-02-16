import { Injectable } from '@angular/core';
import { environment } from "../environments/environment";
import { HttpClient } from "@angular/common/http";
import { NumberResponse } from "./models/NumberResponse";

const ENDPOINT_URL = environment.apiUrlBase + '/statistics';

@Injectable({
  providedIn: 'root'
})
export class CipherStatisticsService {
  constructor(private http: HttpClient) { }

  getUniqueSymbols(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/uniqueSymbols');
  }

  getMultiplicity(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/multiplicity');
  }

  getEntropy(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/entropy');
  }

  getIndexOfCoincidence(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/indexOfCoincidence');
  }

  getBigramRepeats(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/bigramRepeats');
  }

  getCycleScore(cipherName: string) {
    return this.http.get<NumberResponse>(ENDPOINT_URL + '/' + cipherName + '/cycleScore');
  }
}
