var http = require('http');

var config = require('./config.json');
var app = require('./app.js');

var port = config.http.port;

var server = http.createServer(app.create(port));
server.listen(port, function() {
	console.info('started on port ' + port);
});
