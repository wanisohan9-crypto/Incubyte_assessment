import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Country, Employee, EmployeeForm, MedianResponse, Page } from './models';

const API = 'http://localhost:8080/api';

export interface EmployeeQuery {
  countryId?: number;
  search?: string;
  status?: string;
  page?: number;
  size?: number;
  sort?: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  private http = inject(HttpClient);

  countries(): Observable<Country[]> {
    return this.http.get<Country[]>(`${API}/countries`);
  }

  employees(q: EmployeeQuery): Observable<Page<Employee>> {
    let params = new HttpParams();
    if (q.countryId != null) params = params.set('countryId', q.countryId);
    if (q.search) params = params.set('search', q.search);
    if (q.status) params = params.set('status', q.status);
    params = params.set('page', q.page ?? 0).set('size', q.size ?? 20);
    if (q.sort) params = params.set('sort', q.sort);
    return this.http.get<Page<Employee>>(`${API}/employees`, { params });
  }

  createEmployee(form: EmployeeForm): Observable<Employee> {
    return this.http.post<Employee>(`${API}/employees`, form);
  }

  updateEmployee(id: number, form: EmployeeForm): Observable<Employee> {
    return this.http.put<Employee>(`${API}/employees/${id}`, form);
  }

  deactivateEmployee(id: number): Observable<void> {
    return this.http.patch<void>(`${API}/employees/${id}/deactivate`, {});
  }

  median(countryId?: number): Observable<MedianResponse> {
    let params = new HttpParams();
    if (countryId != null) params = params.set('countryId', countryId);
    return this.http.get<MedianResponse>(`${API}/salaries/median`, { params });
  }
}
