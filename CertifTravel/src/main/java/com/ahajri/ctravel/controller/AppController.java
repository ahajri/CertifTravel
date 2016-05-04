package com.ahajri.ctravel.controller;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ahajri.ctravel.constants.ActionResultName;
import com.ahajri.ctravel.constants.Keys;
import com.ahajri.ctravel.domain.ActionResult;
import com.ahajri.ctravel.domain.ClientErrorInformation;
import com.ahajri.ctravel.search.SearchCriteria;
import com.ahajri.ctravel.service.AppService;
import com.ahajri.ctravel.utils.ConversionUtils;
import com.ahajri.ctravel.utils.SecurityUtils;
import com.marklogic.client.ResourceNotFoundException;

/**
 * Common Discussion Controller to manage messaging system
 * 
 * @author
 *         <p>
 *         ahajri
 *         <p>
 *
 */
@RestController
public class AppController extends AController {

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(AppController.class);
	

	


	/**
	 * Send Message Via JMS Broker and save it on database
	 * 
	 * @param action:
	 *            {@link Map} from JSON data
	 * @param position:
	 *            Latitude/Longitude position
	 * @return {@link ResponseEntity}
	 */
	@RequestMapping(value = "/discuss/sendMessage", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> sendMessage(@RequestBody Map<String, Object> action,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position) {
		LOGGER.debug("Send Message ....");
		action.put("position", position);
		action.put("messageID", SecurityUtils.genUUID());
		String topicName = (String) action.get("topicName");
		String discussionID = (String) action.get("discussionID");
		String text = (String) action.get("textMessage");
		ActionResult jmsResult = null;

		if (!jmsResult.getActionResultName().equals(ActionResultName.FAIL)) {
			// TODO: Save Message in database

		}
		// ActionResult result = documentService.createDocument(action,
		// DISCUSS_COLLECTIONS, xml);
		return new ResponseEntity<Object>(jmsResult.getJsonReturnData(), jmsResult.getStatus());
	}

	@RequestMapping(value = "/discuss/readMessage", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> readMessage(@RequestBody Map<String, Object> action,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position) {
		LOGGER.debug("Read message ....");
		action.put("position", position);
		action.put("messageID", SecurityUtils.genUUID());
		String topicName = (String) action.get("topicName");
		String discussionID = (String) action.get("discussionID");
		ActionResult jmsResult = null;

		if (jmsResult != null && !jmsResult.getActionResultName().equals(ActionResultName.FAIL)) {
			// TODO: change message status

		}
		// ActionResult result = documentService.createDocument(action,
		// DISCUSS_COLLECTIONS, xml);
		return new ResponseEntity<Object>(jmsResult.getJsonReturnData(), jmsResult.getStatus());
	}

	/**
	 * 
	 * @param action
	 * @param position
	 * @return
	 */
	@RequestMapping(value = "/discuss/endDiscussion", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> endDiscussion(@RequestBody Map<String, Object> action,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position) {
		LOGGER.debug("End discussion ...." + action.toString());
		ActionResult result = appService.endDiscussion(action);
		return new ResponseEntity<Object>(result.getJsonReturnData(), result.getStatus());

	}

	/**
	 * search by key value
	 * @param criteria
	 * @param headers
	 * @param position
	 * @return
	 */
	@RequestMapping(value = "/searchByKeyValue", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.FOUND)
	public ResponseEntity<Object> searchByKeyValue(@RequestBody SearchCriteria criteria,
			@RequestHeader HttpHeaders headers,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position) {
		LOGGER.debug("Search Document ...." + criteria.toString());
		LinkedHashMap<String, Object> result = appService.searchByKeyValue(criteria, DISCUSS_COLLECTIONS);
		return new ResponseEntity<Object>(result, (HttpStatus) result.get(Keys.HTTP_STATUS.getValue()));
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@RequestMapping(value = "/discuss/addMessage", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> patchMessage(@RequestBody Map map,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position) {
		map.put("position", position);
		LOGGER.debug("Patch Document ...." + map.toString());
		ActionResult result = appService.patchFragment(map);
		return new ResponseEntity<Object>(result.getJsonReturnData(), result.getStatus());
	}

	@RequestMapping(value = "/discuss/deleteTag", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> deleteFragment(@RequestBody Map map,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position) {
		map.put("position", position);
		LOGGER.debug("Patch Document ...." + map.toString());
		ActionResult result = appService.deleteTag(map);
		return new ResponseEntity<Object>(result.getJsonReturnData(), result.getStatus());
	}

	@RequestMapping(value = "/discuss/deleteDiscussion", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<Object> deleteMessage(@RequestBody Map map,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position) {
		LOGGER.debug("delete Document ...." + map.toString());
		List<String> urilList = (List<String>) map.get("docURIs");
		String[] docURIs = new String[urilList.size()];
		for (int i = 0; i < urilList.size(); i++) {
			docURIs[i] = urilList.get(0);
		}
		LinkedHashMap<String, Object> result = appService.deleteDocument(docURIs);
		return new ResponseEntity<Object>(result, (HttpStatus) result.get(Keys.HTTP_STATUS.getValue()));
	}

}
