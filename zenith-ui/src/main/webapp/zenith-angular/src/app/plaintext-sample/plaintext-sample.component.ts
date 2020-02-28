import { AfterContentInit, Component, Input, OnInit } from '@angular/core';
import { ConfigurationService } from "../configuration.service";
import { Subscription } from "rxjs";

@Component({
  selector: 'app-plaintext-sample',
  templateUrl: './plaintext-sample.component.html',
  styleUrls: ['./plaintext-sample.component.css']
})
export class PlaintextSampleComponent implements OnInit, AfterContentInit  {
  @Input() transformedSample: string;
  sample: string;
  samplePlaintextSubscription: Subscription;

  constructor(private configurationService: ConfigurationService) { }

  ngOnInit() {
    this.samplePlaintextSubscription = this.configurationService.getSamplePlaintextAsObservable().subscribe(sample => {
      this.sample = sample;
    });
  }

  ngAfterContentInit() {
    // We just want the initial value, then don't take updates anymore
    this.samplePlaintextSubscription.unsubscribe();
  }

  reset(sampleEditor: HTMLElement) {
    sampleEditor.textContent = ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT;
    this.configurationService.updateSamplePlaintext(ConfigurationService.DEFAULT_SAMPLE_PLAINTEXT);
  }

  edit(sampleEditor: HTMLElement) {
    this.configurationService.updateSamplePlaintext(sampleEditor.textContent);
  }
}
