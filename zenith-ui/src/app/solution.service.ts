import { Injectable } from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {BehaviorSubject, Observable} from "rxjs";
import { filter, map } from 'rxjs/operators';
import { SolutionResponse } from "./models/SolutionResponse";
import { SolutionUpdate } from "./models/SolutionUpdate";
import {SolutionRequest} from "./models/SolutionRequest";

@Injectable({
  providedIn: 'root'
})
export class SolutionService {
  private solution$ = new BehaviorSubject<SolutionResponse>(null);
  private runState$ = new BehaviorSubject<boolean>(false);
  private progressPercentage$ = new BehaviorSubject<number>(0);

  constructor(private apollo: Apollo) {}

  solveSolution(input: SolutionRequest): Observable<string> {
    return this.apollo.mutate({
      mutation: gql`
        mutation SolveSolution($input: SolutionRequest!) {
          solveSolution(input: $input)
        }
      `,
      variables: {
        input
      }
    }).pipe(map((result: any) => result.data.solveSolution));
  }

  solutionUpdates(requestId: string): Observable<SolutionUpdate> {
    return this.apollo.subscribe({
      query: gql`
        subscription SolutionUpdates($requestId: ID!) {
          solutionUpdates(requestId: $requestId) {
            requestId
            type
            epochData {
              epochsCompleted
              epochsTotal
            }
            solutionData {
              plaintext
              scores
            }
          }
        }
      `,
      variables: {
        requestId
      }
    }).pipe(
      filter<any>((result: any) => !result.data.upstreamPublisher),
      map((result: any) => result.data.solutionUpdates)
    );
  }

  handleSolutionUpdate(update: SolutionUpdate): void {
    switch (update.type) {
      case 'EPOCH_COMPLETE':
        if (update.epochData) {
          const progress = (update.epochData.epochsCompleted / update.epochData.epochsTotal) * 100;
          this.updateProgressPercentage(progress);
        }
        break;
      case 'SOLUTION':
        if (update.solutionData) {
          this.updateSolution(new SolutionResponse(update.solutionData.plaintext, update.solutionData.scores));
        }
        this.updateRunState(false);
        this.updateProgressPercentage(100);
        break;
      default:
          break;
    }
  }

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
