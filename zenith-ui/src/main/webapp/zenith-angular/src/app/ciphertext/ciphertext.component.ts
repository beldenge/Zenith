import { Component, Input, OnInit } from '@angular/core';
import { Cipher } from "../models/Cipher";
import { BehaviorSubject, Observable } from "rxjs";
import { BlockifyPipe } from "../blockify.pipe";
import { CipherService } from "../cipher.service";

const originalTooltipText = 'Copy to clipboard';

@Component({
  selector: 'app-ciphertext',
  templateUrl: './ciphertext.component.html',
  styleUrls: ['./ciphertext.component.css']
})
export class CiphertextComponent implements OnInit {
  cipher: Cipher;
  cipher$: Observable<Cipher>;
  tooltipText = new BehaviorSubject<string>(originalTooltipText);
  blockifyPipe = new BlockifyPipe();

  constructor(private cipherService: CipherService) {
    this.cipher$ = cipherService.getSelectedCipherAsObservable()
  }

  ngOnInit() {
    this.cipher$.subscribe(cipher => {
      this.cipher = cipher;
    });
  }

  copyCiphertext() {
    var ciphertextElement = document.createElement("textarea");
    ciphertextElement.id = 'txt';
    ciphertextElement.style.position = 'fixed';
    ciphertextElement.style.top = '0';
    ciphertextElement.style.left = '0';
    ciphertextElement.style.opacity = '0';
    ciphertextElement.value = this.blockifyPipe.transform(this.cipher.ciphertext, this.cipher.columns).toString();
    document.body.appendChild(ciphertextElement);
    ciphertextElement.select();
    document.execCommand('copy');

    this.tooltipText.next('Copied!');
  }

  resetTooltipText() {
    this.tooltipText.next(originalTooltipText);
  }
}
