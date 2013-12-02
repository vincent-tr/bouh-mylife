package org.mylife.home.net.hub.irc.protocol;


public final class Numerics {

	public static final Numerics RPL_WELCOME = new Numerics("001",
			"RPL_WELCOME");
	public static final Numerics RPL_YOURHOST = new Numerics("002",
			"RPL_YOURHOST");
	public static final Numerics RPL_CREATED = new Numerics("003",
			"RPL_CREATED");
	public static final Numerics RPL_MYINFO = new Numerics("004", "RPL_MYINFO");
	public static final Numerics RPL_ISUPPORT = new Numerics("005",
			"RPL_ISUPPORT");
	public static final Numerics RPL_STATSCOMMANDS = new Numerics("212",
			"RPL_STATSCOMMANDS");
	public static final Numerics RPL_ENDOFSTATS = new Numerics("219",
			"RPL_ENDOFSTATS");
	public static final Numerics RPL_UMODEIS = new Numerics("221",
			"RPL_UMODEIS");
	public static final Numerics RPL_STATSUPTIME = new Numerics("242",
			"RPL_STATSUPTIME");
	public static final Numerics RPL_STATSOLINE = new Numerics("243",
			"RPL_STATSOLINE");
	public static final Numerics RPL_LUSERCLIENT = new Numerics("251",
			"RPL_LUSERCLIENT");
	public static final Numerics RPL_LUSEROP = new Numerics("252",
			"RPL_LUSEROP");
	public static final Numerics RPL_LUSERUNKNOWN = new Numerics("253",
			"RPL_LUSERUNKNOWN");
	public static final Numerics RPL_LUSERCHANNELS = new Numerics("254",
			"RPL_LUSERCHANNELS");
	public static final Numerics RPL_LUSERME = new Numerics("255",
			"RPL_LUSERME");
	public static final Numerics RPL_ADMINME = new Numerics("256",
			"RPL_ADMINME");
	public static final Numerics RPL_ADMINLOC1 = new Numerics("257",
			"RPL_ADMINLOC1");
	public static final Numerics RPL_ADMINLOC2 = new Numerics("258",
			"RPL_ADMINLOC2");
	public static final Numerics RPL_ADMINEMAIL = new Numerics("259",
			"RPL_ADMINEMAIL");
	public static final Numerics RPL_AWAY = new Numerics("301", "RPL_AWAY");
	public static final Numerics RPL_USERHOST = new Numerics("302",
			"RPL_USERHOST");
	public static final Numerics RPL_ISON = new Numerics("303", "RPL_ISON");
	public static final Numerics RPL_UNAWAY = new Numerics("305", "RPL_UNAWAY");
	public static final Numerics RPL_NOWAWAY = new Numerics("306",
			"RPL_NOWAWAY");
	public static final Numerics RPL_WHOISUSER = new Numerics("311",
			"RPL_WHOISUSER");
	public static final Numerics RPL_WHOISSERVER = new Numerics("312",
			"RPL_WHOISSERVER");
	public static final Numerics RPL_WHOISOPERATOR = new Numerics("313",
			"RPL_WHOISOPERATOR");
	public static final Numerics RPL_ENDOFWHO = new Numerics("315",
			"RPL_ENDOFWHO");
	public static final Numerics RPL_WHOISIDLE = new Numerics("317",
			"RPL_WHOISIDLE");
	public static final Numerics RPL_ENDOFWHOIS = new Numerics("318",
			"RPL_ENDOFWHOIS");
	public static final Numerics RPL_WHOISCHANNELS = new Numerics("319",
			"RPL_WHOISCHANNELS");
	public static final Numerics RPL_LISTSTART = new Numerics("321",
			"RPL_LISTSTART");
	public static final Numerics RPL_LIST = new Numerics("322", "RPL_LIST");
	public static final Numerics RPL_LISTEND = new Numerics("323",
			"RPL_LISTEND");
	public static final Numerics RPL_CHANNELMODEIS = new Numerics("324",
			"RPL_CHANNELMODEIS");
	public static final Numerics RPL_NOTOPIC = new Numerics("331",
			"RPL_NOTOPIC");
	public static final Numerics RPL_TOPIC = new Numerics("332", "RPL_TOPIC");
	public static final Numerics RPL_TOPICWHOTIME = new Numerics("333",
			"RPL_TOPICWHOTIME");
	public static final Numerics RPL_INVITING = new Numerics("341",
			"RPL_INVITING");
	public static final Numerics RPL_VERSION = new Numerics("351",
			"RPL_VERSION");
	public static final Numerics RPL_WHOREPLY = new Numerics("352",
			"RPL_WHOREPLY");
	public static final Numerics RPL_NAMREPLY = new Numerics("353",
			"RPL_NAMREPLY");
	public static final Numerics RPL_LINKS = new Numerics("364", "RPL_LINKS");
	public static final Numerics RPL_ENDOFLINKS = new Numerics("365",
			"RPL_ENDOFLINKS");
	public static final Numerics RPL_ENDOFNAMES = new Numerics("366",
			"RPL_ENDOFNAMES");
	public static final Numerics RPL_BANLIST = new Numerics("367",
			"RPL_BANLIST");
	public static final Numerics RPL_ENDOFBANLIST = new Numerics("368",
			"RPL_ENDOFBANLIST");
	public static final Numerics RPL_INFO = new Numerics("371", "RPL_INFO");
	public static final Numerics RPL_MOTD = new Numerics("372", "RPL_MOTD");
	public static final Numerics RPL_ENDOFINFO = new Numerics("374",
			"RPL_ENDOFINFO");
	public static final Numerics RPL_MOTDSTART = new Numerics("375",
			"RPL_MOTDSTART");
	public static final Numerics RPL_ENDOFMOTD = new Numerics("376",
			"RPL_ENDOFMOTD");
	public static final Numerics RPL_YOUREOPER = new Numerics("381",
			"RPL_YOUREOPER");
	public static final Numerics RPL_REHASHING = new Numerics("382",
			"RPL_REHASHING");
	public static final Numerics RPL_TIME = new Numerics("391", "RPL_TIME");

