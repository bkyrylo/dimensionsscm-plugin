/*
 * ===========================================================================
 *  Copyright (c) 2007, 2014 Serena Software. All rights reserved.
 *
 *  Use of the Sample Code provided by Serena is governed by the following
 *  terms and conditions. By using the Sample Code, you agree to be bound by
 *  the terms contained herein. If you do not agree to the terms herein, do
 *  not install, copy, or use the Sample Code.
 *
 *  1.  GRANT OF LICENSE.  Subject to the terms and conditions herein, you
 *  shall have the nonexclusive, nontransferable right to use the Sample Code
 *  for the sole purpose of developing applications for use solely with the
 *  Serena software product(s) that you have licensed separately from Serena.
 *  Such applications shall be for your internal use only.  You further agree
 *  that you will not: (a) sell, market, or distribute any copies of the
 *  Sample Code or any derivatives or components thereof; (b) use the Sample
 *  Code or any derivatives thereof for any commercial purpose; or (c) assign
 *  or transfer rights to the Sample Code or any derivatives thereof.
 *
 *  2.  DISCLAIMER OF WARRANTIES.  TO THE MAXIMUM EXTENT PERMITTED BY
 *  APPLICABLE LAW, SERENA PROVIDES THE SAMPLE CODE AS IS AND WITH ALL
 *  FAULTS, AND HEREBY DISCLAIMS ALL WARRANTIES AND CONDITIONS, EITHER
 *  EXPRESSED, IMPLIED OR STATUTORY, INCLUDING, BUT NOT LIMITED TO, ANY
 *  IMPLIED WARRANTIES OR CONDITIONS OF MERCHANTABILITY, OF FITNESS FOR A
 *  PARTICULAR PURPOSE, OF LACK OF VIRUSES, OF RESULTS, AND OF LACK OF
 *  NEGLIGENCE OR LACK OF WORKMANLIKE EFFORT, CONDITION OF TITLE, QUIET
 *  ENJOYMENT, OR NON-INFRINGEMENT.  THE ENTIRE RISK AS TO THE QUALITY OF
 *  OR ARISING OUT OF USE OR PERFORMANCE OF THE SAMPLE CODE, IF ANY,
 *  REMAINS WITH YOU.
 *
 *  3.  EXCLUSION OF DAMAGES.  TO THE MAXIMUM EXTENT PERMITTED BY APPLICABLE
 *  LAW, YOU AGREE THAT IN CONSIDERATION FOR RECEIVING THE SAMPLE CODE AT NO
 *  CHARGE TO YOU, SERENA SHALL NOT BE LIABLE FOR ANY DAMAGES WHATSOEVER,
 *  INCLUDING BUT NOT LIMITED TO DIRECT, SPECIAL, INCIDENTAL, INDIRECT, OR
 *  CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, DAMAGES FOR LOSS OF
 *  PROFITS OR CONFIDENTIAL OR OTHER INFORMATION, FOR BUSINESS INTERRUPTION,
 *  FOR PERSONAL INJURY, FOR LOSS OF PRIVACY, FOR NEGLIGENCE, AND FOR ANY
 *  OTHER LOSS WHATSOEVER) ARISING OUT OF OR IN ANY WAY RELATED TO THE USE
 *  OF OR INABILITY TO USE THE SAMPLE CODE, EVEN IN THE EVENT OF THE FAULT,
 *  TORT (INCLUDING NEGLIGENCE), STRICT LIABILITY, OR BREACH OF CONTRACT,
 *  EVEN IF SERENA HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.  THE
 *  FOREGOING LIMITATIONS, EXCLUSIONS AND DISCLAIMERS SHALL APPLY TO THE
 *  MAXIMUM EXTENT PERMITTED BY APPLICABLE LAW.  NOTWITHSTANDING THE ABOVE,
 *  IN NO EVENT SHALL SERENA'S LIABILITY UNDER THIS AGREEMENT OR WITH RESPECT
 *  TO YOUR USE OF THE SAMPLE CODE AND DERIVATIVES THEREOF EXCEED US$10.00.
 *
 *  4.  INDEMNIFICATION. You hereby agree to defend, indemnify and hold
 *  harmless Serena from and against any and all liability, loss or claim
 *  arising from this agreement or from (i) your license of, use of or
 *  reliance upon the Sample Code or any related documentation or materials,
 *  or (ii) your development, use or reliance upon any application or
 *  derivative work created from the Sample Code.
 *
 *  5.  TERMINATION OF THE LICENSE.  This agreement and the underlying
 *  license granted hereby shall terminate if and when your license to the
 *  applicable Serena software product terminates or if you breach any terms
 *  and conditions of this agreement.
 *
 *  6.  CONFIDENTIALITY.  The Sample Code and all information relating to the
 *  Sample Code (collectively "Confidential Information") are the
 *  confidential information of Serena.  You agree to maintain the
 *  Confidential Information in strict confidence for Serena.  You agree not
 *  to disclose or duplicate, nor allow to be disclosed or duplicated, any
 *  Confidential Information, in whole or in part, except as permitted in
 *  this Agreement.  You shall take all reasonable steps necessary to ensure
 *  that the Confidential Information is not made available or disclosed by
 *  you or by your employees to any other person, firm, or corporation.  You
 *  agree that all authorized persons having access to the Confidential
 *  Information shall observe and perform under this nondisclosure covenant.
 *  You agree to immediately notify Serena of any unauthorized access to or
 *  possession of the Confidential Information.
 *
 *  7.  AFFILIATES.  Serena as used herein shall refer to Serena Software,
 *  Inc. and its affiliates.  An entity shall be considered to be an
 *  affiliate of Serena if it is an entity that controls, is controlled by,
 *  or is under common control with Serena.
 *
 *  8.  GENERAL.  Title and full ownership rights to the Sample Code,
 *  including any derivative works shall remain with Serena.  If a court of
 *  competent jurisdiction holds any provision of this agreement illegal or
 *  otherwise unenforceable, that provision shall be severed and the
 *  remainder of the agreement shall remain in full force and effect.
 * ===========================================================================
 */
