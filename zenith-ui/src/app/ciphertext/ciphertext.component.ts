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

import {ChangeDetectorRef, Component, effect, signal} from '@angular/core';
import { BlockifyPipe } from "../blockify.pipe";
import { MatTooltip } from "@angular/material/tooltip";
import {ConfigurationService} from "../configuration.service";

const originalTooltipText = 'Copy to clipboard';

@Component({
    selector: 'app-ciphertext',
    templateUrl: './ciphertext.component.html',
    styleUrls: ['./ciphertext.component.css'],
    standalone: false
})
export class CiphertextComponent {
  cipher = this.configurationService.selectedCipher;
  tooltipText = signal(originalTooltipText);
  blockifyPipe = new BlockifyPipe();

  constructor(private cdRef: ChangeDetectorRef,
              private configurationService: ConfigurationService) {
    effect(() => {
        if (!!this.cipher()) {
          // I don't understand why, but change detection is not occurring here in some odd scenarios
          this.cdRef.detectChanges();
        }
    });
  }

  async copyCiphertext(tooltip: MatTooltip) {
    const textToCopy = this.blockifyPipe.transform(this.cipher().ciphertext, this.cipher().columns).toString();
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
