package com.ahajri.ctravel.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.ahajri.ctravel.converters.MapEntryConverter;
import com.marklogic.client.io.InputStreamHandle;
import com.thoughtworks.xstream.XStream;

/**
 * Conversion Class Utils
 * 
 * @author
 *         <p>
 *         ahajri
 *         </p>
 *
 */
public class ConversionUtils {

	/**
	 * Convert XML to JSON
	 * 
	 * @param xml:
	 *            XML Input Character
	 * @return: JSON formatted Character
	 */
	public static final String xml2Json(String xml) {
		JSONObject xmlJSONObj = XML.toJSONObject(xml);
		String jsonPrettyPrintString = xmlJSONObj.toString(4);
		return jsonPrettyPrintString;
	}

	/**
	 * Convert Map to XML
	 * 
	 * @param map:
	 *            data {@link Map}
	 * @param rootName:
	 *            name of root XML node
	 * @return XML converted data
	 */
	public static final String getXml(Map<String, Object> map, String rootName) {
		XStream magicApi = new XStream();
		magicApi.registerConverter(new MapEntryConverter());
		magicApi.alias(rootName, Map.class);
		return magicApi.toXML(map);
	}

	/**
	 * get XML from {@link InputStreamHandle}
	 * 
	 * @param ish
	 *            {@link InputStreamHandle}
	 * @return XML formatted data
	 * @throws IOException
	 */
	public static final String getXml(InputStreamHandle ish) throws IOException {
		return IOUtils.toString(ish.get(), Charset.defaultCharset());
	}

	public static LinkedHashMap<String, String> xml2Map(InputStream xml) {
		LinkedHashMap<String, String> m = new LinkedHashMap<>();
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(xml);
			Element root = doc.getDocumentElement();
			NodeList children = root.getChildNodes();
			for (int i = 0; i < children.getLength(); i++) {
				Node child = children.item(i);
				if (child.hasChildNodes()) {
					// m.putAll(xml2Map(new
					// ByteArrayInputStream(child.toString().getBytes())));
				} else {
					String nodeName = child.getNodeName();
					String nodeValue = child.getNodeValue();
					System.out.println(nodeName + "#" + nodeValue);
					m.put(nodeName, nodeValue);
				}

			}
		} catch (ParserConfigurationException e1) {
			e1.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return m;
	}

	public static Document convertStringToDocument(String xmlStr) {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder;
		try {
			builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new InputSource(new StringReader(xmlStr)));
			return doc;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static HashMap<String, String> xml2Map(String xml) {
		HashMap<String, String> map = new HashMap<String, String>();
		Document docXml = ConversionUtils.convertStringToDocument(xml);
		Node user = docXml.getFirstChild();
		NodeList childs = user.getChildNodes();
		Node child;
		for (int i = 0; i < childs.getLength(); i++) {
			child = childs.item(i);
			map.put(child.getNodeName(), child.getTextContent());
		}
		return map;
	}
}