package hudson.plugins.dimensionsscm;

import hudson.EnvVars;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.BuildListener;
import hudson.model.Hudson;
import hudson.model.ModelObject;
import hudson.model.Node;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.scm.ChangeLogParser;
import hudson.scm.PollingResult;
import hudson.scm.PollingResult.Change;
import hudson.scm.RepositoryBrowsers;
import hudson.scm.SCM;
import hudson.scm.SCMDescriptor;
import hudson.scm.SCMRevisionState;
import hudson.util.FormValidation;
import hudson.util.Scrambler;
import hudson.util.VariableResolver;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.io.Writer;
import java.net.InetAddress;
import java.util.Calendar;
import java.util.Map;
import java.util.TimeZone;
import javax.servlet.ServletException;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

/**
 * This experimental plugin extends Jenkins/Hudson support for Dimensions SCM
 * repositories. Main Dimensions SCM class which creates the plugin logic.
 * <p>
 * Jenkins/Hudson required the following functions to be implemented.
 * <pre>
 * public boolean checkout(AbstractBuild build, Launcher launcher,
 *         FilePath workspace, BuildListener listener, File changelogFile)
 *         throws IOException, InterruptedException;
 * public boolean pollChanges(AbstractProject project, Launcher launcher,
 *         FilePath workspace, TaskListener listener)
 *         throws IOException, InterruptedException;
 * public ChangeLogParser createChangeLogParser();
 * public SCMDescriptor<?> getDescriptor();
 * </pre>
 * For this experimental plugin, only the main ones will be implemented.
 *
 * @author Tim Payne
 */
public class DimensionsSCM extends SCM implements Serializable {
    private final String project;
    private final String directory;
    private final String permissions;
    private final String eol;

    private final String jobUserName;
    private final String jobPasswd;
    private final String jobServer;
    private final String jobDatabase;

    private final String[] folders;
    private final String[] pathsToExclude;

    private final String jobTimeZone;
    private final String jobWebUrl;

    private final boolean canJobUpdate;
    private final boolean canJobDelete;
    private final boolean canJobForce;
    private final boolean canJobRevert;
    private final boolean canJobExpand;
    private final boolean canJobNoMetadata;
    private final boolean canJobNoTouch;
    private final boolean forceAsSlave;

    private transient DimensionsAPI cachedAPI;
    private transient DimensionsSCMRepositoryBrowser browser;

    /**
     * Patch matcher that rejects nothing and includes everything.
     */
    private static class NullPathMatcher implements PathMatcher {
        @Override
        public boolean match(String matchText) {
            return true;
        }
    }

    public DimensionsAPI getAPI() {
        DimensionsAPI api = this.cachedAPI;
        if (api == null) {
            api = new DimensionsAPI();
            this.cachedAPI = api;
        }
        return api;
    }

    @Override
    public DimensionsSCMRepositoryBrowser getBrowser() {
        return this.browser;
    }

    /**
     * Gets the unexpanded project name for the connection.
     * @return the project spec
     */
    public String getProject() {
        return this.project;
    }

    /**
     * Gets the expanded project name for the connection. Any variables in the project value will be expanded.
     * @return the project spec without a trailing version number (if there is one).
     */
    public String getProjectName(Run<?, ?> run) {
        String projectVersion = getProjectVersion(run);
        int sc = projectVersion.lastIndexOf(';');
        return sc >= 0 ? projectVersion.substring(0, sc) : projectVersion;
    }

