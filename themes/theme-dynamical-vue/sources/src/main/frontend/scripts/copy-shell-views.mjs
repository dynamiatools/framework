// copy-shell-views.mjs — reorganizes Vite's `dist/` output onto the Maven classpath layout
// the Java side expects (see DynamicalVueTemplate.java):
//
//   dist/index.html, dist/login.html  -> target/classes/views/*.html
//     (resolved by Spring's ClassPathViewResolver for view names "index" / "login")
//   dist/assets/**, dist/anything-else -> target/classes/web/templates/dynamicalvue/**
//     (resolved by ApplicationTemplateResourceHandler for the running theme "DynamicalVue")
//
// Runs as the second step of `pnpm run build` (after `vite build`), so it also works when
// invoked directly by a developer, not just through frontend-maven-plugin.

import { cpSync, existsSync, mkdirSync, readdirSync, rmSync } from 'node:fs';
import { join, dirname } from 'node:path';
import { fileURLToPath } from 'node:url';

const frontendDir = dirname(dirname(fileURLToPath(import.meta.url)));
const distDir = join(frontendDir, 'dist');
const classesDir = join(frontendDir, '..', '..', '..', 'target', 'classes');
const viewsDir = join(classesDir, 'views');
const templateDir = join(classesDir, 'web', 'templates', 'dynamicalvue');

const SHELLS = ['index.html', 'login.html'];

if (!existsSync(distDir)) {
  console.error(`[copy-shell-views] dist/ not found at ${distDir} — did "vite build" run first?`);
  process.exit(1);
}

// Only clean the two subtrees this script owns — never touch the rest of target/classes
// (compiled .class files, src/main/resources output), since this runs at generate-resources,
// ahead of the `compile`/`process-resources` phases in the same reactor build.
rmSync(viewsDir, { recursive: true, force: true });
rmSync(templateDir, { recursive: true, force: true });
mkdirSync(viewsDir, { recursive: true });
mkdirSync(templateDir, { recursive: true });

for (const entry of readdirSync(distDir)) {
  const src = join(distDir, entry);
  const dest = SHELLS.includes(entry) ? join(viewsDir, entry) : join(templateDir, entry);
  cpSync(src, dest, { recursive: true });
}

console.log(`[copy-shell-views] views/{${SHELLS.join(',')}} -> ${viewsDir}`);
console.log(`[copy-shell-views] assets -> ${templateDir}`);
