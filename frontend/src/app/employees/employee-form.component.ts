import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { MatToolbarModule } from '@angular/material/toolbar';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { ApiService } from '../core/api.service';
import { Country, EmployeeForm } from '../core/models';

@Component({
  selector: 'app-employee-form',
  standalone: true,
  imports: [
    FormsModule, RouterLink, MatToolbarModule, MatCardModule, MatFormFieldModule,
    MatInputModule, MatSelectModule, MatButtonModule,
  ],
  templateUrl: './employee-form.component.html',
  styleUrl: './employee-form.component.css',
})
export class EmployeeFormComponent implements OnInit {
  private api = inject(ApiService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  countries = signal<Country[]>([]);
  error = signal('');
  isEdit = false;
  editId?: number;

  form: EmployeeForm = {
    employeeCode: '', firstName: '', lastName: '', email: '',
    countryId: 0, annualSalary: 0, salaryCurrency: '',
  };

  ngOnInit(): void {
    this.api.countries().subscribe((c) => {
      this.countries.set(c);
      if (!this.isEdit && c.length > 0) {
        this.form.countryId = c[0].id;
        this.form.salaryCurrency = c[0].defaultCurrencyCode;
      }
    });
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.isEdit = true;
      this.editId = +id;
      this.api.employees({ page: 0, size: 10000 }).subscribe((p) => {
        const found = p.content.find((e) => e.id === this.editId);
        if (found) {
          this.form = {
            employeeCode: found.employeeCode, firstName: found.firstName,
            lastName: found.lastName, email: found.email, countryId: found.countryId,
            annualSalary: found.annualSalary, salaryCurrency: found.salaryCurrency,
          };
        }
      });
    }
  }

  onCountryChange(): void {
    const c = this.countries().find((x) => x.id === this.form.countryId);
    if (c) this.form.salaryCurrency = c.defaultCurrencyCode;
  }

  save(): void {
    this.error.set('');
    const req = this.isEdit && this.editId
      ? this.api.updateEmployee(this.editId, this.form)
      : this.api.createEmployee(this.form);
    req.subscribe({
      next: () => this.router.navigate(['/employees']),
      error: (e) => this.error.set(e.error?.messages?.join(', ') ?? 'Save failed'),
    });
  }
}
