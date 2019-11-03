import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';
import { HttpClientModule } from '@angular/common/http';
import {FormsModule} from '@angular/forms';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { SideNavComponent } from './side-nav/side-nav.component';
import { MainPanelComponent } from './main-panel/main-panel.component';
import { CipherStatsSummaryComponent } from './cipher-stats-summary/cipher-stats-summary.component';
import { CiphertextComponent } from './ciphertext/ciphertext.component';
import { PlaintextComponent } from './plaintext/plaintext.component';
import { BlockifyPipe } from './blockify.pipe';

@NgModule({
  declarations: [
    AppComponent,
    SideNavComponent,
    MainPanelComponent,
    CipherStatsSummaryComponent,
    CiphertextComponent,
    PlaintextComponent,
    BlockifyPipe
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    FormsModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
