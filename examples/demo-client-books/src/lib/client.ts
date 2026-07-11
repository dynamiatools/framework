import { DynamiaClient } from '@dynamia-tools/sdk';

const baseUrl = import.meta.env.PUBLIC_API_URL ?? 'http://localhost:8484';

export const client = new DynamiaClient({ baseUrl });

