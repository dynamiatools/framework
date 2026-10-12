import { defineComponent, h, type PropType } from 'vue';
import type { ActionMetadata } from '@dynamia-tools/sdk';
import { resolveIconClass, type View, type ActionTriggerPayload } from '@dynamia-tools/ui-core';

export const VueButtonActionRenderer = defineComponent({
  name: 'VueButtonActionRenderer',
  props: {
    action: {
      type: Object as PropType<ActionMetadata>,
      required: true,
    },
    view: {
      type: Object as PropType<View | undefined>,
      default: undefined,
    },
    disabled: {
      type: Boolean,
      default: false,
    },
    executing: {
      type: Boolean,
      default: false,
    },
  },
  emits: {
    trigger: (_payload?: ActionTriggerPayload) => true,
  },
  setup(props, { emit }) {
    return () => h(
      'button',
      {
        type: 'button',
        class: 'dynamia-action-btn',
        title: props.action.description,
        disabled: props.disabled || props.executing,
        onClick: () => emit('trigger', {}),
      },
      [
        props.action.icon
          ? h('i', { class: ['dynamia-action-icon', resolveIconClass(props.action.icon)], 'aria-hidden': 'true' })
          : null,
        h('span', { class: 'dynamia-action-label' }, props.executing ? `${props.action.name}…` : props.action.name),
      ],
    );
  },
});

