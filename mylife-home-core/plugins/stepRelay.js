var api;

var init = function(apiarg) {
	api = apiarg;

	var boolean = api.netobject.netEnum('off', 'on');
	var clazz = api.netobject.netClass(
			api.netobject.netAttribute('output', boolean),
			api.netobject.netAction('input', boolean),
			api.netobject.netAction('on', boolean),
			api.netobject.netAction('off', boolean));

	return {
		'class' : clazz,
		displayName : 'Telerupteur',
		imageUrl : api.tools.loadDefaultIcon(__filename),
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
	
	var setValue = function(value) {
		var val = obj.getAttribute('output');
		if(val !== value) {
			obj.setAttribute('output', value);
		}
	};
	
	obj.on('action#on', function(args) {
		setValue('on');
	});

	obj.on('action#off', function(args) {
		setValue('off');
	});

	return {};
};

module.exports.ui = false;
module.exports.init = init;
module.exports.create = create;
