import { useState } from "react";
import { PlusCircle, Loader2, UtensilsCrossed, ChevronLeft } from "lucide-react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { cn } from "@/lib/utils";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { MealCard } from "@/components/meals/MealCard";
import { MealIngredientPanel } from "@/components/meals/MealIngredientPanel";
import { getAllMeals, createMeal } from "@/api/meals";

export default function MealsPage() {
  const queryClient = useQueryClient();
  const [selectedMealId, setSelectedMealId] = useState<number | null>(null);
  const [newMealOpen, setNewMealOpen] = useState(false);
  const [newMealName, setNewMealName] = useState("");
  const [creating, setCreating] = useState(false);

  const { data: meals, isLoading } = useQuery({
    queryKey: ["meals"],
    queryFn: getAllMeals,
  });

  const handleCreateMeal = async () => {
    if (!newMealName.trim()) return;
    setCreating(true);
    try {
      const created = await createMeal(newMealName.trim());
      queryClient.invalidateQueries({ queryKey: ["meals"] });
      setNewMealName("");
      setNewMealOpen(false);
      setSelectedMealId(created.id);
    } finally {
      setCreating(false);
    }
  };

  return (
    <div className="md:flex md:gap-6 md:h-[calc(100vh-8rem)]">
      {/* Left: meal list — hidden on mobile when a meal is selected */}
      <div className={cn(
        "flex flex-col gap-3 md:w-72 md:shrink-0",
        selectedMealId !== null ? "hidden md:flex" : "flex",
      )}>
        <div className="flex items-center justify-between">
          <h1 className="text-2xl font-bold">Meals</h1>
          <Button size="sm" onClick={() => setNewMealOpen(true)} className="bg-green-600 hover:bg-green-700 text-white">
            <PlusCircle className="h-4 w-4 mr-1" />
            New
          </Button>
        </div>
        <div className="md:flex-1 md:overflow-y-auto space-y-1.5 md:pr-1">
          {isLoading ? (
            <div className="space-y-2">
              {[1, 2, 3, 4].map((i) => <Skeleton key={i} className="h-14 rounded-lg" />)}
            </div>
          ) : meals?.length === 0 ? (
            <p className="text-sm text-muted-foreground text-center py-8">
              No meals yet. Click "New" to get started.
            </p>
          ) : (
            meals?.map((meal) => (
              <MealCard
                key={meal.id}
                meal={meal}
                selected={selectedMealId === meal.id}
                onSelect={() => setSelectedMealId(meal.id)}
                onDeleted={() => {
                  if (selectedMealId === meal.id) setSelectedMealId(null);
                }}
              />
            ))
          )}
        </div>
      </div>

      {/* Right: ingredient panel */}
      <div className={cn(
        "flex flex-col border rounded-lg overflow-hidden",
        "h-[calc(100svh-8rem)] md:h-auto md:flex-1",
        selectedMealId !== null ? "flex" : "hidden md:flex",
      )}>
        {selectedMealId !== null ? (
          <>
            {/* Back button: mobile only */}
            <button
              onClick={() => setSelectedMealId(null)}
              className="md:hidden shrink-0 flex items-center gap-1 px-4 py-3 border-b text-sm text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
            >
              <ChevronLeft className="h-4 w-4" />
              Back to meals
            </button>
            <div className="flex-1 overflow-hidden p-4 md:p-6 flex flex-col min-h-0">
              <MealIngredientPanel mealId={selectedMealId} />
            </div>
          </>
        ) : (
          <div className="flex flex-col items-center justify-center h-full text-muted-foreground gap-3 p-6">
            <UtensilsCrossed className="h-10 w-10 opacity-30" />
            <p className="text-sm">Select a meal to manage its ingredients</p>
          </div>
        )}
      </div>

      <Dialog open={newMealOpen} onOpenChange={setNewMealOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>New meal</DialogTitle>
            <DialogDescription>Enter a name for your new meal.</DialogDescription>
          </DialogHeader>
          <Input
            placeholder="Meal name"
            value={newMealName}
            onChange={(e) => setNewMealName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleCreateMeal()}
            autoFocus
          />
          <DialogFooter>
            <Button variant="outline" onClick={() => setNewMealOpen(false)}>Cancel</Button>
            <Button onClick={handleCreateMeal} disabled={creating || !newMealName.trim()}>
              {creating && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Create
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  );
}
