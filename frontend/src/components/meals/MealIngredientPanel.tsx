import { useState } from "react";
import { PlusCircle, Loader2 } from "lucide-react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { IngredientRow } from "./IngredientRow";
import { getMeal, addIngredient } from "@/api/meals";

interface Props {
  mealId: number;
}

export function MealIngredientPanel({ mealId }: Props) {
  const queryClient = useQueryClient();
  const [rawInput, setRawInput] = useState("");
  const [adding, setAdding] = useState(false);

  const { data: meal, isLoading } = useQuery({
    queryKey: ["meal", mealId],
    queryFn: () => getMeal(mealId),
  });

  const handleAddIngredient = async () => {
    if (!rawInput.trim()) return;
    setAdding(true);
    try {
      await addIngredient(mealId, rawInput.trim());
      setRawInput("");
      queryClient.invalidateQueries({ queryKey: ["meal", mealId] });
      queryClient.invalidateQueries({ queryKey: ["meals"] });
    } finally {
      setAdding(false);
    }
  };

  return (
    <div className="flex flex-col h-full">
      <div className="mb-4">
        <h2 className="text-xl font-semibold">{meal?.name ?? ""}</h2>
        <p className="text-sm text-muted-foreground">
          {meal?.ingredients.length ?? 0} ingredient{meal?.ingredients.length !== 1 ? "s" : ""}
        </p>
      </div>
      <Separator className="mb-3" />
      <div className="flex-1 overflow-y-auto space-y-0.5 pr-1">
        {isLoading ? (
          <div className="space-y-2">
            {[1, 2, 3].map((i) => <Skeleton key={i} className="h-8 w-full" />)}
          </div>
        ) : meal?.ingredients.length === 0 ? (
          <p className="text-sm text-muted-foreground text-center py-12">
            No ingredients yet. Add one below.
          </p>
        ) : (
          meal?.ingredients.map((ing) => (
            <IngredientRow key={ing.id} mealId={mealId} ingredient={ing} />
          ))
        )}
      </div>
      <Separator className="my-3" />
      <div className="flex gap-2">
        <Input
          placeholder='Add ingredient, e.g. "2 onions"'
          value={rawInput}
          onChange={(e) => setRawInput(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleAddIngredient()}
        />
        <Button onClick={handleAddIngredient} disabled={adding || !rawInput.trim()}>
          {adding
            ? <Loader2 className="h-4 w-4 animate-spin" />
            : <PlusCircle className="h-4 w-4" />}
        </Button>
      </div>
    </div>
  );
}
