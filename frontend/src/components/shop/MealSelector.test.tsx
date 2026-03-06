import { describe, it, expect, mock } from "bun:test";
import { render, screen, fireEvent } from "@testing-library/react";
import { MealSelector } from "./MealSelector";
import type { MealSummaryDto } from "@/api/client";

const meals: MealSummaryDto[] = [
  { id: 1, name: "Pasta", ingredientCount: 5 },
  { id: 2, name: "Tacos", ingredientCount: 3 },
];

describe("MealSelector", () => {
  it("renders each meal name", () => {
    render(<MealSelector meals={meals} selectedIds={new Set()} onToggle={() => {}} />);
    expect(screen.getByText("Pasta")).toBeTruthy();
    expect(screen.getByText("Tacos")).toBeTruthy();
  });

  it("renders ingredient count badges", () => {
    render(<MealSelector meals={meals} selectedIds={new Set()} onToggle={() => {}} />);
    expect(screen.getByText("5")).toBeTruthy();
    expect(screen.getByText("3")).toBeTruthy();
  });

  it("shows empty state when no meals", () => {
    render(<MealSelector meals={[]} selectedIds={new Set()} onToggle={() => {}} />);
    expect(screen.getByText(/No meals yet/)).toBeTruthy();
  });

  it("calls onToggle with the meal id when a row is clicked", () => {
    const onToggle = mock(() => {});
    render(<MealSelector meals={meals} selectedIds={new Set()} onToggle={onToggle} />);
    fireEvent.click(screen.getByText("Pasta"));
    expect(onToggle).toHaveBeenCalledWith(1);
  });

  it("marks the correct checkbox as checked", () => {
    render(<MealSelector meals={meals} selectedIds={new Set([2])} onToggle={() => {}} />);
    const checkboxes = screen.getAllByRole("checkbox");
    expect(checkboxes[0]).not.toBeChecked();
    expect(checkboxes[1]).toBeChecked();
  });
});
