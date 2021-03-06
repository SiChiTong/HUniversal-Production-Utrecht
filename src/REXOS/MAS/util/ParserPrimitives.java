package MAS.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import MAS.equiplet.Capability;
import MAS.product.ProductStep;

public class ParserPrimitives {

	protected static JSONObject parsePosition(Position position) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("x", position.getX());
		json.put("y", position.getY());
		return json;
	}

	protected static Position parsePosition(JSONObject json) throws JSONException {
		if (json.has("x") && json.has("y")) {
			int x = json.getInt("x");
			int y = json.getInt("y");
			return new Position(x, y);
		} else {
			throw new JSONException("Parser: parsing position missing arguments x and/or y in " + json);
		}
	}

	protected static JSONArray parseCapabilties(List<Capability> capabilities) throws JSONException {
		JSONArray list = new JSONArray();
		for (Capability capability : capabilities) {
			list.put(parseCapabilty(capability));
		}
		return list;
	}

	protected static JSONArray parseServices(List<String> services) throws JSONException {
		JSONArray list = new JSONArray();
		for (String service : services) {
			list.put(parseService(service));
		}
		return list;
	}
	
	protected static JSONObject parseService(String service) throws JSONException{
		JSONObject json = new JSONObject();
		json.put("service", service);
		return json;
	}
	
	protected static List<String> parseServices(JSONArray list) throws JSONException {
		List<String> services = new ArrayList<>();
		for (int i = 0; i < list.length(); i++) {
			JSONObject json = list.getJSONObject(i);
			if (json.has("key") && json.has("value")) {
				String service = json.getString("value");
				services.add(service);
			} else {
				throw new JSONException("Parser: parsing map failed to parse " + list);
			}
		}
		return services;
	}

	protected static List<Capability> parseCapabilties(JSONArray list) throws JSONException {
		List<Capability> capabilties = new ArrayList<>();
		for (int i = 0; i < list.length(); i++) {
			JSONObject item = list.getJSONObject(i);
			capabilties.add(parseCapabilty(item));
		}
		return capabilties;
	}

	protected static JSONObject parseCapabilty(Capability capability) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("service", capability.getService());
		json.put("limitations", parseMap(capability.getLimitations()));
		json.put("duration", parseTick(capability.getDuration()));
		return json;
	}

	protected static Capability parseCapabilty(JSONObject json) throws JSONException {
		if (json.has("service") && json.has("limitations") && json.has("duration")) {
			String service = json.getString("service");
			Map<String, Object> limitations = parseMap(json.getJSONArray("limitations"));
			
			Tick duration = parseTick(json.getJSONObject("duration"));
			return new Capability(service, limitations, duration);
		} else {
			throw new JSONException("Parser: parsing capability missing arguments service and/or limitations in " + json);
		}
	}

	protected static JSONArray parseMap(Map<String, Object> map) throws JSONException {
		JSONArray list = new JSONArray();
		for (Entry<String, Object> entry : map.entrySet()) {
			JSONObject item = new JSONObject();
			item.put("key", entry.getKey());
			item.put("value", entry.getValue());
			list.put(item);
		}
		return list;
	}

	protected static Map<String, Object> parseMap(JSONArray array) throws JSONException {
		Map<String, Object> map = new HashMap<>();
		for (int i = 0; i < array.length(); i++) {
			JSONObject json = array.getJSONObject(i);
			if (json.has("key") && json.has("value")) {
				String key = json.getString("key");
				Object value = json.get("value");
				map.put(key, value);
			} else {
				throw new JSONException("Parser: parsing map failed to parse " + array);
			}
		}
		return map;
	}

	protected static JSONObject parseTick(Tick time) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("double", time.doubleValue());
		return json;
	}

	protected static Tick parseTick(JSONObject json) throws JSONException {
		if (json.has("double")) {
			return new Tick(json.getDouble("double"));
		} else if (json.has("long")) {
			return new Tick(json.getDouble("long"));
		} else {
			throw new JSONException("Parser: parsing failed to parse tick " + json);
		}
	}

	protected static JSONArray parseProductionTimes(Map<String, Double> productionTimes) throws JSONException {
		JSONArray map = new JSONArray();
		for (Entry<String, Double> entry : productionTimes.entrySet()) {
			JSONObject item = new JSONObject();
			item.put("key", entry.getKey());
			item.put("value", entry.getValue());
			map.put(item);
		}
		return map;
	}

	protected static Map<String, Double> parseProductionTimes(JSONArray list) throws JSONException {
		Map<String, Double> map = new HashMap<>();
		for (int i = 0; i < list.length(); i++) {
			JSONObject json = list.getJSONObject(i);
			if (json.has("key") && json.has("value")) {
				String key = json.getString("key");
				Double value = json.getDouble("value");
				map.put(key, value);
			} else {
				throw new JSONException("Parser: parsing production times failed to parse " + list);
			}
		}
		return map;
	}

	protected static JSONArray parseProductSteps(LinkedList<ProductStep> productSteps) throws JSONException {
		JSONArray list = new JSONArray();
		for (ProductStep productStep : productSteps) {
			list.put(parseProductStep(productStep));
		}
		return list;
	}

	protected static LinkedList<ProductStep> parseProductSteps(JSONArray list) throws JSONException {
		LinkedList<ProductStep> productSteps = new LinkedList<ProductStep>();
		for (int i = 0; i < list.length(); i++) {
			JSONObject item = list.getJSONObject(i);
			productSteps.add(parseProductStep(item));
		}
		return productSteps;
	}

	protected static JSONObject parseProductStep(ProductStep productStep) throws JSONException {
		JSONObject json = new JSONObject();
		json.put("index", productStep.getIndex());
		json.put("service", productStep.getService());
		json.put("criteria", productStep.getCriteria());
		return json;
	}

	protected static ProductStep parseProductStep(JSONObject json) throws JSONException {
		if (json.has("index") && json.has("service") && json.has("criteria")) {
			int index = json.getInt("index");
			String service = json.getString("service");
			JSONObject criteria = json.getJSONObject("criteria");
			return new ProductStep(index, service, criteria);
		} else {
			throw new JSONException("Parser: parsing product step failed to parse " + json);
		}
	}

}
