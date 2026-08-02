import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './login.component.html',
})
export class LoginComponent {
  private authService = inject(AuthService);
  private router = inject(Router);

  username = '';
  password = '';
  // signal: error is set inside an async subscribe() callback, and this app
  // is zoneless — a plain field write there wouldn't trigger a re-render
  error = signal('');

  onSubmit(): void {
    this.error.set('');
    this.authService.login(this.username, this.password).subscribe({
      next: () => this.router.navigate(['/farms']),
      error: () => this.error.set('Invalid username or password'),
    });
  }
}
