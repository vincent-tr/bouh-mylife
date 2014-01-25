var assert = require('assert');
var events = require('events');
var util = require('util');

var irc = require('irc');

var config = require('./config.json');

/*************************** structure ***************************/

function netRange(min, max) {
	return {
		type: "range",
		min: min,
		max: max
	};
}

function netEnum() {
	return {
		type: "enum",
		values: Array.prototype.slice.call(arguments, 0)
	};
}

function netAttribute(name, type) {
	return {
		name: name,
        membertype: "attribute",
        type: type
	};
}

function netAction(name) {
	return {
		name: name,
		membertype: "action",
		arguments: Array.prototype.slice.call(arguments, 1)
	};
}

function netClass() {
	return {
		members: Array.prototype.slice.call(arguments, 0)
	};
}

/*************************** object ***************************/

function netObject(id, clazz) {
	
	var obj = new events.EventEmitter();
	obj.id = id;
	obj.clazz = clazz;
	
	var attributeValues = {};
	
	for(var i=0, l=obj.clazz.members.length; i<l; i++) {
		var member = obj.clazz.members[i];
		if(member.membertype !== 'attribute') {
			continue;
		}
		
		attributeValues[member.name] = null;
	}
	
	obj.getMember = function(name) {
		for(var i=0, l=obj.clazz.members.length; i<l; i++) {
			var member = obj.clazz.members[i];
			if(member.name === name) {
				return member;
			}
		}
		
		return null;
	};

	var checkArg = function(arg, type) {
		if(type.type === 'range') {
			var val = parseInt(arg, 10);
			if(isNaN(val) || val < type.min || val > type.max) {
				throw new Error('invalid argument');
			}
		}

		if(type.type === 'enum') {
			for(var i=0, l=type.values.length; i<l; i++) {
				if(type.values[i] === arg) {
					return;
				}
			}
			
			throw new Error('invalid value');
		}
	};

	obj.executeAction = function(name) {
		
		var member = this.getMember(name);
		assert(member);
		assert(member.membertype === 'action');
		
		var args = Array.prototype.slice.call(arguments, 1);
		if(args.length !== member.arguments.length) {
			throw new Error('invalid arguments count');
		}
		for(var i=0, l=args.length; i<l; i++) {
			checkArg(args[i], member.arguments[i]);
		}
		
		obj.emit('action#' + member.name, args);
		obj.emit('action', member.name, args);
	};

	obj.setAttribute = function(name, value) {

		var member = this.getMember(name);
		assert(member);
		assert(member.membertype === 'attribute');
		
		checkArg(value, member.type);

		attributeValues[member.name] = value;
		
		obj.emit('attribute#' + member.name, value);
		obj.emit('attribute', member.name, value);
	};

	obj.getAttribute = function(name) {
		
		var member = this.getMember(name);
		assert(member);
		assert(member.membertype === 'attribute');
		
		return attributeValues[member.name];
	};
	
	return obj;
}

/*************************** repository ***************************/

var publishLocal = function(object, channel) {
	
	var makeNick = function() {
		
		var nick = object.id;
		
		for(var i=0, l=object.clazz.members.length; i<l; i++) {
			var member = object.clazz.members[i];
			if(member.membertype !== 'attribute') {
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
			object.executeAction.apply(object, args);
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

module.exports.netRange = netRange;
module.exports.netEnum = netEnum;
module.exports.netAttribute = netAttribute;
module.exports.netAction = netAction;
module.exports.netClass = netClass;
module.exports.netObject = netObject;
module.exports.publish = publish;
module.exports.unpublish = unpublish;
