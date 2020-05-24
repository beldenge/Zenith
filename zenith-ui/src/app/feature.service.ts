import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../environments/environment";
import { FeatureResponse } from "./models/FeatureResponse";

const ENDPOINT_URL = environment.apiUrlBase + '/features';

@Injectable({
  providedIn: 'root'
})
export class FeatureService {
  constructor(private http: HttpClient) {}

  getFeatures() {
    return this.http.get<FeatureResponse>(ENDPOINT_URL);
  }
}
