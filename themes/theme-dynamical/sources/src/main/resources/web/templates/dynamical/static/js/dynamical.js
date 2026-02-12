zk.afterMount(function () {


    var skin = $("meta[name=skin]");
    $("body").attr("class", skin[0].content + " layout-fixed sidebar-expand-lg sidebar-mini bg-body-tertiary sidebar-open");

    setTimeout(function () {
        zWatch.fireDown("onSize", '');
    }, 500)

    $('a.sidebar-toggle').click(function () {
        setTimeout(function () {
            zWatch.fireDown("onSize", '');
        }, 300)
    });

    const customEvent = new Event('DOMContentLoaded');
    document.dispatchEvent(customEvent);
});

