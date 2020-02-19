import { Component, Input, OnInit } from '@angular/core';
import { Cipher } from "../models/Cipher";
import { BehaviorSubject, Observable } from "rxjs";
import { BlockifyPipe } from "../blockify.pipe";
import { CipherService } from "../cipher.service";
import { MatTooltip } from "@angular/material/tooltip";

const originalTooltipText = 'Copy to clipboard';

@Component({
  selector: 'app-plaintext',
  templateUrl: './plaintext.component.html',
  styleUrls: ['./plaintext.component.css']
})
export class PlaintextComponent implements OnInit {
  cipher: Cipher;
  cipher$: Observable<Cipher>;
  @Input() solution: string;
  @Input() score: number;
  tooltipText = new BehaviorSubject<string>(originalTooltipText);
  blockifyPipe = new BlockifyPipe();

  constructor(private cipherService: CipherService) {
    this.cipher$ = cipherService.getSelectedCipherAsObservable();
  }

  ngOnInit() {
    this.cipher$.subscribe(cipher => {
      this.cipher = cipher;
    });
  }

  copyPlaintext(tooltip : MatTooltip) {
    var plaintextElement = document.createElement("textarea");
    plaintextElement.id = 'txt';
    plaintextElement.style.position = 'fixed';
    plaintextElement.style.top = '0';
    plaintextElement.style.left = '0';
    plaintextElement.style.opacity = '0';
    plaintextElement.value = this.blockifyPipe.transform(this.solution, this.cipher.columns).toString();
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
