function BGRefresh()
{
	this.changeInterval(1000);
	this.executeRequest('/mylife.home.hw.emulator/data', this.updateResults, this);
}

BGRefresh.prototype.changeInterval = function(value)
{
	this.interval = value;
	var self = this;
	if(this.timer)
		window.clearInterval(this.timer);
	this.timer = window.setInterval(
			function(){ self.executeRequest('/mylife.home.hw.emulator/data', self.updateResults, self); }, 
			this.interval);
}

BGRefresh.prototype.executeRequest = function(url, callback, self)
{
	var rq = new XMLHttpRequest();
	rq.onreadystatechange = function()
	{
		if (rq.readyState != 4)
			return;
		
		if(rq.status==200)
		{
			if(callback)
				callback(self, rq);
		}
		else
			alert('error ' + rq.status + ' : ' + rq.statusText);
	}
	rq.open("GET", url, true);
	rq.send(null);
}

BGRefresh.prototype.updateResults = function(self, xmlhttp)
{
	var doc = xmlhttp.responseXML;
	var pins = doc.getElementsByTagName('pin');
	var data = new Array();
	
	data['3'] = ['', '', ''];
	data['5'] = ['', '', ''];
	data['7'] = ['', '', ''];
	data['8'] = ['', '', ''];
	data['10'] = ['', '', ''];
	data['11'] = ['', '', ''];
	data['12'] = ['', '', ''];
	data['13'] = ['', '', ''];
	data['15'] = ['', '', ''];
	data['16'] = ['', '', ''];
	data['18'] = ['', '', ''];
	data['19'] = ['', '', ''];
	data['21'] = ['', '', ''];
	data['22'] = ['', '', ''];
	data['23'] = ['', '', ''];
	data['24'] = ['', '', ''];
	data['26'] = ['', '', ''];
	
	for (var i=0;i<pins.length;i++)
	{
		var pin = pins.item(i);
		var id = pin.getAttribute('id');
		var type = pin.getAttribute('type');
		var status = pin.getAttribute('status');
		
		var pos;
		if(id % 2 == 0)
			pos = 'Right';
		else
			pos = 'Left';
		var typefull = '/mylife.home.hw.emulator/resources/' + type + pos + '.png';
		var statusfull = '/mylife.home.hw.emulator/resources/' + status + '.png';
		var clic = null;
		
		(function(idcopy)
		{
			if(status == 'SwitchOn')
				clic = function() { self.setInput(idcopy, 'false'); };
			else if(status == 'SwitchOff')
				clic = function() { self.setInput(idcopy, 'true'); };
		}) (id);
				
		data[id] = [typefull, statusfull, clic];
	}
	
	for(var id in data)
	{
		var ctltype = document.getElementById('pinType' + id);
		ctltype.src = data[id][0];
		var ctlstatus = document.getElementById('pinStatus' + id);
		ctlstatus.src = data[id][1];
		ctlstatus.onclick = data[id][2];
	} 
}

BGRefresh.prototype.setInput = function(pinId, value)
{
	this.executeRequest('/mylife.home.hw.emulator/data?pinId='+pinId+"&value="+value, this.updateResults, this);
}
