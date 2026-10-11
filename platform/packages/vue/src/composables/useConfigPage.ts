// useConfigPage: composable that renders a NavigationNode of type "ConfigPage" as a form of application parameters

import { ref, shallowRef } from 'vue';
import type { Ref } from 'vue';
import type { NavigationNode, DynamiaClient } from '@dynamia-tools/sdk';
import { VueFormView } from '../views/VueFormView.js';
import { useToast } from './useToast.js';

/** Options for the {@link useConfigPage} composable */
export interface UseConfigPageOptions {
  /** A NavigationNode whose `type` is `"ConfigPage"` */
  node: NavigationNode;
  /** DynamiaClient instance used for all API calls */
  client: DynamiaClient;
}

/**
 * Composable that auto-generates the form of a `ConfigPage` {@link NavigationNode}.
 *
 * It reads the current values from `GET /api/{internalPath}`, loads the `config` view descriptor named in the response
 * and builds a {@link VueFormView} with them. {@link save} sends the values back with `PUT`; the server maps every field to
 * its application parameter, so only field names travel. A field with a component this frontend does not know is drawn
 * as a text input.
 *
 * Example:
 * <pre>{@code
 * const { view, loading, error, saving, save } = useConfigPage({ node, client });
 * }</pre>
 *
 * @param options - {@link UseConfigPageOptions}
 * @returns Reactive `view`, `loading`, `saving`, `error` refs and the `reload` / `save` functions.
 */
export function useConfigPage(options: UseConfigPageOptions) {
  let activeNode = options.node;
  const { client } = options;
  const toast = useToast();

  const loading: Ref<boolean> = ref(false);
  const saving: Ref<boolean> = ref(false);
  const error: Ref<string | null> = ref(null);
  // shallowRef for the same reason as in useCrudPage: ref() would unwrap the nested Refs of the view
  const view = shallowRef<VueFormView | null>(null);

  function api() {
    if (!activeNode.internalPath) throw new Error(`The ConfigPage ${activeNode.id} has no internalPath`);
    return client.config(activeNode.internalPath);
  }

  async function initialize(): Promise<void> {
    loading.value = true;
    error.value = null;
    view.value = null;
    try {
      const { descriptor: descriptorId, data } = await api().read();
      const descriptor = await client.metadata.getView(descriptorId);
      const form = new VueFormView(descriptor, null);
      await form.initialize();
      form.setValue(data);
      view.value = form;
    } catch (e) {
      error.value = e instanceof Error ? e.message : String(e);
    } finally {
      loading.value = false;
    }
  }

  async function save(): Promise<boolean> {
    const form = view.value;
    if (!form || !form.validate()) return false;
    saving.value = true;
    try {
      const { data } = await api().save({ ...form.values.value });
      form.setValue(data);
      form.isDirty.value = false;
      toast.success('Saved');
      return true;
    } catch (e) {
      toast.error(e instanceof Error ? e.message : String(e));
      return false;
    } finally {
      saving.value = false;
    }
  }

  /** Loads the page again for another node (the component stays mounted while the user navigates) or the same one. */
  async function reload(node: NavigationNode = activeNode): Promise<void> {
    activeNode = node;
    await initialize();
  }

  void initialize();

  return { view, loading, saving, error, reload, save };
}
