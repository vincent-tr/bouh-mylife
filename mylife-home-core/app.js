var path = require('path');

var express = require('express');

var manager = require('./manager.js');

var errorToObject = function(err) {
	var plainObject = {};
	Object.getOwnPropertyNames(err).forEach(function(key) {
		plainObject[key] = err[key];
	});
	return JSON.stringify(plainObject);
};

var create = function(port) {
	
	var app = express();
	app.set('port', port);
	app.use(express.favicon(path.join(__dirname, 'public/images/MyLife.ico')));
	// app.use(express.logger('dev'));
	app.use(express.bodyParser());
	// app.use(express.methodOverride());
	app.use(app.router);
	app.use('/static', express.static(path.join(__dirname, 'public')));
	app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
	
	app.get('/api', function(req, res) {
		res.send('api up and running');
	});
	
	app.get('/api/all', function(req, res) {
		res.json({
			plugins: manager.getPlugins(),
			pluginTypes: manager.getPluginTypes(),
			hardware: manager.getHardware(),
			links: manager.getLinks()
		});
	});
	
	app.get('/api/plugins', function(req, res) {
		res.json(manager.getPlugins());
	});
	
	app.post('/api/plugins', function(req, res) {
		try {
			var config = req.body;
			var id = manager.addPlugin(config);
			res.json(id);
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	app['delete']('/api/plugins/:id', function(req, res) {
		try {
			manager.removePlugin(req.params.id);
			res.json({});
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	app.get('/api/pluginTypes', function(req, res) {
		res.json(manager.getPluginTypes());
	});
	
	app.post('/api/pluginTypes', function(req, res) {
		try {
			var type = req.body;
			manager.addPluginType(type.name, type.content);
			res.json({});
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	app.get('/api/hardware', function(req, res) {
		res.json(manager.getHardware());
	});
	
	app.post('/api/hardware', function(req, res) {
		try {
			var config = req.body;
			var id = manager.addHardware(config);
			res.json(id);
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	app['delete']('/api/hardware/:id', function(req, res) {
		try {
			manager.removeHardware(req.params.id);
			res.json({});
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	app.get('/api/links', function(req, res) {
		res.json(manager.getLinks());
	});
	
	app.post('/api/links', function(req, res) {
		try {
			var config = req.body;
			var id = manager.addLinks(config);
			res.json(id);
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	app['delete']('/api/links/:id', function(req, res) {
		try {
			manager.removeLinks(req.params.id);
			res.json({});
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	app.get('/api/ui', function(req, res) {
		res.json(manager.getUiData());
	});
	
	app.post('/api/ui', function(req, res) {
		try {
			var data = req.body;
			manager.setUiData(data);
			res.json({});
		}catch(err) {
			console.error(err);
			res.json(500, errorToObject(err));
		}
	});
	
	return app;
};

module.exports.create = create;
