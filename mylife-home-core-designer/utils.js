var config = require('./config.json');

var coreUrl = config.core.url;
if (coreUrl.substr(-1) != '/') {
	coreUrl += '/';
}

var arrayKeepIf = function(array, predicate) {
	var copy = array.concat();
	array.length = 0;
	
	for(var i=0, l=copy.length; i<l; i++) {
		var item = copy[i];
		if(predicate(item)) {
			array.push(item);
		}
	}
	
	return array;
};

var arrayRemoveIf = function(array, predicate) {
	return arrayKeepIf(array, function(item) {
		return !predicate(item);
	});
};

module.exports.coreUrl = coreUrl;

module.exports.arrayKeepIf = arrayKeepIf;
module.exports.arrayRemoveIf = arrayRemoveIf;
