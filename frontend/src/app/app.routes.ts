import { Routes } from '@angular/router';
import { LoginComponent } from './pages/login/login.component';
import { FarmListComponent } from './pages/farm-list/farm-list.component';
import { PlanListComponent } from './pages/plan-list/plan-list.component';
import { authGuard } from './guards/auth.guard';

export const routes: Routes = [
  { path: '', redirectTo: 'farms', pathMatch: 'full' },
  { path: 'login', component: LoginComponent },
  { path: 'farms', component: FarmListComponent, canActivate: [authGuard] },
  { path: 'plans', component: PlanListComponent, canActivate: [authGuard] },
];
