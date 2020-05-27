import { Injectable } from '@angular/core';
import { BehaviorSubject, Observable } from "rxjs";
import { SolutionResponse } from "./models/SolutionResponse";

@Injectable({
  providedIn: 'root'
})
export class SolutionService {
  private solution$ = new BehaviorSubject<SolutionResponse>(null);
  private runState$ = new BehaviorSubject<boolean>(false);
  private progressPercentage$ = new BehaviorSubject<number>(0);

  constructor() {}

  getSolutionAsObservable(): Observable<SolutionResponse> {
    return this.solution$.asObservable();
  }

  updateSolution(solution: SolutionResponse): void {
    this.solution$.next(solution);
  }

  getRunStateAsObservable(): Observable<boolean> {
    return this.runState$.asObservable();
  }

  updateRunState(isRunning: boolean): void {
    this.runState$.next(isRunning);
  }

  getProgressPercentageAsObservable(): Observable<number> {
    return this.progressPercentage$.asObservable();
  }

  updateProgressPercentage(progressPercentage: number): void {
    this.progressPercentage$.next(progressPercentage);
  }
}
