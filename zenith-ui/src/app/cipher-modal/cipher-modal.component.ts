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

const WHITESPACE_REGEX = /\s+/g;
const NEWLINE_REGEX = /\n+/g;

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
  ciphertextFormGroup: FormGroup;
  ciphersSubscription: Subscription;

  constructor(public dialogRef: MatDialogRef<CipherModalComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private fb: FormBuilder,
              private cipherService: CipherService,
              private _snackBar: MatSnackBar) { }

  ngOnInit() {
    this.ciphersSubscription = this.cipherService.getCiphersAsObservable().subscribe(ciphers => {
      this.ciphers = ciphers;
    });

    this.cipher = this.data.cipher;
    this.mode = this.data.mode;

    this.nameFormGroup = this.fb.group({
      name: {
        value: this.cipher ? this.cipher.name : '',
        disabled: false
      }
    });

    let self = this;
    let cipherNameValidator: ValidatorFn = (control: AbstractControl): {[key: string]: any} | null => {
      let proposedName = control.value;

      let error = null;

      if (!this.cipher || proposedName !== this.cipher.name) {
        self.ciphers.forEach(cipher => {
          if (cipher.name === proposedName) {
            error = { name: false };
          }
        });
      }

      return error;
    };

    this.nameFormGroup.get('name').setValidators([cipherNameValidator]);

    let blockCiphertext = '';
    if (this.cipher) {
      blockCiphertext = this.cipher.ciphertext.replace(WHITESPACE_REGEX, ' ');
      blockCiphertext = this.blockifyPipe.transform(blockCiphertext, this.cipher.columns).toString();
    }

    this.ciphertextFormGroup = this.fb.group({
      ciphertext: [blockCiphertext]
    });

    let cipherRowLengthValidator: ValidatorFn = () => {
      let dimensions = this.determineDimensions(this.ciphertextFormGroup.get('ciphertext').value);

      return dimensions.rows > 0 && dimensions.columns > 0 ? null : { cipherRowLength: false };
    };

    this.ciphertextFormGroup.get('ciphertext').setValidators([cipherRowLengthValidator]);

    this.newCipherForm = this.fb.group({
      formArray: this.fb.array([
        this.nameFormGroup,
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
    let rawCiphertext = this.ciphertextFormGroup.get('ciphertext').value;
    let dimensions = this.determineDimensions(rawCiphertext);
    // Remove newlines from blockify pipe
    let ciphertext = rawCiphertext.replace(WHITESPACE_REGEX, ' ');
    let request = new CipherRequest(name, dimensions.rows, dimensions.columns, ciphertext);

    if (this.mode === 'CREATE') {
      this.create(request);
    } else if (this.mode === 'EDIT') {
      this.update(request);
    }
  }

  determineDimensions(ciphertext: string) {
    let rows = ciphertext.split(NEWLINE_REGEX);
    let columns = -1;

    for (let i = 0; i < rows.length; i ++) {
      if (columns < 0) {
        columns = rows[i].split(WHITESPACE_REGEX).length;
      } else if (columns !== rows[i].split(WHITESPACE_REGEX).length) {
        columns = -1;
        break;
      }
    }

    return { rows: rows.length, columns: columns };
  }

  create(request: CipherRequest) {
    let cipher = new Cipher(request.name, request.rows, request.columns, request.ciphertext);

    this.ciphers.push(cipher);

    this.cipherService.updateCiphers(this.ciphers);

    this.dialogRef.close();

    this._snackBar.open('Created "' + cipher.name + '"', '',{
      duration: 2000,
      verticalPosition: 'top'
    });
  }

  update(request: CipherRequest) {
    this.cipher.name = request.name;
    this.cipher.rows = request.rows;
    this.cipher.columns = request.columns;
    this.cipher.ciphertext = request.ciphertext;

    this.cipherService.updateCiphers(this.ciphers);

    this.dialogRef.close();

    this._snackBar.open('Updated "' + request.name + '"', '',{
      duration: 2000,
      verticalPosition: 'top'
    });
  }
}
