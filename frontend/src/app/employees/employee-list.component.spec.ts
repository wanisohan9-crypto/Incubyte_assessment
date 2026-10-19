import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideHttpClientTesting } from '@angular/common/http/testing';
import { provideRouter } from '@angular/router';
import { of } from 'rxjs';
import { vi } from 'vitest';
import { EmployeeListComponent } from './employee-list.component';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';

describe('EmployeeListComponent', () => {
  const apiStub = {
    countries: vi.fn(() => of([{ id: 1, name: 'India', isoCode: 'IN', defaultCurrencyCode: 'INR' }])),
    employees: vi.fn(() =>
      of({
        content: [{
          id: 1, employeeCode: 'EMP001', firstName: 'Rahul', lastName: 'Sharma',
          email: 'r@company.com', countryId: 1, countryName: 'India',
          annualSalary: 1200000, salaryCurrency: 'INR', status: 'ACTIVE',
        }],
        totalElements: 1, totalPages: 1, number: 0, size: 20,
      })),
    median: vi.fn(() => of({
      scope: 'GLOBAL', countryId: null, countryName: null,
      medianSalaryUsd: 25000, employeeCount: 1,
    })),
    deactivateEmployee: vi.fn(() => of(undefined)),
  };
  const authStub = { logout: vi.fn(), isLoggedIn: () => true, token: () => 'x', login: vi.fn() };

  beforeEach(async () => {
    vi.clearAllMocks();
    await TestBed.configureTestingModule({
      imports: [EmployeeListComponent],
      providers: [
        provideRouter([]),
        provideHttpClient(),
        provideHttpClientTesting(),
        { provide: ApiService, useValue: apiStub },
        { provide: AuthService, useValue: authStub },
      ],
    }).compileComponents();
  });

  it('renders employee rows and the median banner on load', () => {
    const fixture = TestBed.createComponent(EmployeeListComponent);
    fixture.detectChanges();
    const el = fixture.nativeElement as HTMLElement;

    expect(el.querySelectorAll('tr').length).toBeGreaterThan(1); // header + 1 row
    expect(el.textContent).toContain('EMP001');
    expect(el.textContent).toContain('25,000');
  });

  it('deactivate calls API and reloads', () => {
    const fixture = TestBed.createComponent(EmployeeListComponent);
    fixture.detectChanges();
    vi.spyOn(window, 'confirm').mockReturnValue(true);

    const buttons = [...(fixture.nativeElement as HTMLElement).querySelectorAll('button')];
    buttons.find((b) => b.textContent?.includes('Deactivate'))!.click();
    fixture.detectChanges();

    expect(apiStub.deactivateEmployee).toHaveBeenCalledWith(1);
  });
});
