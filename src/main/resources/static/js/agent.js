$(function() {
	var con = $('<div />').appendTo('body');
	var serverport = 8080;
	var ws = null;
	
	function initWS(agentid) {
		ws = new WebSocket("ws://localhost:8080/agent");			
	    ws.onopen = function() {
			publishEvent('register', {agentid:agentid});
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
	on('wsmessage', function(opts) {
		var json = opts.json;
		console.log('agent wsmessage: ' + JSON.stringify(json));
		publishEvent(json.action, json);
	});
	var agentid = null;
	on('register', function(opts) {
		var json = {action:'register',agentid:opts.agentid};
		sendWSMessage(json);
		agentid = opts.agentid;
	});
	on('connectionready', function(opts) {
		$(agentLoginUI).hide();
		$(agentCTIBar).show();
		$(agentChatUI).show();
	});
	on('setAgentStatus', function(opts) {
		var json = {action:'setAgentStatus',agentid:opts.agentid,status:opts.status};
		sendWSMessage(json);
	});
	on('sendChatMessage', function(evt) {
		var json = {
			action : 'sendChatMessage',
			conversationid : evt.conversationid,
			agentid : agentid,
			chatMessage : evt.chatMessage
		};
		sendWSMessage(json);
	});
	on('closeTask', function(evt) {
		var json = {
			action : 'closeTask',
			taskid : evt.taskid,
			agentid : agentid
		};
		sendWSMessage(json);
	});
	function createAgentLoginUI(con) {
		var box = $('<div />').appendTo(con);
		var agentidInput = $('<input type=text />').appendTo(box);
		var loginBtn = $('<button />').html('Login').appendTo(box);
		$(loginBtn).click(function() {
			var agentid = $(agentidInput).val();
			initWS(agentid);
		});
		return box;
	};
	var agentLoginUI = createAgentLoginUI(con);
	function createAgentCTIBar(con) {
		var box = $('<div />').hide().appendTo(con);
		var stateBtn = $('<span />').appendTo(box);
		renderStateButton(stateBtn);
		return box;
	}; // createAgentCTIBar
	var agentCTIBar = createAgentCTIBar(con);
	function renderStateButton(con) {
		var stateBtn = $('<button />').appendTo(con);
		var states = {'NOT_READY':'READY','READY':'NOT_READY'};
		$(stateBtn).data('state', 'NOT_READY'); // default state
		$(stateBtn).html(states['NOT_READY']);
		$(stateBtn).click(function() {
			var curState  = $(stateBtn).data('state');
			var nextState = states[curState];
			publishEvent('setAgentStatus', {agentid:agentid,status:nextState});
		});
		function updateAgentStatus(state) {
			$(stateBtn).html(states[state]); // show next state when click
			$(stateBtn).data('state', state); // store current state
		}
		on('agentStatusChanged', function(evt) {
			var newstatus = evt.newstatus;
			updateAgentStatus(newstatus);
		});
	}; // renderStateButton
	function createAgentChatUI(con) {
		var box = $('<div />').appendTo(con);
		var tabbar = $('<div />').appendTo(box);
		var tabcontent = $('<div />').appendTo(box);
		var conv = {};
		var c = 0;
		function showconversation(conversationid) {
			$(tabcontent).find('div').filter(function() {
				return typeof $(this).data('conversationid') != 'undefined'
			}).hide();
			$(tabcontent).find('div').filter(function() {
				return $(this).data('conversationid') == conversationid
			}).show();
		}
		function createTabMenu(evt) {
			var btn = $('<button />').appendTo(tabbar);
			$(btn).html('Chat ' + ++c);
			$(btn).data('evt', evt);
			$(btn).click(function() {
				var conversationid = $(btn).data('evt').conversationid
				showconversation(conversationid);
			});
			
			showconversation(evt.conversationid);
		};
		function createTabContent(evt) {
			createTabMenu(evt);
			var tabcon = $('<div />');
			$(tabcon).data('evt', evt);
			$(tabcon).data('conversationid', evt.conversationid);
			$(tabcon).data('taskid', evt.taskid);
			$(tabcon).appendTo(tabcontent);
			return tabcon;
		};
		function createNewConversation(evt) {
			var chatrequestid = Math.random();
			var tabcon = createTabContent(evt);
			$('<div>Conversation ID: ' + evt.conversationid + '</div>').appendTo(tabcon);
			var closeTaskBtn = $('<button>Close Task</button>').appendTo(tabcon);
			$(closeTaskBtn).data('conversationid', evt.conversationid);
			$(closeTaskBtn).data('taskid', evt.taskid);
			$(closeTaskBtn).click(function() {
				var taskid = $(this).data('taskid');
				publishEvent('closeTask', {taskid:taskid});
			});
			conv[evt.conversationid] = {evt:evt,chatrequestid:chatrequestid};
			publishEvent('chatwidget.requestui', {requestid:chatrequestid});
			on('chatwidget.responseui.' + chatrequestid, function(evt1) {
				var chatcon = evt1.con;
				$(chatcon.ui).appendTo(tabcon);
				on('chatwidget.req.sendChatMessage.' + chatrequestid, function(evt2) {
					var chatmessage = evt2.chatmessage;
					var conversationid = evt.conversationid;
					publishEvent('sendChatMessage', {chatMessage:chatmessage,conversationid:conversationid});
				});
			});
		};
		on('incomingTask', function(evt) {
			if(typeof conv[evt.conversationid] == 'undefined') {
				createNewConversation(evt);
			}
		});
		on('chatMessageReceived', function(evt) {
			if(typeof conv[evt.conversationid] == 'undefined') {
				createNewConversation(evt);
			}
			var chatrequestid = conv[evt.conversationid].chatrequestid;
			publishEvent('chatwidget.res.chatMessageReceived.' + chatrequestid, evt);
		});
		on('taskClosed', function(evt) {
			$(box).find('*').filter(function() {
				var taskid = evt.taskid;
				var ev = $(this).data('evt');
				if(ev != null && ev.taskid == taskid) {
					return true;
				}
				return false;
			}).remove();
			var s = $(tabbar).find('*').filter(function() {
				return $(this).data('evt') != null;
			});
			if(s.length) {
				var first = $(s[0]).data('evt').conversationid;
				showconversation(first);
			} 
		});
		return box;
	}; // createAgentChatUI
	var agentChatUI = createAgentChatUI(con);
	
	// Testing mode
	if(true) {
		$(agentLoginUI).find(':input').val('agent1');
		$(agentLoginUI).find('button').click();
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
