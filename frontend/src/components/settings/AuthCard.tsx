import { useState } from "react";
import { ExternalLink, Loader2 } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { getAuthUrl } from "@/api/kroger";
import { type KrogerConfigDto } from "@/api/client";

interface Props {
  config: KrogerConfigDto | undefined;
}

export function AuthCard({ config }: Props) {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const handleConnect = async () => {
    setLoading(true);
    setError(null);
    try {
      const { authorizationUrl } = await getAuthUrl();
      window.location.href = authorizationUrl;
    } catch (e) {
      setError(e instanceof Error ? e.message : "Failed to get auth URL");
      setLoading(false);
    }
  };

  const isConnected = config?.hasToken ?? false;

  return (
    <Card>
      <CardHeader>
        <CardTitle>Kroger Account</CardTitle>
        <CardDescription>
          Connect your Kroger account to enable adding items to your cart.
        </CardDescription>
      </CardHeader>
      <CardContent className="space-y-4">
        <div className="flex items-center gap-3">
          <span className="text-sm font-medium">Status:</span>
          {isConnected ? (
            <Badge variant="success">Connected</Badge>
          ) : (
            <Badge variant="secondary">Not connected</Badge>
          )}
        </div>
        {error && <p className="text-sm text-destructive">{error}</p>}
        <Button onClick={handleConnect} disabled={loading} variant={isConnected ? "outline" : "default"}>
          {loading ? (
            <Loader2 className="h-4 w-4 mr-2 animate-spin" />
          ) : (
            <ExternalLink className="h-4 w-4 mr-2" />
          )}
          {isConnected ? "Reconnect with Kroger" : "Connect with Kroger"}
        </Button>
      </CardContent>
    </Card>
  );
}
