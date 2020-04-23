import { Injectable } from '@angular/core';
import { environment } from "../environments/environment";
import { HttpClient } from "@angular/common/http";
import { WordSegmentationResponse } from "./models/WordSegmentationResponse";
import {WordSegmentationRequest} from "./models/WordSegmentationRequest";

const ENDPOINT_URL = environment.apiUrlBase + '/segments';

@Injectable({
  providedIn: 'root'
})
export class WordSegmentationService {
  constructor(private http: HttpClient) {}

  getWordSegmentation(plaintext: string) {
    return this.http.post<WordSegmentationResponse>(ENDPOINT_URL, new WordSegmentationRequest(plaintext));
  }
}
