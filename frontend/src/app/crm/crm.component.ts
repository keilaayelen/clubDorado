import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';
import { CrmService } from '../core/crm.service';
import { CrmSettings } from '../core/models';

@Component({
  selector: 'app-crm',
  imports: [FormsModule, RouterLink],
  templateUrl: './crm.component.html',
  styleUrl: './crm.component.css'
})
export class CrmComponent implements OnInit {
  settings: CrmSettings = {
    welcomeMessage: '',
    lineOneName: '',
    lineOnePhone: '',
    lineTwoName: '',
    lineTwoPhone: ''
  };
  error = '';
  message = '';
  saving = false;

  constructor(
    private crm: CrmService,
    private auth: AuthService,
    private router: Router
  ) {}

  ngOnInit() {
    this.crm.settings().subscribe({
      next: settings => this.settings = settings,
      error: () => this.error = 'No se pudo abrir el CRM'
    });
  }

  save() {
    this.saving = true;
    this.error = '';
    this.message = '';
    this.crm.save(this.settings).subscribe({
      next: settings => {
        this.settings = settings;
        this.saving = false;
        this.message = 'Guardado. El bot usa este saludo cuando el número esté conectado.';
      },
      error: (err) => {
        this.saving = false;
        this.error = err.error?.error || 'No se pudo guardar';
      }
    });
  }

  logout() {
    this.auth.logout().subscribe({
      next: () => this.router.navigateByUrl('/'),
      error: () => {
        localStorage.removeItem('gc_token');
        this.router.navigateByUrl('/');
      }
    });
  }
}
