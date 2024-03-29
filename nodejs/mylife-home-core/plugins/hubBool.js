var api;

var init = function(apiarg) {
	api = apiarg;

	var boolean = api.netobject.netEnum('off', 'on');
	var clazz = api.netobject.netClass(
			api.netobject.netAttribute('output', boolean),
			api.netobject.netAction('input', boolean));

	return {
		'class' : clazz,
		displayName : 'Hub Booléen',
		imageUrl : api.tools.loadDefaultIcon(__filename),
		arguments : []
	};
};

var create = function(context) {

	var obj = context.object;
	obj.setAttribute('output', 'off');
	
	obj.on('action#input', function(args) {
		obj.setAttribute('output', args[0]);
	});

	return {};
};

module.exports.ui = false;
module.exports.init = init;
module.exports.create = create;
