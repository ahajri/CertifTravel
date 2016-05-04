package com.ahajri.ctravel.service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.activemq.util.TimeUtils;
import org.apache.commons.collections.ListUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.NoSuchMessageException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ahajri.ctravel.constants.ActionResultName;
import com.ahajri.ctravel.constants.Keys;
import com.ahajri.ctravel.domain.ActionResult;
import com.ahajri.ctravel.repository.XmlDataRepository;
import com.ahajri.ctravel.search.SearchCriteria;
import com.ahajri.ctravel.utils.ConversionUtils;
import com.ahajri.ctravel.utils.SecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.marklogic.client.io.DocumentMetadataHandle;

@Service("appService")
public class AppService {

	/** Repository instantiation */
	@Autowired
	private XmlDataRepository xmlDataRepository;

	@Autowired
	ReloadableResourceBundleMessageSource messageSource;

	/** Date formatter */
	private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	/** Logger */
	private static final Logger LOGGER = Logger.getLogger(AppService.class);

	/**
	 * Create User
	 * 
	 * @param action
	 *            {@link Map} input map data
	 * @param collectionNames
	 *            data collections list
	 * @return {@link HashMap}containing all result details
	 */
	public LinkedHashMap<String, Object> createUser(Map<String, Object> action, List<String> collectionNames,
			String xml) {
		LinkedHashMap<String, Object> result = null;
		long startTime = System.currentTimeMillis();
		DocumentMetadataHandle metadata = new DocumentMetadataHandle();
		if (!ListUtils.isEqualList(collectionNames, null)) {
			metadata.getCollections().addAll(collectionNames);
		}
		try {
			System.out.println("##Message###" + messageSource
					.getMessage(new DefaultMessageSourceResolvable("UserAuth.create.success"), Locale.ENGLISH));
		} catch (NoSuchMessageException e1) {
			e1.printStackTrace();
		}
		try {

			result = xmlDataRepository.persist(xml, metadata, (String) action.get("document"));
			putSuccess(result, "User Created", HttpStatus.CREATED);
		} catch (JsonProcessingException e) {
			putError(result, e);
		} catch (IOException e) {
			putError(result, e);
		}
		long duration = System.currentTimeMillis() - startTime;
		result.put(Keys.EXEC_DURATION.getValue(), TimeUtils.printDuration(duration));
		return result;
	}

	/**
	 * populate JSON result
	 * 
	 * @param result:
	 *            result data map
	 */
	private void putSuccess(HashMap<String, Object> result, String msg, HttpStatus status) {
		result.put(Keys.EXEC_SUCCESSFUL.getValue(), true);
		result.put(Keys.HTTP_STATUS.getValue(), status);
		result.put(Keys.MESSAGE.getValue(), msg);
	}

	/**
	 * 
	 * @param result:
	 *            {@link Map} result data map
	 * @param e:
	 *            Exception
	 */
	private void putError(HashMap<String, Object> result, Exception e) {
		LOGGER.error(e);
		result.put(Keys.HTTP_STATUS.getValue(), HttpStatus.INTERNAL_SERVER_ERROR);
		result.put(Keys.EXEC_SUCCESSFUL.getValue(), false);
		result.put(Keys.MESSAGE.getValue(), e.getMessage());
	}

	/**
	 * Search Document
	 * 
	 * @param criteria:
	 *            {@link SearchCriteria}
	 * @return {@link ActionResult}
	 */
	public ActionResult searchDocument(SearchCriteria criteria) {
		ActionResult result = new ActionResult();
		result.setActionResultName(ActionResultName.SUCCESSFULL);
		result.setStatus(HttpStatus.FOUND);
		try {
			List<HashMap<String, String>> found = xmlDataRepository.searchDocument(criteria.getQuery());
			result.setJsonReturnData(ConversionUtils.xml2Json(found.toString()));
		} catch (IOException e) {
			result = getErrorResult();
			result.setJsonReturnData(e.getMessage());
		}
		return result;
	}

