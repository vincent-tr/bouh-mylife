var assert = require('assert');
var events = require('events');
var util = require('util');
var os = require('os');

var irc = require('irc');

var config = require('./config.json');

/** ************************* structure ************************** */

function netRange(min, max) {
	return {
		type : "range",
		min : min,
		max : max
	};
}

function netEnum() {
	return {
		type : "enum",
		values : Array.prototype.slice.call(arguments, 0)
	};
}

function netAttribute(name, type) {
	return {
		name : name,
		membertype : "attribute",
		type : type
	};
}

function netAction(name) {
	return {
		name : name,
		membertype : "action",
		arguments : Array.prototype.slice.call(arguments, 1)
	};
}

function netClass() {
	return {
		members : Array.prototype.slice.call(arguments, 0)
	};
}

/** ************************* object ************************** */

function netObject(id, clazz) {

	var obj = new events.EventEmitter();
	obj.id = id;
	obj.clazz = clazz;

	var attributeValues = {};

	for (var i = 0, l = obj.clazz.members.length; i < l; i++) {
		var member = obj.clazz.members[i];
		if (member.membertype !== 'attribute') {
			continue;
		}

		attributeValues[member.name] = null;
	}

	obj.getMember = function(name) {
		for (var i = 0, l = obj.clazz.members.length; i < l; i++) {
			var member = obj.clazz.members[i];
			if (member.name === name) {
				return member;
			}
		}

		return null;
	};

	var checkArg = function(arg, type) {
		
		if(!arg) {
			return;
		}
		
		if (type.type === 'range') {
			var val = parseInt(arg, 10);
			if (isNaN(val) || val < type.min || val > type.max) {
				throw new Error('invalid argument');
			}
		}

		if (type.type === 'enum') {
			for (var i = 0, l = type.values.length; i < l; i++) {
				if (type.values[i] === arg) {
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
		if (args.length !== member.arguments.length) {
			throw new Error('invalid arguments count');
		}
		for (var i = 0, l = args.length; i < l; i++) {
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

/** ************************* repository ************************** */

var publishLocal = function(object, channels) {

	var makeNick = function() {

		var nick = object.id;

		for (var i = 0, l = object.clazz.members.length; i < l; i++) {
			var member = object.clazz.members[i];
			if (member.membertype !== 'attribute') {
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
		if (to.charAt(0) === '#') {
			if (args.length < 2) {
				return;
			}
			if (!isMyNick(args[0])) {
				return;
			}
			args.shift();
		} else {
			if (args.length < 1) {
				return;
			}
		}

		try {
			object.executeAction.apply(object, args);
		} catch (err) {
			ircclient.notice(from, err.message);
		}
	};

	var ircconf = JSON.parse(JSON.stringify(config.irc));
	ircconf.channels = channels;

	var ircclient = new irc.Client(ircconf.server, makeNick(), ircconf);
	object.addListener('attribute', attributeChanged);
	ircclient.on('message', message);

	var destroy = function() {
		ircclient.disconnect();
		object.removeListener('attribute', attributeChanged);
	};

	return {
		object : object,
		channels : channels,
		local : true,
		destroy : destroy
	};
};

var watcher;

var loadWatcher = function() {
	if (watcher) {
		return watcher;
	}

	var nick = os.hostname() + '-' + process.pid;
	var ircconf = JSON.parse(JSON.stringify(config.irc));
	ircconf.channels = [];
	var ircclient = new irc.Client(ircconf.server, nick, ircconf);

	var channels = {};
	var objects = {};
	var online = false;

	var send = function(channels, msg) {
		if(!online) {
			return;
		}
		
		ircclient.say(channels[0], msg);
	};
	
	var addChannel = function(channel) {
		var count = channels[channel];
		if (count) {
			channels[channel] = count + 1;
			return;
		}

		channels[channel] = 1;
		if(online) {
			ircclient.join(channel);
		}
	};

	var removeChannel = function(channel) {
		var count = channels[channel];
		if (count > 1) {
			channels[channel] = count - 1;
			return;
		}
		
		if(online) {
			ircclient.part(channel);
		}
		delete channels[channel];
	};

	var addObject = function(object) {
		objects[object.id] = object;
	};

	var removeObject = function(object) {
		delete objects[object.id];
	};

	var checkClean = function() {
		if (Object.keys(objects).length !== 0) {
			return;
		}

		ircclient.disconnect();
		watcher = undefined;
	};
	
	var setAttributes = function(object, values) {
		var ai = 0;
		for (var i = 0, l = object.clazz.members.length; i < l; i++) {
			var member = object.clazz.members[i];
			if (member.membertype !== 'attribute') {
				continue;
			}

			var value = null;
			if(values) {
				value = values[ai++];
			}
			object.setAttribute(member.name, value);
		}
	};
	
	var nickOnline = function(nick) {
		var array = nick.split('|');
		var id = array[0];
		var object = objects[id];
		if(!object) {
			return;
		}
		
		array = array.slice(1);
		setAttributes(object, array);
		
		object.emit('connected');
	};
	
	var nickOffline = function(nick) {
		var array = nick.split('|');
		var id = array[0];
		var object = objects[id];
		if(!object) {
			return;
		}

		object.emit('disonnected');
		
		setAttributes(object, null);
	};
	
	var nickChanged = function(oldNick, newNick) {
		var array = newNick.split('|');
		var id = array[0];
		var object = objects[id];
		if(!object) {
			return;
		}
		
		array = array.slice(1);
		setAttributes(object, array);
	};

	ircclient.on('part', function(channel, nick) {
		// on considere qu'on a qu'un seul chan en commun avec le user sp�cifi�
		nickOffline(nick);
	});
	
	ircclient.on('kick', function(channel, nick) {
		// on considere qu'on a qu'un seul chan en commun avec le user sp�cifi�
		nickOffline(nick);
	});

	ircclient.on('kill', function(nick) {
		nickOffline(nick);
	});
	
	ircclient.on('quit', function(nick) {
		nickOffline(nick);
	});

	ircclient.on('join', function(channel, nick) {
		nickOnline(nick);
	});
	
	ircclient.on('names', function(channel, users) {
		for(var i=0, l=users.length; i<l; i++) {
			nickOnline(users[i]);
		}
	});
	
	ircclient.on('nick', function(oldNick, newNick) {
		nickChanged(oldNick, newNick);
	});
	
	var connectionClosed = function() {
		
		if(!online) {
			return;
		}
		
		online = false;
		
		for(var id in objects) {
			if(objects.hasOwnProperty(id)) {
				nickOffline(id);
			}
		}
	};
	
	ircclient.on('registered', function() {
		
		ircclient.conn.on('end', connectionClosed);
		ircclient.conn.on('close', connectionClosed);
		ircclient.conn.on('error', connectionClosed);
		
		online = true;
		
		for(var channel in channels) {
			if(channels.hasOwnProperty(channel)) {
				ircclient.join(channel);
			}
		}
	});
	
	watcher = {
		addChannel : addChannel,
		removeChannel : removeChannel,
		addObject : addObject,
		removeObject : removeObject,
		checkClean : checkClean,
		send : send
	};

	return watcher;
};

var publishRemote = function(object, channels) {

	var watcher = loadWatcher();
	
	for(var i=0, l=channels.length; i<l; i++) {
		watcher.addChannel(channels[i]);
	}
	watcher.addObject(object);
	
	var executeAction = function(name, args) {
		var msg = object.id + ' ' + name;
		if(args) {
			for(var i=0, l=args.length; i<l; i++) {
				msg += ' ' + args[i];
			}
		}
		watcher.send(channels, msg);
	};
	
	object.addListener('action', executeAction);

	var destroy = function() {
		
		object.removeListener('action', executeAction);
		
		watcher.removeObject(object);
		for(var i=0, l=channels.length; i<l; i++) {
			watcher.removeChannel(channels[i]);
		}
		
		watcher.checkClean();
	};
	
	return {
		object : object,
		channels : channels,
		local : false,
		destroy : destroy
	};
};

var netRepository = {};

var publish = function(object, channels, local) {
	var container;

	if (local) {
		container = publishLocal(object, channels);
	} else {
		container = publishRemote(object, channels);
	}

	netRepository[container.object.id] = container;
	return container;
};

var unpublish = function(container) {
	delete netRepository[container.object.id];
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
module.exports.netRepository = netRepository;
