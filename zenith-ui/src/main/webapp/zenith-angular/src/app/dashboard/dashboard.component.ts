import { Component, OnInit } from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { FormBuilder, Validators } from "@angular/forms";
import { WebSocketAPI } from "../websocket.api";
import {SolutionRequest} from "../models/SolutionRequest";
import {SolutionResponse} from "../models/SolutionResponse";

declare var $: any;

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.css']
})
export class DashboardComponent implements OnInit {
  webSocketAPI: WebSocketAPI;
  ciphers: Cipher[];
  selectedCipher: Cipher;
  solution: string;
  isRunning: boolean = false;
  progressPercentage: number = 0;
  hyperparametersForm = this.fb.group({
    epochs: ['1', [Validators.min(1), Validators.pattern("^[0-9]*$")]]
  });

  constructor(private fb: FormBuilder, private cipherService: CipherService) { }

  ngOnInit() {
    this.webSocketAPI = new WebSocketAPI();

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

    let request = new SolutionRequest(this.selectedCipher.rows, this.selectedCipher.columns, this.selectedCipher.ciphertext, this.hyperparametersForm.get('epochs').value);

    let self = this;
    this.webSocketAPI.connectAndSend(request, function (response) {
      console.log('Message received from server: ' + response);

      self.solution = JSON.parse(response.body).plaintext;
      self.isRunning = false;

      // TODO: we won't want to disconnect on the first response when handling multiple progress responses
      self.webSocketAPI.disconnect();
    }, function(error) {
      self.isRunning = false;
    });
  }

  clearSolution() {
    this.solution = null;
  }
}
