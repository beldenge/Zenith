import { Component, OnDestroy, OnInit } from '@angular/core';
import { BehaviorSubject, Subscription } from "rxjs";
import { MatTooltip } from "@angular/material/tooltip";
import { SolutionService } from "../solution.service";
import { SolutionResponse } from "../models/SolutionResponse";
import {PlaintextService} from "../plaintext.service";

const originalTooltipText = 'Copy to clipboard';

@Component({
    selector: 'app-word-segmentation',
    templateUrl: './word-segmentation.component.html',
    styleUrls: ['./word-segmentation.component.css'],
    standalone: false
})
export class WordSegmentationComponent implements OnInit, OnDestroy {
  solution: SolutionResponse;
  tooltipText = new BehaviorSubject<string>(originalTooltipText);
  solutionSubscription: Subscription;
  score: number;
  segmentation: string;

  constructor(private solutionService: SolutionService,
              private plaintextService: PlaintextService) {}

  ngOnInit(): void {
    this.solutionSubscription = this.solutionService.getSolutionAsObservable().subscribe(solution => {
      this.solution = solution;

      if (!solution) {
        this.segmentation = null;
        this.score = null;
        return;
      }

      this.plaintextService.getWordSegmentation(solution.plaintext).subscribe((response: any) => {
        let partialSegmentation = '';
        for (let i = 0; i < response.segmentedPlaintext.length; i++) {
          partialSegmentation += response.segmentedPlaintext[i];

          if (i < response.segmentedPlaintext.length - 1) {
            partialSegmentation += ' ';
          }
        }

        this.segmentation = partialSegmentation;
        this.score = response.probability;
      });
    });
  }

  ngOnDestroy() {
    this.solutionSubscription.unsubscribe();
  }

  copyPlaintext(tooltip: MatTooltip) {
    const plaintextElement = document.createElement('textarea');
    plaintextElement.id = 'txt';
    plaintextElement.style.position = 'fixed';
    plaintextElement.style.top = '0';
    plaintextElement.style.left = '0';
    plaintextElement.style.opacity = '0';
    plaintextElement.value = this.segmentation;
    document.body.appendChild(plaintextElement);
    plaintextElement.select();
    document.execCommand('copy');

    tooltip.hide();
    this.tooltipText.next('Copied!');
    tooltip.show();
  }

  resetTooltipText() {
    this.tooltipText.next(originalTooltipText);
  }
}
