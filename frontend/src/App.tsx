import { Routes, Route, NavLink } from "react-router-dom";
import { ShoppingCart, UtensilsCrossed, Settings, LogOut } from "lucide-react";
import { cn } from "@/lib/utils";
import MealsPage from "@/pages/MealsPage";
import ShopPage from "@/pages/ShopPage";
import SettingsPage from "@/pages/SettingsPage";
import LoginPage from "@/pages/LoginPage";
import { ProtectedRoute } from "@/components/ProtectedRoute";
import { AuthProvider, useAuth } from "@/contexts/AuthContext";

function NavItem({
  to,
  icon: Icon,
  label,
}: {
  to: string;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
}) {
  return (
    <NavLink
      to={to}
      end={to === "/"}
      className={({ isActive }) =>
        cn(
          "flex items-center gap-2 px-4 py-2 rounded-md text-sm font-medium transition-colors",
          isActive
            ? "bg-primary text-primary-foreground"
            : "text-muted-foreground hover:text-foreground hover:bg-accent",
        )
      }
    >
      <Icon className="h-4 w-4" />
      {label}
    </NavLink>
  );
}

function AppLayout() {
  const { user, logout } = useAuth();

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-40 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-14 items-center px-4 gap-2">
          <span className="font-semibold mr-4">Groceries</span>
          <nav className="flex items-center gap-1">
            <NavItem to="/" icon={UtensilsCrossed} label="Meals" />
            <NavItem to="/shop" icon={ShoppingCart} label="Shop" />
            <NavItem to="/settings" icon={Settings} label="Settings" />
          </nav>
          {user && (
            <div className="ml-auto flex items-center gap-3">
              <span className="text-sm text-muted-foreground">{user.username}</span>
              <button
                onClick={logout}
                className="flex items-center gap-1 px-3 py-1.5 rounded-md text-sm text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                title="Sign out"
              >
                <LogOut className="h-4 w-4" />
                Sign out
              </button>
            </div>
          )}
        </div>
      </header>
      <main className="container mx-auto px-4 py-6">
        <Routes>
          <Route path="/" element={<ProtectedRoute><MealsPage /></ProtectedRoute>} />
          <Route path="/shop" element={<ProtectedRoute><ShopPage /></ProtectedRoute>} />
          <Route path="/settings" element={<ProtectedRoute><SettingsPage /></ProtectedRoute>} />
          <Route path="/login" element={<LoginPage />} />
        </Routes>
      </main>
    </div>
  );
}

export default function App() {
  return (
    <AuthProvider>
      <AppLayout />
    </AuthProvider>
  );
}
