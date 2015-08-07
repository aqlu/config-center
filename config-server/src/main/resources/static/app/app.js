Desktop = {};

Ext.onReady(function () {
    if (Ext.isIE) {
        alert("oh my god! \nIE已经被out啦，请下载Chrome或Firefox后继续使用！");
        window.close();
        return;
    }

    Ext.Ajax.on('requestcomplete', function (ajax, xhr, o) {
        if (typeof urchinTracker == 'function' && o && o.url) {
            urchinTracker(o.url);
        }
    });

    Ext.QuickTips.init();

    //var splashscreen = Ext.getBody().mask('Loading application...', 'splashscreen');
    console.log('App is init...');


    //api.expandPath('/root/apidocs');


    var task = new Ext.util.DelayedTask(function () {
            Ext.get('loading').remove();
            Ext.get('loading-mask').fadeOut({
                duration: 1,
                remove: true
            });

            var desktop = new Desktop.Viewport();
            desktop.doLayout();

            console.log('App launched');
        }
    );
    task.delay(100);
});