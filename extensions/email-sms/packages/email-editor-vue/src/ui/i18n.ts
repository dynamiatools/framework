/** Labels of the editor. English is the fallback; Spanish is included. */
const en: Record<string, string> = {
  undo: 'Undo', redo: 'Redo', blocks: 'Blocks', delete: 'Delete', duplicate: 'Duplicate', moveUp: 'Move up', moveDown: 'Move down',
  emptyEmail: 'Drag a block here or click one in the list', emptyColumn: 'Drop blocks here', emptyRepeat: 'Blocks to repeat go here',
  emptyThen: 'Shown when the condition is true', emptyElse: 'Shown otherwise (optional)', htmlDocument: 'HTML document (kept as it is)',
  header: 'Header', text: 'Text', image: 'Image', button: 'Button', divider: 'Divider', spacer: 'Spacer', columns: 'Columns',
  footer: 'Footer', html: 'HTML', repeat: 'Repeat', condition: 'Condition', column: 'Column',
  repeatFor: 'For each {alias} in {source}', ifCondition: 'If {expression}', otherwise: 'Otherwise', hiddenOn: 'Hidden on {device}',
  desktop: 'desktop', mobile: 'mobile', settings: 'Email settings', properties: 'Properties', selectBlock: 'Select a block to edit it',
  noImage: 'Image', save: 'Save', saving: 'Saving…', saved: 'Saved', preview: 'Preview', sendTest: 'Send test', variables: 'Variables',
};
const es: Record<string, string> = {
  undo: 'Deshacer', redo: 'Rehacer', blocks: 'Bloques', delete: 'Eliminar', duplicate: 'Duplicar', moveUp: 'Subir', moveDown: 'Bajar',
  emptyEmail: 'Arrastra un bloque aquí o haz clic en uno de la lista', emptyColumn: 'Suelta bloques aquí', emptyRepeat: 'Aquí van los bloques a repetir',
  emptyThen: 'Se muestra si la condición se cumple', emptyElse: 'Se muestra en caso contrario (opcional)', htmlDocument: 'Documento HTML (se conserva tal cual)',
  header: 'Encabezado', text: 'Texto', image: 'Imagen', button: 'Botón', divider: 'Divisor', spacer: 'Espaciador', columns: 'Columnas',
  footer: 'Pie', html: 'HTML', repeat: 'Repetir', condition: 'Condición', column: 'Columna',
  repeatFor: 'Por cada {alias} en {source}', ifCondition: 'Si {expression}', otherwise: 'Si no', hiddenOn: 'Oculto en {device}',
  desktop: 'escritorio', mobile: 'móvil', settings: 'Ajustes del correo', properties: 'Propiedades', selectBlock: 'Selecciona un bloque para editarlo',
  noImage: 'Imagen', save: 'Guardar', saving: 'Guardando…', saved: 'Guardado', preview: 'Vista previa', sendTest: 'Enviar prueba', variables: 'Variables',
};
const dictionaries: Record<string, Record<string, string>> = { en, es };

/** A translator for a language tag such as `es` or `es-CO`; unknown keys come back as the key. */
export function translator(locale = 'en'): (key: string, values?: Record<string, string | number>) => string {
  const dictionary = dictionaries[locale.toLowerCase().split(/[-_]/)[0] ?? 'en'] ?? en;
  return (key, values) =>
    (dictionary[key] ?? en[key] ?? key).replace(/\{(\w+)\}/g, (_, name: string) => String(values?.[name] ?? `{${name}}`));
}
