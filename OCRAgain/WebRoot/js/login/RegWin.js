/**
 * @author 宋英豪
 * @email haoyingsong@163.com
 * @description All rights reserved
 */
/**
 * @class RegWin
 * @extends Ext.Window
 * @description provide a Register Window
 */
RegWin = Ext.extend(Ext.Window, {
	id:'regwin',
	title : '新用户注册',
	width : 265,
	height : 260,
	closable : false,
	modal : true,
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
						id : 'nameTemp',
						xtype : 'hidden',
						value : ''
					}, {
						id : 'regable',
						fieldLabel: '',
						xtype : 'hidden',
						value : true
					}, {
						id : 'userName',
						name : 'userName',
						fieldLabel : '帐号',
						blankText : '帐号不能为空',
						validator : function() {
							var nameTemp = Ext.get('nameTemp').dom.value;
							var name = Ext.get('userName').dom.value;
							if (nameTemp != name) {
								Ext.get('nameTemp').dom.value = name;
								nameTemp = name;
								Ext.Ajax.request({
									url : 'http://localhost:8088/WebQQApp/check.do',
									method : 'POST',
									params : {
										userName : nameTemp
									},
									success : function(responseObject) {
										var res = responseObject.responseText;
										if (res == 'false') {
											Ext.get('regable').dom.value = false;
										}
										if (res == 'true') {
											Ext.get('regable').dom.value = true;
										}
									}
								});
							}
							return eval(Ext.get('regable').dom.value);
						},
						invalidText : '用户名已经被注册'
					}, {
						name : 'password',
						fieldLabel : '密码',
						blankText : '密码不能为空',
						inputType : 'password'
					}, {
						name : 'RePassword',
						fieldLabel : '确认密码',
						blankText : '密码不能为空',
						inputType : 'password'
					}, {
						name : 'email',
						vtype : 'email',
						fieldLabel : '电子邮件',
						blankText : '邮件不能为空'
					}, {
						xtype : "textarea",
						name : 'intro',
						fieldLabel : '简介',
						blankText : '简介不能为空'
					}]
		});
	},
	save : function() {
		var pass1 = this.fp.form.findField("password").getValue();
		var pass2 = this.fp.form.findField("RePassword").getValue();
		if (pass1 != pass2) {
			this.fp.form.findField("password").markInvalid("两次输入的密码不同!");
			this.fp.form.findField("password").focus(true);
			return;
		}
		var userName = this.fp.form.findField("userName").getValue();
		var password = this.fp.form.findField("password").getValue();
		var email = this.fp.form.findField("email").getValue();
		var introduction = this.fp.form.findField("intro").getValue();
		this.fp.form.submit({
			waitTitle : '请稍候',
			waitMsg : '正在提交.....',
			url : 'http://localhost:8088/WebQQApp/reg.do',
			method : 'POST',
			params : {
				userName : userName,
				password : password,
				email : email,
				introduction : introduction
			},
			success : function(form, action) {
                  Ext.getCmp("regwin").close();
			},
			failure : function(form, action) {
				form.findField("userName").focus(true);
			}
		});
	},
	initComponent : function() {
		this.keys = {
			key : Ext.EventObject.ENTER,
			fn : this.save,
			scope : this
		};
		RegWin.superclass.initComponent.call(this);
		this.fp = this.createFormPanel();
		this.add(this.fp);
		this.addButton('提交', this.save, this);
		this.addButton('取消', function() {
					this.close();
				}, this);
	}
});