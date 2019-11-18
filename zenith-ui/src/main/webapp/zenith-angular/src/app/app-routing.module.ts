import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { DashboardComponent } from "./dashboard/dashboard.component";
import { SettingsComponent } from "./settings/settings.component";
import { NotFoundComponent } from "./not-found/not-found.component";
import { TransformersComponent } from "./transformers/transformers.component";


const routes: Routes = [
  { path: '', redirectTo: '/dashboard', pathMatch: 'full' },
  { path: 'dashboard', component: DashboardComponent, data: { state: 'dashboard' } },
  { path: 'settings', component: SettingsComponent, data: { state: 'settings' } },
  { path: 'transformers', component: TransformersComponent, data: { state: 'transformers' } },
  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
