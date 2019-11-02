import { Component, OnInit } from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";

declare var $: any;

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

    $('#cipher_select_button, #cipher_select').off('mouseover').on('mouseover', function() {
      $('#cipher_select').trigger('focus');
      $('#cipher_select').off('mouseout').on('mouseout', function() {
        $('#cipher_select').trigger('blur');
      });
    });

    $('#cipher_select').off('click').on('click', function() {
      $('#cipher_select').off('mouseout');
    });

    $('#cipher_select').off('change').on('change', function() {
      $('#cipher_select').trigger('blur');
    });
  }
}
