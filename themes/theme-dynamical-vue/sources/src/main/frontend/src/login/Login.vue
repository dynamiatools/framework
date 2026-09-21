<template>
  <div class="flex h-full items-center justify-center bg-slate-100 px-4">
    <div class="w-full max-w-sm rounded-lg bg-white p-8 shadow-sm">
      <div class="mb-6 flex flex-col items-center gap-2 text-center">
        <div
          class="flex h-12 w-12 items-center justify-center rounded-full text-lg font-semibold"
          :style="{ backgroundColor: 'var(--skin-primary)', color: 'var(--skin-primary-fg)' }"
        >
          D
        </div>
        <h1 class="text-lg font-semibold text-slate-900">Sign in</h1>
      </div>

      <p v-if="loggedOut" class="mb-4 rounded-md bg-slate-50 px-3 py-2 text-sm text-slate-500">
        You have been signed out.
      </p>
      <p v-if="errorMessage" class="mb-4 rounded-md bg-red-50 px-3 py-2 text-sm text-red-600">
        {{ errorMessage }}
      </p>

      <form class="flex flex-col gap-4" @submit.prevent="submit">
        <label class="flex flex-col gap-1 text-sm text-slate-600">
          Username
          <input
            v-model="username"
            type="text"
            autocomplete="username"
            required
            class="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </label>

        <label class="flex flex-col gap-1 text-sm text-slate-600">
          Password
          <input
            v-model="password"
            type="password"
            autocomplete="current-password"
            required
            class="rounded-md border border-slate-300 px-3 py-2 text-sm focus:border-slate-500 focus:outline-none"
          />
        </label>

        <button
          type="submit"
          :disabled="submitting"
          class="mt-2 rounded-md px-3 py-2 text-sm font-medium disabled:opacity-60"
          :style="{ backgroundColor: 'var(--skin-primary)', color: 'var(--skin-primary-fg)' }"
        >
          {{ submitting ? 'Signing in…' : 'Sign in' }}
        </button>
      </form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue';

interface LoginJsonResponse {
  status: 'ok' | 'error';
  message?: string;
  redirectUrl?: string;
}

const username = ref('');
const password = ref('');
const submitting = ref(false);
const errorMessage = ref<string | null>(null);
const loggedOut = new URLSearchParams(window.location.search).has('logout');

async function submit(): Promise<void> {
  submitting.value = true;
  errorMessage.value = null;

  try {
    const response = await fetch('/login/json', {
      method: 'POST',
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: username.value, password: password.value }),
    });

    const data = (await response.json()) as LoginJsonResponse;

    if (response.ok && data.status === 'ok') {
      window.location.href = data.redirectUrl || '/';
      return;
    }

    errorMessage.value = data.message || 'Invalid username or password';
  } catch (e) {
    errorMessage.value = 'Could not reach the server. Please try again.';
  } finally {
    submitting.value = false;
  }
}
</script>
