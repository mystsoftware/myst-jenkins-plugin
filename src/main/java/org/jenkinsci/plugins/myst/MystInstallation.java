package org.jenkinsci.plugins.myst;

import hudson.AbortException;
import hudson.CopyOnWrite;
import hudson.EnvVars;
import hudson.Extension;
import hudson.Functions;
import hudson.Launcher;
import hudson.Util;
import hudson.model.EnvironmentSpecific;
import hudson.model.TaskListener;
import hudson.model.Node;
import hudson.remoting.Callable;
import hudson.slaves.NodeSpecific;
import hudson.tools.ToolDescriptor;
import hudson.tools.ToolInstaller;
import hudson.tools.ToolProperty;
import hudson.tools.ToolInstallation;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

/**
* Represents a MyST agent installation in a system.
* @author Fabio Douek
*/
public class MystInstallation extends ToolInstallation implements EnvironmentSpecific<MystInstallation>, NodeSpecific<MystInstallation> {

  @DataBoundConstructor
  public MystInstallation(String name, String home, List<? extends ToolProperty<?>> properties) {
    super(Util.fixEmptyAndTrim(name), Util.fixEmptyAndTrim(home), properties);
  }

  public File getHomeDir() {
	  return new File(getHome());
  }

  /**
  * Gets the executable path of this MyST Agent on the given target system.
  */
  public String getExecutable(Launcher launcher) throws IOException, InterruptedException {
	  return launcher.getChannel().call(new Callable<String, IOException>() {
		  private static final long serialVersionUID = 1L;

		  public String call() throws IOException {
			  String home = Util.replaceMacro(getHome(), EnvVars.masterEnvVars);
			  String mystExecutor=determineMystExecutor();
			  File exe = new File(home, mystExecutor);
			  if (exe.exists()) {
				  if(!Functions.isWindows()) {
					  if(!exe.canExecute()) {
						  boolean changedPermission = exe.setExecutable(true);
						  if(!changedPermission) {
							  throw new AbortException("Couldnt change the permission for myst: " + exe.getCanonicalPath());
						  }
					  }
				  }
				  return exe.getPath();
			  }
			  return null;
		  }
	  });
  }

  private String determineMystExecutor() {
	  return Functions.isWindows() ? "myst.cmd" : "myst";
  }

  private static final long serialVersionUID = 1L;

  public MystInstallation forEnvironment(EnvVars environment) {
	  return new MystInstallation(getName(), environment.expand(getHome()), getProperties().toList());
  }

  public MystInstallation forNode(Node node, TaskListener log) throws IOException, InterruptedException {
	  return new MystInstallation(getName(), translateFor(node, log), getProperties().toList());
  }

  @Extension
  public static class DescriptorImpl extends ToolDescriptor<MystInstallation> {
    @CopyOnWrite
    private volatile MystInstallation[] installations = new MystInstallation[0];

    public DescriptorImpl() {
      load();
    }

    @Override
    public String getDisplayName() {
      return "MyST Agent";
    }

    /* 
     Note: This must be enabled once we figure it out how to handle the crawler
     
    @Override
    public List<? extends ToolInstaller> getDefaultInstallers() {
      return Collections.singletonList(new MystInstaller(null));
    }
    */

    @Override
    public MystInstallation[] getInstallations() {
    	return installations;
    }

    @Override
    public MystInstallation newInstance(StaplerRequest req, JSONObject formData) {
      return (MystInstallation) req.bindJSON(clazz, formData);
    }

    public void setInstallations(MystInstallation... mystInstallations) {
      this.installations = mystInstallations;
      save();
    }

  }

}
