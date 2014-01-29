var assert = require('assert');
var events = require('events');
var util = require('util');
var http = require('http');
var path = require('path');

var express = require('express');

var config = require('./config.json');
var manager = require('./manager.js');

var port = config.http.port;

var app = express();
app.set('port', port);
//app.use(express.favicon());
//app.use(express.logger('dev'));
//app.use(express.bodyParser());
//app.use(express.methodOverride());
app.use(app.router);
//app.use(express.static(path.join(__dirname, 'public')));
app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));

app.get('/api', function(req, res) {
	res.send('api up and running');
});

app.get('/api/plugins', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	res.send(manager.getPlugins());
});

app.post('/api/plugins', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	try {
		var config = req.body;
		var id = manager.addPlugin(config);
		res.send(id);
	}catch(err) {
		res.send(err, 500);
	}
});

app['delete']('/api/plugins/:id', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	try {
		manager.removePlugin(req.params.id);
		res.send({});
	}catch(err) {
		res.send(err, 500);
	}
});

app.get('/api/hardware', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	res.send(manager.getHardware());
});

app.post('/api/hardware', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	try {
		var config = req.body;
		var id = manager.addHardware(config);
		res.send(id);
	}catch(err) {
		res.send(err, 500);
	}
});

app['delete']('/api/hardware/:id', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	try {
		manager.removeHardware(req.params.id);
		res.send({});
	}catch(err) {
		res.send(err, 500);
	}
});

app.get('/api/links', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	res.send(manager.getLinks());
});

app.post('/api/links', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	try {
		var config = req.body;
		var id = manager.addLinks(config);
		res.send(id);
	}catch(err) {
		res.send(err, 500);
	}
});

app['delete']('/api/links/:id', function(req, res) {
	res.setHeader('Content-Type', 'application/json');
	try {
		manager.removeLinks(req.params.id);
		res.send({});
	}catch(err) {
		res.send(err, 500);
	}
});

var server = http.createServer(app);
server.listen(port, function() {
	console.log('started on port ' + port);
});