    /**
     * Gets the expanded project name and version for the connection. Any variables in the project value will be
     * expanded.
     * @return the project spec including its trailing version (if there is one).
     */
    public String getProjectVersion(Run<?, ?> run) {
        EnvVars env = null;
        if (run != null) {
            try {
                env = run.getEnvironment();
            } catch (IOException e) {
                /* don't expand */
            } catch (InterruptedException e) {
                /* don't expand */
            }
        }
        String ret;
        if (env != null) {
            ret = env.expand(this.project);
        } else {
            ret = this.project;
        }
        return ret;
    }

    /**
     * Gets the project path.
     */
    public String getDirectory() {
        return this.directory;
    }

    /**
     * Gets the permissions string.
     */
    public String getPermissions() {
        return this.permissions;
    }

    /**
     * Gets the eol value.
     */
    public String getEol() {
        return this.eol;
    }

    /**
     * Gets the project paths to monitor.
     */
    public String[] getFolders() {
        return this.folders;
    }

    /**
     * Gets paths excluded from monitoring.
     */
    public String[] getPathsToExclude() {
        return pathsToExclude;
    }

    /**
     * Gets the user ID for the connection.
     */
    public String getJobUserName() {
        return this.jobUserName;
    }

    /**
     * Gets the password for the connection.
     */
    public String getJobPasswd() {
        return Scrambler.descramble(jobPasswd);
    }

    /**
     * Gets the server name for the connection.
     */
    public String getJobServer() {
        return this.jobServer;
    }

    /**
     * Gets the database name for the connection.
     */
    public String getJobDatabase() {
        return this.jobDatabase;
    }

    /**
     * Gets the time zone for the connection.
     */
    public String getJobTimeZone() {
        return this.jobTimeZone;
    }

    /**
     * Gets the web URL for the connection.
     */
    public String getJobWebUrl() {
        return this.jobWebUrl;
    }

    /**
     * Gets the expand flag.
     */
    public boolean isCanJobExpand() {
        return this.canJobExpand;
    }

    /**
     * Gets the no metadata flag.
     */
    public boolean isCanJobNoMetadata() {
        return this.canJobNoMetadata;
    }

    /**
     * Gets the no touch flag.
     */
    public boolean isCanJobNoTouch() {
        return this.canJobNoTouch;
    }

    /**
     * Gets the update flag.
     */
    public boolean isCanJobUpdate() {
        return this.canJobUpdate;
    }

    /**
     * Gets the delete flag.
     */
    public boolean isCanJobDelete() {
        return this.canJobDelete;
    }

    /**
     * Gets the force flag.
     */
    public boolean isCanJobForce() {
        return this.canJobForce;
    }

    /**
     * Gets the revert flag.
     */
    public boolean isCanJobRevert() {
        return this.canJobRevert;
    }

    /**
     * Gets force as slave flag.
     */
    public boolean isForceAsSlave() {
        return this.forceAsSlave;
    }

    @Extension
    public static final DescriptorImpl DM_DESCRIPTOR = new DescriptorImpl();

