package org.jenkinsci.plugins.myst.beans;

/**
 * @author Fabio Douek
 *
 */
public class ConfigBean {
	private String name;
	private String desc;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	
}