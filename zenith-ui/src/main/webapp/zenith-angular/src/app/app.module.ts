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

import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HTTP_INTERCEPTORS, HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';
import { JsonPipe } from "@angular/common";

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SideNavComponent } from './side-nav/side-nav.component';
import { CipherStatsSummaryComponent } from './cipher-stats-summary/cipher-stats-summary.component';
import { CiphertextComponent } from './ciphertext/ciphertext.component';
import { PlaintextComponent } from './plaintext/plaintext.component';
import { BlockifyPipe } from './blockify.pipe';
import { DashboardComponent } from './dashboard/dashboard.component';
import { SettingsComponent } from './settings/settings.component';
import { NotFoundComponent } from './not-found/not-found.component';
import { SortablejsModule } from "ngx-sortablejs";
import { CiphertextTransformersComponent } from './ciphertext-transformers/ciphertext-transformers.component';
import { BrowserAnimationsModule } from "@angular/platform-browser/animations";
import { MatTooltipModule } from '@angular/material/tooltip';
import { DefaultHttpInterceptor } from "./interceptors/default-http-interceptor";
import { CipherModalComponent } from './cipher-modal/cipher-modal.component';
import { MatDialogModule } from "@angular/material/dialog";
import { MatButtonModule } from "@angular/material/button";
import { MatStepperModule } from "@angular/material/stepper";
import { MatFormFieldModule } from "@angular/material/form-field";
import { MatInputModule } from "@angular/material/input";
import { ManageCiphersComponent } from './manage-ciphers/manage-ciphers.component';
import { MatTableModule } from "@angular/material/table";
import { MatSortModule } from "@angular/material/sort";
import { MatPaginatorModule } from "@angular/material/paginator";
import { MatSnackBarModule } from "@angular/material/snack-bar";
import { PlaintextTransformersComponent } from './plaintext-transformers/plaintext-transformers.component';
import { FormlyMaterialModule } from "@ngx-formly/material";
import { FormlyModule } from "@ngx-formly/core";
import { PlaintextSampleComponent } from './plaintext-sample/plaintext-sample.component';
import { MatProgressSpinnerModule } from "@angular/material/progress-spinner";
import { MatRadioModule } from "@angular/material/radio";
import { MatSelectModule } from "@angular/material/select";
import { HelpComponent } from './help/help.component';

@NgModule({
  declarations: [
    AppComponent,
    SideNavComponent,
    CipherStatsSummaryComponent,
    CiphertextComponent,
    PlaintextComponent,
    BlockifyPipe,
    DashboardComponent,
    SettingsComponent,
    NotFoundComponent,
    CiphertextTransformersComponent,
    CipherModalComponent,
    ManageCiphersComponent,
    PlaintextTransformersComponent,
    PlaintextSampleComponent,
    HelpComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    SortablejsModule.forRoot({animation: 150}),
    BrowserAnimationsModule,
    MatTooltipModule,
    MatDialogModule,
    MatButtonModule,
    MatStepperModule,
    MatFormFieldModule,
    MatInputModule,
    MatTableModule,
    MatSortModule,
    MatPaginatorModule,
    MatSnackBarModule,
    FormlyModule.forRoot(),
    FormlyMaterialModule,
    MatProgressSpinnerModule,
    MatRadioModule,
    MatSelectModule
  ],
  providers: [
    JsonPipe,
    {
      provide: HTTP_INTERCEPTORS,
      useClass: DefaultHttpInterceptor,
      multi: true
    },
  ],
  bootstrap: [AppComponent]
})
export class AppModule { }
