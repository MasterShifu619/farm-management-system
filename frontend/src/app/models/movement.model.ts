import { Farm } from './farm.model';

export interface FarmDistance {
  farmId: number;
  farmName: string;
  stateCode: string;
  hops: number;
  earliestExposureDate: string;
}

export interface Movement {
  id: number;
  sourceFarm: Farm;
  destinationFarm: Farm;
  movementDate: string;
  animalCount: number;
  species: string;
}
