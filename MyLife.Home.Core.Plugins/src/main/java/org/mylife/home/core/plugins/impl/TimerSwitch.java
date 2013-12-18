package org.mylife.home.core.plugins.impl;

import java.util.Timer;
import java.util.TimerTask;

import org.mylife.home.core.plugins.PluginContext;
import org.mylife.home.core.plugins.enhanced.Attribute;
import org.mylife.home.core.plugins.enhanced.EnhancedPluginFactory;
import org.mylife.home.core.plugins.enhanced.annotations.Plugin;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAction;
import org.mylife.home.core.plugins.enhanced.annotations.PluginAttribute;
import org.mylife.home.core.plugins.enhanced.annotations.PluginConfiguration;
import org.mylife.home.core.plugins.enhanced.annotations.PluginDataName;
import org.mylife.home.core.plugins.enhanced.annotations.PluginDestroy;
import org.mylife.home.core.plugins.enhanced.annotations.PluginInit;

/**
 * Minuterie
 * 
 * @author pumbawoman
 * 
 */
@Plugin(displayType = "Minuterie")
public class TimerSwitch {

	/**
	 * Fabrique
	 * 
	 * @author pumbawoman
	 * 
	 */
	public static class Factory extends EnhancedPluginFactory {
		public Factory() throws Exception {
			super(TimerSwitch.class);
		}
	}

	@PluginConfiguration
	public interface Configuration {

		/**
		 * Durée en minutes
		 * 
		 * @return
		 */
		@PluginDataName(name = "delaySeconds")
		int delay();
	}

	private final Attribute<Types.Boolean> value = new Attribute<Types.Boolean>(
			Types.Boolean.off);

	private int delay;
	private Timer timer;
	private Task task;
	private final Object timerLock = new Object();

	@PluginInit
	public void init(PluginContext context, Configuration config) {
		delay = config.delay();
		String timerName = this.getClass().getSimpleName() + " : "
				+ context.getId();
		timer = new Timer(timerName, true);
	}

	@PluginDestroy
	public void destroy() {
		timer.cancel();
		timer = null;
	}

	@PluginAttribute(index = 0)
	public Attribute<Types.Boolean> output() {
		return value;
	}

	@PluginAction(index = 1)
	public void input(Types.Boolean value) {
		if (value != Types.Boolean.on)
			return;

		initTimer();
	}

	private void initTimer() {
		synchronized (timerLock) {

			// Allumage de la lumière
			value.setValue(Types.Boolean.on);

			// Si une tâche en cours on l'annule
			if (task != null)
				task.cancel();
			// Création de la tâche d'extinction
			task = new Task();
			timer.schedule(task, delay * 1000);
		}
	}

	private void runTimer(Task source) {
		synchronized (timerLock) {
			if (task != source)
				return; // on a été annulé pendant notre exécution

			// Arrêt de la lumière
			value.setValue(Types.Boolean.off);

			task = null;
		}
	}

	private class Task extends TimerTask {
		@Override
		public void run() {
			runTimer(this);
		}
	}
}
