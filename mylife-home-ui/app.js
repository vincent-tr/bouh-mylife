/**
 * Gestion de l'application
 */

var path = require('path');

var express = require('express');

var gateway = require('./gateway.js');
var structure = require('./structure.js');

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
	app.use(app.router);
	app.use('/static', express.static(path.join(__dirname, 'public')));
	app.use(express.errorHandler({
		dumpExceptions : true,
		showStack : true
	}));

	app.get('/', function(req, res) {
		res.redirect('/static/index.html');
	});


	app.get('/structure', function(req, res) {
		structure.get(function(err, ret) {
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
