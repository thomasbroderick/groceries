import { useState } from "react";
import { CheckCircle2, Loader2 } from "lucide-react";
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
  const isConfigured = !!config?.clientId;
  const [editing, setEditing] = useState(!isConfigured);
  const [clientId, setClientId] = useState(config?.clientId ?? "");
  // clientSecret is intentionally not returned by the backend
  const [clientSecret, setClientSecret] = useState("");
  const [saving, setSaving] = useState(false);
  const [saved, setSaved] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleSave = async () => {
    if (!clientId.trim() || !clientSecret.trim()) return;
    setSaving(true);
    setError(null);
    try {
      await saveKrogerConfig({ clientId: clientId.trim(), clientSecret: clientSecret.trim() });
      setSaved(true);
      setEditing(false);
      setClientSecret("");
      setTimeout(() => setSaved(false), 3000);
      onSaved();
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to save credentials");
    } finally {
      setSaving(false);
    }
  };

  const handleCancel = () => {
    setClientId(config?.clientId ?? "");
    setClientSecret("");
    setError(null);
    setEditing(false);
  };

  return (
    <Card>
      <CardHeader>
        <div className="flex items-center justify-between">
          <div>
            <CardTitle>Kroger API Credentials</CardTitle>
            {!editing && (
              <CardDescription className="mt-1">
                {isConfigured ? "Credentials are configured." : "Enter your Kroger Developer API client ID and secret."}
              </CardDescription>
            )}
            {editing && (
              <CardDescription className="mt-1">
                Enter your Kroger Developer API client ID and secret.
              </CardDescription>
            )}
          </div>
          {!editing && (
            <div className="flex items-center gap-2">
              {saved && <CheckCircle2 className="h-4 w-4 text-green-600" />}
              <Button variant="outline" size="sm" onClick={() => setEditing(true)}>
                Edit
              </Button>
            </div>
          )}
        </div>
      </CardHeader>
      {editing && (
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
          <div className="flex gap-2">
            <Button
              onClick={handleSave}
              disabled={saving || !clientId.trim() || !clientSecret.trim()}
            >
              {saving && <Loader2 className="h-4 w-4 mr-2 animate-spin" />}
              Save credentials
            </Button>
            {isConfigured && (
              <Button variant="outline" onClick={handleCancel} disabled={saving}>
                Cancel
              </Button>
            )}
          </div>
        </CardContent>
      )}
    </Card>
  );
}
