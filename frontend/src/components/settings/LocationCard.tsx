import { useState } from "react";
import { Search, Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { RadioGroup, RadioGroupItem } from "@/components/ui/radio-group";
import { Badge } from "@/components/ui/badge";
import { searchLocations } from "@/api/kroger";
import { updateLocation } from "@/api/config";
import { type KrogerConfigDto, type KrogerLocationDto } from "@/api/client";

interface Props {
  config: KrogerConfigDto | undefined;
  onSaved: () => void;
}

export function LocationCard({ config, onSaved }: Props) {
  const [zip, setZip] = useState("");
  const [searching, setSearching] = useState(false);
  const [locations, setLocations] = useState<KrogerLocationDto[]>([]);
  const [selectedId, setSelectedId] = useState(config?.locationId ?? "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSearch = async () => {
    if (!zip.trim()) return;
    setSearching(true);
    setError(null);
    try {
      const results = await searchLocations(zip.trim());
      setLocations(results);
    } catch (e) {
      setError(e instanceof Error ? e.message : "Search failed");
    } finally {
      setSearching(false);
    }
  };

  const handleSave = async () => {
    if (!selectedId) return;
    setSaving(true);
    setError(null);
    try {
      const selectedLoc = locations.find((l) => l.locationId === selectedId);
      await updateLocation(selectedId, selectedLoc?.name ?? null);
      onSaved();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to save location");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Store Location</CardTitle>
        <CardDescription>Find and select your nearest Kroger store.</CardDescription>
        {config?.locationName && (
          <div className="flex items-center gap-2 mt-1 text-sm">
            <span className="text-muted-foreground">Current:</span>
            <Badge variant="outline">{config.locationName}</Badge>
          </div>
        )}
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex gap-2">
          <Input
            placeholder="ZIP code"
            value={zip}
            onChange={(e) => setZip(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSearch()}
            className="max-w-[160px]"
          />
          <Button variant="outline" onClick={handleSearch} disabled={searching}>
            {searching ? (
              <Loader2 className="h-4 w-4 animate-spin" />
            ) : (
              <Search className="h-4 w-4" />
            )}
            Search
          </Button>
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}
        {locations.length > 0 && (
          <>
            <RadioGroup value={selectedId} onValueChange={setSelectedId} className="space-y-1">
              {locations.map((loc) => (
                <div key={loc.locationId} className="flex items-center space-x-2">
                  <RadioGroupItem value={loc.locationId} id={`loc-${loc.locationId}`} />
                  <Label htmlFor={`loc-${loc.locationId}`} className="cursor-pointer font-normal">
                    <span className="font-medium">{loc.name}</span>
                    <span className="text-muted-foreground ml-2 text-sm">{loc.address}</span>
                  </Label>
                </div>
              ))}
            </RadioGroup>
            <Button onClick={handleSave} disabled={saving || !selectedId}>
              {saving && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Save location
            </Button>
          </>
        )}
      </CardContent>
    </Card>
  );
}
