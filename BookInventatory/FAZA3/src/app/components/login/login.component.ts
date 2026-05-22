import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';
import { LoginRequest } from '../../models/auth.model';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css']
})
export class LoginComponent {
  credentials: LoginRequest = { username: '', password: '' };
  errorMessage: string = '';
  isLoading: boolean = false;

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  onSubmit(): void {
    if (!this.credentials.username || !this.credentials.password) {
      this.errorMessage = 'Te rugăm să completezi toate câmpurile!';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    this.authService.login(this.credentials).subscribe({
      next: (response) => {
        console.log('Succes:', response);
        this.isLoading = false;
        if (response.role === 'LIBRARIAN') {
          this.router.navigate(['/admin/books']);
        } else {
          this.router.navigate(['/books']);
        }
      },
      error: (error) => {
        this.isLoading = false;
        console.error('Status:', error.status);
        console.error('Message:', error.message);
        console.error('Error object:', error);

        if (error.status === 0) {
          this.errorMessage = 'Nu se poate conecta la server (port 8081)';
        } else if (error.status === 401 || error.status === 403) {
          this.errorMessage = 'Username sau parolă incorectă! Încearcă: alice/admin123 sau reader1/password';
        } else if (error.error && error.error.message) {
          this.errorMessage = error.error.message;
        } else {
          this.errorMessage = `Eroare ${error.status}: ${error.message}`;
        }
      }
    });
  }
}
