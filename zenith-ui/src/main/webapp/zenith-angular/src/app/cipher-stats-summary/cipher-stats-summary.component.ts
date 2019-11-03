import { Component, Input, OnInit } from '@angular/core';
import { Cipher } from "../models/Cipher";

@Component({
  selector: 'app-cipher-stats-summary',
  templateUrl: './cipher-stats-summary.component.html',
  styleUrls: ['./cipher-stats-summary.component.css']
})
export class CipherStatsSummaryComponent implements OnInit {
  @Input() cipher: Cipher;

  constructor() { }

  ngOnInit() {
  }
}
