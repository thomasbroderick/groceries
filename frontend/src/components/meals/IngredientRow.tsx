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
  const [editingQty, setEditingQty] = useState(false);
  const [qtyDraft, setQtyDraft] = useState(ingredient.quantity ?? "");

  const invalidate = () => queryClient.invalidateQueries({ queryKey: ["meal", mealId] });

  const handleSaveEdit = async () => {
    await updateIngredient(mealId, ingredient.id, {
      name: editName,
      quantity: editQty || null,
    });
    setEditing(false);
    invalidate();
  };

  const handleSaveQty = async () => {
    await updateIngredient(mealId, ingredient.id, {
      quantity: qtyDraft.trim() || null,
    });
    setEditingQty(false);
    invalidate();
  };

  const handleCancelQty = () => {
    setQtyDraft(ingredient.quantity ?? "");
    setEditingQty(false);
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

  const qtyArea = editingQty ? (
    <div className="flex items-center gap-1 shrink-0">
      <Input
        value={qtyDraft}
        onChange={(e) => setQtyDraft(e.target.value)}
        onKeyDown={(e) => { if (e.key === "Enter") handleSaveQty(); if (e.key === "Escape") handleCancelQty(); }}
        placeholder="qty"
        className="h-6 w-16 text-xs px-1.5"
        autoFocus
      />
      <Button size="icon" variant="ghost" className="h-6 w-6" onClick={handleSaveQty}>
        <Check className="h-3 w-3" />
      </Button>
      <Button size="icon" variant="ghost" className="h-6 w-6" onClick={handleCancelQty}>
        <X className="h-3 w-3" />
      </Button>
    </div>
  ) : ingredient.quantity ? (
    <Badge
      variant="secondary"
      className="text-xs shrink-0 cursor-pointer hover:bg-secondary/60"
      onClick={() => { setQtyDraft(ingredient.quantity ?? ""); setEditingQty(true); }}
      title="Click to edit quantity"
    >
      {ingredient.quantity}
    </Badge>
  ) : (
    <Badge
      variant="outline"
      className="text-xs shrink-0 cursor-pointer text-muted-foreground hover:text-foreground hover:border-foreground/40"
      onClick={() => { setQtyDraft(""); setEditingQty(true); }}
      title="Add quantity"
    >
      + qty
    </Badge>
  );

  return (
    <div className="py-1.5 group">
      {/* Main row: qty + name + action buttons */}
      <div className="flex items-center gap-2">
        {qtyArea}
        <span className="flex-1 text-sm min-w-0">{ingredient.name}</span>

        {ingredient.krogerProductName ? (
          <>
            {/* Desktop: product name inline + hover-reveal buttons */}
            <span className="hidden md:inline text-xs text-muted-foreground truncate max-w-[140px]">
              {ingredient.krogerProductName}
            </span>
            <div className="hidden md:flex items-center gap-1 opacity-0 group-hover:opacity-100 transition-opacity">
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
            {/* Mobile: always-visible buttons */}
            <div className="flex md:hidden items-center gap-0.5">
              <Button size="icon" variant="ghost" className="h-8 w-8" onClick={() => setEditing(true)} title="Edit">
                <Pencil className="h-3.5 w-3.5" />
              </Button>
              <ProductSearchPopover initialQuery={ingredient.name} onSelect={handleLinkProduct}>
                <Button size="icon" variant="ghost" className="h-8 w-8" title="Change product">
                  <Link className="h-3.5 w-3.5" />
                </Button>
              </ProductSearchPopover>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-destructive hover:text-destructive" onClick={handleDelete} title="Delete">
                <Trash2 className="h-3.5 w-3.5" />
              </Button>
            </div>
          </>
        ) : (
          <>
            {/* Desktop: link button + hover-reveal edit/delete */}
            <div className="hidden md:flex items-center gap-1">
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
            </div>
            {/* Mobile: edit + delete always visible */}
            <div className="flex md:hidden items-center gap-0.5">
              <Button size="icon" variant="ghost" className="h-8 w-8" onClick={() => setEditing(true)} title="Edit">
                <Pencil className="h-3.5 w-3.5" />
              </Button>
              <Button size="icon" variant="ghost" className="h-8 w-8 text-destructive hover:text-destructive" onClick={handleDelete} title="Delete">
                <Trash2 className="h-3.5 w-3.5" />
              </Button>
            </div>
          </>
        )}
      </div>

      {/* Mobile second line: product name or Link product button */}
      {ingredient.krogerProductName ? (
        <div className="md:hidden ml-10 mt-0.5">
          <span className="text-xs text-muted-foreground">{ingredient.krogerProductName}</span>
        </div>
      ) : (
        <div className="md:hidden ml-10 mt-1">
          <ProductSearchPopover initialQuery={ingredient.name} onSelect={handleLinkProduct}>
            <Button size="sm" variant="outline" className="h-7 text-xs gap-1.5">
              <Link className="h-3 w-3" />
              Link product
            </Button>
          </ProductSearchPopover>
        </div>
      )}
    </div>
  );
}