	/**
	 * 
	 * @return {@link ActionResult}
	 */
	private ActionResult getErrorResult() {
		ActionResult result = new ActionResult();
		result.setActionResultName(ActionResultName.FAIL);
		result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
		return result;
	}

	/**
	 * 
	 * @return {@link ActionResult}
	 */
	private ActionResult getCreatedResult() {
		ActionResult result = new ActionResult();
		result.setActionResultName(ActionResultName.SUCCESSFULL);
		result.setStatus(HttpStatus.CREATED);
		return result;
	}

	/**
	 * Search in XML By Key Value
	 * 
	 * @param criteria:
	 *            {@link Criteria}
	 * 
	 * @param discussCollections
	 *            : Collections names
	 * 
	 * @return {@link ActionResult}
	 */
	// public ActionResult searchByKeyValue(SearchCriteria criteria,
	// List<String> discussCollections) {
	// ActionResult result = new ActionResult();
	// DocumentMetadataHandle metadata = new DocumentMetadataHandle();
	// if (!ListUtils.isEqualList(discussCollections, null)) {
	// metadata.getCollections().addAll(discussCollections);
	// }
	// List<String> foundData = null;
	// try {
	// foundData = xmlDataRepository.searchByKeyValue(criteria,
	// discussCollections, metadata, "discussions");
	// } catch (IOException e) {
	// result.setActionResultName(ActionResultName.FAIL);
	// result.setStatus(HttpStatus.NOT_FOUND);
	// result.setJsonReturnData(e.getMessage());
	// }
	// result.setStatus(HttpStatus.FOUND);
	// result.setActionResultName(ActionResultName.SUCCESSFULL);
	// result.setJsonReturnData(foundData);
	// return result;
	// }
	/**
	 * Search in XML By Key Value
	 * 
	 * @param criteria:
	 *            Search Criteria
	 * @param discussCollections:
	 *            data Collections
	 * @return: {@link LinkedHashMap} found XML data
	 */
	public LinkedHashMap<String, Object> searchByKeyValue(SearchCriteria criteria, List<String> discussCollections) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		DocumentMetadataHandle metadata = new DocumentMetadataHandle();
		if (!ListUtils.isEqualList(discussCollections, null)) {
			metadata.getCollections().addAll(discussCollections);
		}
		try {
			List<HashMap<String, String>> foundData = xmlDataRepository.searchByKeyValue(criteria, discussCollections,
					metadata, "found");
			if (foundData.size() == 0) {
				result.put(Keys.HTTP_STATUS.getValue(), HttpStatus.NOT_FOUND);
			} else {
				result.put(Keys.HTTP_STATUS.getValue(), HttpStatus.FOUND);
			}
			result.put(Keys.EXEC_SUCCESSFUL.getValue(), true);
			result.put(Keys.RETURNED_DATA.getValue(), foundData);
		} catch (IOException e) {
			result.put(Keys.EXEC_SUCCESSFUL.getValue(), false);
			result.put(Keys.HTTP_STATUS.getValue(), HttpStatus.NOT_FOUND);
			result.put(Keys.RETURNED_DATA.getValue(), e.getMessage());
		}
		return result;
	}

	/**
	 * Patch Fragment
	 * 
	 * @param docID:
	 *            Document ID
	 * @param fragment:
	 *            XML fragment to add
	 * @return {@link ActionResult}
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ActionResult patchFragment(Map map) {
		String docID = (String) map.get("docID");
		String fragment = (String) map.get("fragment");
		String tag = (String) map.get("tag");
		String senderID = (String) map.get("senderID");
		ActionResult result = new ActionResult();
		result.setJsonReturnData(docID);
		Map m = new HashMap<>();
		m.put("text", fragment);
		m.put("senderID", senderID);
		m.put("datetime", sdf.format(new Date()));
		m.put("acquitted", false);
		m.put("messageID", SecurityUtils.genUUID());
		String xmlFragment = ConversionUtils.getXml(m, "message");
		boolean isPathced = xmlDataRepository.patchDocument(docID, xmlFragment, tag);
		if (isPathced) {
			result.setStatus(HttpStatus.OK);
			result.setActionResultName(ActionResultName.SUCCESSFULL);
		} else {
			result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			result.setActionResultName(ActionResultName.FAIL);
			result.getTechMessages().add("Patching fragment fails on " + docID);
		}
		return result;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public ActionResult updateFragmentValue(Map map) {
		String docID = (String) map.get("docID");
		String fragment = (String) map.get("fragment");
		String path = (String) map.get("path");
		String senderID = (String) map.get("senderID");
		boolean acquitted = (Boolean) map.get("acquitted");
		ActionResult result = new ActionResult();
		result.setJsonReturnData(docID);
		Map m = new HashMap<>();
		m.put("text", fragment);
		m.put("senderID", senderID);
		m.put("datetime", sdf.format(new Date()));
		m.put("acquitted", acquitted);
		String xmlFragment = ConversionUtils.getXml(m, "message");
		boolean isPathced = xmlDataRepository.replacePatch(docID, xmlFragment, path);
		if (isPathced) {
			result.setStatus(HttpStatus.OK);
			result.setActionResultName(ActionResultName.SUCCESSFULL);
		} else {
			result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			result.setActionResultName(ActionResultName.FAIL);
			result.getTechMessages().add("PAtrching fragment fails on " + docID);
		}
		return result;
	}

	/**
	 * delete documents
	 * 
	 * @param docURIs:
	 *            document URI tab
	 * @return {@link ActionResultName}
	 */
	public LinkedHashMap<String, Object> deleteDocument(String[] docURIs) {
		LinkedHashMap<String, Object> result = new LinkedHashMap<String, Object>();
		if (xmlDataRepository.deleteDocument(docURIs)) {
			result.put(Keys.EXEC_SUCCESSFUL.getValue(), true);
			result.put(Keys.HTTP_STATUS.getValue(), HttpStatus.OK);
			result.put(Keys.MESSAGE.getValue(), "Document deleted !");
		} else {
			result.put(Keys.EXEC_SUCCESSFUL.getValue(), false);
			result.put(Keys.HTTP_STATUS.getValue(), HttpStatus.INTERNAL_SERVER_ERROR);
			result.put(Keys.MESSAGE.getValue(), "Ooops, cannot delete document !");
		}
		return result;
	}

	/**
	 * 
	 * @param action
	 * @return {@link ActionResult}
	 */
	public ActionResult endDiscussion(Map<String, Object> action) {
		ActionResult result = null;
		String docID = (String) action.get("docID");
		String xpath = (String) action.get("xpath");
		Map<String, Object> m = new HashMap<>();
		m.put("endTime", sdf.format(new Date()));
		String xml = ConversionUtils.getXml(m, "endTime");
		xmlDataRepository.replacePatch(docID, xml, xpath);
		return result;
	}

	/**
	 * 
	 * @param map
	 *            : JSON data <code>
	 * Example: {
	 * 			"docID":"/discuss/discussion_20160420151917.xml",
	 *  		"path":"discussion/metadata/remoteUser"
	 *  		}
	 *			</code>
	 * 
	 * @return {@link ActionResultName}
	 */
	public ActionResult deleteTag(Map map) {
		String docID = (String) map.get("docID");
		String path = (String) map.get("path");
		ActionResult result = new ActionResult();
		result.setJsonReturnData(docID);
		boolean isPathced = xmlDataRepository.deleteTag(docID, path);
		if (isPathced) {
			result.setStatus(HttpStatus.OK);
			result.setActionResultName(ActionResultName.SUCCESSFULL);
		} else {
			result.setStatus(HttpStatus.INTERNAL_SERVER_ERROR);
			result.setActionResultName(ActionResultName.FAIL);
			result.getTechMessages().add("PAtrching fragment fails on " + docID);
		}
		return result;
	}
}
