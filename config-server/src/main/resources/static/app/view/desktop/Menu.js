Desktop.MenuPanel = function (config) {

    var me = this;

    var rootContextMenu = new Ext.menu.Menu({
        items: [{
            id: 'refresh-node',
            iconCls: 'icon-refresh',
            text: '刷新菜单'
        }, '-', {
            id: 'add-app-node',
            iconCls: 'icon-add',
            text: '新增应用'
        }],
        listeners: {
            scope: this,
            itemclick: function (item) {
                this.rootMenuItemClick(item);
            }
        }
    });

    var appContextMenu = new Ext.menu.Menu({
        items: [{
            id: 'add-version-node',
            iconCls: 'icon-add',
            text: '创建新版本'
        }, '-', {
            id: 'password-app-node',
            iconCls: 'icon-changepwd',
            text: '重置密码'
        },{
            id: 'delete-app-node',
            iconCls: 'icon-delete',
            text: '删除应用'
        }],
        listeners: {
            scope: this,
            itemclick: function (item) {
                this.appMenuItemClick(item);
            }
        }
    });

    var versionContextMenu = new Ext.menu.Menu({
        items: [{
            id: 'add-group-node',
            iconCls: 'icon-add',
            text: '创建新组'
        }, '-', {
            id: 'copy-version-node',
            iconCls: 'icon-copy',
            text: '复制版本'
        }, {
            id: 'delete-version-node',
            iconCls: 'icon-delete',
            text: '删除版本'
        }],
        listeners: {
            itemclick: function (item) {
                me.versionMenuItemClick(item);
            }
        }
    });

    var groupContextMenu = new Ext.menu.Menu({
        items: [{
            id: 'delete-group-node',
            iconCls: 'icon-delete',
            text: '删除分组'
        }],
        listeners: {
            itemclick: function (item) {
                me.groupMenuItemClick(item);
            }
        }
    });

    // 菜单树
    var treePanel = new Ext.tree.TreePanel({
        id: 'menu-tree',
        region: 'center',
        margins: '0 0 5 5',
        cmargins: '0 0 0 0',
        rootVisible: false,
        lines: true,
        autoScroll: true,
        animCollapse: false,
        animate: true,
        enableDD: true,
        containerScroll: true,
        root: new Ext.tree.AsyncTreeNode({
            text: 'RootMenu',
            id: 'root',
            singleClickExpand: true,
            expanded: true
        }),
        loader: new Ext.tree.TreeLoader({
            preloadChildren: true,
            clearOnLoad: true,
            method: 'get',
            dataUrl: '/menu/tree.do'
        }),
        collapseFirst: false,
        listeners: {
            click: function (node, e) {
                if (node.isLeaf()) {
                    e.stopEvent();

                    var mainTab = Ext.getCmp('desktop.main-tab');
                    if (mainTab) {
                        mainTab.loadTab(node);
                    }
                }
            },
            contextmenu: function (node, e) {
                node.select();
                var c;
                switch (node.attributes.nodeClass) {
                    case "ROOT" :
                        c = rootContextMenu;
                        c.contextNode = node;
                        c.showAt(e.getXY());
                        break;
                    case "APP" :
                        c = appContextMenu;
                        c.contextNode = node;
                        c.showAt(e.getXY());
                        break;
                    case "VERSION" :
                        c = versionContextMenu;
                        c.contextNode = node;
                        c.showAt(e.getXY());
                        break;
                    case "GROUP" :
                        c = groupContextMenu;
                        c.contextNode = node;
                        c.showAt(e.getXY());
                        break;
                }
            }
        }
    });

    //treePanel.getSelectionModel().on('beforeselect', function (sm, node) {
    //    return node.isLeaf();
    //});

    // 工具条
    var toolbar = new Ext.Toolbar({
        region: 'north',
        height: 30,
        cls: 'top-toolbar',
        items: [' ', new Ext.form.TextField({
            width: 150,
            emptyText: '快速查找 ...',
            listeners: {
                scope: this,
                render: function (f) {
                    f.el.on('keydown', me.filterTree, me, {
                        buffer: 350
                    });
                }
            }
        }), ' ', ' ', {
            iconCls: 'icon-expand-all',
            tooltip: '展开所有',
            handler: function () {
                treePanel.root.expand(true);
            }
        }, '-', {
            iconCls: 'icon-collapse-all',
            tooltip: '折叠所有',
            handler: function () {
                treePanel.root.collapse(true);
            }
        }]
    });

    this.menuFilter = new Ext.tree.TreeFilter(treePanel, {
        clearBlank: true,
        autoClear: true
    });

    this.hiddenPkgs = [];


    var cfg = {
        region: 'west',
        split: true,
        width: 220,
        minSize: 220,
        maxSize: 500,
        collapseMode: 'mini',
        layout: 'border',
        border: false,
        items: [toolbar, treePanel]
    };

    this.treePanel = treePanel;
    //this.treePanel.expandAll();

    var allConfig = Ext.applyIf(config || {}, cfg);
    Desktop.MenuPanel.superclass.constructor.call(this, allConfig);


};

