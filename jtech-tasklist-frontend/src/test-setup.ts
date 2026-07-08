import { vi } from 'vitest'

globalThis.ResizeObserver = vi.fn().mockImplementation(function () {
  return {
    observe: vi.fn(),
    unobserve: vi.fn(),
    disconnect: vi.fn(),
  }
})

globalThis.visualViewport = {
  width: 1024,
  height: 768,
  addEventListener: vi.fn(),
  removeEventListener: vi.fn(),
} as unknown as VisualViewport
