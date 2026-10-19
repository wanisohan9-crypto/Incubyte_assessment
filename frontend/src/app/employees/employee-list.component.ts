import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { MatTableModule } from '@angular/material/table';
import { MatPaginatorModule, PageEvent } from '@angular/material/paginator';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { ApiService } from '../core/api.service';
import { AuthService } from '../core/auth.service';
import { Country, Employee, MedianResponse } from '../core/models';

@Component({
  selector: 'app-employee-list',
  standalone: true,
  imports: [
    FormsModule, RouterLink, DecimalPipe, MatTableModule, MatPaginatorModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule, MatToolbarModule, MatCardModule,
  ],
  templateUrl: './employee-list.component.html',
  styleUrl: './employee-list.component.css',
})
export class EmployeeListComponent implements OnInit {
  private api = inject(ApiService);
  private auth = inject(AuthService);
  private router = inject(Router);

  countries = signal<Country[]>([]);
  employees = signal<Employee[]>([]);
  total = signal(0);
  median = signal<MedianResponse | null>(null);

  columns = ['employeeCode', 'name', 'country', 'salary', 'actions'];
  countryId?: number;
  search = '';
  page = 0;
  size = 20;

  ngOnInit(): void {
    this.api.countries().subscribe((c) => this.countries.set(c));
    this.reload();
  }

  reload(): void {
    this.api
      .employees({ countryId: this.countryId, search: this.search, page: this.page, size: this.size })
      .subscribe((p) => {
        this.employees.set(p.content);
        this.total.set(p.totalElements);
      });
    this.api.median(this.countryId).subscribe((m) => this.median.set(m));
  }

  onPage(event: PageEvent): void {
    this.page = event.pageIndex;
    this.size = event.pageSize;
    this.reload();
  }

  deactivate(e: Employee): void {
    if (!confirm(`Deactivate ${e.firstName} ${e.lastName}?`)) return;
    this.api.deactivateEmployee(e.id).subscribe(() => this.reload());
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/login']);
  }
}
