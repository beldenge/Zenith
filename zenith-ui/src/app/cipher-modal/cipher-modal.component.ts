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

const SPACES_TABS_REGEX = /\t| /g;
const WHITESPACE_REGEX = /\s+/g;
const NEWLINE_REGEX = /\r?\n/g;

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
  newCipherForm: FormGroup;
  ciphersSubscription: Subscription;
  rows: number = null;
  columns: number = null;

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

    let blockCiphertext = '';
    if (this.cipher) {
      blockCiphertext = this.cipher.ciphertext.replace(WHITESPACE_REGEX, ' ');
      blockCiphertext = this.blockifyPipe.transform(blockCiphertext, this.cipher.columns).toString();
    }

    this.newCipherForm = this.fb.group({
      name: {
        value: this.cipher ? this.cipher.name : '',
        disabled: false
      },
      ciphertext: [blockCiphertext]
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

    this.newCipherForm.get('name').setValidators([cipherNameValidator]);

    let cipherRowLengthValidator: ValidatorFn = () => {
      let dimensions = this.determineDimensions(this.newCipherForm.get('ciphertext').value);

      this.rows = dimensions.rows > 0 ? dimensions.rows : null;
      this.columns = dimensions.columns > 0 ? dimensions.columns : null;

      return dimensions.rows > 0 && dimensions.columns > 0 ? null : { cipherRowLength: false };
    };

    this.newCipherForm.get('ciphertext').setValidators([cipherRowLengthValidator]);
  }

  ngOnDestroy() {
    this.ciphersSubscription.unsubscribe();
  }

  save() {
    let name = this.newCipherForm.get('name').value;
    let rawCiphertext = this.newCipherForm.get('ciphertext').value;
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
    if (!ciphertext.length) {
      return { rows: -1, columns: -1 };
    }

    let rows = ciphertext.trim().split(NEWLINE_REGEX);
    let columns = -1;

    for (let i = 0; i < rows.length; i ++) {
      if (columns < 0) {
        columns = rows[i].trim().split(WHITESPACE_REGEX).length;
      } else if (columns !== rows[i].trim().split(WHITESPACE_REGEX).length) {
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

  injectSpaces() {
    let rawCiphertext = this.newCipherForm.get('ciphertext').value.replace(SPACES_TABS_REGEX, '');
    let rawRows = rawCiphertext.split(NEWLINE_REGEX);
    let newCiphertext = '';

    let firstRow = true;
    for (let i = 0; i < rawRows.length; i ++) {
      if (!firstRow) {
        newCiphertext += '\n';
      }

      firstRow = false;

      let first = true;
      for (let j = 0; j < rawRows[i].length; j ++) {
        if (!first) {
          newCiphertext += ' ';
        }

        first = false;

        newCiphertext += rawRows[i].charAt(j);
      }
    }

    this.newCipherForm.get('ciphertext').setValue(newCiphertext);
  }
}
