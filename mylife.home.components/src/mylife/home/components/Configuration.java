package mylife.home.components;

import aQute.bnd.annotation.metatype.Meta;

/**
 * Configuration des composants
 * @author pumbawoman
 *
 */
@Meta.OCD
interface Configuration {

	@Meta.AD(
			optionValues={"mylife.home.components.Button","mylife.home.components.Light","mylife.home.components.DimmableLight"},
			optionLabels={"Button", "Light", "DimmableLight"})
	String type();
	
}
