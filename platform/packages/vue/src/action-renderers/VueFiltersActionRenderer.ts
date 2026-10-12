import { defineComponent, h, reactive, ref, type PropType } from 'vue';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import { CrudView, resolveIconClass, type ResolvedField, type View, type ActionTriggerPayload } from '@dynamia-tools/ui-core';

type FilterKind = 'text' | 'number' | 'boolean';

/**
 * The kind of input for a field, or `null` when the server cannot filter it. The REST read (`RestNavigationQuerySupport`)
 * filters text, numbers and booleans by `?field=value`; dates, references and collections are left out.
 */
function filterKind(fieldClass: string | undefined): FilterKind | null {
  switch (fieldClass) {
    case 'java.lang.String':
      return 'text';
    case 'java.lang.Boolean':
    case 'boolean':
      return 'boolean';
    case 'java.lang.Integer': case 'java.lang.Long': case 'java.lang.Short': case 'java.lang.Byte':
    case 'java.lang.Double': case 'java.lang.Float': case 'java.math.BigDecimal': case 'java.math.BigInteger':
    case 'int': case 'long': case 'short': case 'byte': case 'double': case 'float':
      return 'number';
    default:
      return null;
  }
}

/**
 * Advanced search of the CRUD: renderer `filters` of `FiltersAction`. A button opens a panel with one input per filterable
 * column of the table; applying it triggers the action with `{ field: value }` as the `data` of the request, clearing it
 * sends an empty object.
 */
export const VueFiltersActionRenderer = defineComponent({
  name: 'VueFiltersActionRenderer',
  props: {
    action: { type: Object as PropType<ActionMetadata>, required: true },
    view: { type: Object as PropType<View | undefined>, default: undefined },
    disabled: { type: Boolean, default: false },
    executing: { type: Boolean, default: false },
  },
  emits: {
    trigger: (_payload?: ActionTriggerPayload) => true,
  },
  setup(props, { emit }) {
    const open = ref(false);
    const table = props.view instanceof CrudView ? props.view.tableView : null;
    // Read at render time: the table resolves its columns when it initialises, which can be after this toolbar is created.
    const filterable = () => (table?.getResolvedColumns() ?? [])
      .map((column: ResolvedField) => ({ column, kind: filterKind(column.fieldClass) }))
      .filter((entry): entry is { column: ResolvedField; kind: FilterKind } => entry.kind !== null && entry.column.resolvedVisible);
    const values = reactive<Record<string, string>>(
      Object.fromEntries(Object.entries(table?.getFilters() ?? {}).map(([name, value]) => [name, String(value)])),
    );

    const apply = () => emit('trigger', { request: { data: { ...values } } });
    const clear = () => {
      for (const key of Object.keys(values)) delete values[key];
      emit('trigger', { request: { data: {} } });
    };

    const input = ({ column, kind }: { column: ResolvedField; kind: FilterKind }) => {
      const name = column.name;
      if (kind === 'boolean') {
        return h('select', {
          id: `dynamia-filter-${name}`,
          value: values[name] ?? '',
          onChange: (event: Event) => { values[name] = (event.target as HTMLSelectElement).value; },
        }, [
          h('option', { value: '' }, ''),
          h('option', { value: 'true' }, 'true'),
          h('option', { value: 'false' }, 'false'),
        ]);
      }
      return h('input', {
        id: `dynamia-filter-${name}`,
        type: kind === 'number' ? 'number' : 'text',
        value: values[name] ?? '',
        onInput: (event: Event) => { values[name] = (event.target as HTMLInputElement).value; },
      });
    };

    return () => h('div', { class: 'dynamia-action-filters' }, [
      h('button', {
        type: 'button',
        class: 'dynamia-action-btn',
        'aria-expanded': String(open.value),
        disabled: props.disabled || props.executing,
        onClick: () => { open.value = !open.value; },
      }, [
        props.action.icon
          ? h('i', { class: ['dynamia-action-icon', resolveIconClass(props.action.icon)], 'aria-hidden': 'true' })
          : null,
        h('span', { class: 'dynamia-action-label' }, props.action.name),
      ]),
      open.value
        ? h('form', {
            class: 'dynamia-action-filters-panel',
            onSubmit: (event: Event) => { event.preventDefault(); apply(); },
          }, [
            ...filterable().map((entry) => h('label', { class: 'dynamia-action-filters-field', for: `dynamia-filter-${entry.column.name}` }, [
              h('span', entry.column.resolvedLabel),
              input(entry),
            ])),
            h('div', { class: 'dynamia-action-filters-buttons' }, [
              h('button', { type: 'submit', class: 'dynamia-action-btn' }, 'OK'),
              h('button', { type: 'button', class: 'dynamia-action-btn', onClick: clear }, '×'),
            ]),
          ])
        : null,
    ]);
  },
});