	public static final Numerics ERR_NOSUCHNICK = new Numerics("401",
			"ERR_NOSUCHNICK");
	public static final Numerics ERR_NOSUCHSERVER = new Numerics("402",
			"ERR_NOSUCHSERVER");
	public static final Numerics ERR_NOSUCHCHANNEL = new Numerics("403",
			"ERR_NOSUCHCHANNEL");
	public static final Numerics ERR_CANNOTSENDTOCHAN = new Numerics("404",
			"ERR_CANNOTSENDTOCHAN");
	public static final Numerics ERR_UNKNOWNCOMMAND = new Numerics("421",
			"ERR_UNKNOWNCOMMAND");
	public static final Numerics ERR_NONICKNAMEGIVEN = new Numerics("431",
			"ERR_NONICKNAMEGIVEN");
	public static final Numerics ERR_ERRONEUSNICKNAME = new Numerics("432",
			"ERR_ERRONEUSNICKNAME");
	public static final Numerics ERR_NICKNAMEINUSE = new Numerics("433",
			"ERR_NICKNAMEINUSE");
	public static final Numerics ERR_USERNOTINCHANNEL = new Numerics("441",
			"ERR_USERNOTINCHANNEL");
	public static final Numerics ERR_NOTONCHANNEL = new Numerics("442",
			"ERR_NOTONCHANNEL");
	public static final Numerics ERR_USERONCHANNEL = new Numerics("443",
			"ERR_USERONCHANNEL");
	public static final Numerics ERR_NOTREGISTERED = new Numerics("451",
			"ERR_NOTREGISTERED");
	public static final Numerics ERR_NEEDMOREPARAMS = new Numerics("461",
			"ERR_NEEDMOREPARAMS");
	public static final Numerics ERR_ALREADYREGISTRED = new Numerics("462",
			"ERR_ALREADYREGISTRED");
	public static final Numerics ERR_CHANNELISFULL = new Numerics("471",
			"ERR_CHANNELISFULL");
	public static final Numerics ERR_UNKNOWNMODE = new Numerics("472",
			"ERR_UNKNOWNMODE");
	public static final Numerics ERR_INVITEONLYCHAN = new Numerics("473",
			"ERR_INVITEONLYCHAN");
	public static final Numerics ERR_BANNEDFROMCHAN = new Numerics("474",
			"ERR_BANNEDFROMCHAN");
	public static final Numerics ERR_BADCHANNELKEY = new Numerics("475",
			"ERR_BADCHANNELKEY");
	public static final Numerics ERR_BADCHANMASK = new Numerics("476",
			"ERR_BADCHANMASK");
	public static final Numerics ERR_NOCHANMODES = new Numerics("477",
			"ERR_NOCHANMODES");
	public static final Numerics ERR_BANLISTFULL = new Numerics("478",
			"ERR_BANLISTFULL");
	public static final Numerics ERR_NOPRIVILEGES = new Numerics("481",
			"ERR_NOPRIVILEGES");
	public static final Numerics ERR_CHANOPRIVSNEEDED = new Numerics("482",
			"ERR_CHANOPRIVSNEEDED");
	public static final Numerics ERR_NOOPERHOST = new Numerics("491",
			"ERR_NOOPERHOST");
	public static final Numerics ERR_UMODEUNKNOWNFLAG = new Numerics("501",
			"ERR_UMODEUNKNOWNFLAG");
	public static final Numerics ERR_USERSDONTMATCH = new Numerics("502",
			"ERR_USERSDONTMATCH");

	private final String digits;
	private final String name;

	private Numerics(String digits, String name) {
		this.digits = digits;
		this.name = name;
	}

	public String getDigits() {
		return digits;
	}

	public String getName() {
		return name;
	}

	public static Message createMessage(Numerics num) {
		Message msg = new Message(num.getDigits());
		String text = Util.getResourceString(num.getName());
		if (text != null)
			msg.appendLastParameter(text);
		return msg;
	}

}
