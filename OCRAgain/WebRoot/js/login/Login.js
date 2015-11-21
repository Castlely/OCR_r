/**
 * @author 宋英豪
 * @email haoyingsong@163.com
 * @description All rights reserved
 */

Ext.onReady(function() {
	Ext.QuickTips.init();
	Ext.form.Field.prototype.msgTarget = 'side';
	var w = new LoginWin();
	w.show();
	setTimeout(function() {
				Ext.get('loading-mask').fadeOut({
					remove : true
				});
			}, 250);

});