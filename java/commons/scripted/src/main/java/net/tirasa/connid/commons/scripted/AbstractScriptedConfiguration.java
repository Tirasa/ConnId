/*
 * ====================
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2016 ConnId. All rights reserved.
 *
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * http://opensource.org/licenses/cddl1.php
 * See the License for the specific language governing permissions and limitations
 * under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://opensource.org/licenses/cddl1.php.
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * ====================
 */
package net.tirasa.connid.commons.scripted;

import java.io.File;
import org.identityconnectors.common.logging.Log;
import org.identityconnectors.framework.spi.AbstractConfiguration;
import org.identityconnectors.framework.spi.ConfigurationProperty;

public abstract class AbstractScriptedConfiguration extends AbstractConfiguration {

    protected static final Log LOG = Log.getLog(AbstractScriptedConfiguration.class);

    /**
     * Scripting language.
     */
    private String scriptingLanguage = "GROOVY";

    public String getScriptingLanguage() {
        return scriptingLanguage;
    }

    public void setScriptingLanguage(String value) {
        this.scriptingLanguage = value;
    }

    /**
     * Should password be passed to scripts in clear text?
     */
    private boolean clearTextPasswordToScript = true;

    @ConfigurationProperty(displayMessageKey = "clearTextPasswordToScript.display",
            helpMessageKey = "clearTextPasswordToScript.help", order = 1)
    public boolean getClearTextPasswordToScript() {
        return clearTextPasswordToScript;
    }

    public void setClearTextPasswordToScript(boolean value) {
        this.clearTextPasswordToScript = value;
    }

    /**
     * By default, scripts are loaded and compiled when a connector instance ss created and initialized.
     * Setting reloadScriptOnExecution to true will make the connector load and compile the script every time it is
     * called.
     * Use only for test/debug purpose since this can have a significant impact on performance.
     */
    private boolean reloadScriptOnExecution = false;

    @ConfigurationProperty(displayMessageKey = "reloadScriptOnExecution.display",
            helpMessageKey = "reloadScriptOnExecution.help", order = 2)
    public boolean isReloadScriptOnExecution() {
        return reloadScriptOnExecution;
    }

    public void setReloadScriptOnExecution(boolean reloadScriptOnExecution) {
        this.reloadScriptOnExecution = reloadScriptOnExecution;
    }

    /**
     * Create script string.
     */
    private String createScript = "";

    @ConfigurationProperty(displayMessageKey = "createScript.display", helpMessageKey = "createScript.help", order = 3)
    public String getCreateScript() {
        return createScript;
    }

    public void setCreateScript(String value) {
        this.createScript = value;
    }

    /**
     * Update script string.
     */
    private String updateScript = "";

    @ConfigurationProperty(displayMessageKey = "updateScript.display", helpMessageKey = "updateScript.help", order = 4)
    public String getUpdateScript() {
        return updateScript;
    }

    public void setUpdateScript(String value) {
        this.updateScript = value;
    }

    /**
     * Delete script string.
     */
    private String deleteScript = "";

    @ConfigurationProperty(displayMessageKey = "deleteScript.display", helpMessageKey = "deleteScript.help", order = 5)
    public String getDeleteScript() {
        return deleteScript;
    }

    public void setDeleteScript(String value) {
        this.deleteScript = value;
    }

    /**
     * Search script string.
     */
    private String searchScript = "";

    @ConfigurationProperty(displayMessageKey = "searchScript.display", helpMessageKey = "searchScript.help", order = 6)
    public String getSearchScript() {
        return searchScript;
    }

    public void setSearchScript(String value) {
        this.searchScript = value;
    }

    /**
     * Authenticate script string.
     */
    private String authenticateScript = "";

    @ConfigurationProperty(displayMessageKey = "authenticateScript.display",
            helpMessageKey = "authenticateScript.help", order = 6)
    public String getAuthenticateScript() {
        return authenticateScript;
    }

    public void setAuthenticateScript(String value) {
        this.authenticateScript = value;
    }

    /**
     * Resolve username script string.
     */
    private String resolveUsernameScript = "";

    @ConfigurationProperty(displayMessageKey = "resolveUsernameScript.display", helpMessageKey =
            "resolveUsernameScript.help", order = 6)
    public String getResolveUsernameScript() {
        return resolveUsernameScript;
    }

    public void setResolveUsernameScript(String value) {
        this.resolveUsernameScript = value;
    }

    /**
     * Sync script string.
     */
    private String syncScript = "";

    @ConfigurationProperty(displayMessageKey = "syncScript.display", helpMessageKey = "syncScript.help", order = 7)
    public String getSyncScript() {
        return syncScript;
    }

    public void setSyncScript(String value) {
        this.syncScript = value;
    }

    /**
     * Schema script string.
     */
    private String schemaScript = "";

    @ConfigurationProperty(displayMessageKey = "schemaScript.display", helpMessageKey = "schemaScript.help", order = 8)
    public String getSchemaScript() {
        return schemaScript;
    }

    public void setSchemaScript(String value) {
        this.schemaScript = value;
    }

    /**
     * Test script string.
     */
    private String testScript = "";

