import { Component, Input, OnInit } from '@angular/core';
import { Cipher } from "../models/Cipher";

@Component({
  selector: 'app-plaintext',
  templateUrl: './plaintext.component.html',
  styleUrls: ['./plaintext.component.css']
})
export class PlaintextComponent implements OnInit {
  @Input() cipher: Cipher;
  @Input() solution: string;

  constructor() { }

  ngOnInit() {
  }
}
