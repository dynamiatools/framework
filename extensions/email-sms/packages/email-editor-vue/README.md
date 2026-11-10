# @dynamia-tools/email-editor-vue

A block based, responsive email template editor for Vue 3. It edits a JSON **design** (a tree of blocks), compiles it to
table based HTML with MJML and keeps the template variables (`${x}`, `{{x}}`, pipes, `{{#if}}`, `{{#each}}`) for the Dynamia
template engine. It is the editor of the `email-sms` extension; see its [README](../../README.md) for the server side.

## Use it

```html
<link rel="stylesheet" href="email-editor-vue.css" />
<dynamia-email-editor template-id="7" base-url="" locale="es" source="sales" style="height: 100vh"></dynamia-email-editor>
<script type="module">
  import { registerEmailEditorElement } from '@dynamia-tools/email-editor-vue';
  registerEmailEditorElement();
</script>
```

```ts
import { mountEmailEditor, EmailTemplatesClient } from '@dynamia-tools/email-editor-vue';

const editor = mountEmailEditor(document.getElementById('app')!, {
  api: new EmailTemplatesClient({ baseUrl: '' }), // the session cookie of the page is used
  templateId: 7,
  locale: 'en',
  onSaved: (template) => console.log('saved', template.id),
});
await editor.save();
```

Pieces you can use alone: `EmailEditor` (canvas, palette, properties and variables, no server), `PreviewPane`, the model
(`parseDesign`, `insert`, `move`, `History`...) and `compile(design)`.

## The standalone page

`pnpm --filter @dynamia-tools/email-editor-vue build:page` builds `dist-page/` (relative URLs, so any path can serve it):
`index.html?id=7` edits template 7 of the application that serves it (`&baseUrl=`, `&locale=es`, `&source=sales`), and
`index.html?demo=1` runs against an in-memory template (`&velocity=1` to try the migration). Inside a frame it sends
`{ type: 'dynamia-email-editor:saved', id }` to its parent after each save. The email-sms UI module packages this page
with `mvn -Peditor`.

## Design and compiler

```json
{ "version": 1, "settings": { "width": 600, "backgroundColor": "#f4f4f4" },
  "blocks": [ { "id": "text-1", "type": "text", "props": { "html": "<p>Hi {{name}}</p>" } } ] }
```

`columns` hold `column` blocks; a `column` holds content blocks but never `columns`; `repeat` and `condition` can hold
columns and content. A design made of one `html` block with `fullDocument: true` (what migrating a template produces) is
sent as it is, without MJML. Colours, paddings, numbers, links and the names in `repeat` and `condition` are validated by
the compiler, so a design cannot inject markup or a `javascript:` link. `mjml-browser` is about 1.3 MB and is loaded the
first time something is compiled; it needs `cheerio` next to it, which this package depends on.

## Safety

The HTML of text blocks is sanitized with an allow list in the editor, previews run in an iframe with `sandbox=""`, and
the server only renders request content with the Dynamia engine. The sanitizer is a convenience for the author, not the
security boundary.

## Develop

```bash
pnpm --filter @dynamia-tools/email-editor-vue test       # Vitest: model, compiler (real MJML), UI (happy-dom)
pnpm --filter @dynamia-tools/email-editor-vue typecheck
pnpm --filter @dynamia-tools/email-editor-vue build      # library
pnpm --filter @dynamia-tools/email-editor-vue build:page # standalone page
```
