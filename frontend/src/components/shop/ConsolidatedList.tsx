import { useState } from "react";
import { ShoppingCart, Loader2, AlertTriangle, Link } from "lucide-react";
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
import { addMealsToCart } from "@/api/kroger";
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

  const linkedCount = items.filter((i) => i.krogerProductId).length;
  const unlinkedCount = items.length - linkedCount;

  const handleLink = async (item: ConsolidatedIngredientDto, product: KrogerProductDto) => {
    if (!product.upc) return;
    await linkProduct(item.name, mealIds, product.upc, product.description);
    onLinked();
  };

  const handleAddToCart = async () => {
    setAdding(true);
    setResult(null);
    setError(null);
    try {
      const res = await addMealsToCart(mealIds);
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
      <Table>
        <TableHeader>
          <TableRow>
            <TableHead>Ingredient</TableHead>
            <TableHead>Qty</TableHead>
            <TableHead>Kroger Product</TableHead>
          </TableRow>
        </TableHeader>
        <TableBody>
          {items.map((item) => (
            <TableRow key={item.name}>
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
            </TableRow>
          ))}
        </TableBody>
      </Table>

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
