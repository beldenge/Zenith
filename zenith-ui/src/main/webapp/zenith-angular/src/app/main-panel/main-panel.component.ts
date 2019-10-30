import { Component, OnInit } from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";

@Component({
  selector: 'app-main-panel',
  templateUrl: './main-panel.component.html',
  styleUrls: ['./main-panel.component.css']
})
export class MainPanelComponent implements OnInit {
  ciphers: Cipher[];
  selectedCipher: Cipher;

  constructor(private cipherService: CipherService) { }

  ngOnInit() {
    this.cipherService.getCiphers().subscribe(cipherResponse => {
      this.ciphers = cipherResponse.ciphers;
      this.selectedCipher = this.ciphers[0];
    });
  }
}
