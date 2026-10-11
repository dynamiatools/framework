import { defineComponent, h, ref, type PropType } from 'vue';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import { CrudView, type View, type ActionTriggerPayload } from '@dynamia-tools/ui-core';

/**
 * Search box of the CRUD: renderer `find` of `FindAction`. Submitting it (Enter or the button) triggers the action with the
 * text as the `data` of the request; an empty text shows every row again.
 */
export const VueFindActionRenderer = defineComponent({
  name: 'VueFindActionRenderer',
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
    const initial = props.view instanceof CrudView ? props.view.tableView?.getSearchQuery() : '';
    const text = ref(initial ?? '');
    const submit = () => emit('trigger', { request: { data: text.value.trim() } });

    return () => h('form', {
      class: 'dynamia-action-find',
      role: 'search',
      onSubmit: (event: Event) => { event.preventDefault(); submit(); },
    }, [
      h('input', {
        type: 'search',
        class: 'dynamia-action-find-input',
        value: text.value,
        placeholder: props.action.name,
        'aria-label': props.action.name,
        disabled: props.disabled || props.executing,
        onInput: (event: Event) => { text.value = (event.target as HTMLInputElement).value; },
      }),
      h('button', {
        type: 'submit',
        class: 'dynamia-action-btn',
        disabled: props.disabled || props.executing,
      }, props.action.name),
    ]);
  },
});
