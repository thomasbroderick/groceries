import { useState } from "react";
import { Pencil, Trash2, Link, Check, X } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Badge } from "@/components/ui/badge";
import { ProductSearchPopover } from "./ProductSearchPopover";
import { type IngredientDto, type KrogerProductDto } from "@/api/client";
import { updateIngredient, deleteIngredient } from "@/api/meals";
import { useQueryClient } from "@tanstack/react-query";

interface Props {
  mealId: number;
  ingredient: IngredientDto;
}

export function IngredientRow({ mealId, ingredient }: Props) {
  const queryClient = useQueryClient();
  const [editing, setEditing] = useState(false);
  const [editName, setEditName] = useState(ingredient.name);
  const [editQty, setEditQty] = useState(ingredient.quantity ?? "");

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["meal", mealId] });

  const handleSaveEdit = async () => {
    await updateIngredient(mealId, ingredient.id, {
      name: editName,
      quantity: editQty || null,
    });
    setEditing(false);
    invalidate();
  };

  const handleDelete = async () => {
    await deleteIngredient(mealId, ingredient.id);
    invalidate();
  };

  const handleLinkProduct = async (product: KrogerProductDto) => {
    if (!product.upc) return;
    await updateIngredient(mealId, ingredient.id, {
      krogerProductId: product.upc,
      krogerProductName: product.description,
    });
    invalidate();
  };

  if (editing) {
    return (
      <div className="flex items-center gap-2 py-1">
        <Input
          value={editQty}
          onChange={(e) => setEditQty(e.target.value)}
          placeholder="qty"
          className="h-7 w-20 text-sm"
        />
        <Input
          value={editName}
          onChange={(e) => setEditName(e.target.value)}
          placeholder="name"
          className="h-7 flex-1 text-sm"
        />
        <Button size="icon" variant="ghost" className="h-7 w-7" onClick={handleSaveEdit}>
          <Check className="h-3 w-3" />
        </Button>
        <Button size="icon" variant="ghost" className="h-7 w-7" onClick={() => setEditing(false)}>
          <X className="h-3 w-3" />
        </Button>
      </div>
    );
  }

  return (
    <div className="flex items-center gap-2 py-1.5 group">
      {ingredient.quantity && (
        <Badge variant="secondary" className="text-xs shrink-0">
          {ingredient.quantity}
        </Badge>
      )}
      <span className="flex-1 text-sm">{ingredient.name}</span>

      {ingredient.krogerProductName ? (
        // Linked: show product name + hover actions
        <>
          <span className="text-xs text-muted-foreground truncate max-w-[140px]">
            {ingredient.krogerProductName}
          </span>
          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <Button size="icon" variant="ghost" className="h-6 w-6" onClick={() => setEditing(true)} title="Edit">
              <Pencil className="h-3 w-3" />
            </Button>
            <ProductSearchPopover initialQuery={ingredient.name} onSelect={handleLinkProduct}>
              <Button size="icon" variant="ghost" className="h-6 w-6" title="Change product">
                <Link className="h-3 w-3" />
              </Button>
            </ProductSearchPopover>
            <Button size="icon" variant="ghost" className="h-6 w-6 text-destructive hover:text-destructive" onClick={handleDelete} title="Delete">
              <Trash2 className="h-3 w-3" />
            </Button>
          </div>
        </>
      ) : (
        // Unlinked: always-visible "Link product" CTA + hover edit/delete
        <>
          <ProductSearchPopover initialQuery={ingredient.name} onSelect={handleLinkProduct}>
            <Button size="sm" variant="outline" className="h-7 text-xs gap-1.5 shrink-0">
              <Link className="h-3 w-3" />
              Link product
            </Button>
          </ProductSearchPopover>
          <div className="flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
            <Button size="icon" variant="ghost" className="h-6 w-6" onClick={() => setEditing(true)} title="Edit">
              <Pencil className="h-3 w-3" />
            </Button>
            <Button size="icon" variant="ghost" className="h-6 w-6 text-destructive hover:text-destructive" onClick={handleDelete} title="Delete">
              <Trash2 className="h-3 w-3" />
            </Button>
          </div>
        </>
      )}
    </div>
  );
}
