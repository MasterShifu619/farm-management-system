import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Farm } from '../../models/farm.model';
import { FarmDistance, Movement } from '../../models/movement.model';
import { MovementService } from '../../services/movement.service';
import { AuthService } from '../../services/auth.service';

interface GraphNode {
  farm: Farm;
  x: number;
  y: number;
}

interface GraphEdge {
  id: string;
  x1: number;
  y1: number;
  x2: number;
  y2: number;
  labelX: number;
  labelY: number;
  labelAngle: number;
  species: string;
  movementDate: string;
}

const NODE_RADIUS = 26;
const SVG_SIZE = 420;
const CENTER = SVG_SIZE / 2;
const LAYOUT_RADIUS = 160;

// sequential blue ramp, darkest = closest (source), lightest = farthest;
// unreached farms render in the muted gray instead
const HOP_COLORS = ['#0d366b', '#184f95', '#1c5cab', '#256abf', '#2a78d6', '#3987e5'];
const MUTED_FILL = '#e1e0d9';
const MUTED_STROKE = '#c3c2b7';

@Component({
  selector: 'app-movement-traversal',
  standalone: true,
  imports: [CommonModule, FormsModule, RouterLink],
  templateUrl: './movement-traversal.component.html',
})
export class MovementTraversalComponent implements OnInit {
  private movementService = inject(MovementService);
  protected authService = inject(AuthService);

  // farm dropdown + graph are built from movement data (source/destination
  // farms seen in any movement), not the role-scoped /api/farms endpoint —
  // that one blocks reviewers entirely, but movements are visible to every role
  movements = signal<Movement[]>([]);
  results = signal<FarmDistance[] | null>(null);
  loading = signal(false);
  error = signal('');

  farmId: number | null = null;
  hops = 2;
  selectedSpecies = 'ALL';

  speciesOptions = computed<string[]>(() => {
    const species = new Set(this.movements().map((m) => m.species));
    return [...species].sort();
  });

  farms = computed<Farm[]>(() => {
    const farmMap = new Map<number, Farm>();
    for (const m of this.movements()) {
      farmMap.set(m.sourceFarm.id, m.sourceFarm);
      farmMap.set(m.destinationFarm.id, m.destinationFarm);
    }
    return [...farmMap.values()].sort((a, b) => a.name.localeCompare(b.name));
  });

  // circle layout: evenly space farms around a ring, starting at the top
  nodePositions = computed<Map<number, GraphNode>>(() => {
    const farms = this.farms();
    const positions = new Map<number, GraphNode>();
    farms.forEach((farm, i) => {
      const angle = (i / farms.length) * 2 * Math.PI - Math.PI / 2;
      positions.set(farm.id, {
        farm,
        x: CENTER + LAYOUT_RADIUS * Math.cos(angle),
        y: CENTER + LAYOUT_RADIUS * Math.sin(angle),
      });
    });
    return positions;
  });

  // template-friendly array form (NgFor over Map.values() would hand it a
  // fresh, single-use iterator each render — an array is the safe contract)
  nodes = computed<GraphNode[]>(() => [...this.nodePositions().values()]);

  // edges trimmed back by NODE_RADIUS so the arrowhead lands on the circle's
  // edge instead of being hidden underneath it
  edges = computed<GraphEdge[]>(() => {
    const positions = this.nodePositions();
    return this.movements().map((m) => {
      const from = positions.get(m.sourceFarm.id)!;
      const to = positions.get(m.destinationFarm.id)!;
      const dx = to.x - from.x;
      const dy = to.y - from.y;
      const len = Math.sqrt(dx * dx + dy * dy) || 1;
      const ux = dx / len;
      const uy = dy / len;
      // perpendicular offset so the label sits beside the line, not on top of it;
      // angle so the text reads along the arrow's direction (flipped upright
      // when the arrow points right-to-left, so it's never upside down)
      const px = -uy;
      const py = ux;
      const midX = (from.x + to.x) / 2;
      const midY = (from.y + to.y) / 2;
      let labelAngle = (Math.atan2(dy, dx) * 180) / Math.PI;
      if (labelAngle > 90 || labelAngle < -90) {
        labelAngle += 180;
      }
      return {
        id: `${m.id}`,
        x1: from.x + ux * NODE_RADIUS,
        y1: from.y + uy * NODE_RADIUS,
        x2: to.x - ux * (NODE_RADIUS + 8), // extra gap for the arrowhead marker
        y2: to.y - uy * (NODE_RADIUS + 8),
        labelX: midX + px * 10,
        labelY: midY + py * 10,
        labelAngle,
        species: m.species,
        movementDate: m.movementDate,
      };
    });
  });

  readonly nodeRadius = NODE_RADIUS;
  readonly svgSize = SVG_SIZE;

  ngOnInit(): void {
    this.movementService.list().subscribe({
      next: (movements) => this.movements.set(movements),
      error: (err) => console.error('Failed to load movements', err),
    });
  }

  nodeFill(farmId: number): string {
    if (this.farmId !== null && farmId === this.farmId && this.results() !== null) {
      return HOP_COLORS[0];
    }
    const res = this.results();
    if (res) {
      const match = res.find((r) => r.farmId === farmId);
      if (match) return HOP_COLORS[Math.min(match.hops, HOP_COLORS.length - 1)];
    }
    return MUTED_FILL;
  }

  nodeStroke(farmId: number): string {
    return this.nodeFill(farmId) === MUTED_FILL ? MUTED_STROKE : this.nodeFill(farmId);
  }

  nodeIsHighlighted(farmId: number): boolean {
    return this.nodeFill(farmId) !== MUTED_FILL;
  }

  // dims edges that don't carry the selected species, so it's visually
  // obvious which movements are actually eligible for this trace
  edgeOpacity(edge: GraphEdge): number {
    return this.selectedSpecies === 'ALL' || edge.species === this.selectedSpecies ? 1 : 0.2;
  }

  runTraversal(): void {
    if (this.farmId === null) return;
    this.error.set('');
    this.loading.set(true);
    const species = this.selectedSpecies === 'ALL' ? undefined : this.selectedSpecies;
    this.movementService.traversal(this.farmId, this.hops, species).subscribe({
      next: (result) => {
        this.results.set(result);
        this.loading.set(false);
      },
      error: (err) => {
        this.error.set('Traversal failed: ' + (err.error ?? err.message));
        this.results.set(null);
        this.loading.set(false);
      },
    });
  }
}
