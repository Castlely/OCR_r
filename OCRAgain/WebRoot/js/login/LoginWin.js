/**
 * @author 宋英豪
 * @email haoyingsong@163.com
 * @description All rights reserved
 */
/**
 * @class LoginWin
 * @extends Ext.Window
 * @description provide a Login Window
 */
LoginWin = Ext.extend(Ext.Window, {
	id:'LoginWin',
	title : 'WebQQ登录系统',
	width : 265,
	height : 140,
	closable : false,
	collapsible : true,
	defaults : {
		border : false
	},
	buttonAlign : 'center',
	createFormPanel : function() {
		return new Ext.form.FormPanel({
			bodyStyle : 'padding-top:6px',
			defaultType : 'textfield',
			labelAlign : 'right',
			labelWidth : 55,
			labelPad : 0,
			frame : true,
			defaults : {
				allowBlank : false,
				width : 158,
				selectOnFocus : true
			},
			items : [{
						name : 'userName',
						cls:'user',
						fieldLabel : '帐号',
						blankText : '帐号不能为空'
					}, {
						name : 'password',
						cls:'key',
						fieldLabel : '密码',
						blankText : '密码不能为空',
						inputType : 'password'
					}]
		});
	},
	login : function() {
		var userName = this.fp.form.findField("userName").getValue();
		var password = this.fp.form.findField("password").getValue();
		if (userName == null || userName == '') {
			this.fp.form.findField("userName").markInvalid("用户名不能为空!");
			return;
		}
		if (password == null || password == '') {
			this.fp.form.findField("password").markInvalid("密码不能为空!");
			return;
		}
		this.fp.form.submit({
			waitTitle : '请稍候',
			waitMsg : '正在登录.......',
			url : 'login.do',
			method : 'POST',
			params : {
				userName : userName,
				password : password
			},
			success : function(form, action) {
				Ext.getCmp("LoginWin").close();
				window.location.href = "main.jsp?userName="+userName;
			},
			failure : function(form, action) {
				Ext.MessageBox.alert('警告', "姓名或密码不正确");
					form.findField("password").setRawValue('');
					form.findField("userName").focus(true);
			},
			scope : this
		});
	},
	initComponent : function() {
		this.keys = {
			key : Ext.EventObject.ENTER,
			fn : this.login,
			scope : this
		};
		LoginWin.superclass.initComponent.call(this);
		this.fp = this.createFormPanel();
		this.add(this.fp);
		this.addButton('登陆', this.login, this);
		this.addButton('注册', function() {
					var win = new RegWin();
					win.show();
				}, this);
		this.addButton('退出', function() {
					Ext.Msg.confirm("提示", "确认要退出系统吗？", function(btn) {
								if (btn == "yes")
									window.location.href = "login.html";
							})
				}, this);
	}

});