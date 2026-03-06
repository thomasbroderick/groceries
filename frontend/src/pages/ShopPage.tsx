import { useState } from "react";
import { useQuery, useMutation } from "@tanstack/react-query";
import { Skeleton } from "@/components/ui/skeleton";
import { MealSelector } from "@/components/shop/MealSelector";
import { ConsolidatedList } from "@/components/shop/ConsolidatedList";
import { getAllMeals, consolidateMeals } from "@/api/meals";
import { getKrogerConfig } from "@/api/config";

export default function ShopPage() {
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());

  const { data: meals, isLoading: mealsLoading } = useQuery({
    queryKey: ["meals"],
    queryFn: getAllMeals,
  });

  const { data: config } = useQuery({
    queryKey: ["kroger-config"],
    queryFn: getKrogerConfig,
  });

  const {
    mutate: consolidate,
    reset: resetConsolidation,
    data: consolidated = [],
    isPending: consolidating,
  } = useMutation({
    mutationFn: (ids: number[]) => consolidateMeals(ids),
  });

  const toggleMeal = (id: number) => {
    const next = new Set(selectedIds);
    if (next.has(id)) next.delete(id);
    else next.add(id);
    setSelectedIds(next);
    if (next.size === 0) resetConsolidation();
    else consolidate([...next]);
  };

  return (
    <div className="space-y-4">
      <h1 className="text-2xl font-bold">Shop</h1>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 md:min-h-[60vh]">
        {/* Left: meal selector */}
        <div>
          <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-3">
            Select Meals
          </h2>
          {mealsLoading ? (
            <div className="space-y-2">
              {[1, 2, 3].map((i) => (
                <Skeleton key={i} className="h-14 rounded-lg" />
              ))}
            </div>
          ) : (
            <MealSelector
              meals={meals ?? []}
              selectedIds={selectedIds}
              onToggle={toggleMeal}
            />
          )}
        </div>

        {/* Right: consolidated list */}
        <div>
          <h2 className="text-sm font-semibold text-muted-foreground uppercase tracking-wide mb-3">
            Consolidated Ingredients
            {consolidating && (
              <span className="ml-2 text-xs normal-case font-normal">(updating...)</span>
            )}
          </h2>
          <ConsolidatedList
            items={consolidated}
            mealIds={[...selectedIds]}
            hasKrogerAuth={config?.hasToken ?? false}
            onLinked={() => {
              if (selectedIds.size > 0) consolidate([...selectedIds]);
            }}
          />
        </div>
      </div>
    </div>
  );
}
