// starRatingManager.ts — app-specific manager backing the "star-rating" CUSTOM flow step.
//
// This is deliberately NOT part of @dynamia-tools/ui-core or @dynamia-tools/vue: CUSTOM is the
// escape hatch for step UIs with no core equivalent (see docs/design/SERVER_DRIVEN_ACTION_FLOWS.md §3),
// so its renderer belongs to whichever app registers it — here, theme-dynamical-vue itself, wired up in
// main.ts via `registerFlowStepRenderer('star-rating', ...)`. Mirrors the single-flight queue shape of
// ui-core's PromptManager/ConfirmManager, scoped down since only one call site (main.ts) ever asks.

import { reactive } from 'vue';

export interface StarRatingRequest {
  id: string;
  max: number;
}

interface PendingRating {
  request: StarRatingRequest;
  resolve: (value: number | null) => void;
}

const queue: PendingRating[] = [];

/** Reactive view of the current request, consumed by StarRatingHost.vue. */
export const starRatingState = reactive<{ current: StarRatingRequest | null }>({ current: null });

/** Queue a star-rating request; resolves with the picked value, or null if cancelled. */
export function askStarRating(data: unknown): Promise<number | null> {
  const max = typeof (data as { max?: unknown })?.max === 'number' ? (data as { max: number }).max : 5;
  return new Promise<number | null>(resolve => {
    const request: StarRatingRequest = { id: crypto.randomUUID(), max };
    const wasEmpty = queue.length === 0;
    queue.push({ request, resolve });
    if (wasEmpty) starRatingState.current = request;
  });
}

/** Answer the current request (by id, defensively) and advance to the next queued one, if any. */
export function answerStarRating(id: string, value: number | null): void {
  const pending = queue[0];
  if (!pending || pending.request.id !== id) return;
  queue.shift();
  pending.resolve(value);
  starRatingState.current = queue[0]?.request ?? null;
}
