import { Component, OnInit, ViewChild } from '@angular/core';
import { CipherService } from "../cipher.service";
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";
import { MatPaginator } from "@angular/material/paginator";
import { Cipher } from "../models/Cipher";
import { CipherModalComponent } from "../cipher-modal/cipher-modal.component";
import { MatDialog } from "@angular/material/dialog";
import { MatSnackBar } from "@angular/material/snack-bar";

@Component({
  selector: 'app-manage-ciphers',
  templateUrl: './manage-ciphers.component.html',
  styleUrls: ['./manage-ciphers.component.css']
})
export class ManageCiphersComponent implements OnInit {
  displayedColumns: string[] = ['name', 'rows', 'columns', 'ciphertext', 'actions'];
  ciphersDataSource: MatTableDataSource<any>;
  pageSizeOptions = [10, 20, 50];
  ciphers: Cipher[];

  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private cipherService: CipherService, private dialog: MatDialog, private _snackBar: MatSnackBar) { }

  ngOnInit() {
    this.cipherService.getCiphersAsObservable().subscribe((ciphers) => {
      this.ciphersDataSource = new MatTableDataSource(ciphers);
      this.ciphersDataSource.sort = this.sort;
      this.ciphersDataSource.paginator = this.paginator;
      this.ciphersDataSource.filterPredicate = (data: Cipher, filter: string) => {
        return data.name.indexOf(filter) > -1;
      };
      this.ciphers = ciphers;
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.ciphersDataSource.filter = filterValue.trim().toLowerCase();
  }

  createCipher() {
    this.dialog.open(CipherModalComponent, {
      data: {
        mode: 'CREATE'
      }
    });
  }

  editCipher(cipher: Cipher) {
    this.dialog.open(CipherModalComponent, {
      data: {
        cipher: cipher,
        mode: 'EDIT'
      }
    });
  }

  deleteCipher(cipher: Cipher) {
    this.cipherService.deleteCipher(cipher.name).subscribe(() => {
      let filteredCiphers = this.ciphers.filter((next) => {
        return next.name !== cipher.name;
      });

      this.cipherService.updateCiphers(filteredCiphers);

      this._snackBar.open('Deleted "' + cipher.name + '"', '',{
        duration: 2000,
        verticalPosition: 'top'
      });
    });
  }
}
