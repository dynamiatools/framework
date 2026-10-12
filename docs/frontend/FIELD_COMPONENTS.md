# Field components in the Vue UI

How a field of a view descriptor becomes a Vue component in `@dynamia-tools/vue`, what the server must send for it,
and where it is rendered (forms and table cells).

## How a field is resolved

`FieldResolver` (`@dynamia-tools/ui-core`) picks `resolvedComponent` in this order:

1. `field.component` (the `component:` of the descriptor) or `params.component`.
2. A collection of entities (`field.entity && field.collection`, e.g. a many-to-many `Set<Genre>`):
   `entityrefmultipicker` when `params.entityAlias` is set, `label` otherwise.
3. A single entity (`field.entity`, e.g. a `@ManyToOne`): `entityrefpicker` when `params.entityAlias` is set,
   `entityreflabel` (read-only) otherwise.
4. The field class: text, integer, decimal, checkbox, date, enum (`combobox`)... `textbox` when unknown.

`Field.vue` (through `resolveFieldComponent`, `components/fields/fieldComponents.ts`) turns the identifier into a
component, in this order:

1. **`FieldComponentRegistry`** (`@dynamia-tools/ui-core`), case-insensitive: what a theme or the application registered
   for that name. It can override any built-in name.
2. **The components of `@dynamia-tools/vue`**: the identifiers above, plus basic versions of `rating` (stars, params
   `max` and `scale`), `switch`/`toggle` (a checkbox) and `badge`/`status` (the enum label), so they work without a theme.
3. **The component of the field type** (`FieldResolver.inferComponent(fieldClass)`) when nobody implements the name:
   an unknown `component: fancything` on a boolean is still a checkbox, on a number a number box. Only when not even the
   type is known is a plain text input shown.

Identifiers defined in `FieldComponent` that have no Vue component yet (`doublespinner`, `timebox`, `listbox`,
`colorpicker`, `htmleditor`, `sliderbox`, `numberrangebox`, `imagebox`, `entityfileimage`, `enumiconimage`, `crudview`,
`printercombobox`, `providermultipickerbox`) therefore draw the one of their field type.

A theme registers its own implementations once, before mounting. `theme-dynamical-vue` does it in
`src/lib/fieldComponents.ts` with the TailAdmin components (`ext/input/Rating`, `ui/Badge`, `ext/display/Status`):

```ts
FieldComponentRegistry.register('rating', StarRating, ['stars']);
```

What `theme-dynamical-vue` registers today (each one reads its `params:`, and shows a read-only form in a table cell):

| Name (aliases) | TailAdmin component | params |
|---|---|---|
| `rating` (`stars`) | `ext/input/Rating` | `max`, `scale` |
| `badge` | `ui/Badge` | `color`, `colors` |
| `status` | `ext/display/Status` | `color`, `colors` |
| `colorpicker` (`colorbox`) | `ext/input/ColorPicker` | `presets` (hex list), `showText` |
| `moneyinput` (`moneybox`, `money`) | `ext/input/MoneyInput` | `currency`, `locale`, `decimals`, `min`, `max` |
| `quantityinput` (`quantitybox`, `quantity`) | `ext/input/QuantityInput` | `min`, `max`, `step`, `allowDecimal` |

`ext/display/QrCode` is not registered: it needs the `qrcode` package, which TailAdmin does not install.

A component receives `field` (`ResolvedField`), `modelValue`, `readOnly` and `params` (the `params:` of the descriptor
field), and emits `update:modelValue`.

```yaml
rating:
  component: rating          # 0-10 value drawn with 5 stars
  params: { max: 5, scale: 10 }
certification:
  component: badge          # enum value as a pill
```

## What the server sends

`ApiFieldCustomizer` (`platform/app`) is a `FieldCustomizer` for `form` views. Before it, only the ZK customizers added
this data, so a Vue-only application had enums without options and entity fields without a picker:

| Field | Param added | Used by |
|---|---|---|
| enum | `ENUM_CONSTANTS`: the constant names | `Combobox.vue` |
| entity or collection of entities | `entityAlias`, plus `minChars: 1` | `EntityRefPicker.vue`, `EntityRefMultiPicker.vue` |

`entityAlias` is the one of the entity's `EntityReferenceRepository`, or the `@Reference` value. An entity without a
repository gets a `DefaultEntityReferenceRepository` registered on demand (searches the first of `name`, `title`,
`fullName`, `label`, `description`). Params already set in the descriptor are never overwritten. The pickers search with
`GET /apps/api/metadata/entities/ref/{alias}/search?q=`.

## Saving collections

A many-to-many field is sent as `[{id, name}]`. `JsonViewDescriptorDeserializer.applyCollectionPatch` replaces the
collection with the existing entities of those ids, and is shared by the REST `PUT` and the `save` actions. Any other
collection (owned children, element collections) is left untouched when patching.

## Editing loads the whole entity

The rows of a list carry the columns of the table view, not the relations. `CrudView.setEntityLoader` makes
`startEdit` show the row at once and replace it with `GET /api/{path}/{id}` when that arrives (wired in `useCrudPage`).

## Table cells

`Table.vue` renders a column with `Field.vue` (read-only) in two cases (`isCellComponent`):

- the field is a **boolean**: a disabled checkbox, with no `component` needed;
- the descriptor names a **display component** that exists (built in or registered): `coollabel`, `enumlabel`,
  `entityreflabel`, `label`, `link`, `rating`, `badge`, `status`, or anything a theme registered.

Input-only names (`textbox`, `intbox`, `combobox`, `datebox`...) and names nobody implements stay plain text in a cell.

A column with no property of its own (a virtual field) may declare `params.bindings`. The serializer then writes the
field as an object with the value of every bound path:

```yaml
users:
  component: coollabel
  params:
    bindings: { title: username, subtitle: fullname, description: email, imageURL: photo.thumbnailUrl }
```

becomes `"users": {"title": "admin", "subtitle": "Administrator", "description": "...", "imageURL": "..."}`.
`CoolLabel.vue` reads those keys and shows an initial avatar when there is no photo or it fails to load (it does not
depend on `noImagePath`, which points to a resource of the ZK module).
