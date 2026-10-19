import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: 'login', loadComponent: () => import('./login/login.component').then((m) => m.LoginComponent) },
  {
    path: 'employees',
    loadComponent: () => import('./employees/employee-list.component').then((m) => m.EmployeeListComponent),
    canActivate: [authGuard],
  },
  {
    path: 'employees/new',
    loadComponent: () => import('./employees/employee-form.component').then((m) => m.EmployeeFormComponent),
    canActivate: [authGuard],
  },
  {
    path: 'employees/:id/edit',
    loadComponent: () => import('./employees/employee-form.component').then((m) => m.EmployeeFormComponent),
    canActivate: [authGuard],
  },
  { path: '', redirectTo: '/employees', pathMatch: 'full' },
  { path: '**', redirectTo: '/employees' },
];

