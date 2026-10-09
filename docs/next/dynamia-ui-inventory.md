# Dynamia UI: action inventory

**Status:** measurement, 2026-10-09, branch `next` of `dynamia-tools` and `dynamia-erp`. Companion of [dynamia-ui.md](dynamia-ui.md).

## How it was measured (and how far to trust it)

Static scan of every Java source outside `src/test`, `target` and `node_modules`. An **action** is a concrete class whose
superclass chain reaches `AbstractAction` (`AbstractCrudAction`, `AbstractLocalAction`, `AbstractClassAction`, remote and
flow actions...) or that carries `@InstallAction`. Abstract bases are counted separately. Renderers are excluded.

An action is **ZK-bound** when it, or any ancestor, imports `org.zkoss.*` or `tools.dynamia.zk.*`. Each bound action is
then classified by what it uses (grep on its source and its ZK ancestors):

| Bucket | Meaning | What it takes |
|---|---|---|
| **FREE** | No ZK in the class or its ancestors | Review, then mark `HeadlessCapable` / `runtime=HEADLESS`. No code change |
| **EASY** | Only messages, prompts, files, progress or navigation | Swap the ZK lines for existing or small facades (`UIMessages`, `UIFiles`, `UIProgress`, `UINavigation`) |
| **MEDIUM** | Also shows a form / table / selector in a dialog | Needs `UIViews.showForm/showView` (or `UIChoices`); logic stays |
| **HARD** | Builds ZK widgets by hand or uses a custom ZK action renderer | Not a line swap: client hint (`runtime=CLIENT`), a descriptor-based rewrite of the dialog, or `ZK_ONLY` |

Limits: it is a heuristic. It does not follow calls into ZK-dependent helper services; "FREE" does not mean "safe to
publish" (restrictions, non-determinism and side effects before a question still have to be reviewed); `Window` and
widget patterns can match text that is not a real use; an action is classified by its worst need. Treat the numbers as
+/- 10 %, and the lists below as the starting point of a review, not its result.

## Totals

| | Concrete actions | FREE | EASY | MEDIUM | HARD | Abstract bases |
|---|---|---|---|---|---|---|
| `dynamia-tools` | 65 | 30 | 10 | 11 | 14 | 18 |
| `dynamia-erp` | 366 | 131 | 65 | 99 | 71 | 18 |
| **both** | **431** | **161** (37 %) | **75** (17 %) | **110** (25 %) | **85** (19 %) | |

Reading: **about 60 % of all actions (FREE + EASY) can run without ZK with no more than replacing a handful of lines**;
about a quarter more (MEDIUM) need one new facade, the form/view dialog; the HARD tail (~20 %) is where
`ZK_ONLY` and client hints are the honest answer.

Only 10 concrete actions are marked `HeadlessCapable` today (`SaveAction`, `DeleteAction`, `SaveAndNewAction`,
`SaveAndEditAction` in tools; six `Anular*Action` in the ERP: ventas, compras, cuentas, caja), so most of the FREE bucket
is a classification and review job, not a coding one. The ERP `Anular*` actions are the proof that the approach already
works on ERP code.

## What the bound actions need

Frequency among the ZK-bound actions (an action can need several):

| Need | tools | erp | Facade that covers it |
|---|---|---|---|
| message / question (`Messagebox`, `ZKUtil.showMessage`) | 5 | 45 | `UIMessages` (exists) |
| value prompt | 0 | 9 | `UIMessages.showInput` (exists) |
| dialog window (`ZKUtil.showDialog`, `Window`) | 14 | 79 | `UIViews` (new) |
| form / table / `Viewer` inside a dialog | 14 | 91 | `UIViews` (new) |
| list selector | 1 | 11 | `UIChoices` (new) |
| file download / upload | 8 | 26 | `UIFiles` (new) |
| long operation / busy indicator | 7 | 18 | `UIProgress` (new) |
| navigation / redirect | 2 | 28 | `UINavigation` (new) |
| client-side JavaScript | 0 | 2 | client hint / `ZK_ONLY` |
| ZK widgets built by hand (layouts, listbox, combobox...) | 8 | 30 | client hint / `ZK_ONLY` / rewrite as descriptor |
| custom ZK action renderer (date range, find box, combobox in the toolbar) | 9 | 42 | client hint (action input widget) |
| ZK event thread / event queue | 2 | 9 | goes away with the above |

