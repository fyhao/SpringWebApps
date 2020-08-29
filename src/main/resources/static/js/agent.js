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
	});
	on('setAgentStatus', function(opts) {
		var json = {action:'setAgentStatus',agentid:opts.agentid,status:opts.status};
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
		var box = $('<div />').appendTo(con);
		var stateBtn = $('<span />').appendTo(box);
		renderStateButton(stateBtn);
		return box;
	}; // createAgentCTIBar
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
	var agentCTIBar = createAgentCTIBar(con);
	
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
