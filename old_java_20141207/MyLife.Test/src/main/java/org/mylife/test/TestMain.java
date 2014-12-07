package org.mylife.test;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.mylife.home.net.exchange.ExchangeManager;
import org.mylife.home.net.exchange.ui.XmlUiContainer;
import org.mylife.home.net.exchange.ui.XmlUiImage;

public class TestMain {

	public static void main(String[] args) throws IOException, JAXBException {

		XmlUiContainer container = new XmlUiContainer();
		container.images = new XmlUiImage[3];
		
		XmlUiImage imageOn = container.images[0] = new XmlUiImage();
		imageOn.id = "lightOn";
		imageOn.content = loadFile("C:\\Users\\pumbawoman\\Desktop\\imgs\\light-bulb-idea-icone-6547-16.png");

		XmlUiImage imageOff = container.images[1] = new XmlUiImage();
		imageOff.id = "lightOff";
		imageOff.content = loadFile("C:\\Users\\pumbawoman\\Desktop\\imgs\\idea-lightbulb-icone-4974-16.png");

		XmlUiImage imageMain = container.images[2] = new XmlUiImage();
		imageMain.id = "main";
		imageMain.content = loadFile("C:\\Users\\pumbawoman\\Desktop\\imgs\\main.png");
		
		OutputStream os = new FileOutputStream("C:\\Users\\pumbawoman\\Desktop\\img-ui.xml");
		ExchangeManager.exportUiContainer(container, os);
	}

	private static byte[] loadFile(String name) throws IOException {
		return IOUtils.toByteArray(new FileInputStream(name));
	}
}
