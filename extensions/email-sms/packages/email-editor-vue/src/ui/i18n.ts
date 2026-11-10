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
  bold: 'Bold', italic: 'Italic', underline: 'Underline', link: 'Link', bulletList: 'Bulleted list', numberList: 'Numbered list', clearFormat: 'Clear formatting', linkUrl: 'Link address', apply: 'Apply',
  searchVariables: 'Search variables', loopVariables: 'Inside the repeat', noVariables: 'No variables available', insertVariable: 'Insert {name}',
  invalidPadding: 'Use 1 to 4 numbers like 10px 24px', invalidColor: 'Use a colour like #1a73e8', invalidPath: 'Use a name like items or order.lines',
  invalidAlias: 'Use a single word', invalidCondition: 'Use names, comparisons and && || ! only', addColumn: 'Add column', removeColumn: 'Remove column',
  columnWidth: 'Column {n} width (%)', none: 'none', left: 'Left', center: 'Center', right: 'Right', 'opt.none': 'Always shown', 'opt.desktop': 'Hide on desktop', 'opt.mobile': 'Hide on mobile',
  'opt.left': 'Left', 'opt.center': 'Center', 'opt.right': 'Right', htmlSource: 'HTML', fullDocumentNote: 'This block is a whole HTML document and is sent as it is.',
  'f.logoUrl': 'Logo URL', 'f.logoWidth': 'Logo width (px)', 'f.logoAlt': 'Logo description', 'f.title': 'Title', 'f.align': 'Alignment', 'f.backgroundColor': 'Background', 'f.padding': 'Padding',
  'f.html': 'Content', 'f.color': 'Colour', 'f.fontSize': 'Font size (px)', 'f.lineHeight': 'Line height', 'f.hideOn': 'Visibility', 'f.src': 'Image URL', 'f.alt': 'Description', 'f.href': 'Link',
  'f.width': 'Width', 'f.text': 'Text', 'f.borderRadius': 'Corner radius (px)', 'f.fullWidth': 'Full width', 'f.thickness': 'Thickness (px)', 'f.height': 'Height (px)',
  'f.stackOnMobile': 'Stack on mobile', 'f.source': 'List (variable)', 'f.alias': 'Name of each item', 'f.expression': 'Condition', 'f.contentBackground': 'Email background',
  'f.fontFamily': 'Font', 'f.textColor': 'Text colour', 'f.linkColor': 'Link colour', 'f.preheader': 'Preheader (inbox preview text)',
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
  bold: 'Negrita', italic: 'Cursiva', underline: 'Subrayado', link: 'Enlace', bulletList: 'Lista con viñetas', numberList: 'Lista numerada', clearFormat: 'Quitar formato', linkUrl: 'Dirección del enlace', apply: 'Aplicar',
  searchVariables: 'Buscar variables', loopVariables: 'Dentro del repetir', noVariables: 'No hay variables disponibles', insertVariable: 'Insertar {name}',
  invalidPadding: 'Usa de 1 a 4 números como 10px 24px', invalidColor: 'Usa un color como #1a73e8', invalidPath: 'Usa un nombre como items u order.lines',
  invalidAlias: 'Usa una sola palabra', invalidCondition: 'Usa nombres, comparaciones y && || ! solamente', addColumn: 'Agregar columna', removeColumn: 'Quitar columna',
  columnWidth: 'Ancho de la columna {n} (%)', none: 'ninguno', left: 'Izquierda', center: 'Centro', right: 'Derecha', 'opt.none': 'Siempre visible', 'opt.desktop': 'Ocultar en escritorio', 'opt.mobile': 'Ocultar en móvil',
  'opt.left': 'Izquierda', 'opt.center': 'Centro', 'opt.right': 'Derecha', htmlSource: 'HTML', fullDocumentNote: 'Este bloque es un documento HTML completo y se envía tal cual.',
  'f.logoUrl': 'URL del logo', 'f.logoWidth': 'Ancho del logo (px)', 'f.logoAlt': 'Descripción del logo', 'f.title': 'Título', 'f.align': 'Alineación', 'f.backgroundColor': 'Fondo', 'f.padding': 'Relleno',
  'f.html': 'Contenido', 'f.color': 'Color', 'f.fontSize': 'Tamaño de letra (px)', 'f.lineHeight': 'Interlineado', 'f.hideOn': 'Visibilidad', 'f.src': 'URL de la imagen', 'f.alt': 'Descripción', 'f.href': 'Enlace',
  'f.width': 'Ancho', 'f.text': 'Texto', 'f.borderRadius': 'Radio de esquina (px)', 'f.fullWidth': 'Ancho completo', 'f.thickness': 'Grosor (px)', 'f.height': 'Alto (px)',
  'f.stackOnMobile': 'Apilar en móvil', 'f.source': 'Lista (variable)', 'f.alias': 'Nombre de cada elemento', 'f.expression': 'Condición', 'f.contentBackground': 'Fondo del correo',
  'f.fontFamily': 'Fuente', 'f.textColor': 'Color del texto', 'f.linkColor': 'Color de enlaces', 'f.preheader': 'Preencabezado (texto de la bandeja)',
};
const dictionaries: Record<string, Record<string, string>> = { en, es };

/** A translator for a language tag such as `es` or `es-CO`; unknown keys come back as the key. */
export function translator(locale = 'en'): (key: string, values?: Record<string, string | number>) => string {
  const dictionary = dictionaries[locale.toLowerCase().split(/[-_]/)[0] ?? 'en'] ?? en;
  return (key, values) =>
    (dictionary[key] ?? en[key] ?? key).replace(/\{(\w+)\}/g, (_, name: string) => String(values?.[name] ?? `{${name}}`));
}
