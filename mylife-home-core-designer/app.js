var path = require('path');
var request = require('request');

var express = require('express');

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
	app.use(express.errorHandler({ dumpExceptions: true, showStack: true }));
	
	app.get('/', function(req, res) {
		res.redirect('/static/index.html');
	});
	
	app.all(/^\/core\/(.+)/, function(req, res) {
		var url = coreUrl + req.params[0];
		req.pipe(request(url)).pipe(res);
	});
	
	return app;
};

module.exports.create = create;