    /**
     * Does this SCM plugin require a workspace for polling?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean requiresWorkspaceForPolling() {
        return false;
    }

    /**
     * Does this SCM plugin support polling?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean supportsPolling() {
        return true;
    }

    /**
     * Build up environment variables for build support.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public void buildEnvVars(AbstractBuild<?, ?> build, Map<String, String> env) {
        // To be implemented when build support put in.
        super.buildEnvVars(build, env);
    }

    private static final String[] DEFAULT_FOLDERS = new String[] { "/" };

    @DataBoundConstructor
    public DimensionsSCM(String project, String[] folders, String[] pathsToExclude, String workarea,
            boolean canJobDelete, boolean canJobForce, boolean canJobRevert, String jobUserName, String jobPasswd,
            String jobServer, String jobDatabase, boolean canJobUpdate, String jobTimeZone, String jobWebUrl,
            String directory, String permissions, String eol, boolean canJobExpand, boolean canJobNoMetadata,
            boolean canJobNoTouch, boolean forceAsSlave) {
        // Check the folders specified have data specified.
        this.folders = folders != null ? Values.notEmptyOrElse(Values.trimCopy(folders), DEFAULT_FOLDERS) :
                (Values.hasText(directory) ? new String[] { directory } : DEFAULT_FOLDERS);
        this.pathsToExclude = pathsToExclude != null ? Values.notEmptyOrElse(Values.trimCopy(pathsToExclude),
                Values.EMPTY_STRING_ARRAY) : Values.EMPTY_STRING_ARRAY;

        // Copying arguments to fields.
        this.project = Values.textOrElse(project, "${JOB_NAME}");
        this.directory = Values.textOrElse(directory, null);
        this.permissions = Values.textOrElse(permissions, "DEFAULT");
        this.eol = Values.textOrElse(eol, "DEFAULT");

        this.jobServer = Values.textOrElse(jobServer, getDescriptor().getServer());
        this.jobUserName = Values.textOrElse(jobUserName, getDescriptor().getUserName());
        this.jobDatabase = Values.textOrElse(jobDatabase, getDescriptor().getDatabase());
        String passwd = Values.textOrElse(jobPasswd, getDescriptor().getPasswd());
        this.jobPasswd = Scrambler.scramble(passwd);

        this.canJobUpdate = Values.hasText(jobServer) ? canJobUpdate : getDescriptor().isCanUpdate();

        this.canJobDelete = canJobDelete;
        this.canJobForce = canJobForce;
        this.canJobRevert = canJobRevert;
        this.canJobExpand = canJobExpand;
        this.canJobNoMetadata = canJobNoMetadata;
        this.canJobNoTouch = canJobNoTouch;
        this.forceAsSlave = forceAsSlave;

        this.jobTimeZone = Values.textOrElse(jobTimeZone, getDescriptor().getTimeZone());
        this.jobWebUrl = Values.textOrElse(jobWebUrl, getDescriptor().getWebUrl());

        Logger.debug("Starting job for project '" + this.project + "' ('" + this.folders.length + "')" +
                ", connecting to " + this.jobServer + "-" + this.jobUserName + ":" + this.jobDatabase);
    }

    /**
     * Checkout method for the plugin.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean checkout(final AbstractBuild build, final Launcher launcher, final FilePath workspace,
            final BuildListener listener, final File changelogFile) throws IOException, InterruptedException {
        if (!isCanJobUpdate()) {
            Logger.debug("Skipping checkout - " + this.getClass().getName());
        }

        Logger.debug("Invoking checkout - " + this.getClass().getName());

        boolean bRet;
        try {
            // Load other Dimensions plugins if set.
            DimensionsBuildWrapper.DescriptorImpl bwplugin = (DimensionsBuildWrapper.DescriptorImpl)
                                Hudson.getInstance().getDescriptor(DimensionsBuildWrapper.class);
            DimensionsBuildNotifier.DescriptorImpl bnplugin = (DimensionsBuildNotifier.DescriptorImpl)
                                Hudson.getInstance().getDescriptor(DimensionsBuildNotifier.class);

            String nodeName = build.getBuiltOn().getNodeName();

            if (DimensionsChecker.isValidPluginCombination(build, listener)) {
                Logger.debug("Plugins are ok");
            } else {
                listener.fatalError("\n[DIMENSIONS] The plugin combinations you have selected are not valid.");
                listener.fatalError("\n[DIMENSIONS] Please review online help to determine valid plugin uses.");
                return false;
            }

            if (isCanJobUpdate()) {
                DimensionsAPI dmSCM = getAPI();
                int version = 2009;
                long key = dmSCM.login(getJobUserName(), getJobPasswd(), getJobDatabase(), getJobServer(), build);

                if (key > 0L) {
                    // Get the server version.
                    Logger.debug("Login worked.");
                    version = dmSCM.getDmVersion();
                    if (version == 0) {
                        version = 2009;
                    }
                    dmSCM.logout(key, build);
                }

                // Get the details of the master.
                InetAddress netAddr = InetAddress.getLocalHost();
                String hostname = netAddr.getHostName();

                boolean master = true;
                if (isForceAsSlave()) {
                    master = false;
                    Logger.debug("Forced processing as slave...");
                } else {
                    Logger.debug("Checking if master or slave...");
                    if (nodeName != null && nodeName.length() > 0) {
                        master = false;
                    }
                }

                if (master) {
                    // Running on master...
                    listener.getLogger().println("[DIMENSIONS] Running checkout on master...");
                    listener.getLogger().flush();
                    // Using Java API because this allows the plugin to work on platforms where Dimensions has not
                    // been ported, e.g. MAC OS, which is what I use.
                    CheckOutAPITask task = new CheckOutAPITask(build, this, workspace, listener, version);
                    bRet = workspace.act(task);
                } else {
                    // Running on slave... Have to use the command line as Java API will not work on remote hosts.
                    // Cannot serialise it...
                    // VariableResolver does not appear to be serialisable either, so...
                    VariableResolver<String> myResolver = build.getBuildVariableResolver();

                    String baseline = myResolver.resolve("DM_BASELINE");
                    String requests = myResolver.resolve("DM_REQUEST");

                    listener.getLogger().println("[DIMENSIONS] Running checkout on slave...");
                    listener.getLogger().flush();

                    CheckOutCmdTask task = new CheckOutCmdTask(getJobUserName(), getJobPasswd(), getJobDatabase(),
                            getJobServer(), getProjectVersion(build), baseline, requests, isCanJobDelete(),
                            isCanJobRevert(), isCanJobForce(), isCanJobExpand(), isCanJobNoMetadata(),
                            isCanJobNoTouch(), (build.getPreviousBuild() == null), getFolders(), version,
                            permissions, eol, workspace, listener);
                    bRet = workspace.act(task);
                }
            } else {
                bRet = true;
            }

            if (bRet) {
                bRet = generateChangeSet(build, listener, changelogFile);
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = "An unknown error occurred. Please try the operation again.";
            }
            listener.fatalError("Unable to run checkout callout - " + errMsg);
            // e.printStackTrace();
            //throw new IOException("Unable to run checkout callout - " + e.getMessage());
            bRet = false;
        }
        return bRet;
    }

    /**
     * Generate the changeset.
     */
    private boolean generateChangeSet(final AbstractBuild build, final BuildListener listener,
            final File changelogFile) throws IOException, InterruptedException {
        long key = -1L;
        boolean bRet = false;
        DimensionsAPI dmSCM = new DimensionsAPI();

        try {
            // When are we building files for?
            // Looking for the last successful build and then go forward from there - could use the last build as well.
            Calendar lastBuildCal = (build.getPreviousBuild() != null) ? build.getPreviousBuild().getTimestamp() : null;
            // Calendar lastBuildCal = (build.getPreviousNotFailedBuild() != null) ?
            //         build.getPreviousNotFailedBuild().getTimestamp() : null;
            Calendar nowDateCal = Calendar.getInstance();

            TimeZone tz = (getJobTimeZone() != null && getJobTimeZone().length() > 0) ?
                    TimeZone.getTimeZone(getJobTimeZone()) : TimeZone.getDefault();
            if (getJobTimeZone() != null && getJobTimeZone().length() > 0) {
                Logger.debug("Job timezone setting is " + getJobTimeZone());
            }
            Logger.debug("Log updates between " + (lastBuildCal != null ?
                    DateUtils.getStrDate(lastBuildCal, tz) : "0") + " -> " + DateUtils.getStrDate(nowDateCal, tz) +
                    " (" + tz.getID() + ")");

            dmSCM.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = dmSCM.login(getJobUserName(), getJobPasswd(), getJobDatabase(), getJobServer(), build);

            if (key > 0L) {
                Logger.debug("Login worked.");
                VariableResolver<String> myResolver = build.getBuildVariableResolver();

                String baseline = myResolver.resolve("DM_BASELINE");
                String requests = myResolver.resolve("DM_REQUEST");

                if (baseline != null) {
                    baseline = baseline.trim();
                    baseline = baseline.toUpperCase(Values.ROOT_LOCALE);
                }
                if (requests != null) {
                    requests = requests.replaceAll(" ", "");
                    requests = requests.toUpperCase(Values.ROOT_LOCALE);
                }

                Logger.debug("Extra parameters - " + baseline + " " + requests);
                String[] folders = getFolders();

                if (baseline != null && baseline.length() == 0) {
                    baseline = null;
                }
                if (requests != null && requests.length() == 0) {
                    requests = null;
                }
                bRet = true;

                // Iterate through the project folders and process them in Dimensions.
                for (String folderN : folders) {
                    if (!bRet) {
                        break;
                    }
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Looking for changes in '" + folderN + "'...");

                    // Check out the folder.
                    bRet = dmSCM.createChangeSetLogs(key, getProjectName(build), dname, lastBuildCal, nowDateCal,
                            changelogFile, tz, jobWebUrl, baseline, requests);
                    if (requests != null) {
                        break;
                    }
                }

                // Close the changes log file.
                {
                    Writer writer = null;
                    try {
                        writer = new FileWriter(changelogFile, true);
                        PrintWriter pw = new PrintWriter(writer);
                        pw.println("</changelog>");
                        pw.flush();
                        bRet = true;
                    } catch (Exception e) {
                        throw new IOException("Unable to write change log - " + e.getMessage());
                    } finally {
                        if (writer != null) {
                            writer.close();
                        }
                    }
                }
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = "An unknown error occurred. Please try the operation again.";
            }
            listener.fatalError("Unable to run change set callout - " + errMsg);
            // e.printStackTrace();
            //throw new IOException("Unable to run change set callout - " + e.getMessage());
            bRet = false;
        } finally {
            dmSCM.logout(key, build);
        }
        return bRet;
    }

