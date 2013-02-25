function BGRefresh()
{
	this.changeInterval(value);
}

BGRefresh.prototype.changeInterval = function(value)
{
	this.interval = value;
	if(this.timer)
		window.clearInterval(this.timer);
	this.timer = window.setInterval(
			function(){ this.executeRequest('/mylife.home.hw.emulator/data', null, this.updateResults) }, 
			interval);
}

BGRefresh.prototype.excuteRequest = function(url, data, callback)
{
	var rq = new XMLHttpRequest();
	rq.onreadystatechange = function()
	{
		if (rq.readyState != 4)
			return;
		
		if(rq.status==200)
		{
			if(success)
				success(rq);
		}
		else
			alert('error ' + rq.status + ' : ' + rq.statusText);
	}
	rq.open("POST", url, true);
	rq.send(data);
}

BGRefresh.prototype.updateResults = function(xmlhttp)
{
	var doc = xmlhttp.responseXML;
	var pins = doc.getElementsByTagName('pin');
	var data = new Array();
	
	data['3'] = ['', ''];
	data['5'] = ['', ''];
	data['7'] = ['', ''];
	data['8'] = ['', ''];
	data['10'] = ['', ''];
	data['11'] = ['', ''];
	data['12'] = ['', ''];
	data['13'] = ['', ''];
	data['15'] = ['', ''];
	data['16'] = ['', ''];
	data['18'] = ['', ''];
	data['19'] = ['', ''];
	data['21'] = ['', ''];
	data['22'] = ['', ''];
	data['23'] = ['', ''];
	data['24'] = ['', ''];
	data['26'] = ['', ''];
	
	for (i=0;i<nodes.length;i++)
	{
		var pin = pins.item(i);
		var id = pin.getAttribute('id');
		var type = pin.getAttribute('type');
		var status = pin.getAttribute('status');
		
		var typepos;
		var typefull;
		if(pin % 2 == 0)
			typepos = 'Right';
		else
			typepos = 'Left';
		typefull = '/mylife.home.hw.emulator/resources/' + type + typepos + '.png';
		
		data[pin] = [typefull, status];
	}
	
	// TODO
}

BGRefresh.prototype.setInput = function(pinId, value)
{
	this.executeRequest('/mylife.home.hw.emulator/data', 'pinId='+pinId+"&value="+value, this.updateResults);
}
