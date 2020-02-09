import { Component, OnInit, ViewChild } from '@angular/core';
import { CipherService } from "../cipher.service";
import { MatTableDataSource } from "@angular/material/table";
import { MatSort } from "@angular/material/sort";
import { MatPaginator } from "@angular/material/paginator";
import { Cipher } from "../models/Cipher";
import { NewCipherModalComponent } from "../new-cipher-modal/new-cipher-modal.component";
import { MatDialog } from "@angular/material/dialog";

@Component({
  selector: 'app-manage-ciphers',
  templateUrl: './manage-ciphers.component.html',
  styleUrls: ['./manage-ciphers.component.css']
})
export class ManageCiphersComponent implements OnInit {
  displayedColumns: string[] = ['name', 'rows', 'columns', 'ciphertext', 'actions'];
  ciphers: MatTableDataSource<any>;
  pageSizeOptions = [10, 20, 50];

  @ViewChild(MatSort, { static: true }) sort: MatSort;
  @ViewChild(MatPaginator, { static: true }) paginator: MatPaginator;

  constructor(private cipherService: CipherService, private dialog: MatDialog) { }

  ngOnInit() {
    this.cipherService.getCiphersAsObservable().subscribe((ciphers$) => {
      this.ciphers = new MatTableDataSource(ciphers$);
      this.ciphers.sort = this.sort;
      this.ciphers.paginator = this.paginator;
      this.ciphers.filterPredicate = (data: Cipher, filter: string) => {
        return data.name.indexOf(filter) > -1;
      };
    });
  }

  applyFilter(event: Event) {
    const filterValue = (event.target as HTMLInputElement).value;
    this.ciphers.filter = filterValue.trim().toLowerCase();
  }

  openNewCipherModal() {
    const dialogRef = this.dialog.open(NewCipherModalComponent, {
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log('The dialog was closed');
    });
  }
}
