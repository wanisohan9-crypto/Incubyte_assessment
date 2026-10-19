export interface Country {
  id: number;
  name: string;
  isoCode: string;
  defaultCurrencyCode: string;
}

export interface Employee {
  id: number;
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  countryId: number;
  countryName: string;
  annualSalary: number;
  salaryCurrency: string;
  status: 'ACTIVE' | 'INACTIVE';
}

export interface EmployeeForm {
  employeeCode: string;
  firstName: string;
  lastName: string;
  email: string;
  countryId: number;
  annualSalary: number;
  salaryCurrency: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface MedianResponse {
  scope: 'COUNTRY' | 'GLOBAL';
  countryId: number | null;
  countryName: string | null;
  medianSalaryUsd: number;
  employeeCount: number;
}
