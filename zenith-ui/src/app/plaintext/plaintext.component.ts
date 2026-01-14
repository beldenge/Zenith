/*
 * Copyright 2017-2026 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

import {Component, signal} from '@angular/core';
import { BlockifyPipe } from "../blockify.pipe";
import { MatTooltip } from "@angular/material/tooltip";
import { SolutionService } from "../solution.service";
import {ConfigurationService} from "../configuration.service";

const originalTooltipText = 'Copy to clipboard';

@Component({
    selector: 'app-plaintext',
    templateUrl: './plaintext.component.html',
    styleUrls: ['./plaintext.component.css'],
    standalone: false
})
export class PlaintextComponent {
  cipher = this.configurationService.selectedCipher;
  solution = this.solutionService.solution;
  tooltipText = signal(originalTooltipText);
  blockifyPipe = new BlockifyPipe();

  constructor(private solutionService: SolutionService,
              private configurationService: ConfigurationService) {}

  async copyPlaintext(tooltip: MatTooltip) {
    if (!this.solution()?.plaintext) {
      return;
    }

    const textToCopy = this.blockifyPipe.transform(this.solution().plaintext.split(' '), this.cipher().columns).toString();
    await navigator.clipboard.writeText(textToCopy);

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
