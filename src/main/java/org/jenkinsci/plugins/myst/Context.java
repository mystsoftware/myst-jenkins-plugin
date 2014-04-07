package org.jenkinsci.plugins.myst;

import java.util.ArrayList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.GetMethod;
import org.jenkinsci.plugins.myst.beans.ActionBean;
import org.jenkinsci.plugins.myst.beans.ConfigBean;

/**
 * @author Rubicon Red
 *
 */
public class Context {
	
	/**
	 * Retrieve the available actions
	 * @param mystWebConsoleUrl
	 * @return
	 */
	public static ActionBean[] getActions(String mystWebConsoleUrl) {
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(mystWebConsoleUrl + "/actionList.jsp");
		String responseBody = null;

		ArrayList<ActionBean> actionBeanList = new ArrayList<ActionBean>();
		ActionBean[] actions = null;
		try {
			int statusCode = client.executeMethod(method);
			responseBody = new String(method.getResponseBody());
			JSONArray array = JSONArray.fromObject(responseBody);
			for (Object object : array) {
				ActionBean actionBean = new ActionBean();
				JSONObject jsonStr = (JSONObject) JSONSerializer.toJSON(object);
				actionBean.setName((String) jsonStr.get("name"));
				actionBean.setDesc((String) jsonStr.get("description"));
				actionBeanList.add(actionBean);
			}
			actions = actionBeanList.toArray(new ActionBean[array.size()]);
		} catch (Exception e) {
			e.printStackTrace();
			// Jenkins
			ActionBean actionBean = new ActionBean();
			actionBean.setName("Error retrieving data from MyST Dashboard. " + e.getMessage());
			actionBeanList.add(actionBean);
			actions = actionBeanList.toArray(new ActionBean[1]);
		} finally {
			method.releaseConnection();
		}
		return actions;
	}

	/**
	 * Retrieves the available configurations from the WebConsole
	 * 
	 * @param mystWebConsoleUrl
	 * @return
	 */
	public static ConfigBean[] getAvailableConfigs(String mystWebConsoleUrl) {
		HttpClient client = new HttpClient();
		HttpMethod method = new GetMethod(mystWebConsoleUrl + "/configList.jsp");
		String responseBody = null;

		ArrayList<ConfigBean> configBeanList = new ArrayList<ConfigBean>();
		ConfigBean[] configs = null;
		try {
			int statusCode = client.executeMethod(method);
			responseBody = new String(method.getResponseBody());
			JSONArray array = JSONArray.fromObject(responseBody);
			for (Object object : array) {
				ConfigBean configBean = new ConfigBean();
				JSONObject jsonStr = (JSONObject) JSONSerializer.toJSON(object);
				configBean.setName((String) jsonStr.get("name"));
				configBean.setDesc((String) jsonStr.get("description"));
				configBeanList.add(configBean);
			}
			configs = configBeanList.toArray(new ConfigBean[array.size()]);
		} catch (Exception e) {
			e.printStackTrace();
			// Jenkins
			ConfigBean configBean = new ConfigBean();
			configBean.setName("Error retrieving data from MyST Dashboard. " + e.getMessage());
			configBeanList.add(configBean);
			configs = configBeanList.toArray(new ConfigBean[1]);
		} finally {
			method.releaseConnection();
		}
		return configs;
	}
}
