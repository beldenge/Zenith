import { Component, Input, OnInit } from '@angular/core';
import { ConfigurationService } from "../configuration.service";

@Component({
  selector: 'app-plaintext-sample',
  templateUrl: './plaintext-sample.component.html',
  styleUrls: ['./plaintext-sample.component.css']
})
export class PlaintextSampleComponent implements OnInit {
  originalSample = ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT;
  @Input() transformedSample: string;

  constructor(private configurationService: ConfigurationService) { }

  ngOnInit() {
  }

  reset(sampleEditor: HTMLElement) {
    sampleEditor.textContent = ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT;
    this.configurationService.updateSamplePlaintext(ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT);
  }

  edit(sampleEditor: HTMLElement) {
    this.configurationService.updateSamplePlaintext(sampleEditor.textContent);
  }
}
