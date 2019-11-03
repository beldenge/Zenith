import { Component, OnInit } from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { SolutionService } from "../solution.service";

declare var $: any;

@Component({
  selector: 'app-main-panel',
  templateUrl: './main-panel.component.html',
  styleUrls: ['./main-panel.component.css']
})
export class MainPanelComponent implements OnInit {
  ciphers: Cipher[];
  selectedCipher: Cipher;
  solution: string;
  isRunning: boolean = false;

  constructor(private cipherService: CipherService, private solutionService: SolutionService) { }

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

  solve() {
    this.isRunning = true;
    this.solution = null;

    this.solutionService.solve(this.selectedCipher).subscribe(solutionResponse => {
      this.solution = solutionResponse.plaintext;
      this.isRunning = false;
    });
  }

  clearSolution() {
    this.solution = null;
  }
}