Two facts stand out:

1. **`UIMessages` and `UIMessages.showInput` already cover the message and prompt needs of about 50 actions** (`Messagebox.show` appears 136
   times in the ERP): a mechanical replacement that needs no new design.
2. **The biggest remaining need is one new facade, `UIViews`**: about 100 actions show a form or table in a dialog
   (`ZKUtil.showDialog` is called 143 times, `Viewer` is imported by 59 actions). It is the facade to build first.

## Corrections found while implementing

- `ExportCSVAction`, `ExportExcelAction`, `ExportJsonAction` (`platform/ui/zk`) were counted EASY (files + progress). They
  are not: they read the data from ZK's `CrudController` (query result, `TreeModel`). They are `runtime=CLIENT` candidates
  (a front end exports what it already shows), not conversion targets.
- `ReloadEntityFileStoragesAction` was ZK-bound only by unused imports (FREE after cleaning them).

## Where the work is: leverage points

Abstract bases that carry ZK for many subclasses. Fixing the base converts all of its subclasses at once:

| Base | Repo | ZK-bound subclasses it carries | Its need |
|---|---|---|---|
| `VentaAction` | erp | 15 | VIEWER |
| `TableViewRowAction` | erp | 6 | WIDGET |
| `AbstractCrearCuentaRapidaAction` | erp | 4 | VIEWER |
| `CompraAction` | erp | 4 | VIEWER |
| `ViewDataAction` | erp | 3 | DIALOG, VIEWER, WIDGET |
| `AbstractRegistrarPagoCuentaAction` | erp | 3 | RENDERER |
| `AbstractExportAction` | tools | 1 | FILES |
| `ImportAction` | erp | 1 | RENDERER |
| `ImportExcelAction` | erp | 1 | FILES, MSG, RENDERER |
| `AbstractPedidoAction` | erp | 1 | DIALOG |

Also: `ViewDataAction`, `ExportExcelAction` and `TableViewRowAction` (all in `tools.dynamia.zk`) are **used by
composition** by ERP actions (`VerTerceroAction`, `ExportarItemInventarioAction`...); moving them behind facades helps
those too.

## Structural blocker: actions live in ZK-dependent modules

The ERP keeps its actions in `*-ui` modules that depend on `tools.dynamia.zk` (e.g. `modulo-ventas/ui`, `pom.xml` depends
on `tools.dynamia.zk`). **128 of the 131 FREE ERP actions are in modules named `*-ui`** (the dependency was checked in `modulo-ventas/ui`; check the
rest when planning). So even a FREE action is not available
to a ZK-free deployment: the module is not on the classpath, or drags ZK in. In `dynamia-tools` the picture is better
(`platform/core/crud`, `extensions/*/core` already hold the FREE ones), thanks to the work of epic #130.

Consequence for the plan: the module split is part of the work, not a detail.

- Move FREE and converted actions to the module's `core` (or `api`) artifact, keep ZK-only ones in `ui`.
- Keep the package and class names (Spring bean names, `@InstallAction` ids, restrictions and saved configurations do
  not change); only the Maven module changes.
- Same technique as MIGRATION_ZK_SEPARATION.md section 2 for the extensions.

## `dynamia-tools`: every ZK-bound action