    /**
     * Has the repository had any changes since last build?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public SCMRevisionState calcRevisionsFromBuild(AbstractBuild<?, ?> build, Launcher launcher, TaskListener listener)
            throws IOException, InterruptedException {
        // Stub function for now
        return null;
    }

    /**
     * Has the repository had any changes?
     * <p>
     * {@inheritDoc}
     */
    @Override
    protected PollingResult compareRemoteRevisionWith(AbstractProject<?, ?> project, Launcher launcher,
            FilePath workspace, TaskListener listener, SCMRevisionState baseline)
            throws IOException, InterruptedException {
        // New polling function - to use old polling function for the moment.
        Change change = Change.NONE;

        try {
            if (pollCMChanges(project, launcher, workspace, listener)) {
                return PollingResult.BUILD_NOW;
            }
        } catch (Exception e) {
            /* swallow exception. */
        }
        return new PollingResult(change);
    }

    /**
     * Okay to clear the area?
     * <p>
     * {@inheritDoc}
     */
    @Override
    public boolean processWorkspaceBeforeDeletion(AbstractProject<?, ?> project, FilePath workspace, Node node)
            throws IOException, InterruptedException {
        // Not used at the moment, so we have a stub...
        return true;
    }

    /**
     * Has the repository had any changes?
     * <p>
     * {@inheritDoc}
     */
    private boolean pollCMChanges(final AbstractProject project, final Launcher launcher, final FilePath workspace,
            final TaskListener listener) throws IOException, InterruptedException {
        boolean bChanged = false;

        Logger.debug("Invoking pollChanges - " + this.getClass().getName());
        Logger.debug("Checking job - " + project.getName());
        long key = -1L;

        if (getProject() == null || getProject().length() == 0) {
            return false;
        }
        if (project.getLastBuild() == null) {
            return true;
        }
        DimensionsAPI dmSCM = getAPI();
        try {
            Calendar lastBuildCal = project.getLastBuild().getTimestamp();

            Calendar nowDateCal = Calendar.getInstance();
            TimeZone tz = (getJobTimeZone() != null && getJobTimeZone().length() > 0) ?
                    TimeZone.getTimeZone(getJobTimeZone()) : TimeZone.getDefault();
            if (getJobTimeZone() != null && getJobTimeZone().length() > 0) {
                Logger.debug("Job timezone setting is " + getJobTimeZone());
            }
            Logger.debug("Checking for any updates between " + (lastBuildCal != null ?
                    DateUtils.getStrDate(lastBuildCal, tz) : "0") + " -> " + DateUtils.getStrDate(nowDateCal, tz) +
                    " (" + tz.getID() + ")");

            dmSCM.setLogger(listener.getLogger());

            // Connect to Dimensions...
            key = dmSCM.login(jobUserName, getJobPasswd(), jobDatabase, jobServer);
            if (key > 0L) {
                String[] folders = getFolders();
                // Iterate through the project folders and process them in Dimensions
                for (String folderN : folders) {
                    if (bChanged) {
                        break;
                    }
                    File fileName = new File(folderN);
                    FilePath dname = new FilePath(fileName);

                    Logger.debug("Polling using key " + key);
                    Logger.debug("Polling '" + folderN + "'...");

                    if (dmSCM.getPathMatcher() == null) {
                        dmSCM.setPathMatcher(createPathMatcher());
                    }
                    bChanged = dmSCM.hasRepositoryBeenUpdated(key, getProjectName(project.getLastBuild()), dname,
                            lastBuildCal, nowDateCal, tz);
                }
            }
        } catch (Exception e) {
            String errMsg = e.getMessage();
            if (errMsg == null) {
                errMsg = "An unknown error occurred. Please try the operation again.";
            }
            listener.fatalError("Unable to run pollChanges callout - " + errMsg);
            // e.printStackTrace();
            //throw new IOException("Unable to run pollChanges callout - " + e.getMessage());
            bChanged = false;
        } finally {
            dmSCM.logout(key);
        }

        if (bChanged) {
            Logger.debug("Polling returned true");
        }
        return bChanged;
    }

