package org.mylife.home.ui.web;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * Servlet de base pour retour json
 * @author pumbawoman
 *
 */
public abstract class WebJsonBase extends HttpServlet {
	
	/**
	 * Logger
	 */
	private final static Logger log = Logger.getLogger(WebJsonBase.class
			.getName());

	/**
	 * 
	 */
	private static final long serialVersionUID = 7766028135173894545L;

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatch(req, resp);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		dispatch(req, resp);
	}

	private void dispatch(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		log.info("Getting json data from " + this.getClass().toString());
		
		Object data = getObjectData(req);
		
		
		resp.setContentType("application/json");
		PrintWriter writer = resp.getWriter();

		Gson gson = buildGson();
		gson.toJson(data, writer);
		writer.flush();
	}
	
	protected abstract Object getObjectData(HttpServletRequest req);
	
	private Gson buildGson() {
		return new GsonBuilder().registerTypeHierarchyAdapter(byte[].class,
	            new ByteArrayToBase64TypeAdapter()).create();
	}
	
	private static class ByteArrayToBase64TypeAdapter implements JsonSerializer<byte[]>, JsonDeserializer<byte[]> {
        public byte[] deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return Base64.decodeBase64(json.getAsString().getBytes());
        }
 
        public JsonElement serialize(byte[] src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(new String(Base64.encodeBase64(src)));
        }
    }
}
