import { Component, Input, OnInit } from '@angular/core';
import { Cipher } from "../models/Cipher";

@Component({
  selector: 'app-ciphertext',
  templateUrl: './ciphertext.component.html',
  styleUrls: ['./ciphertext.component.css']
})
export class CiphertextComponent implements OnInit {
  @Input() cipher: Cipher;

  constructor() { }

  ngOnInit() {
  }
}
