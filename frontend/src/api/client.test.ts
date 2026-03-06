import { describe, it, expect } from "bun:test";
import { ApiError } from "./client";

describe("ApiError", () => {
  it("has name 'ApiError'", () => {
    const err = new ApiError(404, "Not found");
    expect(err.name).toBe("ApiError");
  });

  it("stores the status code", () => {
    const err = new ApiError(500, "Server error");
    expect(err.status).toBe(500);
  });

  it("stores the message", () => {
    const err = new ApiError(400, "Bad request");
    expect(err.message).toBe("Bad request");
  });

  it("is an instance of Error", () => {
    const err = new ApiError(401, "Unauthorized");
    expect(err instanceof Error).toBe(true);
  });

  it("is an instance of ApiError", () => {
    const err = new ApiError(403, "Forbidden");
    expect(err instanceof ApiError).toBe(true);
  });
});
