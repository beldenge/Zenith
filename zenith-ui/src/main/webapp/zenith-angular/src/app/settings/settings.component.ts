import { Component, OnInit } from '@angular/core';
import { FormBuilder } from '@angular/forms';
import { JsonPipe } from '@angular/common';
import { Validators } from '@angular/forms';

@Component({
  selector: 'app-settings',
  templateUrl: './settings.component.html',
  styleUrls: ['./settings.component.css']
})
export class SettingsComponent implements OnInit {
  generalSettingsForm = this.fb.group({
    epochs: ['', [Validators.min(1), Validators.pattern("^[0-9]*$")]],
    optimizer: ['SimulatedAnnealingOptimizer'],
    plaintextEvaluator: ['']
  });

  constructor(private fb: FormBuilder, private json: JsonPipe) { }

  ngOnInit() {
  }

  onSubmit() {
    console.log('Form data: ' + this.json.transform(this.generalSettingsForm.value));
  }
}
