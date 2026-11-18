/**
 * A small allow-list sanitizer for the HTML of text blocks. The editor keeps the text of a block as HTML, which is
 * shown in the canvas and compiled into the email, so anything that could run script or load things is removed:
 * scripts, styles, frames, forms, event attributes, `javascript:` links and unsafe CSS. Variables such as `{{name}}`
 * and `${name}` are plain text and are not touched. This is a convenience for the author: the server stores what the
 * client compiles, and previews run in a sandboxed frame.
 */

const ALLOWED = new Set(['A', 'B', 'STRONG', 'I', 'EM', 'U', 'S', 'P', 'BR', 'SPAN', 'DIV', 'H1', 'H2', 'H3', 'H4', 'UL', 'OL', 'LI', 'BLOCKQUOTE', 'HR', 'IMG', 'CODE', 'SMALL', 'SUB', 'SUP']);
const DROP_WITH_CONTENT = new Set(['SCRIPT', 'STYLE', 'IFRAME', 'OBJECT', 'EMBED', 'LINK', 'META', 'FORM', 'INPUT', 'BUTTON', 'TEXTAREA', 'SELECT', 'SVG', 'MATH', 'TEMPLATE', 'NOSCRIPT', 'BASE', 'TITLE']);
const ATTRIBUTES: Record<string, string[]> = { A: ['href', 'title', 'target', 'rel'], IMG: ['src', 'alt', 'width', 'height', 'title'] };
const STYLE_PROPERTIES = new Set(['color', 'background-color', 'font-size', 'font-weight', 'font-style', 'font-family', 'text-align', 'text-decoration', 'line-height', 'letter-spacing']);
const STYLE_VALUE = /^[#\w\s.,%'"()-]+$/;

function safeStyle(style: string): string {
  return style
    .split(';')
    .map((declaration) => declaration.split(/:(.*)/s))
    .filter(([name, value]) => name !== undefined && value !== undefined && STYLE_PROPERTIES.has(name.trim().toLowerCase()) && STYLE_VALUE.test(value.trim()) && !/url\s*\(|expression|javascript/i.test(value))
    .map(([name, value]) => `${(name as string).trim().toLowerCase()}: ${(value as string).trim()}`)
    .join('; ');
}

function safeLink(url: string): boolean {
  const value = url.trim().replace(/[\u0000- ]/g, '');
  return value === '' || /^(https?:|mailto:|tel:|#|\/|\{\{|\$\{)/i.test(value) || !/^[a-z][a-z0-9+.-]*:/i.test(value);
}

function clean(node: Node, doc: Document): void {
  for (const child of Array.from(node.childNodes)) {
    if (child.nodeType === 8) {
      child.parentNode?.removeChild(child);
    } else if (child.nodeType === 1) {
      const element = child as Element;
      const tag = element.tagName.toUpperCase();
      if (DROP_WITH_CONTENT.has(tag)) {
        element.remove();
        continue;
      }
      clean(element, doc);
      if (!ALLOWED.has(tag)) {
        while (element.firstChild) {
          element.parentNode?.insertBefore(element.firstChild, element);
        }
        element.remove();
        continue;
      }
      const keep = ATTRIBUTES[tag] ?? [];
      for (const attribute of Array.from(element.attributes)) {
        const name = attribute.name.toLowerCase();
        if (name === 'style') {
          const style = safeStyle(attribute.value);
          if (style) {
            element.setAttribute('style', style);
          } else {
            element.removeAttribute('style');
          }
        } else if (!keep.includes(name) || ((name === 'href' || name === 'src') && !safeLink(attribute.value))) {
          element.removeAttribute(attribute.name);
        }
      }
      if (tag === 'A' && element.getAttribute('target') === '_blank') {
        element.setAttribute('rel', 'noopener noreferrer');
      }
    }
  }
}

/** The same HTML with everything outside the allow list removed. Needs a DOM (a browser, or happy-dom in tests). */
export function sanitizeHtml(html: string): string {
  if (html === '') {
    return '';
  }
  const doc = new DOMParser().parseFromString(`<body>${html}</body>`, 'text/html');
  clean(doc.body, doc);
  return doc.body.innerHTML;
}