| Action | Module | Bucket | Needs |
|---|---|---|---|
| `LongOperationActionDemo` | `examples/demo-zk-books` | EASY | PROGRESS |
| `DownloadFileAction` | `extensions/entity-files/sources/ui` | EASY | NAV |
| `ReloadEntityFileStoragesAction` | `extensions/entity-files/sources/ui` | EASY | PROGRESS |
| `ViewFileURLAction` | `extensions/entity-files/sources/ui` | EASY | MSG |
| `ExportReportAction` | `extensions/reports/sources/ui` | EASY | FILES |
| `ImportReportAction` | `extensions/reports/sources/ui` | EASY | FILES |
| `TestReportDatasourceAction` | `extensions/reports/sources/ui` | EASY | MSG |
| `ExportCSVAction` | `platform/ui/zk` | EASY | FILES, PROGRESS |
| `ExportExcelAction` | `platform/ui/zk` | EASY | FILES, PROGRESS |
| `SendHttpRequestViewAction` | `platform/ui/zk` | EASY | EVENT, NAV |
| `TestSMSAction` | `extensions/email-sms/sources/ui` | MEDIUM | DIALOG, MSG, VIEWER |
| `MoveEntityFileLocalToRemoteStorageAction` | `extensions/entity-files/sources/ui` | MEDIUM | CHOOSE, PROGRESS |
| `NewDirectoryAction` | `extensions/entity-files/sources/ui` | MEDIUM | VIEWER |
| `NewFileAction` | `extensions/entity-files/sources/ui` | MEDIUM | FILES, VIEWER |
| `TestHttpFunctionAction` | `extensions/http-functions/sources/ui` | MEDIUM | DIALOG, MSG, VIEWER |
| `NewAccountPaymentAction` | `extensions/saas/sources/ui` | MEDIUM | DIALOG, VIEWER |
| `ViewAccountLogAction` | `extensions/saas/sources/ui` | MEDIUM | DIALOG, VIEWER |
| `ViewAccountPayments` | `extensions/saas/sources/ui` | MEDIUM | DIALOG, VIEWER |
| `ViewAccountStatsAction` | `extensions/saas/sources/ui` | MEDIUM | DIALOG, VIEWER |
| `ResetPasswordAction` | `extensions/security/sources/ui` | MEDIUM | DIALOG, VIEWER |
| `ExportJsonAction` | `platform/ui/zk` | MEDIUM | FILES, PROGRESS, VIEWER |
| `FilterBookByPublishDateAction` | `examples/demo-zk-books` | HARD | RENDERER |
| `PreviewEmailTemplateAction` | `extensions/email-sms/sources/ui` | HARD | DIALOG, FILES, WIDGET |
| `TestEmailAccountAction` | `extensions/email-sms/sources/ui` | HARD | DIALOG, MSG, PROGRESS, RENDERER, VIEWER |
| `GenerateImportFormatAction` | `extensions/file-importer/sources/ui` | HARD | FILES, RENDERER |
| `FilterAccountByRegionAction` | `extensions/saas/sources/ui` | HARD | RENDERER, WIDGET |
| `ShowAccountAdminActions` | `extensions/saas/sources/ui` | HARD | DIALOG, RENDERER, WIDGET |
| `ViewAccountLogoAction` | `extensions/saas/sources/ui` | HARD | DIALOG, WIDGET |
| `CallZKGlobalCommandViewAction` | `platform/ui/zk` | HARD | WIDGET |
| `ExportAction` | `platform/ui/zk` | HARD | RENDERER |
| `FiltersAction` | `platform/ui/zk` | HARD | DIALOG, EVENT, RENDERER, VIEWER, WIDGET |
| `FindAction` | `platform/ui/zk` | HARD | RENDERER, VIEWER |
| `SaveConfigAction` | `platform/ui/zk` | HARD | RENDERER |
| `ViewDataAction` | `platform/ui/zk` | HARD | DIALOG, VIEWER, WIDGET |
| `ViewReportParametersAction` | `platform/ui/zk` | HARD | DIALOG, WIDGET |

