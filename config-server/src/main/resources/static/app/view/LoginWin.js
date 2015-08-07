LoginWindow = function (config) {

    var topPanel = {
        border: false,
        region: 'north',
        height: 60,
        html: '<img style="height: 50px; width:300px; left: 0px; top: 0px;" src="images/login/ban.png"/>'
    };

    var southPanel = new Ext.Panel({
        border: false,
        region: 'south',
        height: 20
    });

    var fp = new Ext.form.FormPanel({
        id: 'login-form',
        region: 'center',
        xtype: 'form',
        border: false,
        //padding: '10px',
        labelWidth: 85,
        labelAlign: 'right',
        defaults: {
            xtype: 'textfield',
            msgTarget: 'under',
            allowBlank: false,
            enableKeyEvents: true,
            selectOnFocus: true
        },
        items: [{
            fieldLabel: '用户名',
            name: 'username',
            listeners: {
                scope: this,
                keyup: function (textfield, event) {
                    // enter key event
                    if (event.keyCode == 13) {
                        this.submitForm();
                    }
                }
            }
        }, {
            fieldLabel: '密  码',
            name: 'password',
            inputType: 'password',
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
            text: '登  录',
            scope: this,
            handler: function () {
                this.submitForm();
            }
        }, {
            text: '取  消',
            scope: this,
            handler: function () {
                southPanel.update("");
                fp.getForm().reset();
            }
        }]
    });

    var cfg = {
        iconCls: 'icon-favicon',
        width: 300,
        modal: true,
        autoHeight: true,
        title: '千米网 - 配置管理中心',
        resizable: false,
        closable: false,
        //bbar: new Ext.ux.StatusBar({
        //    id: 'login-form-statusbar',
        //    defaultText: 'Ready',
        //    plugins: new Ext.ux.ValidationStatus({form: 'login-form'})
        //}),
        items: [topPanel, fp]
    };

    this.fp = fp;
    this.southPanel = southPanel;

    var winConfig = Ext.applyIf(config || {}, cfg);

    LoginWindow.superclass.constructor.call(this, winConfig);
};

Ext.extend(LoginWindow, Ext.Window, {
    submitForm: function () {
        if (this.fp.getForm().isValid()) {
            //var sb = Ext.getCmp('login-form-statusbar');
            //sb.showBusy('Saving form...');
            //this.fp.getEl().mask();

            this.fp.getForm().submit({
                url: '/login',
                waitMsg: '正在登录，请稍后...',
                timeout: 30,
                scope: this,
                success: function (form, action) {
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
                            if (action.response && action.response.status === 200 && action.response.responseText && action.response.responseText.indexOf("login") < 0) {
                                window.location.href = '/';
                            } else {
                                // 密码已过期或初始密码登录
                                this.southPanel.update("<center><font color='red'>鉴权失败</font></center>");
                            }
                            break;
                        default:
                            this.southPanel.update("<center><font color='red'>鉴权失败</font></center>");
                    }
                }
            });
        }
    }
});