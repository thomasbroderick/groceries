import { useState } from "react";
import { useMutation } from "@tanstack/react-query";
import { Link } from "react-router-dom";
import { Sparkles, Loader2, CheckCircle2, AlertCircle } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { generateMealPlan, type FitnessGoalRequest } from "@/api/client";

export default function MealPlanPage() {
  const [form, setForm] = useState<FitnessGoalRequest>({
    mealsPerDay: 3,
    numberOfDays: 7,
  });

  const mutation = useMutation({
    mutationFn: generateMealPlan,
  });

  function setField<K extends keyof FitnessGoalRequest>(key: K, value: FitnessGoalRequest[K]) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  function parseList(value: string): string[] {
    return value
      .split(",")
      .map((s) => s.trim())
      .filter(Boolean);
  }

  function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    mutation.mutate(form);
  }

  return (
    <div className="space-y-6 max-w-2xl">
      <h1 className="text-2xl font-bold">AI Meal Planner</h1>

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Sparkles className="h-5 w-5" />
            Generate Meal Plan
          </CardTitle>
          <CardDescription>
            Enter your fitness goals and the AI will create personalized meal recipes
            directly in your meals library. Generation takes 30–60 seconds.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form onSubmit={handleSubmit} className="space-y-5">
            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1">
                <Label htmlFor="mealsPerDay">Meals per day</Label>
                <Input
                  id="mealsPerDay"
                  type="number"
                  min={1}
                  max={6}
                  value={form.mealsPerDay ?? ""}
                  onChange={(e) => setField("mealsPerDay", e.target.valueAsNumber || undefined)}
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="numberOfDays">Number of days</Label>
                <Input
                  id="numberOfDays"
                  type="number"
                  min={1}
                  max={14}
                  value={form.numberOfDays ?? ""}
                  onChange={(e) => setField("numberOfDays", e.target.valueAsNumber || undefined)}
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="dailyCalories">Daily calories (kcal)</Label>
                <Input
                  id="dailyCalories"
                  type="number"
                  min={0}
                  placeholder="e.g. 2000"
                  value={form.dailyCalories ?? ""}
                  onChange={(e) => setField("dailyCalories", e.target.valueAsNumber || undefined)}
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div className="space-y-1">
                <Label htmlFor="protein">Protein (g/day)</Label>
                <Input
                  id="protein"
                  type="number"
                  min={0}
                  placeholder="e.g. 150"
                  value={form.proteinGrams ?? ""}
                  onChange={(e) => setField("proteinGrams", e.target.valueAsNumber || undefined)}
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="carbs">Carbs (g/day)</Label>
                <Input
                  id="carbs"
                  type="number"
                  min={0}
                  placeholder="e.g. 200"
                  value={form.carbsGrams ?? ""}
                  onChange={(e) => setField("carbsGrams", e.target.valueAsNumber || undefined)}
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="fat">Fat (g/day)</Label>
                <Input
                  id="fat"
                  type="number"
                  min={0}
                  placeholder="e.g. 65"
                  value={form.fatGrams ?? ""}
                  onChange={(e) => setField("fatGrams", e.target.valueAsNumber || undefined)}
                />
              </div>
            </div>

            <div className="space-y-1">
              <Label htmlFor="dietaryRestrictions">Dietary restrictions</Label>
              <Input
                id="dietaryRestrictions"
                placeholder="e.g. vegetarian, gluten-free, dairy-free"
                defaultValue={(form.dietaryRestrictions ?? []).join(", ")}
                onBlur={(e) => setField("dietaryRestrictions", parseList(e.target.value))}
              />
              <p className="text-xs text-muted-foreground">Comma-separated</p>
            </div>

            <div className="space-y-1">
              <Label htmlFor="cuisinePreferences">Cuisine preferences</Label>
              <Input
                id="cuisinePreferences"
                placeholder="e.g. Mediterranean, Asian, Mexican"
                defaultValue={(form.cuisinePreferences ?? []).join(", ")}
                onBlur={(e) => setField("cuisinePreferences", parseList(e.target.value))}
              />
              <p className="text-xs text-muted-foreground">Comma-separated</p>
            </div>

            <div className="space-y-1">
              <Label htmlFor="notes">Additional notes</Label>
              <Input
                id="notes"
                placeholder="e.g. prefer quick recipes under 30 min"
                value={form.notes ?? ""}
                onChange={(e) => setField("notes", e.target.value || undefined)}
              />
            </div>

            <Button type="submit" disabled={mutation.isPending} className="w-full">
              {mutation.isPending ? (
                <>
                  <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                  Generating meal plan… (this may take up to 60s)
                </>
              ) : (
                <>
                  <Sparkles className="mr-2 h-4 w-4" />
                  Generate Meal Plan
                </>
              )}
            </Button>
          </form>
        </CardContent>
      </Card>

      {mutation.isError && (
        <Alert variant="destructive">
          <AlertCircle className="h-4 w-4" />
          <AlertDescription>
            {mutation.error instanceof Error ? mutation.error.message : "Failed to generate meal plan."}
          </AlertDescription>
        </Alert>
      )}

      {mutation.isSuccess && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-green-700">
              <CheckCircle2 className="h-5 w-5" />
              Meal Plan Created
            </CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="whitespace-pre-wrap text-sm leading-relaxed">
              {mutation.data.summary}
            </div>
            <Link
              to="/"
              className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
            >
              View Meals →
            </Link>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