FREE in tools (30): `ActionPlaceholder`, `ApplyDiscountAction`, `CancelAction`, `ClearAccountCacheAction`, `ClearEntityFileCacheAction`, `DeleteAction`, `DeleteFileAction`, `EditAction`, `EditProfileAction`, `FastAction`, `FastCrudAction`, `FileAction`, `FilterBookByBuyDateAction`, `HeadlessCrudRemoteAction`, `MarkOutOfStockAction`, `NewAction`, `NewProfileAction`, `QuickEditPricingAction`, `RateBookAction`, `ReinitAccountAction`, `ReloadDashboardAction`, `ResetAccountBalanceAction`, `SaveAccountFeaturesAction`, `SaveAction`, `SaveAndEditAction`, `SaveAndNewAction`, `SetPreferredAccountAction`, `SetUserProfilesAction`, `SomeGlobalAction`, `ViewReportAction`.

Notes: `ViewDataAction`, `FindAction`, `FiltersAction`, `SaveConfigAction`, `ExportAction` (module `platform/ui/zk`) are
the ZK implementations of toolbar behaviours; they are `runtime=CLIENT` candidates (each front end implements find /
filters / export once), not conversion targets. The 20 ZK-importing `CrudAction`s of UI_PORTS_FOR_ACTIONS.md are a
subset of the table above.

## `dynamia-erp` by module

| Module | Actions | FREE | EASY | MEDIUM | HARD |
|---|---|---|---|---|---|
| `sources/modulos/modulo-ventas/ui` | 65 | 20 | 11 | 18 | 16 |
| `sources/modulos/modulo-inventario/control-ui` | 48 | 16 | 8 | 14 | 10 |
| `sources/modulos/modulo-compras/ui` | 41 | 15 | 6 | 14 | 6 |
| `sources/modulos/modulo-restaurante/ui` | 28 | 9 | 4 | 10 | 5 |
| `sources/modulos/modulo-cuentas/ui` | 27 | 9 | 4 | 9 | 5 |
| `sources/modulos/modulo-inventario/ui` | 23 | 12 | 5 | 4 | 2 |
| `sources/modulos/modulo-contabilidad/ui` | 19 | 10 | 5 | 1 | 3 |
| `sources/erp/erp-pagos-ui` | 19 | 10 | 3 | 3 | 3 |
| `sources/modulos/modulo-caja/ui` | 17 | 11 | 1 | 2 | 3 |
| `sources/modulos/modulo-face/ui` | 14 | 2 | 6 | 2 | 4 |
| `sources/modulos/modulo-empresa/nomina-ui` | 9 | 1 | 5 | 1 | 2 |
| `sources/modulos/modulo-localizacion/ui` | 8 | 3 | 1 | 4 | 0 |
| `sources/modulos/modulo-pagos/ui` | 8 | 4 | 0 | 2 | 2 |
| `sources/modulos/modulo-seguridad/ui` | 8 | 2 | 2 | 4 | 0 |
| `sources/modulos/modulo-terceros/ui` | 6 | 0 | 0 | 3 | 3 |
| `sources/erp/erp-ui` | 6 | 2 | 2 | 1 | 1 |
| `sources/erp/erp-soporte-ui` | 6 | 0 | 0 | 4 | 2 |
| `sources/modulos/modulo-ventas/comisiones-ui` | 5 | 0 | 1 | 2 | 2 |
| `sources/erp/erp-ecommerce` | 5 | 3 | 1 | 0 | 1 |
| `sources/modulos/modulo-comentarios/ui` | 1 | 0 | 0 | 1 | 0 |
| `sources/modulos/modulo-ventas/agenda-ui` | 1 | 1 | 0 | 0 | 0 |
| `sources/modulos/modulo-inventario/pedidos-ui` | 1 | 0 | 0 | 0 | 1 |
| `sources/modulos/modulo-documentos/ui` | 1 | 1 | 0 | 0 | 0 |

## `dynamia-erp`: the non-FREE actions, by module

(FREE actions are the ones not listed; their count is in the table above.) Format: `Action` (needs).

### `sources/modulos/modulo-ventas/ui`

