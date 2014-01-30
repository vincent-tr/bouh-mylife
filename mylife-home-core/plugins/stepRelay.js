var api;

var init = function(apiarg) {
	api = apiarg;

	var boolean = api.netobject.netEnum('off', 'on');
	var clazz = api.netobject.netClass(
			api.netobject.netAttribute('output', boolean),
			api.netobject.netAction('input', boolean));

	return {
		clazz : clazz,
		displayName : 'Telerupteur',
		arguments : []
	};
};

var create = function(context) {

	var obj = context.object;
	obj.setAttribute('output', 'off');
	
	obj.on('action#input', function(args) {
		if(args[0] !== 'on') {
			return;
		}
		
		var val = obj.getAttribute('output');
		val = val === 'on' ? 'off' : 'on';
		obj.setAttribute('output', val);
	});

	return {};
};

module.exports.ui = false;
module.exports.init = init;
module.exports.create = create;
