import { TestBed } from '@angular/core/testing';
import { Router, provideRouter } from '@angular/router';
import { authGuard } from './auth.guard';
import { AuthService } from './auth.service';

describe('authGuard', () => {
  function run(isLoggedIn: boolean) {
    const authStub = { isLoggedIn: () => isLoggedIn } as AuthService;
    TestBed.configureTestingModule({
      providers: [provideRouter([]), { provide: AuthService, useValue: authStub }],
    });
    return TestBed.runInInjectionContext(() => authGuard({} as never, {} as never));
  }

  it('allows navigation when logged in', () => {
    expect(run(true)).toBe(true);
  });

  it('redirects to /login when logged out', () => {
    const result = run(false);
    const router = TestBed.inject(Router);
    expect(result).toEqual(router.parseUrl('/login'));
  });
});
