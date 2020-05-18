import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../environments/environment";
import { FitnessFunctionResponse } from "./models/FitnessFunctionResponse";

const ENDPOINT_URL = environment.apiUrlBase + '/fitness-functions';

@Injectable({
  providedIn: 'root'
})
export class FitnessFunctionService {
  constructor(private http: HttpClient) {}

  getFitnessFunctions() {
    return this.http.get<FitnessFunctionResponse>(ENDPOINT_URL).toPromise();
  }
}
