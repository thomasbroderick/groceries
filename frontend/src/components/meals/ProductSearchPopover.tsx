import { useState, useEffect, useRef } from "react";
import { Search, Loader2 } from "lucide-react";
import { useQuery } from "@tanstack/react-query";
import { Popover, PopoverContent, PopoverTrigger } from "@/components/ui/popover";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { type KrogerProductDto } from "@/api/client";
import { searchProducts } from "@/api/kroger";
import { getKrogerConfig } from "@/api/config";

interface Props {
  initialQuery?: string;
  onSelect: (product: KrogerProductDto) => void;
  children: React.ReactNode;
}

export function ProductSearchPopover({ initialQuery = "", onSelect, children }: Props) {
  const [open, setOpen] = useState(false);
  const [query, setQuery] = useState(initialQuery);
  const [results, setResults] = useState<KrogerProductDto[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const hasAutoSearched = useRef(false);

  const { data: config } = useQuery({
    queryKey: ["kroger-config"],
    queryFn: getKrogerConfig,
  });

  const runSearch = async (term: string, locationId?: string | null) => {
    if (!term.trim()) return;
    setLoading(true);
    setError(null);
    try {
      const products = await searchProducts(term.trim(), locationId ?? undefined);
      setResults(products);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Search failed");
    } finally {
      setLoading(false);
    }
  };

  // Auto-search once when the popover opens, but wait until config is loaded
  // so the locationId is available for the search.
  useEffect(() => {
    if (!open) {
      hasAutoSearched.current = false;
      setResults([]);
      setError(null);
      setQuery(initialQuery);
      return;
    }
    if (initialQuery && config !== undefined && !hasAutoSearched.current) {
      hasAutoSearched.current = true;
      setQuery(initialQuery);
      runSearch(initialQuery, config.locationId);
    }
  }, [open, config]); // eslint-disable-line react-hooks/exhaustive-deps

  const handleSelect = (product: KrogerProductDto) => {
    onSelect(product);
    setOpen(false);
  };

  return (
    <Popover open={open} onOpenChange={setOpen}>
      <PopoverTrigger asChild>{children}</PopoverTrigger>
      <PopoverContent className="w-80 max-w-[calc(100vw-2rem)] p-3" align="start">
        <div className="space-y-2">
          <div className="flex gap-2">
            <Input
              placeholder="Search Kroger products..."
              value={query}
              onChange={(e) => setQuery(e.target.value)}
              onKeyDown={(e) => e.key === "Enter" && runSearch(query, config?.locationId)}
              className="h-8 text-sm"
              autoFocus
            />
            <Button size="sm" variant="outline" onClick={() => runSearch(query, config?.locationId)} disabled={loading}>
              {loading ? <Loader2 className="h-3 w-3 animate-spin" /> : <Search className="h-3 w-3" />}
            </Button>
          </div>
          {error && <p className="text-xs text-destructive">{error}</p>}
          {results.length > 0 && (
            <>
              {!config?.locationId && (
                <p className="text-xs text-muted-foreground">
                  Set a store location in Settings to see prices and enable linking.
                </p>
              )}
              <ul className="max-h-52 overflow-y-auto space-y-1">
                {results.map((p) => (
                  <li key={p.productId}>
                    <button
                      className="w-full text-left px-2 py-1.5 text-sm rounded hover:bg-accent transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center gap-2"
                      onClick={() => handleSelect(p)}
                      disabled={!p.upc}
                      title={!p.upc ? "No UPC — set a store location in Settings" : undefined}
                    >
                      {p.imageUrl && (
                        <img
                          src={p.imageUrl}
                          alt=""
                          className="w-10 h-10 object-contain shrink-0 rounded"
                        />
                      )}
                      <span className="flex-1 min-w-0">
                        <span className="block font-medium leading-tight">{p.description}</span>
                        {p.price && (
                          <span className="text-muted-foreground text-xs">${p.price.toFixed(2)}</span>
                        )}
                      </span>
                    </button>
                  </li>
                ))}
              </ul>
            </>
          )}
          {results.length === 0 && !loading && query && (
            <p className="text-xs text-muted-foreground text-center py-2">No results</p>
          )}
        </div>
      </PopoverContent>
    </Popover>
  );
}
