export interface AuthResponse {
  token: string;
  username: string;
  role: string; // e.g. "ROLE_PRODUCER"
}