    @ConfigurationProperty(displayMessageKey = "testScript.display", helpMessageKey = "testScript.help", order = 9)
    public String getTestScript() {
        return testScript;
    }

    public void setTestScript(String value) {
        this.testScript = value;
    }

    /**
     * Create script filename.
     */
    private String createScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "createScriptFileName.display",
            helpMessageKey = "createScriptFileName.help", order = 10)
    public String getCreateScriptFileName() {
        return createScriptFileName;
    }

    public void setCreateScriptFileName(String value) {
        this.createScriptFileName = value;
    }

    /**
     * Update script FileName.
     */
    private String updateScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "updateScriptFileName.display",
            helpMessageKey = "updateScriptFileName.help", order = 11)
    public String getUpdateScriptFileName() {
        return updateScriptFileName;
    }

    public void setUpdateScriptFileName(String value) {
        this.updateScriptFileName = value;
    }

    /**
     * Delete script FileName.
     */
    private String deleteScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "deleteScriptFileName.display",
            helpMessageKey = "deleteScriptFileName.help", order = 12)
    public String getDeleteScriptFileName() {
        return deleteScriptFileName;
    }

    public void setDeleteScriptFileName(String value) {
        this.deleteScriptFileName = value;
    }

    /**
     * Search script FileName.
     */
    private String searchScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "searchScriptFileName.display",
            helpMessageKey = "searchScriptFileName.help", order = 13)
    public String getSearchScriptFileName() {
        return searchScriptFileName;
    }

    public void setSearchScriptFileName(String value) {
        this.searchScriptFileName = value;
    }

    /**
     * Authenticate script FileName.
     */
    private String authenticateScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "authenticateScriptFileName.display",
            helpMessageKey = "authenticateScriptFileName.help", order = 14)
    public String getAuthenticateScriptFileName() {
        return authenticateScriptFileName;
    }

    public void setAuthenticateScriptFileName(String value) {
        this.authenticateScriptFileName = value;
    }

    /**
     * Resolve username script FileName.
     */
    private String resolveUsernameScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "resolveUsernameScriptFileName.display",
            helpMessageKey = "resolveUsernameScriptFileName.help", order = 15)
    public String getResolveUsernameScriptFileName() {
        return resolveUsernameScriptFileName;
    }

    public void setResolveUsernameScriptFileName(String value) {
        this.resolveUsernameScriptFileName = value;
    }

    /**
     * Sync script FileName.
     */
    private String syncScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "syncScriptFileName.display",
            helpMessageKey = "syncScriptFileName.help", order = 16)
    public String getSyncScriptFileName() {
        return syncScriptFileName;
    }

    public void setSyncScriptFileName(String value) {
        this.syncScriptFileName = value;
    }

    /**
     * Schema script FileName.
     */
    private String schemaScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "schemaScriptFileName.display",
            helpMessageKey = "schemaScriptFileName.help", order = 17)
    public String getSchemaScriptFileName() {
        return schemaScriptFileName;
    }

    public void setSchemaScriptFileName(String value) {
        this.schemaScriptFileName = value;
    }

    /**
     * Test script FileName.
     */
    private String testScriptFileName = null;

    @ConfigurationProperty(displayMessageKey = "testScriptFileName.display",
            helpMessageKey = "testScriptFileName.help", order = 18)
    public String getTestScriptFileName() {
        return testScriptFileName;
    }

    public void setTestScriptFileName(String value) {
        this.testScriptFileName = value;
    }

    // =======================================================================
    // Configuration Interface
    // =======================================================================
    /**
     * Attempt to validate the arguments added to the Configuration.
     */
    @Override
    public void validate() {
        // Validate the actions
        LOG.info("Checking Create Script filename");
        checkFileIsReadable("Create", getCreateScriptFileName());
        LOG.info("Checking Update Script filename");
        checkFileIsReadable("Update", getUpdateScriptFileName());
        LOG.info("Checking Delete Script filename");
        checkFileIsReadable("Delete", getDeleteScriptFileName());
        LOG.info("Checking Search Script filename");
        checkFileIsReadable("Search", getSearchScriptFileName());
        LOG.info("Checking Sync Script filename");
        checkFileIsReadable("Sync", getSyncScriptFileName());
        LOG.info("Checking Test Script filename");
        checkFileIsReadable("Test", getTestScriptFileName());
    }

    /**
     * Format message with arguments.
     *
     * @param key key of the message
     * @param objects arguments
     * @return the localized message string
     */
    public String getMessage(String key, Object... objects) {
        final String fmt = getConnectorMessages().format(key, key, objects);
        LOG.ok("Get for a key {0} connector message {1}", key, fmt);
        return fmt;
    }

    private void checkFileIsReadable(final String type, final String fileName) {
        if (fileName == null) {
            LOG.ok("{0} Script Filename is null", type);
        } else {
            File file = new File(AbstractScriptedConnector.resolveVariables(fileName));
            try {
                if (file.canRead()) {
                    LOG.ok("{0} is readable", fileName);
                } else {
                    throw new IllegalArgumentException("Can't read " + fileName);
                }
            } catch (SecurityException e) {
                throw new IllegalArgumentException("Can't read " + fileName, e);
            }
        }
    }

}
