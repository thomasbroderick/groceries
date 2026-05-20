import { ReactNode } from "react";
import { Navigate } from "react-router-dom";
import { useAuth } from "@/contexts/AuthContext";

export function ProtectedRoute({
  children,
  requireAi = false,
}: {
  children: ReactNode;
  requireAi?: boolean;
}) {
  const { user, isLoading } = useAuth();

  if (isLoading) return null;
  if (!user) return <Navigate to="/login" replace />;
  if (requireAi && !user.canUseAi) return <Navigate to="/" replace />;

  return <>{children}</>;
}