    /**
     * Creates path matcher to ignore changes on certain paths.
     *
     * @return path matcher
     */
    public PathMatcher createPathMatcher() {
        return Values.isNullOrEmpty(getPathsToExclude()) ? new NullPathMatcher()
                : new DefaultPathMatcher(getPathsToExclude(), null);
    }

    /**
     * Create a log parser object.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public ChangeLogParser createChangeLogParser() {
        Logger.debug("Invoking createChangeLogParser - " + this.getClass().getName());
        return new DimensionsChangeLogParser();
    }

    /**
     * Return an SCM descriptor.
     * <p>
     * {@inheritDoc}
     */
    @Override
    public DescriptorImpl getDescriptor() {
        return DM_DESCRIPTOR;
    }

    /**
     * Implementation class for Dimensions plugin.
     */
    public static class DescriptorImpl extends SCMDescriptor<DimensionsSCM> implements ModelObject {
        DimensionsAPI connectionCheck;

        private String server;
        private String userName;
        private String passwd;
        private String database;

        private String timeZone;
        private String webUrl;

        private boolean canUpdate;

        /**
         * Loads the SCM descriptor.
         */
        public DescriptorImpl() {
            super(DimensionsSCM.class, DimensionsSCMRepositoryBrowser.class);
            load();
            Logger.debug("Loading " + this.getClass().getName());
        }

