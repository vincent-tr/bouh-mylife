var assert = require('assert');
var events = require('events');
var util = require('util');

var irc = require('irc');

var config = require('./config.json');
var netstructure = require('./netstructure.js');
var netobject = require('./netobject.js');

var publishLocal = function(object, channel) {
	
	var makeNick = function() {
		
		var nick = object.id;
		
		for(var i=0, l=object.clazz.members.length; i<l; i++) {
			var member = object.clazz.members[i];
			if(!(member instanceof netstructure.NetAttribute)) {
				continue;
			}
			
			var value = object.getAttribute(member.name);
			nick += '|' + value;
		}
		
		return nick;
	};
	
	var attributeChanged = function() {
		var nick = makeNick();
		ircclient.send('NICK', nick);
	};
	
	var isMyNick = function(nick) {
		var nickid = nick.split('|')[0];
		return nickid === object.id;
	};
	
	var message = function(from, to, text) {
		var args = text.split(' ');
		if(to.toLowerCase() === ('#' + channel).toLowerCase()) {
			if(args.length < 2) {
				return;
			}
			if(!isMyNick(args[0])) {
				return;
			}
			args.shift();
		} else {
			if(args.length < 1) {
				return;
			}
		}
		
		try {
			object.executeAction.call(object, args);
		} catch(err) {
			ircclient.notice(from, err.message);
		}
	};
	
	var ircconf = JSON.parse(JSON.stringify(config.irc));
	ircconf.channels = ['#' + channel];
	
	var ircclient = new irc.Client(ircconf.server, makeNick(), ircconf);
	object.addListener('attribute', attributeChanged);
	ircclient.on('message', message);
	
	var destroy = function() {
		ircclient.disconnect();
		object.removeListener('attribute', attributeChanged);
	};
	
	return {
		object: object,
		channel: channel,
		local: true,
		destroy: destroy
	};
};

var publish = function(object, channel, local) {
	if(local) {
		return publishLocal(object, channel);
	} else {
		throw new Error('not implemented');
	}
};

var unpublish = function(container) {
	container.destroy();
};

module.exports.publish = publish;
module.exports.unpublish = unpublish;
