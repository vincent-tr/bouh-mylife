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
	app.use(express.errorHandler({
		dumpExceptions : true,
		showStack : true
	}));

	app.get('/', function(req, res) {
		res.redirect('/static/index.html');
	});

	/*
	 * app.all(/^\/core\/(.+)/, function(req, res) { var url = coreUrl +
	 * req.params[0]; req.pipe(request(url)).pipe(res); });
	 */

	app.get('/data', function(req, res) {
		manager.data(function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});

	app.post('/updateHardware', function(req, res) {
		var urlObject = JSON.parse(req.body);
		manager.updateHardware(urlObject.url, function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});

	app.post('/merge', function(req, res) {
		var newData = JSON.parse(req.body);
		manager.merge(newData, function(err, ret) {
			if (err) {
				console.error(err);
				res.json(500, errorToObject(err));
				return;
			}

			res.json(ret);
		});
	});
	
	app.post('/apply', function(req, res) {
		var mergeData = JSON.parse(req.body);
		manager.apply(mergeData, function(err, ret) {
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
