var path = require('path');

var express = require('express');

var componentManager = require('./componentManager.js');
var uiManager = require('./uiManager.js');

var errorToObject = function(err) {
	var plainObject = {};
	Object.getOwnPropertyNames(err).forEach(function(key) {
		plainObject[key] = err[key];
	});
	return plainObject;
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
	app.use(express.errorHandler({
		dumpExceptions : true,
		showStack : true
	}));

	app.get('/', function(req, res) {
		res.redirect('/static/index.html');
	});

	app.get('/component/data', function(req, res) {
		componentManager.data(function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});

	app.post('/component/hardware', function(req, res) {
		var urlObject = req.body;
		componentManager.hardware(urlObject.url, function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});

	app.post('/component/merge', function(req, res) {
		var newData = req.body;
		manager.merge(newData, function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});
	
	app.post('/component/apply', function(req, res) {
		var mergeData = req.body;
		componentManager.apply(mergeData, function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});

	app.get('/ui/data', function(req, res) {
		uiManager.getUiData(function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});

	app.post('/ui/data', function(req, res) {
		var data = req.body;
		uiManager.setUiData(data, function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});
	
	app.get('/ui/components', function(req, res) {
		uiManager.components(function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});
	
	return app;
};

module.exports.create = create;
