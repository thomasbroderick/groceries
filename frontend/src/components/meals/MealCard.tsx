import { useState } from "react";
import { MoreVertical, Pencil, Trash2, Loader2 } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuSeparator,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
  DialogDescription,
} from "@/components/ui/dialog";
import { Input } from "@/components/ui/input";
import { type MealSummaryDto } from "@/api/client";
import { updateMeal, deleteMeal } from "@/api/meals";
import { cn } from "@/lib/utils";

interface Props {
  meal: MealSummaryDto;
  selected: boolean;
  onSelect: () => void;
  onDeleted: () => void;
}

export function MealCard({ meal, selected, onSelect, onDeleted }: Props) {
  const queryClient = useQueryClient();
  const [renameOpen, setRenameOpen] = useState(false);
  const [deleteOpen, setDeleteOpen] = useState(false);
  const [newName, setNewName] = useState(meal.name);
  const [saving, setSaving] = useState(false);

  const handleRename = async () => {
    if (!newName.trim()) return;
    setSaving(true);
    try {
      await updateMeal(meal.id, newName.trim());
      queryClient.invalidateQueries({ queryKey: ["meals"] });
      queryClient.invalidateQueries({ queryKey: ["meal", meal.id] });
      setRenameOpen(false);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async () => {
    setSaving(true);
    try {
      await deleteMeal(meal.id);
      queryClient.invalidateQueries({ queryKey: ["meals"] });
      setDeleteOpen(false);
      onDeleted();
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div
        className={cn(
          "flex items-center justify-between px-3 py-2.5 rounded-lg border cursor-pointer transition-colors",
          selected
            ? "bg-primary text-primary-foreground border-primary"
            : "bg-card hover:bg-accent",
        )}
        onClick={onSelect}
      >
        <div className="min-w-0">
          <p className="font-medium truncate">{meal.name}</p>
          <p className={cn("text-xs", selected ? "text-primary-foreground/70" : "text-muted-foreground")}>
            {meal.ingredientCount} ingredient{meal.ingredientCount !== 1 ? "s" : ""}
          </p>
        </div>
        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              size="icon"
              variant="ghost"
              className={cn("h-7 w-7 shrink-0 ml-2", selected && "hover:bg-primary-foreground/20 text-primary-foreground")}
              onClick={(e) => e.stopPropagation()}
            >
              <MoreVertical className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>
          <DropdownMenuContent align="end" onClick={(e) => e.stopPropagation()}>
            <DropdownMenuItem onClick={() => { setNewName(meal.name); setRenameOpen(true); }}>
              <Pencil className="h-4 w-4 mr-2" />
              Rename
            </DropdownMenuItem>
            <DropdownMenuSeparator />
            <DropdownMenuItem
              className="text-destructive focus:text-destructive"
              onClick={() => setDeleteOpen(true)}
            >
              <Trash2 className="h-4 w-4 mr-2" />
              Delete
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </div>

      <Dialog open={renameOpen} onOpenChange={setRenameOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Rename meal</DialogTitle>
            <DialogDescription>Enter a new name for "{meal.name}".</DialogDescription>
          </DialogHeader>
          <Input
            value={newName}
            onChange={(e) => setNewName(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleRename()}
            autoFocus
          />
          <DialogFooter>
            <Button variant="outline" onClick={() => setRenameOpen(false)}>Cancel</Button>
            <Button onClick={handleRename} disabled={saving || !newName.trim()}>
              {saving && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Save
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      <Dialog open={deleteOpen} onOpenChange={setDeleteOpen}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Delete meal</DialogTitle>
            <DialogDescription>
              Are you sure you want to delete "{meal.name}"? This cannot be undone.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteOpen(false)}>Cancel</Button>
            <Button variant="destructive" onClick={handleDelete} disabled={saving}>
              {saving && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Delete
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
