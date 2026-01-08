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

import { Component, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { CipherService } from "../cipher.service";
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";
import { MatPaginator } from "@angular/material/paginator";
import { Cipher } from "../models/Cipher";
import { CipherModalComponent } from "../cipher-modal/cipher-modal.component";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { IntroductionService } from "../introduction.service";
import { Subscription } from "rxjs";

@Component({
    selector: 'app-manage-ciphers',
    templateUrl: './manage-ciphers.component.html',
    styleUrls: ['./manage-ciphers.component.css'],
    standalone: false
})
export class ManageCiphersComponent implements OnInit, OnDestroy {
  showIntroManageCiphersSubscription: Subscription;
  displayedColumns: string[] = ['name', 'rows', 'columns', 'ciphertext', 'actions'];
  ciphersDataSource: MatTableDataSource<any>;
  pageSizeOptions = [10, 20, 50];
  ciphers: Cipher[];
  selectedCipher: Cipher;
  ciphersSubscription: Subscription;
  selectedCipherSubscription: Subscription;

  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private cipherService: CipherService,
              private dialog: MatDialog,
              private snackBar: MatSnackBar,
              private introductionService: IntroductionService) {}

  ngOnInit() {
    this.ciphersSubscription = this.cipherService.getCiphersAsObservable().subscribe((ciphers) => {
      this.ciphersDataSource = new MatTableDataSource(ciphers);
      this.ciphersDataSource.sort = this.sort;
      this.ciphersDataSource.paginator = this.paginator;
      this.ciphersDataSource.filterPredicate = (data: Cipher, filter: string) => {
        return data.name.indexOf(filter) > -1;
      };
      this.ciphers = ciphers;
    });

    this.showIntroManageCiphersSubscription = this.introductionService.getShowIntroManageCiphersAsObservable().subscribe(showIntro => {
      if (showIntro) {
        setTimeout(() =>{
          this.introductionService.startIntroManageCiphers();
          this.introductionService.updateShowIntroManageCiphers(false);
        }, 500);
      }
    });

    // This is used to check and handled when the selected cipher is deleted
    this.selectedCipherSubscription = this.cipherService.getSelectedCipherAsObservable().subscribe(selectedCipher => {
      this.selectedCipher = selectedCipher
    });
  }

  ngOnDestroy() {
    this.ciphersSubscription.unsubscribe();
    this.showIntroManageCiphersSubscription.unsubscribe();
    this.selectedCipherSubscription.unsubscribe();
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.ciphersDataSource.filter = filterValue.trim().toLowerCase();
  }

  createCipher() {
    this.dialog.open(CipherModalComponent, {
      disableClose: true,
      width: '50%',
      data: {
        mode: 'CREATE'
      }
    });
  }

  editCipher(cipher: Cipher) {
    this.dialog.open(CipherModalComponent, {
      disableClose: true,
      width: '50%',
      data: {
        cipher,
        mode: 'EDIT'
      }
    });
  }

  deleteCipher(cipher: Cipher) {
    const filteredCiphers = this.ciphers.filter((next) => {
      return next.name !== cipher.name;
    });

    // If the selected cipher is the one that is deleted, change the selected cipher to the first in the array
    if (this.selectedCipher.name === cipher.name) {
      this.cipherService.updateSelectedCipher(filteredCiphers[0]);
    }

    this.cipherService.updateCiphers(filteredCiphers);

    this.snackBar.open('Deleted "' + cipher.name + '"', '',{
      duration: 2000,
      verticalPosition: 'top'
    });
  }

  cloneCipher(cipher: Cipher) {
    const suffix = '-copy';
    let name = cipher.name + suffix;
    let isUnique = false;

    while (!isUnique) {
      isUnique = true;

      for (const item of this.ciphers) {
        if (name === item.name) {
          name = name + suffix;
          isUnique = false;
          break;
        }
      }
    }

    const clone = new Cipher(name, cipher.rows, cipher.columns, cipher.ciphertext);

    this.ciphers.push(clone);
    this.cipherService.updateCiphers(this.ciphers);

    this.snackBar.open('Cloned "' + cipher.name + '"', '',{
      duration: 2000,
      verticalPosition: 'top'
    });
  }
}
