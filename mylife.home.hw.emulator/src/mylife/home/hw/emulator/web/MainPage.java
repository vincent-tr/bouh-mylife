package mylife.home.hw.emulator.web;

import mylife.home.hw.emulator.web.render.Division;
import mylife.home.hw.emulator.web.render.ImageRender;
import mylife.home.hw.emulator.web.render.Page;
import mylife.home.hw.emulator.web.render.StringRender;
import mylife.home.hw.emulator.web.render.Table;

/**
 * Page principale
 * @author pumbawoman
 *
 */
public class MainPage extends Page {

	public MainPage() {

		this.setTitle("MyLife.Home HW Emulator");
		this.setIcon("MyLife-128.png");
		this.getScripts().add("bgrefresh.js");
		this.getStyles().add("mylife.css");
		
		Table table = new Table();
		this.getContent().add(table);
		table.getAttributes().put("width", "100%");
		
		Table title = new Table();
		title.getAttributes().put("align", "center");
		table.getContent().add(new Table.Row(new Table.Data(title)));
		title.getContent().add(
				new Table.Row(
						new Table.Data(new ImageRender("MyLife-48.png")),
						new Table.Data(new StringRender("<h1>MyLife.Home HW Emulator</h1>"))));
		
		Division container = new Division();
		table.getContent().add(new Table.Row((Table.Data)new Table.Data(container).putAttribute("align", "center")));
		container.getAttributes().put("class", "container");
		container.getAttributes().put("style", "position: relative; width: 435px; height: 581px");
		container.getContent().add(new ImageRender("LayoutBack.png").putAttribute("style", "z-index:-1"));
		// gauche : 173px , droite : 238px , top = (68 + idx * 36)px
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType3").putAttribute("style", "position:absolute; top:104px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType5").putAttribute("style", "position:absolute; top:140px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType7").putAttribute("style", "position:absolute; top:176px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType8").putAttribute("style", "position:absolute; top:176px; left:238px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType10").putAttribute("style", "position:absolute; top:212px; left:238px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType11").putAttribute("style", "position:absolute; top:248px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType12").putAttribute("style", "position:absolute; top:248px; left:238px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType13").putAttribute("style", "position:absolute; top:284px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType15").putAttribute("style", "position:absolute; top:320px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType16").putAttribute("style", "position:absolute; top:320px; left:238px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType18").putAttribute("style", "position:absolute; top:356px; left:238px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType19").putAttribute("style", "position:absolute; top:392px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType21").putAttribute("style", "position:absolute; top:428px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType22").putAttribute("style", "position:absolute; top:428px; left:238px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputLeft.png").putAttribute("id", "pinType23").putAttribute("style", "position:absolute; top:464px; left:173px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType24").putAttribute("style", "position:absolute; top:464px; left:238px; z-index:1"));
		container.getContent().add(new ImageRender("AnalogOuputRight.png").putAttribute("id", "pinType26").putAttribute("style", "position:absolute; top:500px; left:238px; z-index:1"));
	}
	
}
