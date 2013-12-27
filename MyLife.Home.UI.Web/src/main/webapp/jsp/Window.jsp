<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<%@ page import="org.mylife.home.common.web.WebTools"%>
<%@ page import="org.mylife.home.ui.web.WebUiTools"%>
<%@ page import="org.mylife.home.ui.structure.*"%>
<%@ page import="org.apache.commons.lang3.*"%>

<%
	Window window = (Window) pageContext.getRequest().getAttribute(
			"window");
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<title>MyLife.Home.UI.Web</title>
<script src="http://code.jquery.com/jquery-1.9.1.js"></script>
<script src="http://code.jquery.com/ui/1.10.3/jquery-ui.js"></script>

<script>
	$(function() {
		if (!("WebSocket" in window)) {
			alert("WebSocket NOT supported !");
			return;
		}
		
		window.mylife = {
			
			net : {
				
				ws : new WebSocket('<%=WebUiTools.webSocketUrl(pageContext)%>'),

				netOpen : function() {
					console.log('netEndpoint opened');
					this.sendWindows();
				},
				
				netClose : function() {
					console.log('netEndpoint closed');
				},
				
				sendMessage : function(msg) {
					console.log('netEndpoint send : ' + msg);
					this.ws.send(msg);
				},
				
				receiveMessage : function(evt) {
					var msg = evt.data;
					console.log('netEndpoint receive : ' + msg);
					var args = msg.split(' ');
					if(args.length == 0)
						return;
					var command = args[0];
					args.shift();
					
					switch(command) {
					
					case 'icon':
						if(args.length < 3)
							return;
						this.receiveIcon(args[0], args[1], args[2]);
						break;
						
					case 'online':
						if(args.length < 2)
							return;
						this.receiveOnlineChanged(args[0], args[1], true);
						break;
						
					case 'offline':
						if(args.length < 2)
							return;
						this.receiveOnlineChanged(args[0], args[1], false);
						break;
						
					case 'structureChanged':
						this.receiveStructureChanged();
						break;
					}
				},
				
				/*
				 * Format de message :
				 * 
				 * c -> s : window window1.id window2.id
				 * 
				 * c -> s : action window.id component.id primary/secondary (seulement
				 * si core action)
				 * 
				 * s -> c : icon window.id component.id image.id
				 * 
				 * s -> c : online window.id component.id
				 * 
				 * s -> c : offline window.id component.id
				 * 
				 * s -> c : structureChanged
				 */
				
				receiveIcon : function(windowId, componentId, imageId) {
					window.mylife.ui.changeIcon(windowId, componentId, imageId);
				},
				
				receiveOnlineChanged : function(windowId, componentId, online) {
					window.mylife.ui.changeIcon(windowId, componentId, 'null');
				},
				
				receiveStructureChanged : function() {
					
				},
				
				sendWindows : function() {
					var msg = 'window';
					for(var i = 0, len = window.mylife.windows.list.length; i < len; i++) {
						msg += ' ' + window.mylife.windows.list[i];
					}
					this.sendMessage(msg);
				},
				
				sendAction : function(windowId, componentId, primary) {
					var msg = 'action ' + windowId + ' ' + componendId + ' ' + (primary ? 'primary' : 'secondary');
					this.sendMessage(msg);
				},
			},
			
			windows : {
				
				list : ['<%=window.getId()%>'],
				
				push : function(id) {
					this.list.push(id);
					window.mylife.net.sendWindows();
				},
				
				pop : function() {
					this.list.pop();
					window.mylife.net.sendWindows();
				},
			},
			
			ui : {
				changeIcon : function(windowId, componentId, imageId) {
					// on trouve le div correspondant et on cache toutes les images dedans
					$('#' + windowId + ':' + componentId + ' > img').hide();
					
					if(imageId == 'null')
						return;
					
					$('#' + windowId + ':' + componentId + ':' + imageId).show();
				}
			},
		};
		
		window.mylife.net.ws.onopen = function() { window.mylife.net.netOpen(); };
		window.mylife.net.ws.onclose = function() { window.mylife.net.netClose(); };
		window.mylife.net.ws.onmessage = function(evt) { window.mylife.net.receiveMessage(evt); };
	});
</script>
</head>
<body>
	<jsp:include page="Panel.jsp" />
</body>
</html>
