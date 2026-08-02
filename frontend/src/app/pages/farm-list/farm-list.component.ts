import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink, Router } from '@angular/router';
import { Farm, FarmRequest } from '../../models/farm.model';
import { FarmService } from '../../services/farm.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-farm-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './farm-list.component.html',
})
export class FarmListComponent implements OnInit {
  private farmService = inject(FarmService);
  private router = inject(Router);
  protected authService = inject(AuthService);

  // signal, not a plain field: this app is zoneless (no zone.js dependency),
  // so a plain field set inside an async subscribe() callback never triggers
  // a re-render — only signal writes (or an unrelated event like Router
  // navigation) do.
  farms = signal<Farm[]>([]);
  role = this.authService.getRole();
  newFarm: FarmRequest = { name: '', stateCode: '' };

  ngOnInit(): void {
    // reviewers have no farms and the backend blocks them from this endpoint
    // entirely (403) — bounce them to the page that's actually theirs, covers
    // direct navigation / refresh, not just the post-login redirect
    if (this.role === 'REVIEWER') {
      this.router.navigate(['/plans']);
      return;
    }
    this.loadFarms();
  }

  loadFarms(): void {
    this.farmService.list().subscribe({
      next: (farms) => this.farms.set(farms),
      error: (err) => console.error('Failed to load farms', err),
    });
  }

  createFarm(): void {
    this.farmService.create(this.newFarm).subscribe({
      next: () => {
        this.newFarm = { name: '', stateCode: '' };
        this.loadFarms();
      },
      error: (err) => console.error('Failed to create farm', err),
    });
  }

  deleteFarm(id: number): void {
    this.farmService.delete(id).subscribe({
      next: () => this.loadFarms(),
      error: (err) => console.error('Failed to delete farm', err),
    });
  }
}
