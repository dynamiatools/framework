// Field component registry and resolved field type for ui-core

import type {ViewField} from '@dynamia-tools/sdk';

/**
 * Maps descriptor component strings to known field component identifiers.
 * Names match ZK component names exactly for vocabulary consistency.
 * This is a const object, not an enum. External modules should define their
 * own component identifier constants following the same pattern.
 */
export const FieldComponent = {
    Textbox: 'textbox',
    Intbox: 'intbox',
    Longbox: 'longbox',
    Decimalbox: 'decimalbox',
    Spinner: 'spinner',
    Doublespinner: 'doublespinner',
    Combobox: 'combobox',
    Datebox: 'datebox',
    Dateselector: 'datebox',
    Timebox: 'timebox',
    Checkbox: 'checkbox',
    EntityPicker: 'entitypicker',
    EntityRefPicker: 'entityrefpicker',
    EntityRefLabel: 'entityreflabel',
    CrudView: 'crudview',
    CoolLabel: 'coollabel',
    EntityFileImage: 'entityfileimage',
    Link: 'link',
    EnumIconImage: 'enumiconimage',
    PrinterCombobox: 'printercombobox',
    ProviderMultiPickerbox: 'providermultipickerbox',
    Textareabox: 'textareabox',
    HtmlEditor: 'htmleditor',
    ColorPicker: 'colorpicker',
    NumberRangeBox: 'numberrangebox',
    ImageBox: 'imagebox',
    SliderBox: 'sliderbox',
    Listbox: 'listbox',
} as const satisfies Record<string, string>;

/** All valid field component identifiers */
export type FieldComponent = (typeof FieldComponent)[keyof typeof FieldComponent];

/**
 * A fully resolved field descriptor, enriched with computed layout and component info.
 * Extends the SDK ViewField with runtime-resolved values.
 */
export interface ResolvedField extends ViewField {
    /** The resolved component identifier to use for rendering */
    resolvedComponent: FieldComponent | string;
    /** The resolved display label (localized or default) */
    resolvedLabel: string;
    /** The grid column span for layout (default 1) */
    gridSpan: number;
    /** Whether this field is currently visible */
    resolvedVisible: boolean;
    /** Whether this field is required */
    resolvedRequired: boolean;
    /** The group name this field belongs to (if any) */
    group?: string;
    /** The row index in the grid layout */
    rowIndex: number;
    /** The column index in the grid layout */
    colIndex: number;
}
