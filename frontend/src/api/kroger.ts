import { apiFetch, type KrogerProductDto, type KrogerLocationDto, type AuthUrlResponseDto } from "./client";

export const getAuthUrl = (): Promise<AuthUrlResponseDto> =>
  apiFetch("/api/kroger/auth/url");

export const searchProducts = (term: string, locationId?: string): Promise<KrogerProductDto[]> =>
  apiFetch("/api/kroger/products/search", {
    method: "POST",
    body: JSON.stringify({ term, locationId }),
  });

export const searchLocations = (zipCode: string): Promise<KrogerLocationDto[]> =>
  apiFetch("/api/kroger/locations/search", {
    method: "POST",
    body: JSON.stringify({ zipCode }),
  });

export const addToCart = (items: { upc: string; quantity: number }[]): Promise<{ message: string }> =>
  apiFetch("/api/kroger/cart", {
    method: "POST",
    body: JSON.stringify({ items }),
  });

export const addMealsToCart = (mealIds: number[]): Promise<{ message: string }> =>
  apiFetch("/api/kroger/cart/meals", {
    method: "POST",
    body: JSON.stringify({ mealIds }),
  });
