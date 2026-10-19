import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { of, throwError } from 'rxjs';
import { vi } from 'vitest';
import { LoginComponent } from './login.component';
import { AuthService } from '../core/auth.service';

describe('LoginComponent', () => {
  const authStub = { login: vi.fn(), logout: vi.fn(), isLoggedIn: () => false, token: () => null };
  const navigate = vi.fn(() => Promise.resolve(true));

  beforeEach(async () => {
    authStub.login.mockReset();
    navigate.mockClear();
    await TestBed.configureTestingModule({
      imports: [LoginComponent],
      providers: [
        provideRouter([]),
        { provide: AuthService, useValue: authStub },
        { provide: Router, useValue: { navigate, parseUrl: (u: string) => u } },
      ],
    }).compileComponents();
  });

  it('renders the login form', () => {
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;
    expect(el.textContent).toContain('ACME Salary Management');
    expect(el.querySelector('button')?.textContent).toContain('Login');
  });

  it('submits credentials and navigates to /employees on success', () => {
    authStub.login.mockReturnValue(of({ token: 't' }));
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    (fixture.nativeElement as HTMLElement).querySelector('button')!.click();
    fixture.detectChanges();

    expect(authStub.login).toHaveBeenCalledWith('hr@company.com', '');
    expect(navigate).toHaveBeenCalledWith(['/employees']);
  });

  it('shows an error and stays put when credentials are rejected', () => {
    authStub.login.mockReturnValue(throwError(() => new Error('401')));
    const fixture = TestBed.createComponent(LoginComponent);
    fixture.detectChanges();

    (fixture.nativeElement as HTMLElement).querySelector('button')!.click();
    fixture.detectChanges();

    const el = fixture.nativeElement as HTMLElement;
    expect(el.querySelector('.error')?.textContent).toContain('Invalid credentials');
    expect(navigate).not.toHaveBeenCalled();
  });
});
