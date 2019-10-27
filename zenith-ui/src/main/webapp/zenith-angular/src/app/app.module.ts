import { BrowserModule } from '@angular/platform-browser';
import { NgModule } from '@angular/core';

import { AppRoutingModule } from './app-routing.module';
import { AppComponent } from './app.component';
import { TopNavComponent } from './top-nav/top-nav.component';
import { SideNavComponent } from './side-nav/side-nav.component';
import { MainPanelComponent } from './main-panel/main-panel.component';
import { CipherStatsSummaryComponent } from './cipher-stats-summary/cipher-stats-summary.component';
import { CiphertextComponent } from './ciphertext/ciphertext.component';
import { PlaintextComponent } from './plaintext/plaintext.component';

@NgModule({
  declarations: [
    AppComponent,
    TopNavComponent,
    SideNavComponent,
    MainPanelComponent,
    CipherStatsSummaryComponent,
    CiphertextComponent,
    PlaintextComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
