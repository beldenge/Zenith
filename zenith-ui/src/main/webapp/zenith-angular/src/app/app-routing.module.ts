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

import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from "./dashboard/dashboard.component";
import { SettingsComponent } from "./settings/settings.component";
import { NotFoundComponent } from "./not-found/not-found.component";
import { CiphertextTransformersComponent } from "./ciphertext-transformers/ciphertext-transformers.component";
import { ManageCiphersComponent } from "./manage-ciphers/manage-ciphers.component";
import { PlaintextTransformersComponent } from "./plaintext-transformers/plaintext-transformers.component";
import { HelpComponent } from "./help/help.component";


const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, data: { state: 'dashboard' } },
  { path: 'settings', component: SettingsComponent, data: { state: 'settings' } },
  { path: 'transformers/ciphertext', component: CiphertextTransformersComponent, data: { state: 'ciphertext-transformers' } },
  { path: 'transformers/plaintext', component: PlaintextTransformersComponent, data: { state: 'plaintext-transformers' }},
  { path: 'ciphers', component: ManageCiphersComponent, data: { state: 'ciphers' } },
  { path: 'help', component: HelpComponent, data: { state: 'help' } },
  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
