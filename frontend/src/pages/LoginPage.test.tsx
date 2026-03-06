import { mock } from "bun:test";

const mockLogin = mock((_u: string, _p: string) => Promise.resolve());
const mockRegister = mock((_u: string, _p: string) => Promise.resolve());

mock.module("@/contexts/AuthContext", () => ({
  useAuth: () => ({
    user: null,
    login: mockLogin,
    register: mockRegister,
    logout: () => {},
    isLoading: false,
  }),
  AuthProvider: ({ children }: { children: unknown }) => children,
}));

import { describe, it, expect, beforeEach } from "bun:test";
import { render, screen, fireEvent, waitFor } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import LoginPage from "./LoginPage";

function renderLogin() {
  return render(
    <MemoryRouter>
      <LoginPage />
    </MemoryRouter>,
  );
}

describe("LoginPage", () => {
  beforeEach(() => {
    mockLogin.mockReset();
    mockRegister.mockReset();
    mockLogin.mockImplementation(() => Promise.resolve());
    mockRegister.mockImplementation(() => Promise.resolve());
  });

  it("renders the app title", () => {
    renderLogin();
    expect(screen.getByText("5 Minute Groceries")).toBeTruthy();
  });

  it("shows 'Sign in to your account' subtitle by default", () => {
    renderLogin();
    expect(screen.getByText("Sign in to your account")).toBeTruthy();
  });

  it("renders email and password fields", () => {
    renderLogin();
    expect(screen.getByLabelText("Email")).toBeTruthy();
    expect(screen.getByLabelText("Password")).toBeTruthy();
  });

  it("renders a 'Sign In' submit button by default", () => {
    renderLogin();
    expect(screen.getByRole("button", { name: "Sign In" })).toBeTruthy();
  });

  it("switches to register mode when 'Register' link is clicked", () => {
    renderLogin();
    fireEvent.click(screen.getByRole("button", { name: "Register" }));
    expect(screen.getByText("Create a new account")).toBeTruthy();
    expect(screen.getByRole("button", { name: "Register" })).toBeTruthy();
  });

  it("switches back to sign-in mode from register mode", () => {
    renderLogin();
    fireEvent.click(screen.getByRole("button", { name: "Register" }));
    fireEvent.click(screen.getByRole("button", { name: "Sign In" }));
    expect(screen.getByText("Sign in to your account")).toBeTruthy();
  });

  it("calls login with entered credentials on submit", async () => {
    renderLogin();
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "test@example.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "secret" } });
    fireEvent.click(screen.getByRole("button", { name: "Sign In" }));
    await waitFor(() => expect(mockLogin).toHaveBeenCalledWith("test@example.com", "secret"));
  });

  it("calls register with entered credentials when in register mode", async () => {
    renderLogin();
    fireEvent.click(screen.getByRole("button", { name: "Register" }));
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "new@example.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "pass" } });
    fireEvent.click(screen.getByRole("button", { name: "Register" }));
    await waitFor(() => expect(mockRegister).toHaveBeenCalledWith("new@example.com", "pass"));
  });

  it("displays a plain error message string on ApiError", async () => {
    const { ApiError } = await import("@/api/client");
    mockLogin.mockImplementation(() => Promise.reject(new ApiError(401, "Invalid credentials")));
    renderLogin();
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "x@x.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "bad" } });
    fireEvent.click(screen.getByRole("button", { name: "Sign In" }));
    await waitFor(() => expect(screen.getByText("Invalid credentials")).toBeTruthy());
  });

  it("displays the 'error' field from a JSON ApiError body", async () => {
    const { ApiError } = await import("@/api/client");
    mockLogin.mockImplementation(() =>
      Promise.reject(new ApiError(401, JSON.stringify({ error: "Account not found" }))),
    );
    renderLogin();
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "x@x.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "bad" } });
    fireEvent.click(screen.getByRole("button", { name: "Sign In" }));
    await waitFor(() => expect(screen.getByText("Account not found")).toBeTruthy());
  });

  it("shows fallback error for non-ApiError exceptions", async () => {
    mockLogin.mockImplementation(() => Promise.reject(new Error("Network failure")));
    renderLogin();
    fireEvent.change(screen.getByLabelText("Email"), { target: { value: "x@x.com" } });
    fireEvent.change(screen.getByLabelText("Password"), { target: { value: "bad" } });
    fireEvent.click(screen.getByRole("button", { name: "Sign In" }));
    await waitFor(() => expect(screen.getByText("An unexpected error occurred")).toBeTruthy());
  });
});
