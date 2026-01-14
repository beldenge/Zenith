import {Injectable, Signal, signal, WritableSignal} from '@angular/core';
import { Apollo, gql } from 'apollo-angular';
import {Observable} from "rxjs";
import { filter, map } from 'rxjs/operators';
import { SolutionResponse } from "./models/SolutionResponse";
import { SolutionUpdate } from "./models/SolutionUpdate";
import {SolutionRequest} from "./models/SolutionRequest";

@Injectable({
  providedIn: 'root'
})
export class SolutionService {
  private solutionInternal: WritableSignal<SolutionResponse> = signal<SolutionResponse>(null);
  public solution: Signal<SolutionResponse> = this.solutionInternal.asReadonly();
  private runStateInternal: WritableSignal<boolean> = signal(false);
  public runState: Signal<boolean> = this.runStateInternal.asReadonly();
  private progressPercentageInternal: WritableSignal<number> = signal(0);
  public progressPercentage: Signal<number> = this.progressPercentageInternal.asReadonly();

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

  updateSolution(solution: SolutionResponse): void {
    this.solutionInternal.update(() => solution);
  }

  updateRunState(isRunning: boolean): void {
    this.runStateInternal.update(() => isRunning);
  }

  updateProgressPercentage(progressPercentage: number): void {
    this.progressPercentageInternal.update(() => progressPercentage);
  }
}
