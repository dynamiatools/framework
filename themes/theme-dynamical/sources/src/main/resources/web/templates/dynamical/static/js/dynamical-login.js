function applyJqueryStuff() {


}

function onPageLoaded() {

}


zk.afterMount(function () {
    var skin = $("meta[name=skin]");
    $("body").attr("class", skin[0].content + " login-page");
    applyJqueryStuff();
});
