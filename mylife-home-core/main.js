var assert = require('assert');
var events = require('events');
var util = require('util');
var http = require('http');
var path = require('path');

var express = require('express');

var config = require('./config.json');
var manager = require('./manager.js');
var app = require('./app.js');

var port = config.http.port;

manager.initialize();

var server = http.createServer(app.create(port));
server.listen(port, function() {
	console.info('started on port ' + port);
});