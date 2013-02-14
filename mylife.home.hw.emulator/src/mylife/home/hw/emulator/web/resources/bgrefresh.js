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
			function(){ this.executeRequest('/mylife.home.hw.emulator/refresh', null, this.updateResults) }, 
			interval);
}

BGRefresh.prototype.excuteRequest = function(url, data, callback)
{
	rq = new XMLHttpRequest();
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
	// TODO
}

BGRefresh.prototype.setInput = function(pinId, value)
{
	this.executeRequest('/mylife.home.hw.emulator/update', 'pinId='+pinId+"&value="+value, this.updateResults);
}
