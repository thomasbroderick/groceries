import { useState } from "react";
import { Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Button } from "@/components/ui/button";
import { saveKrogerConfig } from "@/api/config";
import { type KrogerConfigDto } from "@/api/client";

interface Props {
  config: KrogerConfigDto | undefined;
  onSaved: () => void;
}

export function CredentialsCard({ config, onSaved }: Props) {
  const [clientId, setClientId] = useState(config?.clientId ?? "");
  // clientSecret is intentionally not returned by the backend
  const [clientSecret, setClientSecret] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
    if (!clientId.trim() || !clientSecret.trim()) return;
    setSaving(true);
    setError(null);
    try {
      await saveKrogerConfig({ clientId: clientId.trim(), clientSecret: clientSecret.trim() });
      onSaved();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to save credentials");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Card>
      <CardHeader>
        <CardTitle>Kroger API Credentials</CardTitle>
        <CardDescription>
          Enter your Kroger Developer API client ID and secret.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="space-y-2">
          <Label htmlFor="clientId">Client ID</Label>
          <Input
            id="clientId"
            value={clientId}
            onChange={(e) => setClientId(e.target.value)}
            placeholder="your-client-id"
          />
        </div>
        <div className="space-y-2">
          <Label htmlFor="clientSecret">Client Secret</Label>
          <Input
            id="clientSecret"
            type="password"
            value={clientSecret}
            onChange={(e) => setClientSecret(e.target.value)}
            placeholder="your-client-secret"
          />
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button
          onClick={handleSave}
          disabled={saving || !clientId.trim() || !clientSecret.trim()}
        >
          {saving && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
          Save credentials
        </Button>
      </CardContent>
    </Card>
  );
}
