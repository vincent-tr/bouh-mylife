var http = require('http');

var express = require('express');

var config = require('./config.json');
var manager = require('./manager.js');
var app = require('./app.js');

var port = config.http.port;

manager.initialize();
process.on('exit', function() {
	manager.terminate();
});

var server = http.createServer(app.create(port));
server.listen(port, function() {
	console.info('started on port ' + port);
});