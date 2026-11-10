import { createApp, h, type App } from 'vue';
import { EmailTemplatesClient, type EmailTemplatesApi, type TemplateData } from './api.js';
import EmailTemplateEditor from './ui/EmailTemplateEditor.vue';

export interface MountOptions {
  api: EmailTemplatesApi;
  templateId: number;
  locale?: string;
  /** The model source of the template, used to list its variables. */
  source?: string;
  onSaved?: (template: TemplateData) => void;
}

export interface MountedEditor {
  unmount(): void;
  /** Saves like the Save button; resolves true when it was saved and false when it failed or there is no editor. */
  save(): Promise<boolean>;
}

/** Mounts the template editor inside `target` (light DOM, so the page CSS and the editor CSS apply). */
export function mountEmailEditor(target: Element, options: MountOptions): MountedEditor {
  let instance: { save(): Promise<boolean> } | null = null;
  const app: App = createApp({
    render: () =>
      h(EmailTemplateEditor, {
        api: options.api,
        templateId: options.templateId,
        ...(options.locale ? { locale: options.locale } : {}),
        ...(options.source ? { source: options.source } : {}),
        ...(options.onSaved ? { onSaved: options.onSaved } : {}),
        ref: (component) => {
          instance = component as unknown as { save(): Promise<boolean> } | null;
        },
      }),
  });
  app.mount(target);
  return { unmount: () => app.unmount(), save: () => instance?.save() ?? Promise.resolve(false) };
}

/**
 * `<dynamia-email-editor template-id="7" base-url="" locale="es" source="sales">`: the editor as an HTML tag. It talks
 * to `base-url` + `/api/email/templates` with the session of the page and fires `saved` (a CustomEvent with the
 * template as `detail`) after each save. Include the CSS of the package in the page.
 */
// the package is also imported where there is no DOM (tests, server side): only the tag needs a real HTMLElement
const ElementBase = (typeof HTMLElement === 'undefined' ? class {} : HTMLElement) as typeof HTMLElement;

export class DynamiaEmailEditorElement extends ElementBase {
  static readonly observedAttributes = ['template-id', 'base-url', 'locale', 'source'];
  private mounted: MountedEditor | null = null;

  connectedCallback(): void {
    this.render();
  }

  disconnectedCallback(): void {
    this.mounted?.unmount();
    this.mounted = null;
  }

  attributeChangedCallback(): void {
    if (this.isConnected) {
      this.render();
    }
  }

  /** Saves like the Save button. */
  save(): Promise<boolean> {
    return this.mounted?.save() ?? Promise.resolve(false);
  }

  private render(): void {
    this.mounted?.unmount();
    this.mounted = null;
    const id = Number(this.getAttribute('template-id'));
    if (!Number.isInteger(id) || id <= 0) {
      return;
    }
    const locale = this.getAttribute('locale');
    const source = this.getAttribute('source');
    this.style.display = 'block';
    this.style.height = this.style.height || '100%';
    this.mounted = mountEmailEditor(this, {
      api: new EmailTemplatesClient({ baseUrl: this.getAttribute('base-url') ?? '' }),
      templateId: id,
      ...(locale ? { locale } : {}),
      ...(source ? { source } : {}),
      onSaved: (template) => this.dispatchEvent(new CustomEvent('saved', { detail: template, bubbles: true })),
    });
  }
}

/** Defines the tag once; safe to call again. */
export function registerEmailEditorElement(tag = 'dynamia-email-editor'): void {
  if (typeof customElements !== 'undefined' && !customElements.get(tag)) {
    customElements.define(tag, DynamiaEmailEditorElement);
  }
}
