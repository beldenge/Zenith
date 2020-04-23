import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { SolutionResponse } from "./models/SolutionResponse";

@Injectable({
  providedIn: 'root'
})
export class SolutionService {
  private solution$ = new BehaviorSubject<SolutionResponse>(null);

  constructor() {}

  getSolutionAsObservable(): Observable<SolutionResponse> {
    return this.solution$.asObservable();
  }

  updateSolution(solution: SolutionResponse): void {
    this.solution$.next(solution);
  }
}
