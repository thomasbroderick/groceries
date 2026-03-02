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

export async function apiFetch<T>(
  path: string,
  options?: RequestInit,
): Promise<T> {
  const response = await fetch(API_BASE + path, {
    headers: {
      "Content-Type": "application/json",
      ...options?.headers,
    },
    ...options,
  });

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
