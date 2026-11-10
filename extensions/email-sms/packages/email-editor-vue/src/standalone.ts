import './ui/standalone.css';
import { EmailTemplatesClient } from './api.js';
import { createDemoApi } from './demo.js';
import { mountEmailEditor, registerEmailEditorElement } from './element.js';

/**
 * The page of the editor: `index.html?id=7` edits template 7 of the application that serves the page
 * (`&baseUrl=` for another origin, `&locale=es`, `&source=sales`). `?demo=1` runs against an in-memory template.
 * When the page is inside a frame it tells the parent after each save with a `postMessage`.
 */
const params = new URLSearchParams(window.location.search);
const root = document.getElementById('app');
registerEmailEditorElement();

function notifyParent(id: number): void {
  if (window.parent !== window) {
    window.parent.postMessage({ type: 'dynamia-email-editor:saved', id }, window.location.origin);
  }
}

if (root) {
  const demo = params.get('demo') === '1';
  const id = demo ? 1 : Number(params.get('id'));
  if (!Number.isInteger(id) || id <= 0) {
    root.textContent = 'Missing template id: open this page with ?id=<template id>';
  } else {
    const locale = params.get('locale') ?? navigator.language.split('-')[0] ?? 'en';
    const source = params.get('source');
    mountEmailEditor(root, {
      api: demo ? createDemoApi({ velocity: params.get('velocity') === '1' }) : new EmailTemplatesClient({ baseUrl: params.get('baseUrl') ?? '' }),
      templateId: id,
      locale,
      ...(source ? { source } : {}),
      onSaved: (template) => notifyParent(template.id),
    });
  }
}
