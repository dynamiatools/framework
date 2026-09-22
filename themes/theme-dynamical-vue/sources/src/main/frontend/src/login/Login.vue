<template>
  <AuthSplit>
    <template #brand>
      <div class="flex flex-col items-center gap-3 text-center">
        <div class="flex h-16 w-16 items-center justify-center rounded-full bg-brand-500 text-2xl font-semibold text-white">D</div>
        <p class="text-2xl font-semibold text-white">Dynamia</p>
      </div>
    </template>

    <h1 class="mb-6 text-title-sm font-semibold text-gray-800 dark:text-white/90">Sign in</h1>

    <p v-if="loggedOut" class="mb-4 rounded-lg bg-gray-100 px-3 py-2 text-sm text-gray-500 dark:bg-white/5 dark:text-gray-400">
      You have been signed out.
    </p>
    <p v-if="errorMessage" class="mb-4 rounded-lg bg-error-50 px-3 py-2 text-sm text-error-600 dark:bg-error-500/15 dark:text-error-500">
      {{ errorMessage }}
    </p>

    <form class="flex flex-col gap-5" @submit.prevent="submit">
      <label class="flex flex-col gap-1.5 text-sm font-medium text-gray-700 dark:text-gray-400">
        Username
        <input v-model="username" type="text" autocomplete="username" required :class="inputClass" />
      </label>

      <label class="flex flex-col gap-1.5 text-sm font-medium text-gray-700 dark:text-gray-400">
        Password
        <input v-model="password" type="password" autocomplete="current-password" required :class="inputClass" />
      </label>

      <!-- Button's own click handling is a prop, not a native event; submit goes through the form. -->
      <button
        type="submit"
        :disabled="submitting"
        class="inline-flex items-center justify-center rounded-lg bg-brand-500 px-5 py-3.5 text-sm font-medium text-white shadow-theme-xs transition hover:bg-brand-600 disabled:cursor-not-allowed disabled:opacity-60"
      >
        {{ submitting ? 'Signing in…' : 'Sign in' }}
      </button>
    </form>
  </AuthSplit>
</template>

<script setup lang="ts">
import { ref } from 'vue';
import AuthSplit from '@dynamia-tools/tailadmin-vue/components/ext/layouts/AuthSplit.vue';
import { rememberUsername } from '../lib/currentUser.js';

interface LoginJsonResponse {
  status: 'ok' | 'error';
  message?: string;
  redirectUrl?: string;
}

// Same input treatment the package uses across its form components.
const inputClass =
  'h-11 w-full rounded-lg border border-gray-300 bg-transparent px-4 py-2.5 text-sm text-gray-800 shadow-theme-xs placeholder:text-gray-400 focus:border-brand-300 focus:outline-hidden focus:ring-3 focus:ring-brand-500/10 dark:border-gray-700 dark:bg-gray-900 dark:text-white/90 dark:placeholder:text-white/30 dark:focus:border-brand-800';

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
      rememberUsername(username.value);
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
