import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { environment } from "../environments/environment";
import { FitnessFunctionResponse } from "./models/FitnessFunctionResponse";
import { firstValueFrom } from "rxjs";

const ENDPOINT_URL = environment.apiUrlBase + '/fitness-functions';

@Injectable({
  providedIn: 'root'
})
export class FitnessFunctionService {
  constructor(private http: HttpClient) {}

  getFitnessFunctions() {
    return firstValueFrom(this.http.get<FitnessFunctionResponse>(ENDPOINT_URL));
  }
}
