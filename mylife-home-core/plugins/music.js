var api;

var volumeInterval = 10;

var init = function(apiarg) {
	api = apiarg;

	var state = api.netobject.netEnum('unknown', 'stop', 'play', 'pause');
	var volume = api.netobject.netRange(0, 100);
	var clazz = api.netobject.netClass(
			api.netobject.netAttribute('state', state),
			api.netobject.netAttribute('volume', volume),
			api.netobject.netAction('setstate', state),
			api.netobject.netAction('setvolume', volume),
			api.netobject.netAction('stop'),
			api.netobject.netAction('play'),
			api.netobject.netAction('pause'),
			api.netobject.netAction('state-switch'),
			api.netobject.netAction('volume-more'),
			api.netobject.netAction('volume-less'));

	return {
		'class' : clazz,
		displayName : 'Musique',
		imageUrl : api.tools.loadDefaultIcon(__filename),
		arguments : []
	};
};

var create = function(context) {

	var obj = context.object;
	obj.setAttribute('state', 'stop');
	obj.setAttribute('volume', '0');
	
	var stateTimeout = null;
	var volumeTimeout = null;
	
	obj.on('action#setstate', function(args) {
		// Attente de 5 secs avant propagation pour éviter les boucles/flood
		if(stateTimeout) {
			clearTimeout(stateTimeout);
		}
		stateTimeout = setTimeout(function() {
			obj.setAttribute('state', args[0]);
			stateTimeout = null;
		}, 5000);
	});
	
	obj.on('action#setvolume', function(args) {
		// Attente de 5 secs avant propagation pour éviter les boucles/flood
		if(volumeTimeout) {
			clearTimeout(volumeTimeout);
		}
		volumeTimeout = setTimeout(function() {
			obj.setAttribute('volume', args[0]);
			volumeTimeout = null;
		}, 5000);
	});
	
	obj.on('action#stop', function(args) {
		obj.setAttribute('state', 'stop');
	});
	
	obj.on('action#play', function(args) {
		obj.setAttribute('state', 'play');
	});
	
	obj.on('action#pause', function(args) {
		obj.setAttribute('state', 'pause');
	});
	
	obj.on('action#state-switch', function(args) {
		var state = obj.getAttribute('state');
		if(state === ('play')) {
			state = 'pause';
		} else {
			state = 'play';
		}
		obj.setAttribute('state', state);
	});
	
	obj.on('action#volume-more', function(args) {
		var vol = obj.getAttribute('volume');
		vol = parseInt(vol);
		vol += volumeInterval;
		if(vol > 100) {
			vol = 100;
		}
		obj.setAttribute('volume', vol);
	});
	
	obj.on('action#volume-less', function(args) {
		var vol = obj.getAttribute('volume');
		vol = parseInt(vol);
		vol -= volumeInterval;
		if(vol < 0) {
			vol = 0;
		}
		obj.setAttribute('volume', vol);
	});
	
	return {};
};

module.exports.ui = true;
module.exports.init = init;
module.exports.create = create;
