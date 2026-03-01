import { apiFetch, type KrogerConfigDto } from "./client";

export const getKrogerConfig = (): Promise<KrogerConfigDto> =>
  apiFetch("/api/config/kroger");

export const saveKrogerConfig = (data: { clientId: string; clientSecret: string }): Promise<KrogerConfigDto> =>
  apiFetch("/api/config/kroger", {
    method: "PUT",
    body: JSON.stringify(data),
  });

export const updateLocation = (locationId: string, locationName: string | null): Promise<KrogerConfigDto> =>
  apiFetch("/api/config/kroger/location", {
    method: "PATCH",
    body: JSON.stringify({ locationId, locationName }),
  });
