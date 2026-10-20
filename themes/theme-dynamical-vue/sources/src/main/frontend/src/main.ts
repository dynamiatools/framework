import { registerFlowStepRenderer } from '@dynamia-tools/ui-core';
import { createDynamiaApp } from './index.js';
import StarRatingHost from './components/StarRatingHost.vue';
import { askStarRating } from './lib/starRatingManager.js';

// App-specific CUSTOM flow step renderer for RateBookAction's demo — see StarRatingHost.vue and
// docs/design/SERVER_DRIVEN_ACTION_FLOWS.md §6.
registerFlowStepRenderer('star-rating', step => askStarRating(step.data));

createDynamiaApp({ hosts: [StarRatingHost] });
