/** Texts of the reports components. Pass a partial object in the `labels` prop to translate or reword them. */
export interface ReportLabels {
  loading: string;
  search: string;
  noReports: string;
  run: string;
  running: string;
  reset: string;
  export: string;
  exporting: string;
  required: string;
  selectOption: string;
  yes: string;
  no: string;
  noResults: string;
  rows: string;
  of: string;
  page: string;
  previous: string;
  next: string;
  rowsPerPage: string;
  truncated: string;
  charts: string;
  chartError: string;
  designerReports: string;
  designerTools: string;
  designerNotAllowed: string;
  queryLanguage: string;
  query: string;
  datasourceId: string;
  parameters: string;
  preview: string;
  previewTruncated: string;
  exportDefinition: string;
  importDefinition: string;
  importDone: string;
  reportId: string;
  testDatasource: string;
}

/** English texts, the default. */
export const englishLabels: ReportLabels = {
  loading: 'Loading...',
  search: 'Search reports',
  noReports: 'There are no reports available',
  run: 'Run',
  running: 'Running...',
  reset: 'Reset',
  export: 'Export',
  exporting: 'Exporting...',
  required: 'Required',
  selectOption: 'Select...',
  yes: 'Yes',
  no: 'No',
  noResults: 'The report returned no results',
  rows: 'rows',
  of: 'of',
  page: 'Page',
  previous: 'Previous',
  next: 'Next',
  rowsPerPage: 'Rows per page',
  truncated: 'The report reached the maximum number of rows, so the result is incomplete. Use more filters.',
  charts: 'Charts',
  chartError: 'Cannot draw the chart',
  designerReports: 'Reports',
  designerTools: 'Tools',
  designerNotAllowed: 'You are not allowed to use the designer tools',
  queryLanguage: 'Query language',
  query: 'Query',
  datasourceId: 'Datasource id (empty for the application database)',
  parameters: 'Parameters (JSON)',
  preview: 'Preview',
  previewTruncated: 'There are more rows than the preview shows',
  exportDefinition: 'Export definition',
  importDefinition: 'Import definition',
  importDone: 'Report imported with id',
  reportId: 'Report id',
  testDatasource: 'Test datasource',
};

/** Spanish texts. */
export const spanishLabels: ReportLabels = {
  loading: 'Cargando...',
  search: 'Buscar informes',
  noReports: 'No hay informes disponibles',
  run: 'Ejecutar',
  running: 'Ejecutando...',
  reset: 'Limpiar',
  export: 'Exportar',
  exporting: 'Exportando...',
  required: 'Requerido',
  selectOption: 'Seleccione...',
  yes: 'Si',
  no: 'No',
  noResults: 'El informe no devolvio resultados',
  rows: 'filas',
  of: 'de',
  page: 'Pagina',
  previous: 'Anterior',
  next: 'Siguiente',
  rowsPerPage: 'Filas por pagina',
  truncated: 'El informe llego al maximo de filas, el resultado esta incompleto. Use mas filtros.',
  charts: 'Graficas',
  chartError: 'No se puede dibujar la grafica',
  designerReports: 'Informes',
  designerTools: 'Herramientas',
  designerNotAllowed: 'No tiene permiso para usar las herramientas de diseno',
  queryLanguage: 'Lenguaje de consulta',
  query: 'Consulta',
  datasourceId: 'Id de la fuente de datos (vacio para la base de datos de la aplicacion)',
  parameters: 'Parametros (JSON)',
  preview: 'Vista previa',
  previewTruncated: 'Hay mas filas de las que muestra la vista previa',
  exportDefinition: 'Exportar definicion',
  importDefinition: 'Importar definicion',
  importDone: 'Informe importado con id',
  reportId: 'Id del informe',
  testDatasource: 'Probar fuente de datos',
};

/** Merges custom texts over the English ones. */
export function resolveLabels(custom?: Partial<ReportLabels>): ReportLabels {
  return { ...englishLabels, ...custom };
}
