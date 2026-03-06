import { mock } from "bun:test";

mock.module("@/api/meals", () => ({
  updateMeal: mock(() => Promise.resolve({ id: 1, name: "Renamed", ingredients: [] })),
  deleteMeal: mock(() => Promise.resolve()),
}));

import { describe, it, expect } from "bun:test";
import { screen, fireEvent, waitFor } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { renderWithProviders } from "@/test/test-utils";
import { MealCard } from "./MealCard";
import type { MealSummaryDto } from "@/api/client";

const meal: MealSummaryDto = { id: 1, name: "Pasta Bake", ingredientCount: 4 };

describe("MealCard", () => {
  it("renders the meal name", () => {
    renderWithProviders(
      <MealCard meal={meal} selected={false} onSelect={() => {}} onDeleted={() => {}} />,
    );
    expect(screen.getByText("Pasta Bake")).toBeTruthy();
  });

  it("renders ingredient count with plural label", () => {
    renderWithProviders(
      <MealCard meal={meal} selected={false} onSelect={() => {}} onDeleted={() => {}} />,
    );
    expect(screen.getByText("4 ingredients")).toBeTruthy();
  });

  it("renders singular 'ingredient' for count of 1", () => {
    const single: MealSummaryDto = { ...meal, ingredientCount: 1 };
    renderWithProviders(
      <MealCard meal={single} selected={false} onSelect={() => {}} onDeleted={() => {}} />,
    );
    expect(screen.getByText("1 ingredient")).toBeTruthy();
  });

  it("calls onSelect when the card is clicked", () => {
    const onSelect = mock(() => {});
    renderWithProviders(
      <MealCard meal={meal} selected={false} onSelect={onSelect} onDeleted={() => {}} />,
    );
    fireEvent.click(screen.getByText("Pasta Bake"));
    expect(onSelect).toHaveBeenCalledTimes(1);
  });

  it("opens the rename dialog when 'Rename' is clicked", async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <MealCard meal={meal} selected={false} onSelect={() => {}} onDeleted={() => {}} />,
    );
    // The trigger is the only button; Radix uses onPointerDown so userEvent is required
    await user.click(screen.getByRole("button"));
    await waitFor(() => screen.getByText("Rename"));
    await user.click(screen.getByText("Rename"));
    expect(screen.getByText("Rename meal")).toBeTruthy();
    expect(screen.getByDisplayValue("Pasta Bake")).toBeTruthy();
  });

  it("opens the delete dialog when 'Delete' is clicked", async () => {
    const user = userEvent.setup();
    renderWithProviders(
      <MealCard meal={meal} selected={false} onSelect={() => {}} onDeleted={() => {}} />,
    );
    await user.click(screen.getByRole("button"));
    await waitFor(() => screen.getByText("Delete"));
    await user.click(screen.getByText("Delete"));
    expect(screen.getByText("Delete meal")).toBeTruthy();
    expect(screen.getByText(/cannot be undone/)).toBeTruthy();
  });

  it("calls updateMeal and closes dialog on rename save", async () => {
    const { updateMeal } = await import("@/api/meals");
    const user = userEvent.setup();
    renderWithProviders(
      <MealCard meal={meal} selected={false} onSelect={() => {}} onDeleted={() => {}} />,
    );
    await user.click(screen.getByRole("button"));
    await waitFor(() => screen.getByText("Rename"));
    await user.click(screen.getByText("Rename"));

    const input = screen.getByDisplayValue("Pasta Bake");
    await user.clear(input);
    await user.type(input, "New Name");
    await user.click(screen.getByRole("button", { name: "Save" }));

    await waitFor(() => expect(updateMeal).toHaveBeenCalledWith(1, "New Name"));
  });

  it("calls deleteMeal and onDeleted when delete is confirmed", async () => {
    const { deleteMeal } = await import("@/api/meals");
    const onDeleted = mock(() => {});
    const user = userEvent.setup();
    renderWithProviders(
      <MealCard meal={meal} selected={false} onSelect={() => {}} onDeleted={onDeleted} />,
    );
    await user.click(screen.getByRole("button"));
    await waitFor(() => screen.getByText("Delete"));
    await user.click(screen.getByText("Delete"));
    await user.click(screen.getAllByRole("button", { name: "Delete" })[0]);

    await waitFor(() => {
      expect(deleteMeal).toHaveBeenCalledWith(1);
      expect(onDeleted).toHaveBeenCalledTimes(1);
    });
  });
});
