import { Component, OnInit } from '@angular/core';
import { MatDialogRef} from "@angular/material/dialog";
import { AbstractControl, FormBuilder, FormGroup, Validators } from "@angular/forms";
import { CipherService } from "../cipher.service";
import { CipherRequest } from "../models/CipherRequest";
import { Cipher } from "../models/Cipher";
import { MatSnackBar } from "@angular/material/snack-bar";

@Component({
  selector: 'app-new-cipher-modal',
  templateUrl: './new-cipher-modal.component.html',
  styleUrls: ['./new-cipher-modal.component.css']
})
export class NewCipherModalComponent implements OnInit {
  ciphers: Cipher[];

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

  newCipherForm: FormGroup = this.fb.group({
    formArray: this.fb.array([
      this.nameFormGroup,
      this.dimensionsFormGroup,
      this.ciphertextFormGroup
    ])
  });

  constructor(public dialogRef: MatDialogRef<NewCipherModalComponent>, private fb: FormBuilder, private cipherService: CipherService, private _snackBar: MatSnackBar) { }

  ngOnInit() {
    this.cipherService.getCiphersAsObservable().subscribe(ciphers => {
      this.ciphers = ciphers;
    });
  }

  get formArray(): AbstractControl | null {
    return this.newCipherForm.get('formArray');
  }

  save() {
    let name = this.nameFormGroup.get('name').value;
    let rows = this.dimensionsFormGroup.get('rows').value;
    let columns = this.dimensionsFormGroup.get('columns').value;
    let ciphertext = this.ciphertextFormGroup.get('ciphertext').value;
    let request = new CipherRequest(name, rows, columns, ciphertext);
    console.log(JSON.stringify(request));
    this.cipherService.createCipher(request).subscribe(() => {
      let cipher = new Cipher(name, rows, columns, ciphertext);

      this.ciphers.push(cipher);

      this.cipherService.updateCiphers(this.ciphers);

      this.dialogRef.close();

      this._snackBar.open('Created "' + cipher.name + '"', '',{
        duration: 2000,
        verticalPosition: 'top'
      });
    });
  }
}
