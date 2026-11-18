# @dynamia-tools/template-engine

A small, safe text template engine. It is the TypeScript twin of the Java `tools.dynamia.templates.DynamiaTemplateEngine`:
for the same data both print the same text (they run the same cases, see
[`platform/contract/fixtures/templates/conformance.json`](../../contract/fixtures/templates/conformance.json)).

No code from the template is ever executed: no `eval`, no method calls, no prototype access. A template can only read
data, apply a fixed list of filters, and repeat or skip a block.

```ts
import { DynamiaTemplateEngine } from '@dynamia-tools/template-engine';

const engine = new DynamiaTemplateEngine({ locale: 'es-CO' });
engine.evaluate('Hola ${user.name}, total {{total|currency:0}}', { user: { name: 'Ada' }, total: 1234567 });
// Hola Ada, total 1.234.567
```

## Syntax

| Form | Example |
|---|---|
| Variables | `${name}`, `{{name}}`, `$user.name` (short form, only when `user` exists) |
| Paths | `${order.customer.name}` (object properties and `Map` keys) |
| Filters | `${total\|currency:0}`, `{{name\|trim\|upper\|default:-}}` |
| Ternary | `${paid ? 'PAID' : 'PENDING'}` |
| Arithmetic | `${qty * price}`, `${(a + b) / 2}` (exact decimals) |
| If | `{{#if total > 100 && vip}}...{{else}}...{{/if}}` |
| Each | `{{#each items as item}}${item.name} ${loop.number}/${loop.count}{{/each}}` |

Filters: `upper`, `lower`, `trim`, `default:x`, `number:pattern`, `currency:decimals`, `date:pattern`, `count`, `sum:field`,
`avg:field`, `min:field`, `max:field`, `list`, `list_dots`, `list_number`, `join:sep`, `raw`. Add your own with `registerFilter`.

Pass `{ escapeHtml: true }` to replace `& < > " '` in every printed value (`${body|raw}` prints trusted HTML; text of the
template is never escaped).

Unknown variables are left as written (`${other}`), which is what a live preview needs; `null` prints nothing.
Values inserted into the text are never evaluated again. Unbalanced block tags leave the text untouched. Blocks are
capped at 10 levels and 10,000 iterations.

## Where it differs from Java

- Numbers from JSON have no scale: `2.0` is `2`, and totals keep no trailing zeros. Java keeps the scale of its own
  numbers (a `Double` `2.0` prints `2.0`, a `BigDecimal` `10.00` prints `10.00`), so avoid such values in shared cases.
- `number` patterns support grouping, minimum and maximum decimals and a literal prefix or suffix (`$ #,##0.00`).
  Percent, exponent and negative sub-patterns are not supported and the value is printed as it is.
- `date` supports `y M d E H h m s a` and quoted text. Dates are ISO strings (`2026-11-01`, `2026-11-01T14:30:00`,
  an instant with `Z`) or `Date` objects; instants show in the local time zone.
- Only arrays and sets are lists, and only own properties of plain objects are readable (no getters).

## Develop

```bash
pnpm --filter @dynamia-tools/template-engine test
pnpm --filter @dynamia-tools/template-engine build
```

A change to a case in `conformance.json` must be made for both engines.
