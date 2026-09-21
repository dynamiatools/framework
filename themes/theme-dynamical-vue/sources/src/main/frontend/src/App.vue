<template>
  <div class="flex h-full">
    <AppSidebar
      :nodes="sidebarNodes"
      :current-path="currentPath"
      :app-name="appInfo?.name ?? 'Dynamia'"
      :logo="appInfo?.logo"
      :mobile-open="mobileSidebarOpen"
      @navigate="navigateTo"
      @close="mobileSidebarOpen = false"
    />

    <div class="flex min-w-0 flex-1 flex-col">
      <AppTopbar
        :module="currentModule"
        :group="currentGroup"
        :page="activeNode"
        :skin="skin"
        @toggle-sidebar="mobileSidebarOpen = !mobileSidebarOpen"
        @change-skin="setSkin"
        @logout="logout"
      />

      <main class="flex-1 overflow-auto bg-slate-50 p-6">
        <div v-if="loading" class="flex h-full items-center justify-center text-sm text-slate-400">
          Loading navigation…
        </div>

        <div v-else-if="error" class="flex h-full flex-col items-center justify-center gap-3 text-sm">
          <p class="text-red-600">Could not load navigation from backend.</p>
          <p class="text-slate-400">{{ error }}</p>
          <button type="button" class="rounded-md bg-slate-800 px-3 py-1.5 text-white" @click="reload()">
            Retry
          </button>
        </div>

        <ContentArea v-else-if="activeNode" :node="activeNode" :client="client" />

        <div v-else class="flex h-full items-center justify-center text-sm text-slate-400">
          Select a page from the navigation menu.
        </div>
      </main>

      <AppFooter :app-info="appInfo" />
    </div>

    <!-- Mounted once here: every useConfirm()/useToast()/useInput()/useFormDialog() call anywhere in
         the app shares these — also what renders a FlowRemoteAction's CONFIRM/NOTIFY/INPUT/DIALOG
         steps, see runActionFlow in @dynamia-tools/vue. -->
    <DynamiaConfirmHost />
    <DynamiaToastHost />
    <DynamiaPromptHost />
    <DynamiaFormDialogHost />
    <!-- App-specific CUSTOM flow step renderer, registered under "star-rating" in main.ts. -->
    <StarRatingHost />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue';
import { useNavigation } from '@dynamia-tools/vue';
import type { ApplicationMetadata } from '@dynamia-tools/sdk';
import { client } from './lib/client.js';
import { useSkin } from './lib/useSkin.js';
import { withResolvedIcons } from './lib/withResolvedIcons.js';
import AppSidebar from './layout/AppSidebar.vue';
import AppTopbar from './layout/AppTopbar.vue';
import AppFooter from './layout/AppFooter.vue';
import ContentArea from './layout/ContentArea.vue';
import StarRatingHost from './components/StarRatingHost.vue';

const { nodes, currentPath, currentModule, currentGroup, currentPage, loading, error, navigateTo, reload } =
  useNavigation(client, { autoSelectFirst: true });

const activeNode = computed(() => currentPage.value);
const sidebarNodes = computed(() => withResolvedIcons(nodes.value));

const { skin, setSkin } = useSkin();
const mobileSidebarOpen = ref(false);

const appInfo = ref<ApplicationMetadata | null>(null);
onMounted(async () => {
  try {
    appInfo.value = await client.metadata.getApp();
  } catch {
    // Branding/footer just falls back to defaults — not fatal to the rest of the shell.
  }
});

async function logout(): Promise<void> {
  await fetch('/logout', { method: 'POST', credentials: 'include' });
  window.location.href = '/login';
}
</script>
