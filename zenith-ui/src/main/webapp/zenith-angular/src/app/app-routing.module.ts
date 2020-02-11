import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from "./dashboard/dashboard.component";
import { SettingsComponent } from "./settings/settings.component";
import { NotFoundComponent } from "./not-found/not-found.component";
import { CiphertextTransformersComponent } from "./ciphertext-transformers/ciphertext-transformers.component";
import { ManageCiphersComponent } from "./manage-ciphers/manage-ciphers.component";
import { PlaintextTransformersComponent } from "./plaintext-transformers/plaintext-transformers.component";


const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, data: { state: 'dashboard' } },
  { path: 'settings', component: SettingsComponent, data: { state: 'settings' } },
  { path: 'transformers/ciphertext', component: CiphertextTransformersComponent, data: { state: 'ciphertext-transformers' } },
  { path: 'transformers/plaintext', component: PlaintextTransformersComponent, data: { state: 'plaintext-transformers' }},
  { path: 'ciphers', component: ManageCiphersComponent, data: { state: 'ciphers' } },
  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
