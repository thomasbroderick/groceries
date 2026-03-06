import { describe, it, expect } from "bun:test";
import { cn } from "./utils";

describe("cn", () => {
  it("returns a single class unchanged", () => {
    expect(cn("foo")).toBe("foo");
  });

  it("joins multiple classes", () => {
    expect(cn("foo", "bar")).toBe("foo bar");
  });

  it("ignores falsy values", () => {
    expect(cn("foo", false, undefined, null, "bar")).toBe("foo bar");
  });

  it("handles conditional object syntax", () => {
    expect(cn({ foo: true, bar: false })).toBe("foo");
  });

  it("merges conflicting Tailwind classes (last wins)", () => {
    expect(cn("p-2", "p-4")).toBe("p-4");
    expect(cn("text-red-500", "text-blue-500")).toBe("text-blue-500");
  });

  it("handles array inputs", () => {
    expect(cn(["foo", "bar"])).toBe("foo bar");
  });

  it("returns empty string when given no truthy inputs", () => {
    expect(cn(false, undefined)).toBe("");
  });
});
