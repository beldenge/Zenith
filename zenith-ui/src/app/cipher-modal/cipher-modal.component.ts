/*
 * Copyright 2017-2020 George Belden
 *
 * This file is part of Zenith.
 *
 * Zenith is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Zenith is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Zenith. If not, see <http://www.gnu.org/licenses/>.
 */

import { Component, Inject, OnDestroy, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { AbstractControl, FormBuilder, FormGroup, ValidatorFn, Validators } from "@angular/forms";
import { CipherService } from "../cipher.service";
import { CipherRequest } from "../models/CipherRequest";
import { Cipher } from "../models/Cipher";
import { MatSnackBar } from "@angular/material/snack-bar";
import { Subscription } from "rxjs";
import { BlockifyPipe } from "../blockify.pipe";

const NEWLINES_REGEX = /(\r\n|\r|\n)/g;

@Component({
  selector: 'app-cipher-modal',
  templateUrl: './cipher-modal.component.html',
  styleUrls: ['./cipher-modal.component.css']
})
export class CipherModalComponent implements OnInit, OnDestroy {
  blockifyPipe = new BlockifyPipe();
  ciphers: Cipher[];
  cipher: Cipher;
  mode: string;
  nameFormGroup: FormGroup;
  newCipherForm: FormGroup;
  dimensionsFormGroup: FormGroup;
  ciphertextFormGroup: FormGroup;
  ciphersSubscription: Subscription;

  constructor(public dialogRef: MatDialogRef<CipherModalComponent>, @Inject(MAT_DIALOG_DATA) public data: any, private fb: FormBuilder, private cipherService: CipherService, private _snackBar: MatSnackBar) { }

  ngOnInit() {
    this.ciphersSubscription = this.cipherService.getCiphersAsObservable().subscribe(ciphers => {
      this.ciphers = ciphers;
    });

    this.cipher = this.data.cipher;
    this.mode = this.data.mode;

    this.nameFormGroup = this.fb.group({
      name: {
        value: this.cipher ? this.cipher.name : '',
        disabled: this.mode === 'EDIT'
      }
    });

    this.dimensionsFormGroup = this.fb.group({
      rows: [this.cipher ? this.cipher.rows : '', [Validators.min(0), Validators.pattern('^[0-9]+$')]],
      columns: [this.cipher ? this.cipher.columns : '', [Validators.min(0), Validators.pattern('^[0-9]+$')]]
    });

    this.ciphertextFormGroup = this.fb.group({
      ciphertext: [this.cipher ? this.cipher.ciphertext : '']
    });

    let cipherLengthValidator: ValidatorFn = () => {
      let expectedLength = this.dimensionsFormGroup.get('rows').value * this.dimensionsFormGroup.get('columns').value;

      return this.ciphertextFormGroup.get('ciphertext').value.replace(NEWLINES_REGEX, '').length === expectedLength ? null : { cipherLength: false };
    };

    this.ciphertextFormGroup.get('ciphertext').setValidators([cipherLengthValidator]);

    this.newCipherForm = this.fb.group({
      formArray: this.fb.array([
        this.nameFormGroup,
        this.dimensionsFormGroup,
        this.ciphertextFormGroup
      ])
    });
  }

  ngOnDestroy() {
    this.ciphersSubscription.unsubscribe();
  }

  get formArray(): AbstractControl | null {
    return this.newCipherForm.get('formArray');
  }

  save() {
    let name = this.nameFormGroup.get('name').value;
    let rows = this.dimensionsFormGroup.get('rows').value;
    let columns = this.dimensionsFormGroup.get('columns').value;
    // Remove newlines from blockify pipe
    let ciphertext = this.ciphertextFormGroup.get('ciphertext').value.replace(NEWLINES_REGEX, '');
    let request = new CipherRequest(name, rows, columns, ciphertext);

    if (this.mode === 'CREATE') {
      this.create(request);
    } else if (this.mode === 'EDIT') {
      this.update(request);
    }
  }

  create(request: CipherRequest) {
    this.cipherService.createCipher(request).subscribe(() => {
      let cipher = new Cipher(request.name, request.rows, request.columns, request.ciphertext);

      this.ciphers.push(cipher);

      this.cipherService.updateCiphers(this.ciphers);

      this.dialogRef.close();

      this._snackBar.open('Created "' + cipher.name + '"', '',{
        duration: 2000,
        verticalPosition: 'top'
      });
    }, () => {
      this._snackBar.open('Unable to create "' + request.name + '"', '',{
        duration: 2000,
        verticalPosition: 'top'
      });
    });
  }

  update(request: CipherRequest) {
    this.cipherService.updateCipher(request.name, request).subscribe(() => {
      this.ciphers.forEach((next) => {
        if(next.name === request.name) {
          next.rows = request.rows;
          next.columns = request.columns;
          next.ciphertext = request.ciphertext;
        }
      });

      this.cipherService.updateCiphers(this.ciphers);

      this.dialogRef.close();

      this._snackBar.open('Updated "' + request.name + '"', '',{
        duration: 2000,
        verticalPosition: 'top'
      });
    }, () => {
      this._snackBar.open('Unable to update "' + request.name + '"', '',{
        duration: 2000,
        verticalPosition: 'top'
      });
    });
  }

  ciphertextChange() {
    let expectedLength = this.dimensionsFormGroup.get('rows').value * this.dimensionsFormGroup.get('columns').value;

    let blockCiphertext = this.ciphertextFormGroup.get('ciphertext').value.replace(NEWLINES_REGEX, '').substr(0, expectedLength);

    blockCiphertext = this.blockifyPipe.transform(blockCiphertext, this.dimensionsFormGroup.get('columns').value).toString();

    this.ciphertextFormGroup.get('ciphertext').setValue(blockCiphertext);
  }
}
