import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [FormsModule, MatCardModule, MatFormFieldModule, MatInputModule, MatButtonModule],
  template: `
    <div class="login-wrap">
      <mat-card>
        <mat-card-title>ACME Salary Management</mat-card-title>
        <mat-card-subtitle>HR Manager login</mat-card-subtitle>
        <mat-card-content>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Email</mat-label>
            <input matInput type="email" [(ngModel)]="email" placeholder="hr@company.com" />
          </mat-form-field>
          <mat-form-field appearance="outline" class="full">
            <mat-label>Password</mat-label>
            <input matInput type="password" [(ngModel)]="password" (keyup.enter)="login()" />
          </mat-form-field>
          @if (error()) {
            <p class="error">{{ error() }}</p>
          }
        </mat-card-content>
        <mat-card-actions>
          <button mat-raised-button color="primary" (click)="login()" [disabled]="loading()">
            {{ loading() ? 'Signing in…' : 'Login' }}
          </button>
        </mat-card-actions>
      </mat-card>
    </div>
  `,
  styles: [
    `
      .login-wrap {
        display: flex;
        justify-content: center;
        padding-top: 10vh;
      }
      mat-card {
        width: 360px;
        padding: 8px;
      }
      .full {
        width: 100%;
      }
      .error {
        color: #c62828;
      }
    `,
  ],
})
export class LoginComponent {
  private auth = inject(AuthService);
  private router = inject(Router);

  email = 'hr@company.com';
  password = '';
  loading = signal(false);
  error = signal('');

  login(): void {
    this.loading.set(true);
    this.error.set('');
    this.auth.login(this.email, this.password).subscribe({
      next: () => {
        this.loading.set(false);
        this.router.navigate(['/employees']);
      },
      error: () => {
        this.loading.set(false);
        this.error.set('Invalid credentials. Try hr@company.com / Password123!');
      },
    });
  }
}
