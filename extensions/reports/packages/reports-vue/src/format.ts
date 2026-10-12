import type { ReportColumn, ReportFilterDefinition, ReportFilterValue } from '@dynamia-tools/reports-sdk';

/** Message of an unknown error. */
export function messageOf(e: unknown): string {
  return e instanceof Error ? e.message : String(e);
}

const ISO_DATE = /^(\d{4})-(\d{2})-(\d{2})$/;

/**
 * Text to show in a table cell. Numbers and dates use the browser locale; text columns marked `upperCase` are shown
 * in capitals; booleans as a check or a cross.
 */
export function formatCell(column: ReportColumn, value: unknown, locale?: string): string {
  if (value === null || value === undefined || value === '') return '';
  let text: string;
  switch (column.dataType) {
    case 'NUMBER':
    case 'CURRENCY': {
      const n = typeof value === 'number' ? value : Number(value);
      text = Number.isFinite(n)
        ? new Intl.NumberFormat(locale, {
            maximumFractionDigits: 6,
            minimumFractionDigits: column.dataType === 'CURRENCY' ? 2 : 0,
          }).format(n)
        : String(value);
      break;
    }
    case 'BOOLEAN':
      text = value === true || value === 'true' ? '✓' : '✗';
      break;
    case 'DATE': {
      const match = ISO_DATE.exec(String(value));
      text = match
        ? new Date(Number(match[1]), Number(match[2]) - 1, Number(match[3])).toLocaleDateString(locale)
        : String(value);
      break;
    }
    case 'DATE_TIME': {
      const date = new Date(String(value));
      text = Number.isNaN(date.getTime()) ? String(value) : date.toLocaleString(locale);
      break;
    }
    default:
      text = String(value);
  }
  return column.upperCase ? text.toUpperCase() : text;
}

/**
 * Value of the filter form control for a value coming from the API. `DATE_TIME` values (`yyyy-MM-dd HH:mm:ss`) become
 * the `yyyy-MM-ddTHH:mm` an `<input type="datetime-local">` uses, `TIME` values lose the seconds.
 */
export function toInputValue(filter: ReportFilterDefinition, apiValue: string | null | undefined): string {
  if (!apiValue) return '';
  if (filter.dataType === 'DATE_TIME') return apiValue.replace(' ', 'T').slice(0, 16);
  if (filter.dataType === 'TIME') return apiValue.slice(0, 5);
  return apiValue;
}

/**
 * Value to send to the API for the value of a filter form control; the opposite of {@link toInputValue}.
 * Returns `undefined` for an empty control, so the filter is not sent.
 */
export function toApiValue(filter: ReportFilterDefinition, input: string | undefined): ReportFilterValue {
  if (input === undefined || input === null || input === '') return undefined;
  switch (filter.dataType) {
    case 'DATE_TIME': {
      const [date, time = '00:00'] = input.split('T');
      return `${date} ${time.length === 5 ? `${time}:00` : time}`;
    }
    case 'TIME':
      return input.length === 5 ? `${input}:00` : input;
    default:
      return input;
  }
}

/** File name of an export: `my-report-2026-10-08.xlsx`. */
export function exportFileName(reportName: string, format: string, date: Date = new Date()): string {
  const slug =
    reportName
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '') || 'report';
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${slug}-${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}.${format}`;
}

/** Saves a blob in the browser as a file download. */
export function downloadBlob(blob: Blob, filename: string): void {
  const url = URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  link.remove();
  setTimeout(() => URL.revokeObjectURL(url), 0);
}
