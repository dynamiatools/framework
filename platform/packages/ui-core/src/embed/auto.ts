// auto.ts — side-effect entry: registers <dynamia-embed> on import.
// This is the entry point bundled for direct `<script>` consumption (dist/dynamia-embed.global.js).

import { registerDynamiaEmbed } from './register.js';

registerDynamiaEmbed();
