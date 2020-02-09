import { Component, OnInit } from '@angular/core';
import { MatDialogRef } from "@angular/material/dialog";
import { FormBuilder, Validators } from "@angular/forms";

@Component({
  selector: 'app-new-cipher-modal',
  templateUrl: './new-cipher-modal.component.html',
  styleUrls: ['./new-cipher-modal.component.css']
})
export class NewCipherModalComponent implements OnInit {
  nameFormGroup = this.fb.group({
    name: ['']
  });

  dimensionsFormGroup = this.fb.group({
    rows: ['', [Validators.min(0), Validators.pattern('^[0-9]+$')]],
    columns: ['', [Validators.min(0), Validators.pattern('^[0-9]+$')]]
  });

  ciphertextFormGroup = this.fb.group({
    ciphertext: ['']
  });

  constructor(public dialogRef: MatDialogRef<NewCipherModalComponent>, private fb: FormBuilder) { }

  ngOnInit() {
  }
}
