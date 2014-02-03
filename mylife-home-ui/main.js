/**
 * 
 */

// http://www.html5rocks.com/en/tutorials/frameworks/angular-websockets/
var sys = require('sys');
var http = require('http');
var routerFactory = require('node-simple-router');
var io = require('socket.io');

var gatewayFactory = require('./gateway.js');
var config = require('./config.json');

var initServer = function() {
	
	sys.log('Server starting');
	
	// sert les fichiers par défaut dans public
	var router = routerFactory();
	router.get('/structure', function (request, response) {
		response.end('structure');
	});
	
	var server = http.createServer(router);
	
	var ioServer = io.listen(server, {
		'resource' : '/socket.io'
	});
	
	ioServer.on('connection', function(socket) {
		gatewayFactory.create(socket);
	});
	
	var listenPort = config.http.port;
	sys.log('Listening on port ' + listenPort);
	server.listen(listenPort);
	
	sys.log('Server started');
};

initServer();
