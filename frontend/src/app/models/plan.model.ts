import { Farm } from './farm.model';

export type PlanStatus = 'DRAFT' | 'SUBMITTED' | 'APPROVED' | 'REJECTED';

export interface BiosecurityPlan {
  id: number;
  farm: Farm;
  status: PlanStatus;
  hasPerimeterFencing: boolean;
  hasVisitorLog: boolean;
  hasDisinfectionProtocol: boolean;
  notes: string;
}

export interface PlanRequest {
  farmId: number | null;
  hasPerimeterFencing: boolean;
  hasVisitorLog: boolean;
  hasDisinfectionProtocol: boolean;
  notes: string;
}
