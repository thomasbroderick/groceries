import { apiFetch } from "./client";

export interface UserDto {
  id: number;
  username: string;
  canUseAi: boolean;
}

export interface AuthResponse {
  token: string;
  user: UserDto;
}

export function login(username: string, password: string): Promise<AuthResponse> {
  return apiFetch("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

export function register(username: string, password: string): Promise<AuthResponse> {
  return apiFetch("/api/auth/register", {
    method: "POST",
    body: JSON.stringify({ username, password }),
  });
}

export function me(): Promise<UserDto> {
  return apiFetch("/api/auth/me");
}
