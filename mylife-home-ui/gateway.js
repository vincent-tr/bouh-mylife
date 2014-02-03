/**
 * 
 */

var sys = require('sys');
var irc = require('irc');

var config = require('./config.json');

module.exports.create = function(socket) {

	var endpoint = socket.address();
	sys.log('connection from ' + endpoint.address + ':' + endpoint.port);
	var id = endpoint.address + '-' + endpoint.port;
	var channel = config.irc.channels[0];

	var ircclient = new irc.Client(config.irc.server, id, config.irc);

	var partall = function() {
		socket.emit('partall', {});
	};

	var part = function(nick) {
		socket.emit('part', {
			nick : nick
		});
	};

	var join = function(nick) {
		socket.emit('join', {
			nick : nick
		});
	};

	var changed = function(oldnick, newnick) {
		socket.emit('changed', {
			oldnick : oldnick,
			newnick : newnick
		});
	};

	// ------------------ socket -------------------

	socket.on('disconnect', function(socket) {
		console.log('client disconnected : ' + id);

		socket.close();
		ircclient.disconnect();
	});

	socket.on('message', function(data) {
		// data = { target, message }
		ircclient.say(channel, data.target + ' ' + data.message);
	});

	// ------------------- irc -------------------

	ircclient.on('error', function(message) {
		console.log('client error : ' + id + ', ' + message);
	});

	ircclient.on('close', function() {
		partall();
	});

	ircclient.on('join', function(channel, who) {
		join(who);
	});

	ircclient.on('part', function(channel, who, reason) {
		part(who);
	});

	ircclient.on('kick', function(channel, who, by, reason) {
		part(who);
	});

	ircclient.on('kill', function(who, reason) {
		part(who);
	});

	ircclient.on('quit', function(who, reason) {
		part(who);
	});

	ircclient.on('nick', function(oldnick, newnick) {
		changed(oldnick, newnick);
	});

	ircclient.on('names', function(channel, users) {
		users.forEach(function(user) {
			join(user);
		});
	});
};