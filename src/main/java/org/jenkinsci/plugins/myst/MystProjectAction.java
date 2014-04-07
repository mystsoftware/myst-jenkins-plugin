package org.jenkinsci.plugins.myst;

import hudson.model.ProminentProjectAction;
import hudson.model.AbstractProject;

/**
 * Allows user to go to the Myst Dashboard.
 *
 * @author Fabio Douek
 */
public final class MystProjectAction implements ProminentProjectAction {
  private final AbstractProject<?, ?> project;
  private String url;

  public MystProjectAction(AbstractProject<?, ?> project, String url) {
    this.project = project;
    this.url = url;
  }

  public String getIconFileName() {
	return "/plugin/myst-jenkins-plugin/images/myst.png";
  }

  public String getDisplayName() {
	return "MyST Dashboard";
  }

  public String getUrlName() {
	return url;
  }
}
