Desktop.PropertyEditorTab = function (config) {
    var groupUrl = "/config/" + config.appName + "/" + config.version + "/" + config.groupName + "/_list";

    var Property = Ext.data.Record.create([{
        name: 'key',
        type: 'string'
    }, {
        name: 'value',
        type: 'string'
    }, {
        name: 'description',
        type: 'string'
    }]);

    var store = new Ext.data.GroupingStore({
        reader: new Ext.data.JsonReader({idProperty: 'key', root:'datas', fields: Property}),
        autoLoad: true,
        autoDestroy: true,
        proxy: new Ext.data.HttpProxy({
            url: groupUrl,
            //timeout: 120000,
            method: 'GET'
        }),
        sortInfo: {field: 'key', direction: 'ASC'}
    });

    var propNameEditor = new Ext.form.TextField({
        xtype: 'textfield',
        emptyText: '属性名',
        readOnly: true,
        allowBlank: false
    });

    var propValueEditor = new Ext.form.TextField({
        xtype: 'textfield',
        allowBlank: true
    });

    var descriptionEditor = new Ext.form.TextField({
        xtype: 'textfield',
        allowBlank: true
    });

    var editor = new Ext.ux.grid.RowEditor({
        saveText: '保  存',
        cancelText: '取  消',
        listeners: {
            scope: this,
            beforeedit: function(){
                grid.removeBtn.setDisabled(true);
            },
            afteredit: function(roweditor, changes, record, rowindex){
                propNameEditor.setReadOnly(true);
                this.saveRow(record, store);
            },
            canceledit: function(roweditor, forced){
                propNameEditor.setReadOnly(true);
                if(forced){
                    this.reload();
                    //store.load();
                }
            }
        }
    });

    var propTab = this;

    var grid = new Ext.grid.GridPanel({
        store: store,
        stripeRows: true,
        loadMask: {
            msg: "努力加载数据中，请稍后..."
        },
        border: false,
        region:'center',
        margins: '0 5 5 5',
        //autoExpandColumn: 'description',
        plugins: [editor],
        view: new Ext.grid.GroupingView({
            emptyText: 'no data',
            markDirty: false
        }),
        tbar: [{
            iconCls: 'icon-add',
            text: '新建',
            handler: function(){
                if(!editor.editing){
                    propNameEditor.setReadOnly(false);
                    var e = new Property({
                        key: '',
                        value: '',
                        description: ''
                    });
                    //editor.stopEditing();
                    store.insert(0, e);
                    grid.getView().refresh();
                    grid.getSelectionModel().selectRow(0);
                    editor.startEditing(0);
                }
            }
        },{
            ref: '../removeBtn',
            iconCls: 'icon-delete',
            text: '删除',
            disabled: true,
            handler: function(){
                editor.stopEditing();
                propTab.removeRow(grid, store);
            }
        },{
            iconCls: 'icon-refresh',
            text: '刷新',
            handler: function(){
                store.load();
            }
        }],
        columns: [
            new Ext.grid.RowNumberer(),
            {
                header: '属性名',
                dataIndex: 'key',
                width: 200,
                sortable: true,
                editor: propNameEditor
            },{
                header: '属性值',
                dataIndex: 'value',
                width: 200,
                sortable: true,
                editor: propValueEditor
            },{
                id: 'description',
                header: '注释',
                dataIndex: 'description',
                width: 300,
                sortable: true,
                editor: descriptionEditor
            }]
    });

    var cfg = {
        closable: true,
        border: false,
        autoScroll: true,
        //autoHeight: true,
        layout: 'border',
        margins: '35 5 5 0',
        containerScroll: true,
        items: [grid]
    };

    grid.getSelectionModel().on('beforerowselect', function(sm, rowIndex, keepExisting, record){
        if(editor.editing){
            return false;
        }
    });

    grid.getSelectionModel().on('selectionchange', function(sm){
        grid.removeBtn.setDisabled(sm.getCount() < 1);
    });
    this.store = store;
    this.grid = grid;

    var allConfig = Ext.applyIf(config || {}, cfg);
    Desktop.PropertyEditorTab.superclass.constructor.call(this, allConfig);
};

Ext.extend(Desktop.PropertyEditorTab, Ext.Panel, {
    reload: function(){
        this.grid.removeBtn.setDisabled(this.grid.getSelectionModel().getCount() < 1);
        this.store.load();
    },
    removeRow: function(grid, store){

        var s = grid.getSelectionModel().getSelections();
        var keys = "";
        for(var i = 0, r; r = s[i]; i++){
            keys += r.id;
            if(i < s.length - 1){
                keys += ",";
            }
        }

        if(keys == ""){
            return;
        }

        var reqUrl= "/config/" + this.appName + "/" + this.version + "/" + this.groupName + "/" + keys + "/_delete";
        var me = this;

        Ext.Msg.show({
            title: '确认删除?',
            msg: '数据删除后会同步到所有节点，是否确定删除？',
            buttons: Ext.Msg.YESNO,
            fn: function (buttonId, text, opt) {
                if (buttonId == 'yes') {
                    Ext.Ajax.request({
                        method: 'POST',
                        url: reqUrl,
                        success: function (response, opts) {
                            var obj = Ext.decode(response.responseText);
                            if (obj.success) {
                                me.reload();
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

        grid.getView().refresh();
    },
    saveRow: function (record) {

        var key = record.id || record.data.key;
        if(key.indexOf("ext-record") == 0 ){
            key = record.data.key;
        }
        var value = record.data.value;
        var description = record.data.description;
        var reqUrl= "/config/" + this.appName + "/" + this.version + "/" + this.groupName + "/" + key + "/";

        var me = this;

        Ext.Msg.show({
            title: '确认修改?',
            msg: '数据修改后会同步到所有节点，是否确定修改？',
            buttons: Ext.Msg.YESNO,
            fn: function (buttonId, text, opt) {
                if (buttonId == 'yes') {
                    Ext.Ajax.request({
                        method: 'POST',
                        url: reqUrl,
                        params: {
                            key: key,
                            value: value,
                            description: description
                        },
                        success: function (response, opts) {
                            var obj = Ext.decode(response.responseText);
                            if (obj.success) {
                                me.reload();
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

    }
});
