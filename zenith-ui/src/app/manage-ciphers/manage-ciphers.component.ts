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

import {Component, effect, ViewChild} from '@angular/core';
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";
import { MatPaginator } from "@angular/material/paginator";
import { Cipher } from "../models/Cipher";
import { CipherModalComponent } from "../cipher-modal/cipher-modal.component";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";
import { IntroductionService } from "../introduction.service";
import {ConfigurationService} from "../configuration.service";

@Component({
    selector: 'app-manage-ciphers',
    templateUrl: './manage-ciphers.component.html',
    styleUrls: ['./manage-ciphers.component.css'],
    standalone: false
})
export class ManageCiphersComponent {
  showIntro = this.introductionService.showIntroManageCiphers;
  displayedColumns: string[] = ['name', 'rows', 'columns', 'ciphertext', 'actions'];
  ciphersDataSource: MatTableDataSource<any>;
  pageSizeOptions = [20, 50, 100];
  ciphers = this.configurationService.ciphers;
  selectedCipher = this.configurationService.selectedCipher;

  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private dialog: MatDialog,
              private snackBar: MatSnackBar,
              private introductionService: IntroductionService,
              private configurationService: ConfigurationService) {
    effect(() => {
      if (this.showIntro()) {
        setTimeout(() => {
          this.introductionService.startIntroManageCiphers();
          this.introductionService.updateShowIntroManageCiphers(false);
        }, 0);
      }
    });

    effect(() => {
      this.ciphersDataSource = new MatTableDataSource(this.ciphers());
      this.ciphersDataSource.sort = this.sort;
      this.ciphersDataSource.paginator = this.paginator;
      // BUG FIX: The filter is lowercased in applyFilter() but name was not lowercased here,
      // causing case-sensitive mismatches (e.g., searching "z340" wouldn't find "Z340").
      this.ciphersDataSource.filterPredicate = (data: Cipher, filter: string) => {
        return data.name.toLowerCase().indexOf(filter) > -1;
      };
    });
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
    const filteredCiphers = this.ciphers().filter((next) => {
      return next.name !== cipher.name;
    });

    // If the selected cipher is the one that is deleted, change the selected cipher to the first in the array
    if (this.selectedCipher().name === cipher.name) {
      this.configurationService.updateSelectedCipher(filteredCiphers[0]);
    }

    this.configurationService.updateCiphers(filteredCiphers);

    this.snackBar.open('Deleted "' + cipher.name + '"', '', {
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

      for (const item of this.ciphers()) {
        if (name === item.name) {
          name = name + suffix;
          isUnique = false;
          break;
        }
      }
    }

    const clone = new Cipher(name, cipher.rows, cipher.columns, cipher.ciphertext);

    this.configurationService.updateCiphers([...this.ciphers(), clone]);

    this.snackBar.open('Cloned "' + cipher.name + '"', '', {
      duration: 2000,
      verticalPosition: 'top'
    });
  }
}
