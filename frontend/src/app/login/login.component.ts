import { Component } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../core/auth.service';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.component.html',
  styleUrl: './login.component.css'
})
export class LoginComponent {
  username = '';
  password = '';
  error = '';
  loading = false;

  constructor(
    private auth: AuthService,
    private router: Router
  ) {}

  submit() {
    this.error = '';
    this.loading = true;
    this.auth.login(this.username.trim(), this.password).subscribe({
      next: () => this.router.navigateByUrl('/panel'),
      error: (err) => {
        this.loading = false;
        this.error = err.error?.error || 'No se pudo entrar';
      }
    });
  }
}