- **EASY:** `AnularVentaAction` (msg), `ExportarVentaPDFAction` (files), `GuardarVentaAction` (msg), `RecalcularVentasAction` (files), `ReenviarFaceAction` (msg), `ReenviarFaceAdminAction` (via base: progress), `UsarPuntosClienteAction` (msg), `VerPuntoVentaAction` (nav), `VerTareasClienteAction` (via base: nav), `VerificarCUFEsFaceAdminAction` (via base: progress), `VerificarSalidaVentaAction` (msg)
- **MEDIUM:** `AgregarDescuentoVentaAction` (via base: dialog,viewer), `AgregarImpuestoFijoVentaAction` (via base: choose,viewer), `AgregarRetencionVentaAction` (dialog,event,viewer), `BorrarDescuentoVentaAction` (via base: viewer), `BorrarDetalleVentaAction` (via base: viewer), `BorrarRetencionVentaAction` (dialog,event,viewer), `CapturarSerialesVentaAction` (via base: viewer), `ConsultarPrecioVentaAction` (input,msg,viewer), `ConvertirVariasVentasAction` (via base: dialog,viewer), `ConvertirVentaAction` (choose,msg), `EditarClienteAction` (via base: dialog,viewer), `EditarDescuentoVentaAction` (via base: dialog,viewer), `EditarDetalleVentaAction` (via base: dialog,viewer), `NuevoDetalleVentaAction` (via base: dialog,viewer), `PagarVentaAction` (via base: dialog), `SalirVentaAction` (via base: viewer), `SeleccionarVendedorDetalleAction` (via base: choose,viewer), `VerClienteAction` (via base: dialog,viewer)
- **HARD:** `AddVendibleAction` (via base: dialog,nav,widget), `AgregarDetalleVentaAction` (via base: renderer,viewer), `CambioRapidoVentaAction` (dialog,viewer,widget), `DevolucionRapidoVentaAction` (dialog,viewer,widget), `ExportarVentaPDFRowAction` (via base: widget), `FiltrarConsultaEntreFechaAction` (via base: renderer), `FiltrarFechaVentaAction` (via base: renderer), `FiltrarVentaPorNumeroAction` (via base: renderer), `ImprimirVentaAction` (dialog,event,files,js,widget), `NuevoClienteAction` (dialog,viewer,widget), `VentaPOSFullscreenAction` (js,viewer), `VerCalcularCUFEAction` (dialog,widget), `VerImportadorCarteraAction` (dialog,files,msg,progress,renderer), `VerUltimasVentaClienteAction` (via base: dialog,renderer,viewer), `VerVentaAction` (via base: dialog,renderer,viewer,widget), `VerVentaHoyAction` (dialog,renderer,viewer)

### `sources/modulos/modulo-inventario/control-ui`

- **EASY:** `CopiarMovimientoInventarioAction` (via base: nav), `EditarMovimientoInventarioAction` (via base: nav), `ExportarConteoInventarioExcelAction` (via base: files,progress), `ExportarConteoInventarioPDFAction` (files), `NuevoMovimientoInventarioAction` (via base: nav), `ProcesarTrasladoInventarioAction` (msg), `SalirMovimientoInventarioAction` (via base: nav), `VerificarVariacionesItemAction` (via base: progress)
- **MEDIUM:** `CapturarCodigoBarraVariacionItemAction` (via base: input,viewer), `EditarDetalleMovimientoInventarioAction` (via base: dialog,viewer), `EditarDetalleTrasladoInventarioAction` (via base: dialog,viewer), `ImprimirStickersMovimientoAction` (choose), `NuevoItemInventarioRapidoAction` (dialog,msg,viewer), `ProcesarMovimientoInventarioAction` (dialog,msg), `SalidaRapidaSerialesAction` (dialog,event), `VerExistenciasItemAction` (via base: dialog), `VerImportadorItemCompuestosInventarioAction` (dialog), `VerImportadorItemInventarioAction` (dialog), `VerImportadorPresentacionesItemAction` (via base: dialog,input), `VerImportadorSerialesItemAction` (via base: dialog,input), `VerLogMovimientoInventarioAction` (via base: dialog,viewer), `VerSerialesDetalleMovimientoAction` (dialog,viewer)
- **HARD:** `CapturarItemConteoInventarioAction` (via base: renderer), `CapturarItemMovimientoInventarioAction` (via base: renderer), `CapturarItemTrasladoInventarioAction` (via base: renderer), `CapturarSerialMovimientoInventarioAction` (widget), `FiltrarFechaTrasladosInventarioAction` (via base: renderer), `FiltrarItemPorCodigoAction` (via base: renderer), `FiltroConteoInventarioPorFechaAction` (via base: renderer), `FiltroMovimientosPorFechaAction` (via base: renderer), `ImportarExcelItemAction` (files,input,msg,progress,renderer), `VerMovimientoInventarioAction` (dialog,viewer,widget)

