import { apiFetch, type KrogerConfigDto } from "./client";

export const getKrogerConfig = (): Promise<KrogerConfigDto> =>
  apiFetch("/api/config/kroger");

export const updateLocation = (locationId: string, locationName: string | null): Promise<KrogerConfigDto> =>
  apiFetch("/api/config/kroger/location", {
    method: "PATCH",
    body: JSON.stringify({ locationId, locationName }),
  });
