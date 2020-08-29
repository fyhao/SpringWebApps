$(function() {
	var con = $('<div />').appendTo('body');
	var serverport = 8080;
	var ws = null;
	
	function initWS(conversationid) {
		ws = new WebSocket("ws://localhost:8080/channel");			
	    ws.onopen = function() {
			publishEvent('register', {conversationid:conversationid});
	    };
	    ws.onmessage = function (evt) { 
			var data = evt.data;
			var json = JSON.parse(data);
	        publishEvent('wsmessage', {evt:evt,json:json});
	    };
	    ws.onclose = function() { 
	        
	    };
	};
	function sendWSMessage(json) {
		json.serverport = serverport;
		ws.send(JSON.stringify(json));
	};
	function sendRestRequest(opts) {
		$.ajax({
			url : 'http://localhost:8080' + opts.path,
			method : opts.method,
			success :  opts.success
		});
	};
	on('wsmessage', function(opts) {
		var json = opts.json;
		console.log('customer wsmessage: ' + JSON.stringify(json));
		publishEvent(json.action, json);
	});
	on('createConversation', function(evt) {
		var email = evt.email;
		var channel = evt.channel;
		sendRestRequest({
			method : "GET",
			path : "/webchat/createconversationwithchannel?email=" + email + "&channel=" + channel,
			success : function(conversationid) {
				initWS(conversationid);
			}
		});
	});
	on('register', function(opts) {
		var json = {
			action : 'register',
			conversationid : opts.conversationid,
			serverport: serverport
		};
		sendWSMessage(json);
	});
	on('sendChatMessage', function(evt) {
		var json = {
			action : 'sendChatMessage',
			conversationid : evt.conversationid,
			chatMessage : evt.chatMessage
		};
		sendWSMessage(json);
	});
	on('connectionready', function(evt) {
		var conversationid = evt.conversationid;
		$(createCustomerLoginUI).hide();
		$(customerChatUI).show();
		$(customerChatUI).data('conversationid', conversationid);
	});
	function createCustomerLoginUI(con) {
		var box = $('<div />').appendTo(con);
		var emailInput = $('<input type=text />').appendTo(box);
		var startChatBtn = $('<button>Start Chat</button>').appendTo(box);
		$(startChatBtn).click(function() {
			var email = $(emailInput).val();
			publishEvent('createConversation', {email:email,channel:'webchathotel'});
		});
		return box;
	};
	var createCustomerLoginUI = createCustomerLoginUI(con);
	function createCustomerChatUI(con) {
		var box = $('<div />').appendTo(con);
		var chatrequestid = Math.random();
		publishEvent('chatwidget.requestui', {requestid:chatrequestid});
		on('chatwidget.responseui.' + chatrequestid, function(evt) {
			var chatcon = evt.con;
			$(chatcon.ui).appendTo(box);
			on('chatwidget.req.sendChatMessage.' + chatrequestid, function(evt1) {
				var chatmessage = evt1.chatmessage;
				var conversationid = $(box).data('conversationid');
				publishEvent('sendChatMessage', {chatMessage:chatmessage,conversationid:conversationid});
			});
			on('chatMessageReceived', function(evt1) {
				publishEvent('chatwidget.res.chatMessageReceived.' + chatrequestid, evt1);
			});
		});
		return box;
	}; 
	var customerChatUI = createCustomerChatUI(con);
	// Testing mode
	if(true) {
		$(createCustomerLoginUI).find(':input').val('test@test.com');
		$(createCustomerLoginUI).find('button').click();
	}
});
var publishEvent = function(a,b) {
	setTimeout(function() {
		for(var i = 0; i < _handlers.length; i++) {
			var h = _handlers[i];
			if(h.a == a) {
				h.b(b);
			}
		}
	}, 1);
};
var containerServices = {};
var _handlers = [];
containerServices.addHandler = function(a,b) {
	_handlers.push({a:a,b:b});
};
function on(a,b) {
	containerServices.addHandler(a,b);
};
