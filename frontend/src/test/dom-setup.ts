// This file must be listed FIRST in bunfig.toml preload so DOM globals are
// available before @testing-library/* initializes (ESM imports are hoisted,
// so any file that imports Testing Library will initialize it before the
// module body runs — the DOM must exist at that point).
import { GlobalWindow } from "happy-dom";

const win = new GlobalWindow({ url: "http://localhost/" });

Object.assign(globalThis, {
  window: win,
  document: win.document,
  navigator: win.navigator,
  location: win.location,
  history: win.history,
  screen: win.screen,
  localStorage: win.localStorage,
  sessionStorage: win.sessionStorage,
  Element: win.Element,
  Node: win.Node,
  Event: win.Event,
  CustomEvent: win.CustomEvent,
  EventTarget: win.EventTarget,
  MutationObserver: win.MutationObserver,
  ResizeObserver: win.ResizeObserver,
  IntersectionObserver: win.IntersectionObserver,
  HTMLElement: win.HTMLElement,
  HTMLInputElement: win.HTMLInputElement,
  HTMLTextAreaElement: win.HTMLTextAreaElement,
  HTMLSelectElement: win.HTMLSelectElement,
  HTMLButtonElement: win.HTMLButtonElement,
  HTMLAnchorElement: win.HTMLAnchorElement,
  HTMLLabelElement: win.HTMLLabelElement,
  HTMLFormElement: win.HTMLFormElement,
  HTMLDivElement: win.HTMLDivElement,
  HTMLSpanElement: win.HTMLSpanElement,
  SVGElement: win.SVGElement,
  PointerEvent: win.PointerEvent,
  MouseEvent: win.MouseEvent,
  KeyboardEvent: win.KeyboardEvent,
  FocusEvent: win.FocusEvent,
  InputEvent: win.InputEvent,
  NodeFilter: win.NodeFilter,
  TreeWalker: win.TreeWalker,
  NodeIterator: win.NodeIterator,
  Range: win.Range,
  Selection: win.Selection,
  DOMParser: win.DOMParser,
  XMLSerializer: win.XMLSerializer,
  getComputedStyle: win.getComputedStyle.bind(win),
  requestAnimationFrame: (cb: FrameRequestCallback) => setTimeout(cb, 0),
  cancelAnimationFrame: clearTimeout,
  matchMedia: () => ({
    matches: false,
    media: "",
    onchange: null,
    addListener: () => {},
    removeListener: () => {},
    addEventListener: () => {},
    removeEventListener: () => {},
    dispatchEvent: () => false,
  }),
});

// Vite sets this at build time; Bun's test runner does not.
(import.meta.env as Record<string, string>).BASE_URL = "/";
