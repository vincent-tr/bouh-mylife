/**
 * Point d'entrée
 */
var http = require('http');
var io = require('socket.io');

var config = require('./config.json');
var app = require('./app.js');
var gatewayFactory = require('./gateway.js');

var port = config.http.port;

var server = http.createServer(app.create(port));

var ioServer = io.listen(server, {
	'resource' : '/socket.io'
});

ioServer.on('connection', function(socket) {
	gatewayFactory.create(socket);
});

server.listen(port, function() {
	console.info('started on port ' + port);
});

