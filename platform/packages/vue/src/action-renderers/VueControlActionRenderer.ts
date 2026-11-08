import { defineComponent, h, ref, type PropType } from 'vue';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import { CrudView, type View } from '@dynamia-tools/ui-core';
import { controlFilter, resolvePreset, type ControlValue } from '../actions/controlFilter.js';

/**
 * Renderer `control` of an action that filters the list instead of being a button (`ActionMetadata.control`): a date, a date
 * range, a search box or a choice. A change sets the filter of the control on the list query and reloads it; the action
 * itself is not run on the server, as the control carries everything the server needs (`POST /find`).
 */
export const VueControlActionRenderer = defineComponent({
  name: 'VueControlActionRenderer',
  props: {
    action: { type: Object as PropType<ActionMetadata>, required: true },
    view: { type: Object as PropType<View | undefined>, default: undefined },
    disabled: { type: Boolean, default: false },
    executing: { type: Boolean, default: false },
  },
  setup(props) {
    const control = props.action.control;
    const table = props.view instanceof CrudView ? props.view.tableView : null;
    const label = control?.label ?? props.action.name;
    const preset = resolvePreset(control?.preset);
    const value = ref<ControlValue>(control?.kind === 'DATE_RANGE' || control?.kind === 'DATETIME_RANGE'
      ? (preset ? [preset[0], preset[1]] : ['', ''])
      : '');

    const apply = () => {
      if (control && table) {
        void table.setQueryFilter(control.field, controlFilter(control, value.value));
      }
    };

    const dateInput = (type: string, current: string, onChange: (text: string) => void, extra: Record<string, unknown> = {}) =>
      h('input', {
        type,
        class: 'dynamia-control-input',
        value: current,
        'aria-label': label,
        disabled: props.disabled,
        onChange: (event: Event) => onChange((event.target as HTMLInputElement).value),
        ...extra,
      });

    return () => {
      if (!control) return null;
      const kind = control.kind;
      let body;
      if (kind === 'DATE_RANGE' || kind === 'DATETIME_RANGE') {
        const type = kind === 'DATE_RANGE' ? 'date' : 'datetime-local';
        const pair = value.value as [string, string];
        body = [
          dateInput(type, pair[0], text => { value.value = [text, pair[1]]; apply(); }, { class: 'dynamia-control-input dynamia-control-from' }),
          dateInput(type, pair[1], text => { value.value = [pair[0], text]; apply(); }, { class: 'dynamia-control-input dynamia-control-to' }),
        ];
      } else if (kind === 'DATE') {
        body = [dateInput('date', value.value as string, text => { value.value = text; apply(); })];
      } else if (kind === 'CHOICE') {
        body = [h('select', {
          class: 'dynamia-control-input',
          'aria-label': label,
          disabled: props.disabled,
          value: value.value as string,
          onChange: (event: Event) => { value.value = (event.target as HTMLSelectElement).value; apply(); },
        }, [
          ...(control.allLabel ? [h('option', { value: '' }, control.allLabel)] : []),
          ...(control.options ?? []).map(option => h('option', { value: option.key }, option.label)),
        ])];
      } else {
        body = [h('input', {
          type: 'search',
          class: 'dynamia-control-input',
          value: value.value as string,
          placeholder: label,
          'aria-label': label,
          disabled: props.disabled,
          onInput: (event: Event) => { value.value = (event.target as HTMLInputElement).value; },
          onKeyup: (event: KeyboardEvent) => { if (event.key === 'Enter') apply(); },
        })];
      }
      return h('span', { class: ['dynamia-control', `dynamia-control-${kind.toLowerCase()}`] }, body);
    };
  },
});