### `sources/modulos/modulo-compras/ui`

- **EASY:** `AceptarDocRecibidosAction` (msg), `CopiarCompraAction` (via base: nav), `EditarCompraAction` (via base: nav), `NuevaCompraAction` (via base: nav), `RechazarDocRecibidosAction` (msg), `SincronizarDocRecibidosAction` (msg,progress)
- **MEDIUM:** `AcuceReciboDocRecibidosAction` (msg,progress,viewer), `AgregarDescuentoCompraAction` (via base: dialog,viewer), `AgregarDetalleCompraAction` (via base: dialog,viewer), `AgregarRetencionCompraAction` (via base: viewer), `BorrarRetencionCompraAction` (via base: viewer), `ConfirmarAcuceReciboDocRecibidosAction` (msg,progress,viewer), `EditarDescuentoCompraAction` (via base: dialog,viewer), `EditarDetalleCompraAction` (via base: dialog,viewer), `EditarProveedorAction` (via base: viewer), `NuevaEntradaRapidaAction` (msg,viewer), `NuevoProveedorAction` (via base: viewer), `SeleccionarTodoAction` (via base: viewer), `VerEventosRecepcionAction` (via base: viewer), `VerImportadorProveedoresAction` (via base: dialog)
- **HARD:** `BuscarPorNumeroDocumentoRecibidoAction` (via base: renderer), `ExportarCompraRowAction` (via base: widget), `FiltrarFechaCompraAction` (via base: renderer), `ImportarProveedoresAction` (via base: files,msg,renderer), `VerCompraDevolucionAction` (via base: dialog,viewer,widget), `VerPDFDocRecibidosAction` (dialog,files,widget)

### `sources/modulos/modulo-restaurante/ui`

- **EASY:** `CerrarOrdenMesaAction` (msg), `DescuentoDetalleOrdenAction` (via base: input), `GotoKDSAction` (nav), `PagarOrdenMesaAction` (msg)
- **MEDIUM:** `AnularDetalleOrdenMesaAction` (via base: choose), `AnularOrdenMesaAction` (choose,msg), `BuscarProductoOrdenAction` (dialog,event), `CambiarMesasAmbienteAction` (via base: viewer), `CambiarOrdenMesaAction` (via base: viewer), `DividirCuentaAction` (via base: dialog), `EditarDetalleOrdenAction` (dialog,event,viewer), `GestionMesasPuntoVentaAction` (via base: dialog), `MoverDetalleMesaAction` (via base: choose), `ProbarConfigKDSAction` (via base: viewer)
- **HARD:** `DescuentoOrdenMesaAction` (input,viewer,widget), `FiltrarOrdenMesaPorNumeroAction` (via base: renderer), `FiltrarSucursalRestauranteAction` (via base: renderer), `FinalizarOrdenMesaAction` (via base: renderer), `ImportarVendedoresAction` (via base: choose,renderer)

### `sources/modulos/modulo-cuentas/ui`

