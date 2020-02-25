import { Component, Input, OnInit } from '@angular/core';
import { ConfigurationService } from "../configuration.service";

@Component({
  selector: 'app-plaintext-sample',
  templateUrl: './plaintext-sample.component.html',
  styleUrls: ['./plaintext-sample.component.css']
})
export class PlaintextSampleComponent implements OnInit {
  originalSample = 'thetomatoisaplantinthenightshadefamilyxxxx';
  // sample = this.originalSample;
  @Input() transformedSample: string;

  constructor(private configurationService: ConfigurationService) { }

  ngOnInit() {
    // this.reset();
  }

  reset(sampleEditor: HTMLElement) {
    // this.sample = this.originalSample;
    sampleEditor.textContent = this.originalSample;
    this.configurationService.updateSamplePlaintext(this.originalSample);
  }

  edit(sampleEditor: HTMLElement) {
    this.configurationService.updateSamplePlaintext(sampleEditor.textContent);
  }
}
