import {Component, effect, signal} from '@angular/core';
import { MatTooltip } from "@angular/material/tooltip";
import { SolutionService } from "../solution.service";
import {PlaintextService} from "../plaintext.service";

const originalTooltipText = 'Copy to clipboard';

@Component({
    selector: 'app-word-segmentation',
    templateUrl: './word-segmentation.component.html',
    styleUrls: ['./word-segmentation.component.css'],
    standalone: false
})
export class WordSegmentationComponent {
  solution = this.solutionService.solution;
  tooltipText = signal(originalTooltipText);
  score: number;
  segmentation: string;

  constructor(private solutionService: SolutionService,
              private plaintextService: PlaintextService) {
    effect(() => {
      if (!this.solution()) {
        this.segmentation = null;
        this.score = null;
        return;
      }

      this.plaintextService.getWordSegmentation(this.solution().plaintext).subscribe((response: any) => {
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

  async copyPlaintext(tooltip: MatTooltip) {
    await navigator.clipboard.writeText(this.segmentation);

    tooltip.hide();
    this.tooltipText.update(() => 'Copied!');
    setTimeout(() => {
      tooltip.show();
    }, 0);
  }

  resetTooltipText() {
    this.tooltipText.update(() => originalTooltipText);
  }
}
