$(function() {
	on('chatwidget.requestui', function(evt) {
		var con = renderChatWidget(evt.requestid);
		publishEvent('chatwidget.responseui.' + evt.requestid, {con:con});
	});
	function renderChatWidget(requestid) {
		var con = $('<div />');
		var chat = new ChatWidget();
		chat.requestid = requestid;
		chat.initUI();
		return chat;
	}
	// list of callback functions
	
	function ChatWidget() {
		// public method
		this.initUI = function() {
			var ui = $('<div />');
			this.ui = ui;
			renderChatBox(ui);
		}
		var me = this;
		
		function renderChatBox(ui) {
			renderChatMessageDisplay(ui);
			renderChatInputBar(ui);
		};
		function renderChatMessageDisplay(con) {
			var div = $('<div />').appendTo(con);
			on('chatwidget.newmessage.' + me.requestid, function(evt) {
				var content = evt.content;
				var fromparty = evt.fromparty;
				$(div).append(fromparty + ': ' + content + ' - ' + new Date().toString() + '<br />');
			});
		};
		function renderChatInputBar(con) {
			var div = $('<div />').appendTo(con);
			var input = $('<input type=text />').appendTo(div);
			var sendBtn = $('<button />').html('Send').appendTo(div);
			$(sendBtn).click(function() {
				var chatmessage = $(input).val();
				publishEvent('chatwidget.newmessage.' + me.requestid, {fromparty:'me',content:chatmessage});
				publishEvent('chatwidget.req.sendChatMessage.' + me.requestid,{chatmessage:chatmessage});
			});
			on('chatwidget.res.chatMessageReceived.' + me.requestid, function(evt) {
				evt.fromparty = 'System';
				publishEvent('chatwidget.newmessage.' + me.requestid, evt);
			});
		};
	}
	
});