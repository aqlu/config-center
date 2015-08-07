Desktop.MainTab = function (config) {

    var cfg = {
        region: 'center',
        //margins: '0 5 5 0',
        resizeTabs: true,
        minTabWidth: 135,
        //tabWidth: 120,
        plugins: [
            //new Ext.ux.TabCloseMenu(),
            new Ext.ux.TabScrollerMenu({
                maxText: 15,
                pageSize: 20
            })
        ],
        enableTabScroll: true,
        activeTab: 0,
        items: {
            //cmargins: '10 5 5 0',
            id: 'welcome-panel',
            tabTip: '欢迎页面',
            title: '欢迎您',
            autoLoad: {
                url: 'welcome',
                scope: this
            },
            iconCls: 'icon-blue',
            autoScroll: true
        },
        listeners: {
            'tabchange': function (tp, tab) {
                var menuPanel = Ext.getCmp('desktop.menu-panel');
                if (menuPanel) {
                    menuPanel.selectNode(tab.menuIdPath);
                }
            }
        }
    };

    var allConfig = Ext.applyIf(config || {}, cfg);

    Desktop.MainTab.superclass.constructor.call(this, allConfig);
};

Ext.extend(Desktop.MainTab, Ext.TabPanel, {

    initEvents: function () {
        Desktop.MainTab.superclass.initEvents.call(this);
    },

    // node.attributes.href, node.text, node.attributes.fullPath, node.attributes.serviceKey, node.attributes.fullText
    loadTab: function (node) {

        var appName = node.parentNode.parentNode.text;
        var version = node.parentNode.text;
        var groupName = node.text;

        var id = appName + ">" + version + ">" + groupName;

        var tab = this.getComponent(id);
        if (tab) {
            this.setActiveTab(tab);
        } else {
            var me = this;
            var p = me.add(new Desktop.PropertyEditorTab({
                id: id,
                iconCls: 'icon-yellow',
                title: groupName,
                tabTip: id,
                appName: appName,
                version: version,
                groupName: groupName
            }));
            me.setActiveTab(p);
        }
    }

});

Ext.reg('desktop.main-tab', Desktop.MainTab);