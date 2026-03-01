import { Checkbox } from "@/components/ui/checkbox";
import { Label } from "@/components/ui/label";
import { Badge } from "@/components/ui/badge";
import { type MealSummaryDto } from "@/api/client";

interface Props {
  meals: MealSummaryDto[];
  selectedIds: Set<number>;
  onToggle: (id: number) => void;
}

export function MealSelector({ meals, selectedIds, onToggle }: Props) {
  return (
    <div className="space-y-2">
      {meals.map((meal) => (
        <div
          key={meal.id}
          className="flex items-center gap-3 p-3 rounded-lg border bg-card hover:bg-accent/50 cursor-pointer transition-colors"
          onClick={() => onToggle(meal.id)}
        >
          <Checkbox
            id={`meal-${meal.id}`}
            checked={selectedIds.has(meal.id)}
            onCheckedChange={() => onToggle(meal.id)}
            onClick={(e) => e.stopPropagation()}
          />
          <Label htmlFor={`meal-${meal.id}`} className="flex-1 cursor-pointer font-medium" onClick={(e) => e.stopPropagation()}>
            {meal.name}
          </Label>
          <Badge variant="outline" className="text-xs">
            {meal.ingredientCount}
          </Badge>
        </div>
      ))}
      {meals.length === 0 && (
        <p className="text-sm text-muted-foreground text-center py-8">
          No meals yet. Go to Meals to create some.
        </p>
      )}
    </div>
  );
}
