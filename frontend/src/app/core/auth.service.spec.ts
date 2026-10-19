import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';
import { AuthService } from './auth.service';

describe('AuthService', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(AuthService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('posts credentials and stores the returned token', () => {
    let token = '';
    service.login('hr@company.com', 'Password123!').subscribe((res) => (token = res.token));

    const req = httpMock.expectOne('http://localhost:8080/api/auth/login');
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'hr@company.com', password: 'Password123!' });
    req.flush({ token: 'jwt-123' });

    expect(token).toBe('jwt-123');
    expect(service.token()).toBe('jwt-123');
    expect(service.isLoggedIn()).toBe(true);
  });

  it('does not store anything when login fails', () => {
    service.login('hr@company.com', 'wrong').subscribe({ error: () => {} });

    httpMock.expectOne('http://localhost:8080/api/auth/login').flush('nope', { status: 401, statusText: 'Unauthorized' });

    expect(service.isLoggedIn()).toBe(false);
  });

  it('logout removes the token', () => {
    localStorage.setItem('acme_token', 'x');
    service.logout();
    expect(service.isLoggedIn()).toBe(false);
  });
});
