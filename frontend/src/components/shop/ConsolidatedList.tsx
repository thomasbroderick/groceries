import { useState } from "react";
import { ShoppingCart, Loader2, AlertTriangle, Link, X, RotateCcw } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Alert, AlertDescription } from "@/components/ui/alert";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { ProductSearchPopover } from "@/components/meals/ProductSearchPopover";
import { type ConsolidatedIngredientDto, type KrogerProductDto } from "@/api/client";
import { addToCart } from "@/api/kroger";
import { linkProduct } from "@/api/meals";

interface Props {
  items: ConsolidatedIngredientDto[];
  mealIds: number[];
  hasKrogerAuth: boolean;
  onLinked: () => void;
}

export function ConsolidatedList({ items, mealIds, hasKrogerAuth, onLinked }: Props) {
  const [adding, setAdding] = useState(false);
  const [result, setResult] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [excludedNames, setExcludedNames] = useState<Set<string>>(new Set());

  const exclude = (name: string) => setExcludedNames((prev) => new Set([...prev, name]));
  const restore = (name: string) => setExcludedNames((prev) => { const next = new Set(prev); next.delete(name); return next; });

  const activeItems: ConsolidatedIngredientDto[] = [];
  const excludedItems: ConsolidatedIngredientDto[] = [];
  let linkedCount = 0;
  let unlinkedCount = 0;
  for (const i of items) {
    if (excludedNames.has(i.name)) {
      excludedItems.push(i);
    } else {
      activeItems.push(i);
      if (i.krogerProductId) linkedCount++;
      else unlinkedCount++;
    }
  }

  const handleLink = async (item: ConsolidatedIngredientDto, product: KrogerProductDto) => {
    if (!product.upc) return;
    await linkProduct(item.name, mealIds, product.upc, product.description);
    onLinked();
  };

  const handleAddToCart = async () => {
    const cartItems = activeItems
      .filter((i) => i.krogerProductId)
      .map((i) => ({ upc: i.krogerProductId!, quantity: 1 }));

    setAdding(true);
    setResult(null);
    setError(null);
    try {
      const res = await addToCart(cartItems);
      setResult(res.message);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to add to cart");
    } finally {
      setAdding(false);
    }
  };

  if (items.length === 0) {
    return (
      <div className="flex items-center justify-center h-full text-muted-foreground text-sm py-16">
        Select meals on the left to see consolidated ingredients.
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full gap-4">
      {/* Mobile: card list */}
      <div className="md:hidden space-y-2">
        {activeItems.map((item) => (
          <div key={item.name} className="border rounded-lg p-3 space-y-2">
            <div className="flex items-center justify-between gap-2">
              <span className="font-medium text-sm">{item.name}</span>
              <div className="flex items-center gap-1 shrink-0">
                {item.consolidatedQuantity && item.consolidatedQuantity !== "1" && (
                  <Badge variant="secondary" className="text-xs">
                    {item.consolidatedQuantity}
                  </Badge>
                )}
                <Button
                  size="icon"
                  variant="ghost"
                  className="h-6 w-6 text-muted-foreground hover:text-destructive"
                  title="Remove from cart"
                  onClick={() => exclude(item.name)}
                >
                  <X className="h-3 w-3" />
                </Button>
              </div>
            </div>
            <div>
              {item.krogerProductName ? (
                <div className="flex items-center gap-2">
                  <span className="text-xs text-muted-foreground flex-1">{item.krogerProductName}</span>
                  <ProductSearchPopover
                    initialQuery={item.name}
                    onSelect={(product) => handleLink(item, product)}
                  >
                    <Button size="icon" variant="ghost" className="h-7 w-7 shrink-0" title="Change product">
                      <Link className="h-3 w-3" />
                    </Button>
                  </ProductSearchPopover>
                </div>
              ) : (
                <ProductSearchPopover
                  initialQuery={item.name}
                  onSelect={(product) => handleLink(item, product)}
                >
                  <Button size="sm" variant="outline" className="h-7 text-xs gap-1.5 w-full">
                    <Link className="h-3 w-3" />
                    Link Kroger product
                  </Button>
                </ProductSearchPopover>
              )}
            </div>
          </div>
        ))}

        {excludedItems.length > 0 && (
          <div className="border rounded-lg p-3 space-y-1.5 opacity-50">
            <p className="text-xs font-medium text-muted-foreground uppercase tracking-wide">
              Excluded from cart ({excludedItems.length})
            </p>
            {excludedItems.map((item) => (
              <div key={item.name} className="flex items-center justify-between gap-2">
                <span className="text-sm line-through text-muted-foreground">{item.name}</span>
                <Button
                  size="icon"
                  variant="ghost"
                  className="h-6 w-6 shrink-0"
                  title="Restore"
                  onClick={() => restore(item.name)}
                >
                  <RotateCcw className="h-3 w-3" />
                </Button>
              </div>
            ))}
          </div>
        )}
      </div>

      {/* Desktop: table */}
      <div className="hidden md:block">
        <Table>
          <TableHeader>
            <TableRow>
              <TableHead>Ingredient</TableHead>
              <TableHead>Qty</TableHead>
              <TableHead>Kroger Product</TableHead>
              <TableHead className="w-8" />
            </TableRow>
          </TableHeader>
          <TableBody>
            {activeItems.map((item) => (
              <TableRow key={item.name} className="group">
                <TableCell className="font-medium">{item.name}</TableCell>
                <TableCell>
                  {item.consolidatedQuantity ? (
                    <Badge variant="secondary" className="text-xs">
                      {item.consolidatedQuantity}
                    </Badge>
                  ) : (
                    <span className="text-muted-foreground text-xs">—</span>
                  )}
                </TableCell>
                <TableCell>
                  {item.krogerProductName ? (
                    <div className="flex items-center gap-2 group">
                      <span className="text-sm">{item.krogerProductName}</span>
                      <ProductSearchPopover
                        initialQuery={item.name}
                        onSelect={(product) => handleLink(item, product)}
                      >
                        <Button
                          size="icon"
                          variant="ghost"
                          className="h-6 w-6 opacity-0 group-hover:opacity-100 transition-opacity"
                          title="Change product"
                        >
                          <Link className="h-3 w-3" />
                        </Button>
                      </ProductSearchPopover>
                    </div>
                  ) : (
                    <ProductSearchPopover
                      initialQuery={item.name}
                      onSelect={(product) => handleLink(item, product)}
                    >
                      <Button size="sm" variant="outline" className="h-7 text-xs gap-1.5">
                        <Link className="h-3 w-3" />
                        Link product
                      </Button>
                    </ProductSearchPopover>
                  )}
                </TableCell>
                <TableCell>
                  <Button
                    size="icon"
                    variant="ghost"
                    className="h-6 w-6 text-muted-foreground hover:text-destructive opacity-0 group-hover:opacity-100 transition-opacity"
                    title="Remove from cart"
                    onClick={() => exclude(item.name)}
                  >
                    <X className="h-3 w-3" />
                  </Button>
                </TableCell>
              </TableRow>
            ))}
            {excludedItems.length > 0 && (
              <>
                <TableRow>
                  <TableCell colSpan={4} className="py-1">
                    <p className="text-xs text-muted-foreground uppercase tracking-wide">
                      Excluded from cart
                    </p>
                  </TableCell>
                </TableRow>
                {excludedItems.map((item) => (
                  <TableRow key={item.name} className="opacity-40">
                    <TableCell className="font-medium line-through text-muted-foreground" colSpan={3}>
                      {item.name}
                    </TableCell>
                    <TableCell>
                      <Button
                        size="icon"
                        variant="ghost"
                        className="h-6 w-6"
                        title="Restore"
                        onClick={() => restore(item.name)}
                      >
                        <RotateCcw className="h-3 w-3" />
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
              </>
            )}
          </TableBody>
        </Table>
      </div>

      <div className="mt-auto space-y-3">
        {!hasKrogerAuth && (
          <Alert variant="destructive">
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>
              Kroger account not connected. Go to Settings to connect.
            </AlertDescription>
          </Alert>
        )}
        {unlinkedCount > 0 && (
          <Alert>
            <AlertTriangle className="h-4 w-4" />
            <AlertDescription>
              {unlinkedCount} ingredient{unlinkedCount !== 1 ? "s" : ""} without a linked product
              will not be added to cart.
            </AlertDescription>
          </Alert>
        )}
        {result && <p className="text-sm text-green-600 font-medium">{result}</p>}
        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button
          className="w-full"
          onClick={handleAddToCart}
          disabled={adding || !hasKrogerAuth || linkedCount === 0}
        >
          {adding ? (
            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
          ) : (
            <ShoppingCart className="h-4 w-4 mr-2" />
          )}
          Add {linkedCount} item{linkedCount !== 1 ? "s" : ""} to Kroger Cart
        </Button>
      </div>
    </div>
  );
}
