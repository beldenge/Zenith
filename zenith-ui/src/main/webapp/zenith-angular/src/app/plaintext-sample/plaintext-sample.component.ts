import { Component, Input, OnInit } from '@angular/core';

@Component({
  selector: 'app-plaintext-sample',
  templateUrl: './plaintext-sample.component.html',
  styleUrls: ['./plaintext-sample.component.css']
})
export class PlaintextSampleComponent implements OnInit {
  @Input()
  sample: string;

  constructor() { }

  ngOnInit() {
  }

  edit(sampleEditor: HTMLElement) {
  }
}