Ext.extend(Desktop.MenuPanel, Ext.Panel, {
    reload: function(node){
        var treePanel = this.treePanel;
        var path = "";
        if(node){
            path = node.getPath();
        }

        //重新加载树
        treePanel.getLoader().load(treePanel.getRootNode(), function(){
            if(node){
                treePanel.expandPath(path);
            }else{
                treePanel.root.expand(true);
            }
        });
    },
    selectNode: function (fullPath) {
        if (fullPath) {
            // xxx.xxx.xxx
            this.selectPath('/root/' + fullPath);
        }
    },
    filterTree: function (field) {
        if (this.treePanel) {
            var text = field.target.value;
            //var alias = e.target.alias;
            Ext.each(this.hiddenPkgs, function (n) {
                n.ui.show();
            });

            if (!text) {
                this.menuFilter.clear();
                return;
            }
            this.treePanel.expandAll();

            var re = new RegExp('^' + Ext.escapeRe(text), 'i');
            this.menuFilter.filterBy(function (n) {
                return !n.attributes.leaf || re.test(n.text) || (n.attributes.alias && re.test(n.attributes.alias));
            });

            // hide empty packages that weren't filtered
            this.hiddenPkgs = [];
            var me = this;
            this.treePanel.root.cascade(function (n) {
                if (!n.attributes.leaf && n.ui.ctNode && n.ui.ctNode.offsetHeight < 3) {
                    n.ui.hide();
                    me.hiddenPkgs.push(n);
                }
            });
        }
    },
    rootMenuItemClick: function (item) {
        var n;
        switch (item.id) {
            case 'refresh-node':
                //重新加载树
                this.reload();
                break;
            case 'add-app-node':
                n = item.parentMenu.contextNode;
                var addAppWin = Ext.getCmp("add-app-win");
                if (!addAppWin) {
                    addAppWin = new Desktop.AddAppWin({id: 'add-app-win'});
                }

                addAppWin.showWin(n);
                break;
        }
    },
    appMenuItemClick: function (item) {
        var menuTree = this;
        var n;
        switch (item.id) {
            case 'add-version-node':
                n = item.parentMenu.contextNode;
                var win = Ext.getCmp("add-version-win");
                if (!win) {
                    win = new Desktop.AddVersionWin({id: 'add-version-win'});
                }

                win.showWin(n);
                break;
            case 'password-app-node':
                n = item.parentMenu.contextNode;
                var win = Ext.getCmp("password-app-win");
                if (!win) {
                    win = new Desktop.AddAppWin({id: 'password-app-win', appName: n.text});
                }

                win.showWin(n);
                break;
            case 'delete-app-node':
                n = item.parentMenu.contextNode;
                var appName = n.text;
                Ext.Msg.show({
                    title: '确认删除?',
                    msg: '数据删除会同步到所有节点，且删除后数据不可恢复，是否确定删除？',
                    buttons: Ext.Msg.YESNO,
                    fn: function (buttonId, text, opt) {
                        if (buttonId == 'yes') {
                            Ext.Ajax.request({
                                method: 'POST',
                                url: '/config/' + appName + "/_delete",
                                success: function (response, opts) {
                                    var obj = Ext.decode(response.responseText);
                                    if (obj.success) {
                                        menuTree.reload(n.parentNode);
                                    } else {
                                        alert(obj.ex);
                                    }
                                },
                                failure: function (response, opts) {
                                    console.log('server-side failure with status code ' + response.status);
                                }
                            });
                        }
                    },
                    icon: Ext.MessageBox.WARNING
                });

                break;
        }
    },
    versionMenuItemClick: function (item) {
        var menuTree = this;
        var n;
        switch (item.id) {
            case 'add-group-node':
                n = item.parentMenu.contextNode;
                var win = Ext.getCmp("add-group-win");
                if (!win) {
                    win = new Desktop.AddGroupWin({id: 'add-group-win'});
                }

                win.showWin(n);
                break;
            case 'copy-version-node':
                n = item.parentMenu.contextNode;
                if (n.parentNode) {

                }
                break;
            case 'delete-version-node':
                n = item.parentMenu.contextNode;
                var appName = n.parentNode.text;
                var version = n.text;
                Ext.Msg.show({
                    title: '确认删除?',
                    msg: '数据删除会同步到所有节点，且删除后数据不可恢复，是否确定删除？',
                    buttons: Ext.Msg.YESNO,
                    fn: function (buttonId, text, opt) {
                        if (buttonId == 'yes') {
                            Ext.Ajax.request({
                                method: 'POST',
                                url: '/config/' + appName + "/" + version + "/_delete",
                                success: function (response, opts) {
                                    var obj = Ext.decode(response.responseText);
                                    if (obj.success) {
                                        menuTree.reload(n.parentNode);
                                    } else {
                                        alert(obj.ex);
                                    }
                                },
                                failure: function (response, opts) {
                                    console.log('server-side failure with status code ' + response.status);
                                }
                            });
                        }
                    },
                    icon: Ext.MessageBox.WARNING
                });
                break;
        }
    },
    groupMenuItemClick: function (item) {
        var menuTree = this;
        var n;
        switch (item.id) {
            case 'delete-group-node':
                n = item.parentMenu.contextNode;
                var appName = n.parentNode.parentNode.text;
                var version = n.parentNode.text;
                var groupName = n.text;
                Ext.Msg.show({
                    title: '确认删除?',
                    msg: '数据删除会同步到所有节点，且删除后数据不可恢复，是否确定删除？',
                    buttons: Ext.Msg.YESNO,
                    fn: function (buttonId, text, opt) {
                        if (buttonId == 'yes') {
                            Ext.Ajax.request({
                                method: 'POST',
                                url: '/config/' + appName + "/" + version + "/" + groupName + "/_delete",
                                success: function (response, opts) {
                                    var obj = Ext.decode(response.responseText);
                                    if (obj.success) {
                                        menuTree.reload(n.parentNode);
                                        var tabId = appName + ">" + version + ">" + groupName;
                                        var tab = Ext.getCmp(tabId);
                                        if(tab){
                                            tab.close();
                                        }
                                    } else {
                                        alert(obj.ex);
                                    }
                                },
                                failure: function (response, opts) {
                                    console.log('server-side failure with status code ' + response.status);
                                }
                            });
                        }
                    },
                    icon: Ext.MessageBox.WARNING
                });
                break;
        }
    }
});

Ext.reg('desktop.menu-panel', Desktop.MenuPanel);


