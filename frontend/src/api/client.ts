export class ApiError extends Error {
  constructor(
    public status: number,
    message: string,
  ) {
    super(message);
    this.name = "ApiError";
  }
}

const API_BASE = import.meta.env.BASE_URL.replace(/\/$/, "");

export function setToken(token: string) {
  localStorage.setItem("auth_token", token);
}

export function clearToken() {
  localStorage.removeItem("auth_token");
}

function getToken(): string | null {
  return localStorage.getItem("auth_token");
}

export async function apiFetch<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const token = getToken();
  const headers: Record<string, string> = { "Content-Type": "application/json" };
  if (token) headers["Authorization"] = `Bearer ${token}`;
  if (options?.headers) {
    Object.assign(headers, options.headers);
  }

  const response = await fetch(API_BASE + path, {
    ...options,
    headers,
  });

  if (response.status === 401) {
    clearToken();
    if (!window.location.pathname.endsWith("/login")) {
      window.location.href = import.meta.env.BASE_URL + "login";
    }
    throw new ApiError(401, "Unauthorized");
  }

  if (!response.ok) {
    const text = await response.text().catch(() => response.statusText);
    throw new ApiError(response.status, text);
  }

  if (response.status === 204 || response.headers.get("content-length") === "0") {
    return undefined as T;
  }

  return response.json() as Promise<T>;
}

// Shared TS types mirroring backend DTOs

export interface MealSummaryDto {
  id: number;
  name: string;
  ingredientCount: number;
}

export interface MealDto {
  id: number;
  name: string;
  ingredients: IngredientDto[];
}

export interface IngredientDto {
  id: number;
  name: string;
  quantity: string | null;
  krogerProductId: string | null;
  krogerProductName: string | null;
}

export interface ConsolidatedIngredientDto {
  name: string;
  consolidatedQuantity: string | null;
  krogerProductId: string | null;
  krogerProductName: string | null;
}

export interface KrogerProductDto {
  productId: string;
  upc: string | null;
  description: string;
  price: number | null;
  imageUrl: string | null;
}

export interface KrogerLocationDto {
  locationId: string;
  name: string;
  address: string;
}

export interface KrogerConfigDto {
  clientId: string;
  locationId: string | null;
  locationName: string | null;
  hasToken: boolean;
}

export interface AuthUrlResponseDto {
  authorizationUrl: string;
  state: string;
}

export interface FitnessGoalRequest {
  dailyCalories?: number;
  proteinGrams?: number;
  carbsGrams?: number;
  fatGrams?: number;
  dietaryRestrictions?: string[];
  cuisinePreferences?: string[];
  mealsPerDay?: number;
  numberOfDays?: number;
  notes?: string;
}

export interface MealPlanResponse {
  summary: string;
}

export async function generateMealPlan(goals: FitnessGoalRequest): Promise<MealPlanResponse> {
  return apiFetch<MealPlanResponse>("/api/agent/meal-plan", {
    method: "POST",
    body: JSON.stringify(goals),
  });
}
