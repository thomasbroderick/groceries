import { useState } from "react";
import { Routes, Route, NavLink } from "react-router-dom";
import { ShoppingCart, UtensilsCrossed, Settings, LogOut, Menu } from "lucide-react";
import { cn } from "@/lib/utils";
import { Sheet, SheetContent, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
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
  onClick,
}: {
  to: string;
  icon: React.ComponentType<{ className?: string }>;
  label: string;
  onClick?: () => void;
}) {
  return (
    <NavLink
      to={to}
      end={to === "/"}
      onClick={onClick}
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
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const closeMenu = () => setMobileMenuOpen(false);

  return (
    <div className="min-h-screen bg-background">
      <header className="sticky top-0 z-40 border-b bg-background/95 backdrop-blur supports-[backdrop-filter]:bg-background/60">
        <div className="container mx-auto flex h-14 items-center px-4 gap-2">
          <span className="font-semibold mr-2 md:mr-4">5 Minute Groceries</span>

          {/* Desktop nav */}
          <nav className="hidden md:flex items-center gap-1">
            <NavItem to="/" icon={UtensilsCrossed} label="Meals" />
            <NavItem to="/shop" icon={ShoppingCart} label="Shop" />
            <NavItem to="/settings" icon={Settings} label="Settings" />
          </nav>

          {/* Desktop user info */}
          {user && (
            <div className="hidden md:flex ml-auto items-center gap-3">
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

          {/* Mobile hamburger */}
          <Sheet open={mobileMenuOpen} onOpenChange={setMobileMenuOpen}>
            <SheetTrigger asChild>
              <button
                className="md:hidden ml-auto p-2 rounded-md text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                aria-label="Open menu"
              >
                <Menu className="h-5 w-5" />
              </button>
            </SheetTrigger>
            <SheetContent side="right" className="w-64 p-0" aria-describedby={undefined}>
              <SheetTitle className="sr-only">Navigation menu</SheetTitle>
              <div className="flex flex-col h-full">
                <div className="px-6 py-4 border-b">
                  <span className="font-semibold">5 Minute Groceries</span>
                </div>
                <nav className="flex flex-col gap-1 p-4 flex-1">
                  <NavItem to="/" icon={UtensilsCrossed} label="Meals" onClick={closeMenu} />
                  <NavItem to="/shop" icon={ShoppingCart} label="Shop" onClick={closeMenu} />
                  <NavItem to="/settings" icon={Settings} label="Settings" onClick={closeMenu} />
                </nav>
                {user && (
                  <div className="px-4 py-4 border-t">
                    <div className="text-sm text-muted-foreground mb-2">{user.username}</div>
                    <button
                      onClick={() => { logout(); closeMenu(); }}
                      className="flex items-center gap-2 w-full px-3 py-2 rounded-md text-sm text-muted-foreground hover:text-foreground hover:bg-accent transition-colors"
                    >
                      <LogOut className="h-4 w-4" />
                      Sign out
                    </button>
                  </div>
                )}
              </div>
            </SheetContent>
          </Sheet>
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
