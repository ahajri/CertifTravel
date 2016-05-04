package com.ahajri.ctravel.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.ahajri.ctravel.domain.ClientErrorInformation;
import com.ahajri.ctravel.service.AppService;
import com.ahajri.ctravel.utils.ConversionUtils;
import com.ahajri.ctravel.utils.SecurityUtils;
import com.marklogic.client.ResourceNotFoundException;

public abstract class AController {

	protected static final String XML_PREFIX = ".xml";

	/** discussion root node name */
	protected static final String DISCUSS_ROOT_NODE = "discussion";
	/***/
	protected static final String AUTH_ROOT_NODE = "userauth";
	/** UserAth Collections */
	protected static final List<String> AUTH_COLLECTIONS = Arrays.asList(new String[] { "AuthCollection" });
	/** message root node name */
	protected static final String MESSAGE_ROOT_NODE = "message";
	/** Discussion MarkLogic collections */
	protected static final List<String> DISCUSS_COLLECTIONS = Arrays.asList(new String[] { "DiscussionCollection" });
	/** Date Formatter */
	protected static final SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

	
	@Autowired
	protected AppService appService;
	
	
	

	/**
	 * convert to XML
	 * 
	 * @param action:
	 *            input data map
	 * @param headers:
	 *            http request headers
	 * @param metadata:
	 *            document metadata
	 * @param position:
	 *            Lat/Lon posiiton
	 * @param root:
	 *            XML root node
	 * @return XML formatted String
	 */
	protected String getXmlData(Map<String, Object> action, HttpHeaders headers, Map<String, Object> metadata,
			String position, String root) {
		Map<String, Object> map = new HashMap<>();
		if (headers != null) {
			map.put("headers", headers.toSingleValueMap());
		}
		if (metadata != null) {
			map.put("metadata", (Map<String, Object>) metadata);
		}
		if (position != null) {
			map.put("position", position);
		}

		map.put("document", (String) action.get("document"));
		map.put("insertionTime", sdf.format(new Date()));
		map.put("docID", SecurityUtils.genUUID());
		map.putAll(action);
		return ConversionUtils.getXml(map, root);
	}

	
	
	/**
	 * Handle {@link ClientErrorInformation}
	 * 
	 * @param req:
	 *            {@link HttpServletRequest}
	 * @param ex
	 *            {@link ResourceNotFoundException}
	 * @return {@link ClientErrorInformation}
	 */
	@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "Resource Not Found, Please ensure the document already exists")
	@ExceptionHandler(ResourceNotFoundException.class)
	protected ResponseEntity<ClientErrorInformation> handleResourceNotFoundException(HttpServletRequest req,
			ResourceNotFoundException ex) {
		ClientErrorInformation e = new ClientErrorInformation(ex.getMessage(), HttpStatus.NOT_FOUND.toString());
		return new ResponseEntity<ClientErrorInformation>(e, HttpStatus.NOT_FOUND);
	}

	/**
	 * Handle {@link Exception}
	 * 
	 * @param req:
	 *            {@link HttpServletRequest}
	 * @param ex:
	 *            {@link Exception}
	 * @return {@link ClientErrorInformation}
	 */
	@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
	@ExceptionHandler(Exception.class)
	protected ResponseEntity<ClientErrorInformation> handleException(HttpServletRequest req, Exception ex) {
		ex.printStackTrace();
		ClientErrorInformation e = new ClientErrorInformation(ex.getMessage(),
				HttpStatus.INTERNAL_SERVER_ERROR.toString());
		return new ResponseEntity<ClientErrorInformation>(e, HttpStatus.INTERNAL_SERVER_ERROR);
	}
}
