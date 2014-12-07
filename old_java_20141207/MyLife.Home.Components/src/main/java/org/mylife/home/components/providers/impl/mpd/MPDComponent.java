package org.mylife.home.components.providers.impl.mpd;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.bff.javampd.MPD;
import org.bff.javampd.MPDPlayer;
import org.bff.javampd.MPDPlayer.PlayerStatus;
import org.bff.javampd.events.PlayerBasicChangeEvent;
import org.bff.javampd.events.PlayerBasicChangeListener;
import org.bff.javampd.events.PlayerChangeEvent;
import org.bff.javampd.events.PlayerChangeListener;
import org.bff.javampd.events.VolumeChangeEvent;
import org.bff.javampd.events.VolumeChangeListener;
import org.bff.javampd.monitor.MPDStandAloneMonitor;
import org.mylife.home.components.providers.Component;
import org.mylife.home.components.providers.ComponentContext;
import org.mylife.home.components.providers.impl.BaseComponentFactory;
import org.mylife.home.net.ActionExecutor;
import org.mylife.home.net.NetObject;
import org.mylife.home.net.NetRepository;
import org.mylife.home.net.structure.NetAction;
import org.mylife.home.net.structure.NetAttribute;
import org.mylife.home.net.structure.NetClass;
import org.mylife.home.net.structure.NetEnum;
import org.mylife.home.net.structure.NetRange;

public class MPDComponent implements Component, PlayerChangeListener,
		PlayerBasicChangeListener, VolumeChangeListener {

	/**
	 * Fabrique
	 * @author pumbawoman
	 *
	 */
	public static class Factory extends BaseComponentFactory {

		public Factory() {
			super(MPDComponent.class, "serverAddress");
		}
	}

	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(MPDComponent.class
			.getName());

	/**
	 * Contexte
	 */
	private ComponentContext context;

	/**
	 * Client MPD
	 */
	private MPD mpd;

	/**
	 * Moniteur MPD
	 */
	private MPDStandAloneMonitor mpdMonitor;

	/**
	 * Création de l'objet
	 */
	@Override
	public void init(ComponentContext context) throws Exception {
		this.context = context;

		NetEnum playerStatusType = new NetEnum("stopped", "playing", "paused");
		NetRange playerVolumeType = new NetRange(0, 100);
		NetAttribute attr0 = new NetAttribute(0, "status", playerStatusType);
		NetAttribute attr1 = new NetAttribute(1, "volume", playerVolumeType);
		NetAction action2 = new NetAction(2, "play");
		NetAction action3 = new NetAction(3, "stop");
		NetAction action4 = new NetAction(4, "pause");
		NetAction action5 = new NetAction(5, "setVolume", playerVolumeType);
		NetClass netClass = new NetClass(attr0, attr1, action2, action3,
				action4, action5);

		NetObject obj = new NetObject(context.componentId(), netClass);
		mpd = new MPD(context.parameters().get("serverAddress"));

		// comportement
		obj.setActionExecutor("play", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				try {
					mpd.getMPDPlayer().play();
				} catch (Exception e) {
					log.log(Level.SEVERE, "error sending play", e);
				}
			}
		});
		obj.setActionExecutor("stop", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				try {
					mpd.getMPDPlayer().stop();
				} catch (Exception e) {
					log.log(Level.SEVERE, "error sending stop", e);
				}
			}
		});
		obj.setActionExecutor("pause", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				try {
					mpd.getMPDPlayer().pause();
				} catch (Exception e) {
					log.log(Level.SEVERE, "error sending pause", e);
				}
			}
		});
		obj.setActionExecutor("setVolume", new ActionExecutor() {
			@Override
			public void execute(NetObject obj, NetAction action,
					Object[] arguments) {
				int value = (Integer) arguments[0];
				try {
					mpd.getMPDPlayer().setVolume(value);
				} catch (Exception e) {
					log.log(Level.SEVERE, "error sending volume", e);
				}
			}
		});

		// abonnement aux evenements MPD
		MPDPlayer player = mpd.getMPDPlayer();
		player.addPlayerChangeListener(this);
		player.addVolumeChangeListener(this);

		mpdMonitor = new MPDStandAloneMonitor(mpd, 200);
		mpdMonitor.addPlayerChangeListener(this);
		mpdMonitor.addVolumeChangeListener(this);
		mpdMonitor.start();

		// init
		readStatus(obj);
		readVolume(obj);

		context.registerObject(obj, NetRepository.CHANNEL_HARDWARE);
	}

	@Override
	public void destroy() {
		try {
			mpdMonitor.stop();
			mpd.close();
		} catch (Exception e) {
			log.log(Level.SEVERE, "error closing connection", e);
		}
	}

	@Override
	public void volumeChanged(VolumeChangeEvent event) {
		NetObject obj = context.getObject();
		obj.setAttributeValue("volume", event.getVolume());
	}

	@Override
	public void playerChanged(PlayerChangeEvent event) {
		int id = event.getId();
		String status = null;
		switch (id) {
		case PlayerChangeEvent.PLAYER_STARTED:
			status = "playing";
			break;

		case PlayerChangeEvent.PLAYER_STOPPED:
			status = "stopped";
			break;

		case PlayerChangeEvent.PLAYER_PAUSED:
			status = "paused";
			break;

		default:
			return;
		}

		NetObject obj = context.getObject();
		obj.setAttributeValue("status", status);
	}

	@Override
	public void playerBasicChange(PlayerBasicChangeEvent event) {
		int id = event.getId();
		String status = null;
		switch (id) {
		case PlayerBasicChangeEvent.PLAYER_STARTED:
			status = "playing";
			break;

		case PlayerBasicChangeEvent.PLAYER_STOPPED:
			status = "stopped";
			break;

		case PlayerBasicChangeEvent.PLAYER_PAUSED:
			status = "paused";
			break;

		default:
			return;
		}

		NetObject obj = context.getObject();
		obj.setAttributeValue("status", status);
	}

	private void readStatus(NetObject obj) {
		try {
			PlayerStatus status = mpd.getMPDPlayer().getStatus();
			switch (status) {
			case STATUS_PLAYING:
				obj.setAttributeValue("status", "playing");
				break;

			case STATUS_STOPPED:
				obj.setAttributeValue("status", "stopped");
				break;

			case STATUS_PAUSED:
				obj.setAttributeValue("status", "paused");
				break;
			}

		} catch (Exception e) {
			log.log(Level.SEVERE, "error reading status", e);
		}
	}

	private void readVolume(NetObject obj) {
		try {
			int volume = mpd.getMPDPlayer().getVolume();
			obj.setAttributeValue("volume", volume);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error reading volume", e);
		}
	}
}
