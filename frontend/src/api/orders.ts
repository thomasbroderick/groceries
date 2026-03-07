import { apiFetch } from "./client";

export interface OrderDto {
  id: number;
  createdAt: string;
  mealNames: string[];
}

export const createOrder = (mealIds: number[]): Promise<OrderDto> =>
  apiFetch("/api/orders", {
    method: "POST",
    body: JSON.stringify({ mealIds }),
  });

export const getRecentOrders = (): Promise<OrderDto[]> =>
  apiFetch("/api/orders");
