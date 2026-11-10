// @vitest-environment happy-dom
import { describe, expect, it } from 'vitest';
import { sanitizeHtml } from '../../src/index.js';

describe('sanitizeHtml', () => {
  it('keeps formatting, links and variables', () => {
    const html = '<p>Hi <strong>${user.name|upper}</strong>, {{total|currency:0}} <a href="https://x.com" target="_blank">go</a></p>';
    const out = sanitizeHtml(html);
    expect(out).toContain('${user.name|upper}');
    expect(out).toContain('{{total|currency:0}}');
    expect(out).toContain('href="https://x.com"');
    expect(out).toContain('rel="noopener noreferrer"');
  });

  it('removes scripts, frames, forms, styles and comments with their content', () => {
    const out = sanitizeHtml('<p>a</p><script>alert(1)</script><iframe src="x"></iframe><form><input></form><style>p{}</style><!-- c --><svg onload=x></svg>b');
    expect(out).toBe('<p>a</p>b');
  });

  it('removes event attributes and unsafe links but keeps the element', () => {
    const out = sanitizeHtml('<a href="javascript:alert(1)" onclick="x()">a</a><img src="data:text/html,x" onerror="x()" alt="i"><a href=" JaVa\tScript:x">b</a>');
    expect(out).not.toMatch(/javascript|onclick|onerror|data:/i);
    expect(out).toContain('<a>a</a>');
    expect(out).toContain('alt="i"');
  });

  it('allows relative links, anchors, mailto and variable links', () => {
    const out = sanitizeHtml('<a href="/x">a</a><a href="#top">b</a><a href="mailto:a@x.com">c</a><a href="{{ url }}">d</a>');
    expect(out).toContain('href="/x"');
    expect(out).toContain('href="#top"');
    expect(out).toContain('href="mailto:a@x.com"');
    expect(out).toContain('href="{{ url }}"');
  });

  it('keeps only safe css', () => {
    const out = sanitizeHtml('<span style="color: red; position: fixed; background-color: url(http://x/y); font-size: 18px">a</span><p style="behavior: url(x)">b</p>');
    expect(out).toContain('style="color: red; font-size: 18px"');
    expect(out).toContain('<p>b</p>');
  });

  it('unwraps tags that are not allowed and leaves plain text alone', () => {
    expect(sanitizeHtml('<font color="red"><marquee>hi</marquee></font>')).toBe('hi');
    expect(sanitizeHtml('plain & text')).toBe('plain &amp; text');
    expect(sanitizeHtml('')).toBe('');
  });
});
