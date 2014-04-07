package org.jenkinsci.plugins.myst;

import hudson.AbortException;
import hudson.EnvVars;
import hudson.Launcher;
import hudson.Extension;
import hudson.Util;
import hudson.Launcher.ProcStarter;
import hudson.Proc;
import hudson.util.ArgumentListBuilder;
import hudson.util.FormValidation;
import hudson.model.AbstractBuild;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Computer;
import hudson.model.Hudson;
import hudson.model.ParameterValue;
import hudson.model.AbstractProject;
import hudson.model.ParametersAction;
import hudson.model.StringParameterValue;
import hudson.tasks.Builder;
import hudson.tasks.BuildStepDescriptor;
import hudson.tools.ToolInstallation;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.QueryParameter;

import javax.servlet.ServletException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * MyST Builder
 * 
 * @author Rubicon Red
 */
public class MystBuilder extends Builder {

    private final String mystAction;
    private final String config;
    private final String mystWorkspace;
    private final String properties;
    private final String mystInstallationName;

    @DataBoundConstructor
    public MystBuilder(String mystAction, String config, String mystWorkspace, String properties, String mystInstallationName) {
        this.mystAction = mystAction;
        this.config=config;
        this.mystWorkspace=mystWorkspace;
        this.properties=properties;
        this.mystInstallationName=mystInstallationName;
    }

    public String getMystAction() {
        return mystAction;
    }

    public String getConfig() {
        return config;
    }

    public String getMystWorkspace() {
        return mystWorkspace;
    }

    public String getProperties() {
        return properties;
    }
    
    public String getMystInstallationName() {
        return Util.fixNull(mystInstallationName);
    }

    public MystInstallation getMystInstallation() throws AbortException {
		if (getDescriptor().getMystInstallations().length == 0) {
			throw new AbortException("There is no MyST Agent installation defined in the MyST Global Configuration");
		}

    	if (mystInstallationName == null || mystInstallationName.trim().length()==0) {
    		//If the MyST installation name is null returns the first Agent
    		return getDescriptor().getMystInstallations()[0];
    	}
    	else if (mystInstallationName != null) {
    		for (MystInstallation sri : getDescriptor().getMystInstallations()) {
    			if (mystInstallationName.trim().equalsIgnoreCase(sri.getName().trim())) {
    				return sri;
    			}
        	}
    		throw new AbortException("The MyST Installation name defined in this job (" + mystInstallationName + ") is not defined in the MyST Global Configuration");
        }
    	
    	throw new AbortException("Couldn't determine the MyST Installation");
      }
    

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
    	String url=null;
    	if (getDescriptor().getUseConsoleIntegration()) {
    		//TODO: We will have to introduce a concept os WORKSPACE_ID, as in the web console we might have multiple workspaces
    		url=getDescriptor().getMystConsoleUrl() + "/index.jsp?env=" + "/conf/" + getConfig().replace(".", "/");
    	}
    	return new MystProjectAction(project, url);
    }
    
    @Override
    public boolean perform(AbstractBuild build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
    	ArgumentListBuilder command = new ArgumentListBuilder();

        EnvVars env = build.getEnvironment(listener);
        env.overrideAll(build.getBuildVariables());

		List<ParameterValue> params = new ArrayList<ParameterValue>();

        
    	MystInstallation sri = getMystInstallation();
    	String exe=null;
    	if (sri == null) {
    		//args.add(launcher.isUnix() ? "myst" : "myst.cmd");
        } else {
        	sri = sri.forNode(Computer.currentComputer().getNode(), listener);
            sri = sri.forEnvironment(env);
            exe = sri.getExecutable(launcher);
            listener.getLogger().println("Executor: " + exe);
            if (exe == null) {
            	listener.fatalError("Executable not found for: " + sri.getName() + "; Executable="+exe);
            	return false;
            }
		    params.add(new StringParameterValue("MYST_HOME", sri.getHome()));

        }

    	String cmd=exe + " " + getMystAction() + " " + getConfig();
    	
    	if (properties!=null) {
    		cmd = cmd + " " + properties;
    	}
    	command.addTokenized(cmd);
    	
    	ProcStarter ps = launcher.new ProcStarter();
    	ps = ps.cmds(command).stdout(listener);
    	try {
			String ws=getMystWorkspace();
		    //Keeps the process running if its the start action
		    if(getMystAction().equals("start")) {
		    	params.add(new StringParameterValue("BUILD_ID", "dontKillMe"));
		    }
		    build.addAction(new ParametersAction(params));
			if(ws==null || ws.trim().length()==0) {
				//Change the directory to Jenkins workspace
				ps = ps.pwd(build.getWorkspace()).envs(build.getEnvironment(listener));
			}
			else {
				//Change the directory to the value defined in the MyST workspace field
				ps = ps.pwd(ws).envs(build.getEnvironment(listener));
			}
			Proc proc = launcher.launch(ps);
	    	int retcode = proc.join();
	    	if (retcode!=0) {
	    		return false;
	    	}
    	} catch (Exception e) {
    		return false;
    	} 
        return true;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {
    	private boolean useConsoleIntegration;
        private String mystConsoleUrl;

        public DescriptorImpl(){
            load();
        }
        
        /**
         * Validation
         *
         * @param value
         *      This parameter receives the value that the user has typed.
         * @return
         *      Indicates the outcome of the validation. This is sent to the browser.
         */
        public FormValidation doCheckName(@QueryParameter String value)
                throws IOException, ServletException {
            if (value.length() == 0) {
                return FormValidation.error("Field must be set");
            }
            if (value.length() < 4) {
                return FormValidation.warning("Field lengh is too short");
            }
            return FormValidation.ok();
        }

        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        /**
         * Build Action
         */
        public String getDisplayName() {
            return "Invoke MyST";
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            useConsoleIntegration = formData.getBoolean("useConsoleIntegration");
            mystConsoleUrl = formData.getString("mystConsoleUrl");
            save();
            return super.configure(req,formData);
        }

        /**
         * This method returns true if the global configuration says that the Console integration is enabled.
         */
        public boolean getUseConsoleIntegration() {
            return useConsoleIntegration;
        }

        /**
         * Returns the Myst Console URL
         *
         */
        public String getMystConsoleUrl() {
            return mystConsoleUrl;
        }
        
        public MystInstallation[] getMystInstallations() {
        	return Hudson.getInstance().getDescriptorByType(MystInstallation.DescriptorImpl.class).getInstallations();
        }
        
        public MystInstallation.DescriptorImpl getToolDescriptor() {
            return ToolInstallation.all().get(MystInstallation.DescriptorImpl.class);
        }
    }
}