- **EASY:** `AnularPagoCuentasTerceroAction` (msg), `ReenviarNotaCreditoElectronicaAction` (msg), `ReenviarNotaCreditoElectronicaAdminAction` (via base: progress), `ReenviarNotaDebitoElectronicaAction` (msg)
- **MEDIUM:** `AnularPagosCuentaAction` (via base: dialog,viewer), `AplicarDescuentoManualCXCAction` (via base: viewer), `CambiarCategoriaCuentaAction` (via base: choose,viewer), `CrearAnticipoEnviadoAction` (via base: viewer), `CrearAnticipoRecibidoAction` (via base: viewer), `CrearCXCRapidaAction` (via base: viewer), `CrearCXPRapidaAction` (via base: viewer), `NuevoPrestamoRapidoAction` (dialog,viewer), `VerLogsCuentaAction` (dialog,viewer)
- **HARD:** `FiltrarCuentaPorCodigoAction` (via base: renderer), `FiltrarPagosCuentaPorFechaAction` (via base: renderer), `PagarCuentaCobrarAction` (via base: renderer), `PagarCuentaPagarAction` (via base: renderer), `PagarNotaCreditoAction` (via base: renderer)

### `sources/modulos/modulo-face/ui`

- **EASY:** `ActualizarCuentaEnvioEmailAction` (via base: msg,progress), `GestionarEmisorAction` (via base: nav), `VerificarCertificadosActualesAction` (via base: msg,progress), `VerificarEstadoDocumentoAction` (msg), `VerificarIdsEmisoresAction` (via base: progress), `VerificarPruebaHabilitacionAction` (msg)
- **MEDIUM:** `ValidarDocumentoEnviadoAction` (dialog,msg,viewer), `VerLogEmisorAction` (dialog,viewer)
- **HARD:** `DescargarJsonLogAction` (files,widget), `EditRowAction` (via base: viewer,widget), `FiltrarDocsPorEmisorAction` (via base: renderer), `VerPDFDocumentoRecibidoAction` (dialog,files,widget)

### `sources/modulos/modulo-inventario/ui`

- **EASY:** `CapturarCodigoBarraItemAction` (via base: input), `DesvincularTodosItemsInventarioAction` (msg), `ExportarItemInventarioAction` (via base: files,progress), `ExportarLineasInventarioAction` (via base: files,progress), `VerificarItemSinCodigoAdminAction` (msg)
- **MEDIUM:** `CambiarFotoItemInventarioAction` (files,viewer), `ConfigurarProveedoresAdicionalesItemAction` (via base: dialog), `ExportarCatalogoProveedorAction` (files,viewer), `VerImportadorLineasInventarioAction` (via base: dialog)
- **HARD:** `FiltrarProveedorItemInventarioAction` (via base: renderer,viewer), `ImportarExcelLineaAction` (files,renderer)

### `sources/modulos/modulo-contabilidad/ui`

- **EASY:** `CopiarComprobanteContableAction` (via base: nav), `EditarPlantillaProcesoContableAction` (via base: msg,nav), `ExportarCuentasContableAction` (files), `ExportarPlantillaContableAction` (files), `ImportarPlantillaContableAction` (files)
- **MEDIUM:** `AutoGenerarComprobantesContablesAction` (msg,progress,viewer)
- **HARD:** `BorrarDetalleComprobanteContableAction` (via base: widget), `FiltrarComprobantePorFechasAction` (via base: renderer), `VerComprobanteContableAction` (via base: dialog,viewer,widget)

### `sources/erp/erp-pagos-ui`

- **EASY:** `FacturaCuentaERPAction` (msg), `VerDetallesCuentaERPAction` (via base: nav), `VerLinkPagoNuevaCuentaERPAction` (nav)
- **MEDIUM:** `CancelarCuentaAction` (via base: viewer), `EnviarEmailCuentasERPAction` (dialog,progress,viewer), `GenerarCuponesERPAction` (via base: viewer)
- **HARD:** `FiltrarFacturaCuentaERPAction` (via base: renderer), `ReenviarLinkPagoAction` (via base: widget), `VerLinkPagoFacturaCuentaERPAction` (nav,widget)

### `sources/modulos/modulo-empresa/nomina-ui`

- **EASY:** `AnularNominaAction` (msg), `CopiarNominaAction` (via base: nav), `EditarNominaAction` (via base: nav), `ReenviarNominaAjusteAction` (msg), `ReenviarNominaElectronicaAction` (msg)
- **MEDIUM:** `ConvertirVariasNominasAction` (via base: dialog,viewer)
- **HARD:** `VerPDFNominaAction` (dialog,files,widget), `VerPDFNominaAjusteAction` (dialog,widget)

