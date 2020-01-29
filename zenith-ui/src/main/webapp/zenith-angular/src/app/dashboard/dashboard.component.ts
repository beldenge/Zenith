import { Component, OnInit } from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { SolutionService } from "../solution.service";
import {FormBuilder, Validators} from "@angular/forms";

declare var $: any;

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  ciphers: Cipher[];
  selectedCipher: Cipher;
  solution: string;
  isRunning: boolean = false;
  hyperparametersForm = this.fb.group({
    epochs: ['', [Validators.min(1), Validators.pattern("^[0-9]*$")]]
  });

  constructor(private fb: FormBuilder, private cipherService: CipherService, private solutionService: SolutionService) { }

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

    this.solutionService.solve(this.selectedCipher, this.hyperparametersForm.get('epochs').value).subscribe(solutionResponse => {
      this.solution = solutionResponse.plaintext;
      this.isRunning = false;
    });
  }

  clearSolution() {
    this.solution = null;
  }
}
