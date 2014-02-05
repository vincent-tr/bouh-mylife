var path = require('path');
var request = require('request');

var express = require('express');
var async = require('async');

var config = require('./config.json');

var errorToObject = function(err) {
	var plainObject = {};
	Object.getOwnPropertyNames(err).forEach(function(key) {
		plainObject[key] = err[key];
	});
	return JSON.stringify(plainObject);
};

var create = function(port) {

	var coreUrl = config.core.url;
	if (coreUrl.substr(-1) != '/') {
		coreUrl += '/';
	}

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

		var fetch = function(url, callback) {
			request.get(url, function(err, response, body) {
				if (err) {
					callback(err);
				} else {
					callback(null, JSON.parse(body));
				}
			});
		};
		
		async.parallel({
			plugins : function(cb) { fetch(coreUrl + 'api/plugins', cb); },
			pluginTypes : function(cb) { fetch(coreUrl + 'api/pluginTypes', cb); },
			hardware : function(cb) { fetch(coreUrl + 'api/hardware', cb); },
			links : function(cb) { fetch(coreUrl + 'api/links', cb); }
		}, function(err, ret) {
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
