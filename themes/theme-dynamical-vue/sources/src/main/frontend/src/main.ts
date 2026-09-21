import { createApp } from 'vue';
import { DynamiaVue } from '@dynamia-tools/vue';
import { registerDynamiaEmbed } from '@dynamia-tools/ui-core/embed';
import { registerFlowStepRenderer } from '@dynamia-tools/ui-core';
import App from './App.vue';
import { client } from './lib/client.js';
import { askStarRating } from './lib/starRatingManager.js';
import './styles/app.css';

registerDynamiaEmbed();

// App-specific CUSTOM flow step renderer for RateBookAction's demo — see StarRatingHost.vue and
// docs/design/SERVER_DRIVEN_ACTION_FLOWS.md §6.
registerFlowStepRenderer('star-rating', step => askStarRating(step.data));

const app = createApp(App);
app.use(DynamiaVue, { client });
app.mount('#app');
