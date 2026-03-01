import { apiFetch, type MealSummaryDto, type MealDto, type IngredientDto, type ConsolidatedIngredientDto } from "./client";

export const getAllMeals = (): Promise<MealSummaryDto[]> =>
  apiFetch("/api/meals");

export const getMeal = (id: number): Promise<MealDto> =>
  apiFetch(`/api/meals/${id}`);

export const createMeal = (name: string): Promise<MealDto> =>
  apiFetch("/api/meals", {
    method: "POST",
    body: JSON.stringify({ name }),
  });

export const updateMeal = (id: number, name: string): Promise<MealDto> =>
  apiFetch(`/api/meals/${id}`, {
    method: "PUT",
    body: JSON.stringify({ name }),
  });

export const deleteMeal = (id: number): Promise<void> =>
  apiFetch(`/api/meals/${id}`, { method: "DELETE" });

export const getIngredients = (mealId: number): Promise<IngredientDto[]> =>
  apiFetch(`/api/meals/${mealId}/ingredients`);

export const addIngredient = (mealId: number, raw: string): Promise<IngredientDto> =>
  apiFetch(`/api/meals/${mealId}/ingredients`, {
    method: "POST",
    body: JSON.stringify({ raw }),
  });

export const updateIngredient = (
  mealId: number,
  ingredientId: number,
  data: Partial<Pick<IngredientDto, "name" | "quantity" | "krogerProductId" | "krogerProductName">>,
): Promise<IngredientDto> =>
  apiFetch(`/api/meals/${mealId}/ingredients/${ingredientId}`, {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const deleteIngredient = (mealId: number, ingredientId: number): Promise<void> =>
  apiFetch(`/api/meals/${mealId}/ingredients/${ingredientId}`, { method: "DELETE" });

export const consolidateMeals = (mealIds: number[]): Promise<ConsolidatedIngredientDto[]> =>
  apiFetch("/api/meals/consolidate", {
    method: "POST",
    body: JSON.stringify(mealIds),
  });

export const linkProduct = (
  name: string,
  mealIds: number[],
  krogerProductId: string,
  krogerProductName: string,
): Promise<void> =>
  apiFetch("/api/meals/ingredients/link", {
    method: "PATCH",
    body: JSON.stringify({ name, mealIds, krogerProductId, krogerProductName }),
  });
