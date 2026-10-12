/** Parameters passed to a function: a free-form JSON object validated by the function itself. */
export type FunctionParams = Record<string, unknown>;

/** Options of a function call. */
export interface FunctionCallOptions {
  /**
   * Version of the function to call. When omitted the server uses the highest active version.
   * Sent as the `v` query parameter.
   */
  version?: number;
}

/**
 * JSON body returned by a function that produces structured data.
 * Mirrors the `{ success, data }` map built by `DynamiaHttpFunctionsController`.
 */
export interface FunctionResponse<T = unknown> {
  success: true;
  data: T;
}
