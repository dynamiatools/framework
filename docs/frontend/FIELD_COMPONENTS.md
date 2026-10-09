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

`Field.vue` maps the identifier to a component. Identifiers defined in `FieldComponent` that have no Vue component yet
fall back to a plain text input: `doublespinner`, `timebox`, `listbox`, `colorpicker`, `htmleditor`, `sliderbox`,
`numberrangebox`, `imagebox`, `entityfileimage`, `enumiconimage`, `crudview`, `printercombobox`,
`providermultipickerbox`.

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
`GET /api/app/metadata/entities/ref/{alias}/search?q=`.

## Saving collections

A many-to-many field is sent as `[{id, name}]`. `JsonViewDescriptorDeserializer.applyCollectionPatch` replaces the
collection with the existing entities of those ids, and is shared by the REST `PUT` and the `save` actions. Any other
collection (owned children, element collections) is left untouched when patching.

## Editing loads the whole entity

The rows of a list carry the columns of the table view, not the relations. `CrudView.setEntityLoader` makes
`startEdit` show the row at once and replace it with `GET /api/{path}/{id}` when that arrives (wired in `useCrudPage`).

## Table cells

`Table.vue` renders a column with `Field.vue` (read-only) when the descriptor sets one of `coollabel`, `enumlabel`,
`entityreflabel`, `label` or `link` as its `component`; any other column is plain text.

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
