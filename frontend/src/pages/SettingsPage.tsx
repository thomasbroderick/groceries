import { useEffect } from "react";
import { useQuery, useQueryClient } from "@tanstack/react-query";
import { useSearchParams } from "react-router-dom";
import { CheckCircle2 } from "lucide-react";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Skeleton } from "@/components/ui/skeleton";
import { CredentialsCard } from "@/components/settings/CredentialsCard";
import { LocationCard } from "@/components/settings/LocationCard";
import { AuthCard } from "@/components/settings/AuthCard";
import { getKrogerConfig } from "@/api/config";

export default function SettingsPage() {
  const queryClient = useQueryClient();
  const [searchParams, setSearchParams] = useSearchParams();
  const authSuccess = searchParams.get("auth") === "success";

  const { data: config, isLoading } = useQuery({
    queryKey: ["kroger-config"],
    queryFn: getKrogerConfig,
  });

  // Clear the auth=success param after showing the banner
  useEffect(() => {
    if (authSuccess) {
      const timer = setTimeout(() => {
        setSearchParams({}, { replace: true });
      }, 5000);
      return () => clearTimeout(timer);
    }
  }, [authSuccess, setSearchParams]);

  const handleSaved = () => {
    queryClient.invalidateQueries({ queryKey: ["kroger-config"] });
  };

  return (
    <div className="space-y-6 max-w-2xl">
      <h1 className="text-2xl font-bold">Settings</h1>

      {authSuccess && (
        <Alert className="border-green-500 text-green-700 bg-green-50">
          <CheckCircle2 className="h-4 w-4 text-green-600" />
          <AlertDescription>Kroger account connected successfully!</AlertDescription>
        </Alert>
      )}

      {isLoading ? (
        <div className="space-y-4">
          <Skeleton className="h-36 rounded-lg" />
          <Skeleton className="h-48 rounded-lg" />
          <Skeleton className="h-48 rounded-lg" />
          <Skeleton className="h-36 rounded-lg" />
        </div>
      ) : (
        <div className="space-y-4">
          <CredentialsCard config={config} onSaved={handleSaved} />
          <LocationCard config={config} onSaved={handleSaved} />
          <AuthCard config={config} />
        </div>
      )}
    </div>
  );
}
