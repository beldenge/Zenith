/*
 * Copyright 2017-2026 George Belden
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

import { Component, Inject, OnInit } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogRef } from "@angular/material/dialog";
import { AbstractControl, UntypedFormBuilder, UntypedFormGroup, ValidatorFn } from "@angular/forms";
import { CipherRequest } from "../models/CipherRequest";
import { Cipher } from "../models/Cipher";
import { MatSnackBar } from "@angular/material/snack-bar";
import { BlockifyPipe } from "../blockify.pipe";
import {ConfigurationService} from "../configuration.service";

const SPACES_TABS_REGEX = /\t| /g;
const WHITESPACE_REGEX = /\s+/g;
const NEWLINE_REGEX = /\r?\n/g;

@Component({
    selector: 'app-cipher-modal',
    templateUrl: './cipher-modal.component.html',
    styleUrls: ['./cipher-modal.component.css'],
    standalone: false
})
export class CipherModalComponent implements OnInit {
  blockifyPipe = new BlockifyPipe();
  ciphers = this.configurationService.ciphers;
  cipher: Cipher;
  mode: string;
  newCipherForm: UntypedFormGroup;
  rows: number = null;
  columns: number = null;

  constructor(public dialogRef: MatDialogRef<CipherModalComponent>,
              @Inject(MAT_DIALOG_DATA) public data: any,
              private fb: UntypedFormBuilder,
              private snackBar: MatSnackBar,
              private configurationService: ConfigurationService) { }

  ngOnInit() {
    this.cipher = this.data.cipher;
    this.mode = this.data.mode;

    let blockCiphertext: string;
    if (this.cipher) {
      blockCiphertext = this.blockifyPipe.transform(this.cipher.ciphertext, this.cipher.columns).toString();
    }

    this.newCipherForm = this.fb.group({
      name: {
        value: this.cipher ? this.cipher.name : '',
        disabled: false
      },
      ciphertext: [blockCiphertext]
    });

    const self = this;
    const cipherNameValidator: ValidatorFn = (control: AbstractControl): {[key: string]: any} | null => {
      const proposedName = control.value;

      let error = null;

      if (!this.cipher || proposedName !== this.cipher.name) {
        self.ciphers().forEach(cipher => {
          if (cipher.name === proposedName) {
            error = { name: false };
          }
        });
      }

      return error;
    };

    this.newCipherForm.get('name').setValidators([cipherNameValidator]);

    const cipherRowLengthValidator: ValidatorFn = () => {
      const dimensions = this.determineDimensions(this.newCipherForm.get('ciphertext').value);

      this.rows = dimensions.rows > 0 ? dimensions.rows : null;
      this.columns = dimensions.columns > 0 ? dimensions.columns : null;

      return dimensions.rows > 0 && dimensions.columns > 0 ? null : { cipherRowLength: false };
    };

    this.newCipherForm.get('ciphertext').setValidators([cipherRowLengthValidator]);
  }

  save() {
    const name = this.newCipherForm.get('name').value;
    const rawCiphertext = this.newCipherForm.get('ciphertext').value;
    const dimensions = this.determineDimensions(rawCiphertext);
    // Remove newlines from blockify pipe
    const ciphertext = rawCiphertext.replace(WHITESPACE_REGEX, ' ');
    const request = new CipherRequest(name, dimensions.rows, dimensions.columns, ciphertext);

    if (this.mode === 'CREATE') {
      this.create(request);
    } else if (this.mode === 'EDIT') {
      this.update(request);
    }
  }

  determineDimensions(ciphertext: string) {
    if (!ciphertext?.length) {
      return { rows: -1, columns: -1 };
    }

    const rows = ciphertext.trim().split(NEWLINE_REGEX);
    let columns = -1;

    for (const item of rows) {
      if (columns < 0) {
        columns = item.trim().split(WHITESPACE_REGEX).length;
      } else if (columns !== item.trim().split(WHITESPACE_REGEX).length) {
        columns = -1;
        break;
      }
    }

    return {
      rows: rows.length,
      columns
    };
  }

  create(request: CipherRequest) {
    const cipher = new Cipher(request.name, request.rows, request.columns, request.ciphertext);

    this.configurationService.updateCiphers([...this.ciphers(), cipher]);

    this.dialogRef.close();

    this.snackBar.open('Created "' + cipher.name + '"', '', {
      duration: 2000,
      verticalPosition: 'top'
    });
  }

  update(request: CipherRequest) {
    this.cipher.name = request.name;
    this.cipher.rows = request.rows;
    this.cipher.columns = request.columns;
    this.cipher.ciphertext = request.ciphertext;

    this.dialogRef.close();

    this.snackBar.open('Updated "' + request.name + '"', '', {
      duration: 2000,
      verticalPosition: 'top'
    });
  }

  injectSpaces() {
    if (!this.newCipherForm.get('ciphertext').value) {
      return;
    }

    const rawCiphertext = this.newCipherForm.get('ciphertext').value.replace(SPACES_TABS_REGEX, '');
    const rawRows = rawCiphertext.split(NEWLINE_REGEX);
    let newCiphertext = '';

    let firstRow = true;
    for (const item of rawRows) {
      if (!firstRow) {
        newCiphertext += '\n';
      }

      firstRow = false;

      let first = true;
      for (let j = 0; j < item.length; j ++) {
        if (!first) {
          newCiphertext += ' ';
        }

        first = false;

        newCiphertext += item.charAt(j);
      }
    }

    this.newCipherForm.get('ciphertext').setValue(newCiphertext);
  }
}
