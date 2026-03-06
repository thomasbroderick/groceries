import { mock } from "bun:test";

mock.module("@/api/kroger", () => ({
  addToCart: mock(() => Promise.resolve({ message: "Added to cart!" })),
  searchProducts: mock(() => Promise.resolve([])),
}));

mock.module("@/api/meals", () => ({
  linkProduct: mock(() => Promise.resolve()),
}));

mock.module("@/api/config", () => ({
  getKrogerConfig: mock(() => Promise.resolve(null)),
}));

import { describe, it, expect } from "bun:test";
import { screen, fireEvent } from "@testing-library/react";
import { renderWithProviders } from "@/test/test-utils";
import { ConsolidatedList } from "./ConsolidatedList";
import type { ConsolidatedIngredientDto } from "@/api/client";

const items: ConsolidatedIngredientDto[] = [
  { name: "Garlic", consolidatedQuantity: "3", krogerProductId: "upc-1", krogerProductName: "Garlic Bulb" },
  { name: "Onion", consolidatedQuantity: "2", krogerProductId: null, krogerProductName: null },
  { name: "Olive Oil", consolidatedQuantity: null, krogerProductId: "upc-3", krogerProductName: "Kirkland Olive Oil" },
];

// Both mobile cards and desktop table render simultaneously in tests (no CSS media
// queries), so element counts are doubled. Use getAllBy* and check presence.

describe("ConsolidatedList", () => {
  it("shows empty state when no items", () => {
    renderWithProviders(
      <ConsolidatedList items={[]} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    expect(screen.getByText(/Select meals on the left/)).toBeTruthy();
  });

  it("renders item names", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    expect(screen.getAllByText("Garlic").length).toBeGreaterThan(0);
    expect(screen.getAllByText("Onion").length).toBeGreaterThan(0);
  });

  it("shows Kroger product names for linked items", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    expect(screen.getAllByText("Garlic Bulb").length).toBeGreaterThan(0);
  });

  it("shows 'Add N items to Kroger Cart' button with linked count", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    // 2 items have krogerProductId (Garlic + Olive Oil)
    expect(screen.getByText(/Add 2 items to Kroger Cart/)).toBeTruthy();
  });

  it("shows warning when unlinked items exist", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    expect(screen.getByText(/without a linked product/)).toBeTruthy();
  });

  it("shows Kroger auth warning when hasKrogerAuth is false", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth={false} onLinked={() => {}} />,
    );
    expect(screen.getByText(/Kroger account not connected/)).toBeTruthy();
  });

  it("excludes an item and moves it to the excluded section", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    const removeButtons = screen.getAllByTitle("Remove from cart");
    fireEvent.click(removeButtons[0]);
    // Both mobile and desktop show "Excluded from cart" — just confirm at least one exists
    expect(screen.getAllByText(/Excluded from cart/).length).toBeGreaterThan(0);
  });

  it("restores an excluded item", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    const removeButtons = screen.getAllByTitle("Remove from cart");
    fireEvent.click(removeButtons[0]);

    // Mobile and desktop both render a Restore button — click the first one
    const restoreButtons = screen.getAllByTitle("Restore");
    fireEvent.click(restoreButtons[0]);

    expect(screen.queryByTitle("Restore")).toBeNull();
    expect(screen.queryAllByText(/Excluded from cart/).length).toBe(0);
  });

  it("reduces linked count when a linked item is excluded", () => {
    renderWithProviders(
      <ConsolidatedList items={items} mealIds={[1]} hasKrogerAuth onLinked={() => {}} />,
    );
    expect(screen.getByText(/Add 2 items to Kroger Cart/)).toBeTruthy();

    // Garlic is the first item (linked). Exclude it.
    const removeButtons = screen.getAllByTitle("Remove from cart");
    fireEvent.click(removeButtons[0]);

    expect(screen.getByText(/Add 1 item to Kroger Cart/)).toBeTruthy();
  });
});
