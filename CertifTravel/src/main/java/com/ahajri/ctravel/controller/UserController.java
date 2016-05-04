package com.ahajri.ctravel.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

import org.apache.log4j.Logger;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.ahajri.ctravel.constants.Keys;
import com.ahajri.ctravel.search.SearchCriteria;
import com.ahajri.ctravel.utils.SecurityUtils;

@RestController
public class UserController extends AController {

	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(UserController.class);

	@RequestMapping(value = "/auth/createUser", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.CREATED)
	public ResponseEntity<LinkedHashMap<String, Object>> createUser(@RequestBody Map<String, Object> action,
			@RequestHeader HttpHeaders headers,
			@CookieValue(value = "position", defaultValue = "48.890019, 2.316873") String position,
			@Context HttpServletRequest req) {
		LOGGER.debug("Create User ....");
		// TODO: Get position later in LAT/LON
		Map<String, Object> metadata = new LinkedHashMap<String, Object>();
		metadata.put("remoteAddr", req.getRemoteAddr());
		metadata.put("remoteHost", req.getRemoteHost());
		metadata.put("remoteUser", req.getRemoteUser());
		metadata.put("remotePort", req.getRemotePort());
		String email = (String) action.get("email");
		// Encrypt Password
		String password = (String) action.get("password");
		action.put("password", SecurityUtils.md5(password));
		// Create document name
		String docName = (String) action.get("document");
		if (docName == null) {
			docName = "/auth/user_" + email.split("\\.")[0].replace("@", "_") + XML_PREFIX;
		}
		SearchCriteria criteria = new SearchCriteria();
		criteria.getKeyValues().put(Keys.DOCUMENT.getValue(), docName);
		LinkedHashMap<String, Object> searchResult = appService.searchByKeyValue(criteria, AUTH_COLLECTIONS);
		if (searchResult.get(Keys.HTTP_STATUS.getValue()).equals(HttpStatus.FOUND)) {
			LinkedHashMap<String, Object> r = new LinkedHashMap<>();
			r.put(Keys.EXEC_SUCCESSFUL.getValue(), false);
			r.put(Keys.HTTP_STATUS.getValue(), HttpStatus.CONFLICT);
			r.put(Keys.MESSAGE.getValue(), "User already exists with this email");
			r.put(Keys.RETURNED_DATA.getValue(), searchResult.get(Keys.RETURNED_DATA.getValue()));
			return new ResponseEntity<LinkedHashMap<String, Object>>(r,
					(HttpStatus) r.get(Keys.HTTP_STATUS.getValue()));
		}
		action.put(Keys.DOCUMENT.getValue(), docName);
		String xml = getXmlData(action, headers, metadata, position, AUTH_ROOT_NODE);
		LinkedHashMap<String, Object> result = appService.createUser(action, AUTH_COLLECTIONS, xml);
		return new ResponseEntity<LinkedHashMap<String, Object>>(result,
				(HttpStatus) result.get(Keys.HTTP_STATUS.getValue()));
	}

	@RequestMapping(value = "/auth/searchUser", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.FOUND)
	public ResponseEntity<LinkedHashMap<String, Object>> searchUser(@RequestBody Map<String, Object> action) {
		LOGGER.debug("search User ....");
		Iterator<Entry<String, Object>> iterator = action.entrySet().iterator();
		SearchCriteria criteria = new SearchCriteria();
		while (iterator.hasNext()) {
			Entry<String, Object> entry = iterator.next();
			String key = entry.getKey();
			Object value = entry.getValue();
			criteria.getKeyValues().put(key, value);
		}
		LinkedHashMap<String, Object> r = new LinkedHashMap<>();
		LinkedHashMap<String, Object> searchResult = appService.searchByKeyValue(criteria, AUTH_COLLECTIONS);
		if (searchResult.get(Keys.HTTP_STATUS.getValue()).equals(HttpStatus.FOUND)) {
			r.put(Keys.EXEC_SUCCESSFUL.getValue(), true);
			r.put(Keys.HTTP_STATUS.getValue(), HttpStatus.FOUND);
			r.put(Keys.MESSAGE.getValue(), "User found");
			return new ResponseEntity<LinkedHashMap<String, Object>>(r,
					(HttpStatus) r.get(Keys.HTTP_STATUS.getValue()));
		} else {
			r.put(Keys.EXEC_SUCCESSFUL.getValue(), false);
			r.put(Keys.HTTP_STATUS.getValue(), HttpStatus.NOT_FOUND);
			r.put(Keys.MESSAGE.getValue(), "User not found");
		}

		return new ResponseEntity<LinkedHashMap<String, Object>>(r, HttpStatus.NOT_FOUND);
	}

	@SuppressWarnings("unchecked")
	@RequestMapping(value = "/auth/deleteUser", method = RequestMethod.POST)
	@ResponseStatus(HttpStatus.OK)
	public ResponseEntity<LinkedHashMap<String, Object>> deleteUser(@RequestBody Map<String, Object> action) {
		LOGGER.debug("delete User ....");
		String email = (String) action.get("email");
		LinkedHashMap<String, Object> r = new LinkedHashMap<>();
		SearchCriteria criteria = new SearchCriteria();
		criteria.getKeyValues().put(Keys.EMAIL.getValue(), email);
		LinkedHashMap<String, Object> searchResult = appService.searchByKeyValue(criteria, AUTH_COLLECTIONS);
		if (searchResult.get(Keys.HTTP_STATUS.getValue()).equals(HttpStatus.FOUND)) {
			List<HashMap<String, String>> foundData = (List<HashMap<String, String>>) searchResult
					.get(Keys.RETURNED_DATA.getValue());
			r = appService.deleteDocument(new String[] { foundData.get(0).get(Keys.DOCUMENT.getValue()) });
			r.put(Keys.EXEC_SUCCESSFUL.getValue(), true);
			r.put(Keys.HTTP_STATUS.getValue(), HttpStatus.OK);
			r.put(Keys.MESSAGE.getValue(), "User deleted !");
			return new ResponseEntity<LinkedHashMap<String, Object>>(r,
					(HttpStatus) r.get(Keys.HTTP_STATUS.getValue()));
		} else if (searchResult.get(Keys.HTTP_STATUS.getValue()).equals(HttpStatus.NOT_FOUND)) {
			r.put(Keys.EXEC_SUCCESSFUL.getValue(), false);
			r.put(Keys.HTTP_STATUS.getValue(), HttpStatus.NOT_FOUND);
			r.put(Keys.MESSAGE.getValue(), "User not found !");
			return new ResponseEntity<LinkedHashMap<String, Object>>(r,
					(HttpStatus) r.get(Keys.HTTP_STATUS.getValue()));
		} else {
			r.put(Keys.EXEC_SUCCESSFUL.getValue(), false);
			r.put(Keys.HTTP_STATUS.getValue(), HttpStatus.INTERNAL_SERVER_ERROR);
			r.put(Keys.MESSAGE.getValue(), "Error while deleting User !");
		}
		return new ResponseEntity<LinkedHashMap<String, Object>>(r, (HttpStatus) r.get(Keys.HTTP_STATUS.getValue()));
	}
}