### `sources/modulos/modulo-terceros/ui`

- **MEDIUM:** `ImportarTerceroRefAction` (via base: input,viewer), `NuevoTerceroAction` (via base: dialog,viewer), `VerImportadorTercerosAction` (via base: dialog)
- **HARD:** `ImportarExcelTercerosAction` (files,renderer), `ImportarExcelTercerosRefAction` (files,renderer), `VerTerceroAction` (via base: dialog,viewer,widget)

### `sources/modulos/modulo-seguridad/ui`

- **EASY:** `CambiarseSucursalRapidoAction` (nav), `RotarSecretoClienteOAuth2Action` (msg)
- **MEDIUM:** `AutologinAdminAccountAction` (choose,nav), `CambiarUsuarioDeSucursalAction` (dialog,event,viewer), `ReiniciarAdminPasswordAction` (dialog,viewer), `ReiniciarPasswordAction` (dialog,viewer)

### `sources/modulos/modulo-caja/ui`

- **EASY:** `VerificarCierreCajaAction` (msg)
- **MEDIUM:** `AbrirCerrarCajaAction` (dialog,msg,viewer), `AnularMovimientoAction` (via base: dialog,viewer)
- **HARD:** `FiltrarMovimientoCajaFechaAction` (via base: renderer), `SelectorCajaAction` (nav,renderer), `VerAperturaCierreCajaAction` (via base: dialog,viewer,widget)

### `sources/erp/erp-soporte-ui`

- **MEDIUM:** `CambiarPrioridadTicketAction` (via base: viewer), `ChatSoporteAction` (dialog,nav), `SolicitarSoporteAction` (dialog,msg,viewer), `VerImportadorFAQAction` (via base: dialog)
- **HARD:** `ImportarFAQAction` (files,msg,renderer), `VerTicketAction` (dialog,viewer,widget)

### `sources/modulos/modulo-ventas/comisiones-ui`

- **EASY:** `NuevaLiquidacionComisionesAction` (via base: nav)
- **MEDIUM:** `RecalcularComisionVentaAction` (via base: viewer), `VerCruceLiquidacionAction` (via base: nav,viewer)
- **HARD:** `CalcularLiquidacionAction` (via base: renderer), `VerLiquidacionComisionesAction` (via base: dialog,nav,viewer,widget)

### `sources/modulos/modulo-localizacion/ui`

- **EASY:** `ImportGrupoLocalizacionAction` (files,progress)
- **MEDIUM:** `ExportGrupoLocalizacionAction` (choose,files), `VerImportadorBarriosAction` (via base: dialog), `VerImportadorCiudadesAction` (via base: dialog), `VerImportadorDepartamentosAction` (via base: dialog)

### `sources/modulos/modulo-pagos/ui`

- **MEDIUM:** `EditarFormaPagoAction` (dialog,viewer), `GuardarFormaPagoAction` (via base: viewer)
- **HARD:** `AgregarFormaPagoAction` (dialog,renderer,viewer,widget), `GuardarPagoAction` (via base: renderer)

### `sources/erp/erp-ui`

- **EASY:** `MiCuentaAction` (event,msg,nav), `VerificarCXCVentasAdminAction` (msg)
- **MEDIUM:** `NuevaSucursalMultiVentaAction` (via base: viewer)
- **HARD:** `KeyPadTestAction` (dialog,event,widget)

### `sources/erp/erp-ecommerce`

- **EASY:** `GotoOnlineStoreAction` (nav)
- **HARD:** `DownloadWoocommerceOrderSummaryAction` (files,widget)

### `sources/modulos/modulo-comentarios/ui`

- **MEDIUM:** `VerComentariosAction` (dialog,viewer)

### `sources/modulos/modulo-inventario/pedidos-ui`

- **HARD:** `AutorizarPedidoAction` (via base: dialog,renderer)
