import { createApp } from 'vue';
import { DynamiaVue } from '@dynamia-tools/vue';
import App from './App.vue';
import './style.css';

const app = createApp(App);
app.use(DynamiaVue);
app.mount('#app');

