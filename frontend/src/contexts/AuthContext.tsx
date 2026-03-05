import { createContext, useContext, useEffect, useState, ReactNode } from "react";
import { login as apiLogin, register as apiRegister, me, UserDto } from "@/api/auth";
import { setToken, clearToken } from "@/api/client";

interface AuthContextValue {
  user: UserDto | null;
  login(username: string, password: string): Promise<void>;
  register(username: string, password: string): Promise<void>;
  logout(): void;
  isLoading: boolean;
}

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<UserDto | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    const token = localStorage.getItem("auth_token");
    if (!token) {
      setIsLoading(false);
      return;
    }
    me()
      .then((u) => setUser(u))
      .catch(() => clearToken())
      .finally(() => setIsLoading(false));
  }, []);

  async function login(username: string, password: string) {
    const res = await apiLogin(username, password);
    setToken(res.token);
    setUser(res.user);
  }

  async function register(username: string, password: string) {
    const res = await apiRegister(username, password);
    setToken(res.token);
    setUser(res.user);
  }

  function logout() {
    clearToken();
    setUser(null);
  }

  return (
    <AuthContext.Provider value={{ user, login, register, logout, isLoading }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth(): AuthContextValue {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error("useAuth must be used within AuthProvider");
  return ctx;
}