        @Override
        public String getDisplayName() {
            return "Dimensions";
        }

        /**
         * Save the SCM descriptor configuration.
         * <p>
         * {@inheritDoc}
         */
        @Override
        public boolean configure(StaplerRequest req, JSONObject jobj) throws FormException {
            // Get the values and check them.
            userName = req.getParameter("dimensionsscm.userName");
            passwd = req.getParameter("dimensionsscm.passwd");
            server = req.getParameter("dimensionsscm.server");
            database = req.getParameter("dimensionsscm.database");

            timeZone = req.getParameter("dimensionsscm.timeZone");
            webUrl = req.getParameter("dimensionsscm.webUrl");

            if (userName != null) {
                userName = Util.fixNull(req.getParameter("dimensionsscm.userName").trim());
            }
            if (passwd != null) {
                passwd = Util.fixNull(req.getParameter("dimensionsscm.passwd").trim());
            }
            if (server != null) {
                server = Util.fixNull(req.getParameter("dimensionsscm.server").trim());
            }
            if (database != null) {
                database = Util.fixNull(req.getParameter("dimensionsscm.database").trim());
            }
            if (timeZone != null) {
                timeZone = Util.fixNull(req.getParameter("dimensionsscm.timeZone").trim());
            }
            if (webUrl != null) {
                webUrl = Util.fixNull(req.getParameter("dimensionsscm.webUrl").trim());
            }
            req.bindJSON(DM_DESCRIPTOR, jobj);

            this.save();
            return super.configure(req, jobj);
        }

        @Override
        public SCM newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            // Get variables and then construct a new object.
            String[] folders = req.getParameterValues("dimensionsscm.folders");
            String[] pathsToExclude = req.getParameterValues("dimensionsscm.pathsToExclude");

            String project = req.getParameter("dimensionsscm.project");
            String directory = req.getParameter("dimensionsscm.directory");
            String permissions = req.getParameter("dimensionsscm.permissions");
            String eol = req.getParameter("dimensionsscm.eol");

            Boolean canJobDelete = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobDelete"));
            Boolean canJobForce = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobForce"));
            Boolean canJobRevert = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobRevert"));
            Boolean canJobUpdate = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobUpdate"));
            Boolean canJobExpand = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobExpand"));
            Boolean canJobNoMetadata = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobNoMetadata"));
            Boolean canJobNoTouch = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.canJobNoTouch"));
            Boolean forceAsSlave = "on".equalsIgnoreCase(req.getParameter("dimensionsscm.forceAsSlave"));

            String jobUserName = req.getParameter("dimensionsscm.jobUserName");
            String jobPasswd = req.getParameter("dimensionsscm.jobPasswd");
            String jobServer = req.getParameter("dimensionsscm.jobServer");
            String jobDatabase = req.getParameter("dimensionsscm.jobDatabase");
            String jobTimeZone = req.getParameter("dimensionsscm.jobTimeZone");
            String jobWebUrl = req.getParameter("dimensionsscm.jobWebUrl");

            DimensionsSCM scm = new DimensionsSCM(project, folders, pathsToExclude, null, canJobDelete, canJobForce,
                    canJobRevert, jobUserName, jobPasswd, jobServer, jobDatabase, canJobUpdate, jobTimeZone, jobWebUrl,
                    directory, permissions, eol, canJobExpand, canJobNoMetadata, canJobNoTouch, forceAsSlave);

            scm.browser = RepositoryBrowsers.createInstance(DimensionsSCMRepositoryBrowser.class, req, formData,
                    "browser");
            scm.getAPI();
            return scm;
        }

        /**
         * Gets the timezone for the connection.
         * @return the timezone
         */
        public String getTimeZone() {
            return this.timeZone;
        }

        /**
         * Gets the web URL for the connection.
         * @return the web URL
         */
        public String getWebUrl() {
            return this.webUrl;
        }

        /**
         * Gets the user ID for the connection.
         * @return the user ID of the user as whom to connect
         */
        public String getUserName() {
            return this.userName;
        }

        /**
         * Gets the base database for the connection (as "NAME@CONNECTION").
         * @return the name of the base database to connect to
         */
        public String getDatabase() {
            return this.database;
        }

        /**
         * Gets the server for the connection.
         * @return the name of the server to connect to
         */
        public String getServer() {
            return this.server;
        }

        /**
         * Gets the password.
         * @return the password
         */
        public String getPasswd() {
            return Scrambler.descramble(passwd);
        }

        /**
         * Gets the update.
         * @return the update
         */
        public boolean isCanUpdate() {
            return this.canUpdate;
        }

        /**
         * Sets the update.
         */
        public void setCanUpdate(boolean x) {
            this.canUpdate = x;
        }

