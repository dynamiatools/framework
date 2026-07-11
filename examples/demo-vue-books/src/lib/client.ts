import { DynamiaClient } from '@dynamia-tools/sdk';

const baseUrl = import.meta.env.VITE_DYNAMIA_API_URL ?? '';
const token = import.meta.env.VITE_DYNAMIA_TOKEN;
const username = import.meta.env.VITE_DYNAMIA_USERNAME;
const password = import.meta.env.VITE_DYNAMIA_PASSWORD;

export const client = new DynamiaClient({
  baseUrl,
  token: token || undefined,
  username: username || undefined,
  password: password || undefined,
});

