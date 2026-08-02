export interface FarmOwner {
  id: number;
  username: string;
  role: string;
  stateCode: string | null;
}

export interface Farm {
  id: number;
  name: string;
  stateCode: string;
  owner: FarmOwner;
}

export interface FarmRequest {
  name: string;
  stateCode: string;
}
