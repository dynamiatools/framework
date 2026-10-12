import { createApp } from 'vue'
import { DynamiaClient } from '@dynamia-tools/sdk'
import { DynamiaVue } from '@dynamia-tools/vue'
import { DynamiaDashboardVue, WidgetRendererRegistry } from '@dynamia-tools/dashboard-vue'
import { DynamiaReportsVue, registerReportWidget } from '@dynamia-tools/reports-vue'
import '@dynamia-tools/dashboard-vue/dashboard-vue.css'
import '@dynamia-tools/reports-vue/reports-vue.css'
import './insights.css'
import App from './App.vue'
import ReviewsFeedWidget from './ReviewsFeedWidget.vue'

export type InsightsPage = 'dashboard' | 'reports' | 'design'

// The backoffice and this module are served by the same Spring Boot app: an empty baseUrl resolves against the
// current origin and `withCredentials` sends the DYNAMIA_JWT / session cookies of the logged in user.
const client = new DynamiaClient({ baseUrl: '', withCredentials: true })

// Widget types the dashboard module serves but has no built-in renderer for:
registerReportWidget(WidgetRendererRegistry) // `report` widgets with display: table
WidgetRendererRegistry.register('reviews-feed', ReviewsFeedWidget) // the custom Java LatestReviewsWidget

/**
 * The page is chosen by the module URL: the backoffice embeds `/insights/insights.js?page=dashboard` (or `reports`).
 * <dynamia-embed> imports this module, registers the default export as a custom element and mounts it.
 */
const moduleUrl = new URL(import.meta.url)
const requested = moduleUrl.searchParams.get('page')
const page: InsightsPage = requested === 'reports' || requested === 'design' ? requested : 'dashboard'

export default class MoviesInsights extends HTMLElement {
  private dispose?: () => void

  connectedCallback() {
    const styles = document.createElement('link')
    styles.rel = 'stylesheet'
    styles.href = new URL('./insights.css', moduleUrl).href
    this.appendChild(styles)

    const root = document.createElement('div')
    this.appendChild(root)
    const app = createApp(App, { page }).use(DynamiaVue, { client }).use(DynamiaDashboardVue).use(DynamiaReportsVue)
    app.mount(root)
    this.dispose = () => {
      app.unmount()
      root.remove()
    }
  }

  disconnectedCallback() {
    this.dispose?.()
  }
}
