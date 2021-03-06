Desktop.AddVersionWin = function (config) {

    var southPanel = new Ext.Panel({
        border: false,
        region: 'south',
        autoHeight: true
        //minHeight: 20,
        //height: 20
    });

    var fp = new Ext.form.FormPanel({
        region: 'center',
        xtype: 'form',
        border: false,
        labelWidth: 85,
        padding: '10px 0 10px 0',
        labelAlign: 'right',
        defaults: {
            xtype: 'textfield',
            msgTarget: 'under',
            allowBlank: false,
            enableKeyEvents: true,
            selectOnFocus: true
        },
        items: [{
            fieldLabel: '版本号',
            name: 'version',
            vtype: 'versionValidate',
            listeners: {
                scope: this,
                keyup: function (textfield, event) {
                    // enter key event
                    if (event.keyCode == 13) {
                        this.submitForm();
                    }
                }
            }
        }, southPanel],
        buttonAlign: 'center',
        buttons: [{
            text: '保  存',
            scope: this,
            handler: function () {
                this.submitForm();
            }
        }, {
            text: '取  消',
            scope: this,
            handler: function () {
                this.close();
            }
        }]
    });

    var cfg = {
        iconCls: 'icon-favicon',
        width: 300,
        modal: true,
        autoHeight: true,
        title: '添加版本',
        resizable: false,
        closable: true,
        items: [fp]
    };

    this.fp = fp;
    this.southPanel = southPanel;

    var allConfig = Ext.applyIf(config || {}, cfg);
    Desktop.AddVersionWin.superclass.constructor.call(this, allConfig);
};

Ext.extend(Desktop.AddVersionWin, Ext.Window, {
    showWin: function (node) {
        this.parentNode = node;
        this.show();
    },
    submitForm: function () {
        if (this.fp.getForm().isValid()) {
            var node = this.parentNode;
            var appName = node.text;
            var version = this.fp.getForm().getValues(false).version;

            this.fp.getForm().submit({
                url: '/config/' + appName + '/' + version + "/",
                waitMsg: '数据处理中，请稍后...',
                timeout: 30,
                scope: this,
                success: function (form, action) {
                    //var result = action.response.responseText;

                    //重新加载树
                    Ext.getCmp("desktop.menu-panel").reload(node);

                    this.close();// 关闭窗口
                },
                failure: function (form, action) {
                    var msg = "";
                    switch (action.failureType) {
                        case Ext.form.Action.CLIENT_INVALID:
                            msg = 'Form fields may not be submitted with invalid values';
                            break;
                        case Ext.form.Action.CONNECT_FAILURE:
                            msg = 'Ajax communication failed';
                            break;
                        case Ext.form.Action.SERVER_INVALID:
                            if (action.response && action.response.status === 200 && action.response.responseText) {
                                var result = Ext.decode(action.response.responseText);
                                console.warn(result);
                                this.southPanel.update("<center><font color='red'>保存失败:</font><br/>" + result.ex + "</center>");
                            } else {
                                // 密码已过期或初始密码登录
                                this.southPanel.update("<center><font color='red'>保存失败</font></center>");
                            }
                            break;
                        default:
                            this.southPanel.update("<center><font color='red'>保存失败</font></center>");
                    }
                    console.log(msg);
                }
            });
        }
    }
});

Ext.apply(Ext.form.VTypes, {
    /**
     * 只能是数字、字母、下划线组合
     * @param val
     * @param field
     * @returns {boolean}
     */
    versionValidate: function (val, field) {
        return /^[0-9a-zA-Z_\.-]{1,}$/.test(val);
    },
    versionValidateText: '版本号只能由字母、数字、点、中划线组成'
});