/**
 * Publication de la structure
 */

var request = require('request');

var config = require('./config.json');

var coreUrl = config.core.url;
if (coreUrl.substr(-1) != '/') {
	coreUrl += '/';
}

var get = function(callback) {
	var url = coreUrl + 'api/ui';
	request.get(url, function(err, response, body) {
		if (err) {
			callback(JSON.parse(err));
		} else {
			callback(null, JSON.parse(body));
		}
	});
};

module.exports.get = get;
