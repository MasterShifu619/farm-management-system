import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { BiosecurityPlan, PlanRequest } from '../../models/plan.model';
import { Farm } from '../../models/farm.model';
import { PlanService } from '../../services/plan.service';
import { FarmService } from '../../services/farm.service';
import { AuthService } from '../../services/auth.service';

@Component({
  selector: 'app-plan-list',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './plan-list.component.html',
})
export class PlanListComponent implements OnInit {
  private planService = inject(PlanService);
  private farmService = inject(FarmService);
  protected authService = inject(AuthService);

  // signal: see farm-list.component.ts for why plain fields don't work here (zoneless app)
  plans = signal<BiosecurityPlan[]>([]);
  myFarms = signal<Farm[]>([]); // producer's own farms, for the create-plan dropdown
  role = this.authService.getRole();

  newPlan: PlanRequest = {
    farmId: null,
    hasPerimeterFencing: false,
    hasVisitorLog: false,
    hasDisinfectionProtocol: false,
    notes: '',
  };

  ngOnInit(): void {
    this.loadPlans();
    if (this.role === 'PRODUCER') {
      this.farmService.list().subscribe({
        next: (farms) => this.myFarms.set(farms),
        error: (err) => console.error('Failed to load farms', err),
      });
    }
  }

  createPlan(): void {
    if (!this.newPlan.farmId) return;
    this.planService.create(this.newPlan).subscribe({
      next: () => {
        this.newPlan = {
          farmId: null,
          hasPerimeterFencing: false,
          hasVisitorLog: false,
          hasDisinfectionProtocol: false,
          notes: '',
        };
        this.loadPlans();
      },
      error: (err) => console.error('Failed to create plan', err),
    });
  }

  loadPlans(): void {
    this.planService.list().subscribe({
      next: (plans) => this.plans.set(plans),
      error: (err) => console.error('Failed to load plans', err),
    });
  }

  submit(id: number): void {
    this.planService.submit(id).subscribe({
      next: () => this.loadPlans(),
      error: (err) => console.error('Failed to submit plan', err),
    });
  }

  approve(id: number): void {
    this.planService.approve(id).subscribe({
      next: () => this.loadPlans(),
      error: (err) => console.error('Failed to approve plan', err),
    });
  }

  reject(id: number): void {
    this.planService.reject(id).subscribe({
      next: () => this.loadPlans(),
      error: (err) => console.error('Failed to reject plan', err),
    });
  }
}
