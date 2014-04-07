package org.jenkinsci.plugins.myst;

import hudson.Extension;
import hudson.tools.DownloadFromUrlInstaller;
import hudson.tools.ToolInstallation;

import org.kohsuke.stapler.DataBoundConstructor;

/**
* Automatic MyST installer from Repository
*/
/* Disbled, until we manage to sortout the crawler issue

public class MystInstaller extends DownloadFromUrlInstaller {
  @DataBoundConstructor
  public MystInstaller(String id) {
	  super(id);
  }

  @Extension
  public static final class DescriptorImpl extends DownloadFromUrlInstaller.DescriptorImpl<MystInstaller> {
    public String getDisplayName() {
    	return "Install from Repository - not implemented";
    }

    @Override
    public boolean isApplicable(Class<? extends ToolInstallation> toolType) {
      return toolType == MystInstallation.class;
    }

  }
}
*/