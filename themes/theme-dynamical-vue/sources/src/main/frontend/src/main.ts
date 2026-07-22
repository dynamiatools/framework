import { createApp } from 'vue';
import { DynamiaVue } from '@dynamia-tools/vue';
import { registerDynamiaEmbed } from '@dynamia-tools/ui-core/embed';
import App from './App.vue';
import { client } from './lib/client.js';
import './styles/app.css';

registerDynamiaEmbed();

const app = createApp(App);
app.use(DynamiaVue, { client });
app.mount('#app');
