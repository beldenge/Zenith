import { Component, OnInit } from '@angular/core';
import { CipherService } from "../cipher.service";
import { Cipher } from "../models/Cipher";
import { FormBuilder, Validators } from "@angular/forms";
import { WebSocketAPI } from "../websocket.api";
import { SolutionRequest } from "../models/SolutionRequest";

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

    this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      this.selectedCipher = selectedCipher
    });

    this.cipherService.getCiphersAsObservable().subscribe(ciphers => {
      this.ciphers = ciphers
    });
  }

  onMouseOverSelect(element: HTMLElement) {
    element.focus();
    element.onmouseout = (event: MouseEvent) => {
      element.blur();
    };
  }

  onClickSelect(element: HTMLElement) {
    element.onmouseout = () => {};
  }

  solve() {
    this.progressPercentage = 0;
    this.isRunning = true;
    this.solution = null;

    let request = new SolutionRequest(this.selectedCipher.rows, this.selectedCipher.columns, this.selectedCipher.ciphertext, this.hyperparametersForm.get('epochs').value);

    let self = this;
    this.webSocketAPI.connectAndSend(request, function (response) {
      if (response.headers.type === 'SOLUTION') {
        self.solution = JSON.parse(response.body).plaintext;
        self.isRunning = false;
        self.webSocketAPI.disconnect();
      } else if (response.headers.type === 'EPOCH_COMPLETE') {
        let responseBody = JSON.parse(response.body);
        self.progressPercentage = (responseBody.epochsCompleted / responseBody.epochsTotal) * 100;
      }
    }, function(error) {
      self.isRunning = false;
    });
  }

  byName(c1: Cipher, c2: Cipher): boolean {
    return c1 && c2 ? c1.name === c2.name : c1 === c2;
  }

  onCipherSelect(element: HTMLElement) {
    element.blur();
    this.solution = null;
    localStorage.setItem('selected_cipher_name', this.selectedCipher.name);
    this.cipherService.updateSelectedCipher(this.selectedCipher);
  }
}
