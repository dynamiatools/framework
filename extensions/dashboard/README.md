[![Maven Central](https://img.shields.io/maven-central/v/tools.dynamia.modules/tools.dynamia.modules.dashboard)](https://search.maven.org/search?q=tools.dynamia.modules.dashboard)
![Java Version Required](https://img.shields.io/badge/java-25-blue)


# Dashboard Module

With this [DynamiaTools](https://dynamia.tools) extension you can create beautiful dashboards with custom widgets,
responsive design, charts, and more.

![Screenshot](https://raw.githubusercontent.com/dynamiatools/module-dashboard/master/screenshots/dashboard.png)

## Installation

Add the following dependencies to project classpath

**Maven**

```xml

<dependency>
    <groupId>tools.dynamia.modules</groupId>
    <artifactId>tools.dynamia.modules.dashboard</artifactId>
    <version>26.2.2</version>
</dependency>
```

**Gradle**

```groovy
compile 'tools.dynamia.modules:tools.dynamia.modules.dashboard:26.2.2'
```

## Usage

Create a view descriptor of type `dashboard` and add as fields all the widgets you want to show. This descriptor works
like the `FormView` descriptor

```yaml
view: dashboard
id: mainDashboard

fields:

  monthSales:
    params:
      widget: sales-chart
      type: month

  totalSales:  #field
    params:
      widget: total-sales #widget id
      range: lastMonth #custom widget params

  newProducts:
    params:
      widget: products-table
      type: NEW

  newCustomers:
    params:
      widget: customers-table
      type: NEW
```

Install the dashboard using a `ViewerPage` in your `ModuleProvider`

```java
import tools.dynamia.integration.sterotypes.Provider;
import tools.dynamia.navigation.Module;
import tools.dynamia.navigation.ModuleProvider;
import tools.dynamia.navigation.Page;
import tools.dynamia.zk.viewers.ViewerPage;

@Provider
public class MyApplication implements ModuleProvider {

    @Override
    public Module getModule() {
        var mod = new Module("myApp","My App");
        //(pageId, name, dashboardId) <---
        mod.addPage(new ViewerPage("sales-dashboard","Sales Dashboard","mainDashboard"));
        
        return mod;
    }
}
```

Widgets are created using a classes that implements `DashboardWidget` interface or 
extends `AbstractDashboardWidget` class for convenience. You should annotate this class with
`@InstallDashboardWidget`


Example: 
```java

import tools.dynamia.modules.dashboard.AbstractDashboardWidget;
import tools.dynamia.modules.dashboard.DashboardContext;
import tools.dynamia.modules.dashboard.InstallDashboardWidget;
import tools.dynamia.zk.ui.Infobox;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * Dashboard Widget for total sales. It return an Infobox component
 * Instance of this class are created when the dashboard is rendered and live
 * all the time that the page of the dashboard is open.
 */
@InstallDashboardWidget
public class TotalVentasDashboardWidget extends AbstractDashboardWidget<Infobox> {

    /**
     * Data for this widget. 
     */
    private BigDecimal totalSales;
    private Long numberSales;

    /**
     * Customize some widget properties here
     */
    public TotalVentasDashboardWidget() {
        setTitle("Total Sales");
    }

    /**
     * This is the widget id used in view descriptor
     * @return widgetId
     */
    @Override
    public String getId() {
        return "total-sales"; 
    }

    /**
     * Init the widget data. You can use context to get access to
     * widget configuration
     * @param context
     */
    @Override
    public void init(DashboardContext context) {
        // param from view descriptor
        var range = (String)context.getField().getParam("range");
        
        if("lastMonth".equals(range)){
            this.totalSales = doSomeCalculations();
            this.numberSales = doSomeCalculations();
        }
    }

    /**
     * This is the view of the widget. Can be any ZK component or custom 
     * compnents
     * @return
     */
    @Override
    public Infobox getView() {
        var infobox = new Infobox();
        infobox.setIcon("fa fa-tags");
        infobox.setBackground("bg-primary color-white");
        infobox.setText("Total Sales");
        if (totalSales != null && numberSales != null) {
            infobox.setNumber(DecimalFormat.getCurrencyInstance().format(totalSales));
            infobox.setProgressDescription(numberSales + " sales");
        }
        infobox.setShowProgress(true);
        if (numberSales > 0) {
            infobox.setProgress(100);
        }

        return infobox;
    }
}
```

When widgets are reinitialized the `init(context)` method is invoked again and when is
re-rendered the `getView()` method is invoked. 

## License

DynamiaTools Dashboard is available under Apache 2 License
