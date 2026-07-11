// ConfigView: module configuration parameters view

import type { ViewDescriptor, EntityMetadata } from '@dynamia-tools/sdk';
import { View } from './View.js';
import { ViewTypes } from './ViewType.js';
import type { ConfigState, ConfigParameter } from '../types/state.js';

/**
 * View implementation for module configuration parameters.
 *
 * Example:
 * <pre>{@code
 * const view = new ConfigView(descriptor, metadata);
 * await view.initialize();
 * await view.loadParameters();
 * view.setParameterValue('theme', 'dark');
 * await view.saveParameters();
 * }</pre>
 */
export class ConfigView extends View {
  protected override state: ConfigState;
  private _loader?: () => Promise<ConfigParameter[]>;
  private _saver?: (values: Record<string, unknown>) => Promise<void>;

  constructor(descriptor: ViewDescriptor, entityMetadata: EntityMetadata | null = null) {
    super(ViewTypes.Config, descriptor, entityMetadata);
    this.state = { loading: false, error: null, initialized: false, parameters: [], values: {} };
  }

  async initialize(): Promise<void> {
    this.state.initialized = true;
  }

  validate(): boolean {
    for (const param of this.state.parameters) {
      if (param.required === true && (this.state.values[param.name] === undefined || this.state.values[param.name] === null)) {
        return false;
      }
    }
    return true;
  }

  override getValue(): Record<string, unknown> { return { ...this.state.values }; }

  /** Set loader function for fetching parameters */
  setLoader(loader: () => Promise<ConfigParameter[]>): void { this._loader = loader; }

  /** Set saver function for persisting parameter values */
  setSaver(saver: (values: Record<string, unknown>) => Promise<void>): void { this._saver = saver; }

  /** Load available parameters from backend */
  async loadParameters(): Promise<void> {
    if (!this._loader) return;
    this.state.loading = true;
    try {
      this.state.parameters = await this._loader();
      this.state.initialized = true;
    } finally {
      this.state.loading = false;
    }
  }

  /** Save current parameter values to backend */
  async saveParameters(): Promise<void> {
    if (!this._saver) return;
    this.state.loading = true;
    try {
      await this._saver(this.state.values);
      this.emit('saved', this.state.values);
    } catch (e) {
      this.state.error = String(e);
      this.emit('error', e);
    } finally {
      this.state.loading = false;
    }
  }

  setParameterValue(name: string, value: unknown): void { this.state.values[name] = value; }
  getParameterValue(name: string): unknown { return this.state.values[name]; }
  getParameters(): ConfigParameter[] { return this.state.parameters; }
}
