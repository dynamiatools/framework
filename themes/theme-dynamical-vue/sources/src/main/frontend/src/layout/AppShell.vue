<template>
  <AdminLayout>
    <template #sidebar>
      <AppSidebar :menu-groups="menuGroups">
        <template #sidebar-header>
          <div class="flex items-center gap-3 pt-8 pb-7" :class="{ 'xl:justify-center': !expanded }">
            <img v-if="appInfo?.logo" :src="appInfo.logo" alt="" class="h-8 w-8 shrink-0 rounded" />
            <span v-if="expanded" class="truncate text-lg font-semibold text-gray-800 dark:text-white/90">
              {{ appInfo?.name ?? 'Dynamia' }}
            </span>
          </div>
        </template>
      </AppSidebar>
    </template>

    <template #header>
      <!-- Slots are filled with an (invisible) element, not left empty: Vue falls back to the package's
           demo content for a slot whose content renders to nothing. -->
      <AppHeader>
        <template #logo>
          <span class="truncate font-semibold text-gray-800 xl:hidden dark:text-white/90">{{ appInfo?.name ?? 'Dynamia' }}</span>
        </template>
        <template #search><span class="hidden" /></template>
        <template #notifications><span class="hidden" /></template>
        <template #actions>
          <select
            class="h-9 rounded-lg border border-gray-300 bg-transparent px-2 text-sm text-gray-700 focus:border-brand-300 focus:ring-3 focus:ring-brand-500/10 focus:outline-hidden dark:border-gray-700 dark:bg-gray-900 dark:text-gray-300"
            :value="skin"
            aria-label="Skin"
            @change="setSkin(($event.target as HTMLSelectElement).value)"
          >
            <option value="blue">Blue</option>
            <option value="dynamia">Dynamia</option>
            <option value="dark">Dark</option>
          </select>
        </template>
        <template #user-menu>
          <UserMenu :name="username" email="" :avatar-url="initialsAvatar(username)" :show-language-switcher="false" @sign-out="logout" />
        </template>
      </AppHeader>
    </template>

    <div v-if="loading" class="flex h-64 items-center justify-center text-sm text-gray-400">Loading navigation…</div>

    <div v-else-if="error" class="flex h-64 flex-col items-center justify-center gap-3 text-sm">
      <p class="text-error-600">Could not load navigation from backend.</p>
      <p class="text-gray-400">{{ error }}</p>
      <Button size="sm" :on-click="reload">Retry</Button>
    </div>

    <template v-else-if="activeNode">
      <PageBreadcrumb :page-title="activeNode.name" />
      <ContentArea :node="activeNode" :client="client" />
    </template>

    <div v-else class="flex h-64 items-center justify-center text-sm text-gray-400">
      Select a page from the navigation menu.
    </div>

    <AppFooter :app-info="appInfo" />

    <!-- Mounted once here: every useConfirm()/useToast()/useInput()/useFormDialog() call anywhere in
         the app shares these — also what renders a FlowRemoteAction's CONFIRM/NOTIFY/INPUT/DIALOG
         steps, see runActionFlow in @dynamia-tools/vue. -->
    <DynamiaConfirmHost />
    <DynamiaToastHost />
    <DynamiaPromptHost />
    <DynamiaFormDialogHost />
    <!-- App-specific CUSTOM flow step renderer, registered under "star-rating" in main.ts. -->
    <StarRatingHost />
  </AdminLayout>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useNavigation } from '@dynamia-tools/vue';
import { findFirstPage } from '@dynamia-tools/ui-core';
import type { ApplicationMetadata } from '@dynamia-tools/sdk';
import AdminLayout from '@dynamia-tools/tailadmin-vue/components/layout/AdminLayout.vue';
import AppSidebar from '@dynamia-tools/tailadmin-vue/components/layout/AppSidebar.vue';
import AppHeader from '@dynamia-tools/tailadmin-vue/components/layout/AppHeader.vue';
import UserMenu from '@dynamia-tools/tailadmin-vue/components/layout/header/UserMenu.vue';
import PageBreadcrumb from '@dynamia-tools/tailadmin-vue/components/common/PageBreadcrumb.vue';
import Button from '@dynamia-tools/tailadmin-vue/components/ui/Button.vue';
import { useTheme } from '@dynamia-tools/tailadmin-vue/components/layout/ThemeProvider.vue';
import { useSidebar } from '@dynamia-tools/tailadmin-vue/composables/useSidebar';
import { toInternalPath, toRoutePath } from '../router.js';
import { client } from '../lib/client.js';
import { currentUsername, initialsAvatar } from '../lib/currentUser.js';
import { toMenuGroups } from '../lib/navMenu.js';
import { withResolvedIcons } from '../lib/withResolvedIcons.js';
import { useSkin } from '../lib/useSkin.js';
import AppFooter from './AppFooter.vue';
import ContentArea from './ContentArea.vue';
import StarRatingHost from '../components/StarRatingHost.vue';

const route = useRoute();
const router = useRouter();

const { nodes, currentPage, loading, error, navigateTo, reload } = useNavigation(client);

// The router is the source of truth for "where am I" (so links, back/forward and deep links work);
// useNavigation only resolves that path against the tree.
watch(() => route.path, path => navigateTo(toInternalPath(path)), { immediate: true });

// Opened at the bare "/": land on the first page, like the theme always did.
watch(
  [nodes, () => route.path],
  ([tree, path]) => {
    if (path !== '/' || !tree.length) return;
    const first = findFirstPage(tree);
    if (first?.internalPath) void router.replace(toRoutePath(first.internalPath));
  },
  { immediate: true },
);

const activeNode = computed(() => currentPage.value);
const menuGroups = computed(() => toMenuGroups(withResolvedIcons(nodes.value)));

const { isExpanded, isHovered, isMobileOpen } = useSidebar();
const expanded = computed(() => isExpanded.value || isHovered.value || isMobileOpen.value);

// Skin <-> dark mode: the "dark" skin *is* the package's dark mode. Skin is the source of truth
// (it is what gets persisted and shared with the server), the header's theme toggle feeds back into it.
const { skin, setSkin } = useSkin();
const theme = useTheme() as { isDarkMode: { value: boolean }; toggleTheme: () => void };
let lastLightSkin = skin.value === 'dark' ? 'blue' : skin.value;

watch(skin, value => {
  if (value !== 'dark') lastLightSkin = value;
  if ((value === 'dark') !== theme.isDarkMode.value) theme.toggleTheme();
});
watch(
  () => theme.isDarkMode.value,
  dark => {
    if (dark !== (skin.value === 'dark')) setSkin(dark ? 'dark' : lastLightSkin);
  },
);

const username = currentUsername();

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
