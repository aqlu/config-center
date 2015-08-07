Ext.onReady(function () {
    if (Ext.isIE) {
        alert("oh my god! \nIE已经被out啦，请下载Chrome或Firefox后继续使用！");
        window.close();
        return;
    }

    Ext.QuickTips.init();

    var loginWin = new LoginWindow();
    loginWin.show();
});