        /**
         * Sets the user ID for the connection.
         */
        public void setUserName(String userName) {
            this.userName = userName;
        }

        /**
         * Sets the base database for the connection (as "NAME@CONNECTION").
         */
        public void setDatabase(String database) {
            this.database = database;
        }

        /**
         * Sets the server for the connection.
         */
        public void setServer(String server) {
            this.server = server;
        }

        /**
         * Sets the password.
         */
        public void setPasswd(String password) {
            this.passwd = Scrambler.scramble(password);
        }

        /**
         * Sets the timezone for the connection.
         */
        public void setTimeZone(String x) {
            this.timeZone = x;
        }

        /**
         * Sets the web URL for the connection.
         */
        public void setWebUrl(String x) {
            this.webUrl = x;
        }

        public FormValidation doCheck(StaplerRequest req, StaplerResponse rsp)
                throws IOException, ServletException {
            String value = Util.fixEmpty(req.getParameter("value"));
            String nullText = null;
            if (value == null) {
                if (nullText == null) {
                    return FormValidation.ok();
                } else {
                    return FormValidation.error(nullText);
                }
            } else {
                return FormValidation.ok();
            }
        }

        public FormValidation domanadatoryFieldCheck(StaplerRequest req, StaplerResponse rsp)
                throws IOException, ServletException {
            String value = Util.fixEmpty(req.getParameter("value"));
            String errorTxt = "This value is manadatory.";
            if (value == null) {
                return FormValidation.error(errorTxt);
            } else {
                // Some processing.
                return FormValidation.ok();
            }
        }

        public FormValidation domanadatoryJobFieldCheck(StaplerRequest req, StaplerResponse rsp)
                throws IOException, ServletException {
            String value = Util.fixEmpty(req.getParameter("value"));
            String errorTxt = "This value is manadatory.";
            // Some processing in the future.
            return FormValidation.ok();
        }

        /**
         * Check if the specified Dimensions server is valid.
         */
        public FormValidation docheckTz(StaplerRequest req, StaplerResponse rsp,
                @QueryParameter("dimensionsscm.timeZone") final String timezone,
                @QueryParameter("dimensionsscm.jobTimeZone") final String jobtimezone)
                throws IOException, ServletException {
            try {
                String xtz = (jobtimezone != null) ? jobtimezone : timezone;
                Logger.debug("Invoking docheckTz - " + xtz);
                TimeZone ctz = TimeZone.getTimeZone(xtz);
                String  lmt = ctz.getID();
                if (lmt.equalsIgnoreCase("GMT") && !(xtz.equalsIgnoreCase("GMT") ||
                        xtz.equalsIgnoreCase("Greenwich Mean Time") || xtz.equalsIgnoreCase("UTC") ||
                        xtz.equalsIgnoreCase("Coordinated Universal Time"))) {
                    return FormValidation.error("Timezone specified is not valid.");
                } else {
                    return FormValidation.ok("Timezone test succeeded!");
                }
            } catch (Exception e) {
                return FormValidation.error("timezone check error:" + e.getMessage());
            }
        }

        /**
         * Check if the specified Dimensions server is valid.
         */
        public FormValidation docheckServer(StaplerRequest req, StaplerResponse rsp,
                @QueryParameter("dimensionsscm.userName") final String user,
                @QueryParameter("dimensionsscm.passwd") final String passwd,
                @QueryParameter("dimensionsscm.server") final String server,
                @QueryParameter("dimensionsscm.database") final String database,
                @QueryParameter("dimensionsscm.jobUserName") final String jobuser,
                @QueryParameter("dimensionsscm.jobPasswd") final String jobPasswd,
                @QueryParameter("dimensionsscm.jobServer") final String jobServer,
                @QueryParameter("dimensionsscm.jobDatabase") final String jobDatabase)
                throws IOException, ServletException {
            if (connectionCheck == null) {
                connectionCheck = new DimensionsAPI();
            }
            try {
                String xserver = (jobServer != null) ? jobServer : server;
                String xuser = (jobuser != null) ? jobuser : user;
                String xpasswd = (jobPasswd != null) ? jobPasswd : passwd;
                String xdatabase = (jobDatabase != null) ? jobDatabase : database;
                String dmS = xserver + "-" + xuser + ":" + xdatabase;
                Logger.debug("Invoking serverCheck - " + dmS);
                long key = connectionCheck.login(xuser, xpasswd, xdatabase, xserver);
                if (key < 1L) {
                    return FormValidation.error("Connection test failed");
                } else {
                    connectionCheck.logout(key);
                    return FormValidation.ok("Connection test succeeded!");
                }
            } catch (Exception e) {
                return FormValidation.error("Server connection error:" + e.getMessage());
            }
        }
    }
}
