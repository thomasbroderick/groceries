// DOM globals must be set before this file is loaded (see dom-setup.ts).
import "@testing-library/jest-dom";
import { afterEach } from "bun:test";
import { cleanup } from "@testing-library/react";

// Bun doesn't auto-register @testing-library/react cleanup.
afterEach(cleanup);
