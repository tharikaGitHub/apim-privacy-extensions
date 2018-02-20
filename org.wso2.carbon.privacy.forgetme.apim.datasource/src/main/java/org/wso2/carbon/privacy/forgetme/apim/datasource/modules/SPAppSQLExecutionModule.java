/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.privacy.forgetme.apim.datasource.modules;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.sql.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.sql.exception.SQLModuleException;
import org.wso2.carbon.privacy.forgetme.sql.module.Module;
import org.wso2.carbon.privacy.forgetme.sql.sql.UserSQLQuery;
import org.wso2.carbon.privacy.forgetme.sql.util.NamedPreparedStatement;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SPAppSQLExecutionModule implements Module<UserSQLQuery> {

    private static final Logger log = LoggerFactory.getLogger(SPAppSQLExecutionModule.class);

    private static final String USERNAME = "username";
    private static final String TENANT_ID = "tenant_id";
    private static final String USER_STORE_DOMAIN = "user_store_domain";
    private static final String MODIFIED_APP_NAME = "modifiedAppName";
    private static final String MODIFIED_DESCRIPTION = "modifiedDescription";
    private static final String PSEUDONYM = "pseudonym";

    private DataSource dataSource;

    public SPAppSQLExecutionModule(DataSourceConfig dataSourceConfig) throws SQLModuleException {

        try {
            dataSource = dataSourceConfig.getDatasource();
            if (log.isDebugEnabled()) {
                log.debug("Data source initialized with name: {}.", dataSource.getClass());
            }
        } catch (SQLModuleException e) {
            throw new SQLModuleException("Error occurred while initializing the data source.", e);
        }
    }

    @Override
    public void execute(UserSQLQuery userSQLQuery) throws ModuleException {

        if (dataSource == null) {
            log.warn("No data source configured for name: " + userSQLQuery.getSqlQuery().getBaseDirectory());
            return;
        }

        String username = userSQLQuery.getUserIdentifier().getUsername();

        String query = "UPDATE SP_APP SET USERNAME = `pseudonym`, APP_NAME = `modifiedAppName`, DESCRIPTION = "
                + "`modifiedDescription` WHERE USERNAME = `username` AND USER_STORE = `user_store_domain` AND "
                + "TENANT_ID = `tenant_id`";

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    userSQLQuery.getSqlQuery().toString());
            namedPreparedStatement.setString(USERNAME, username);
            namedPreparedStatement.setInt(TENANT_ID, userSQLQuery.getUserIdentifier().getTenantId());
            namedPreparedStatement.setString(USER_STORE_DOMAIN, userSQLQuery.getUserIdentifier().getUserStoreDomain());

            ResultSet rs = namedPreparedStatement.getPreparedStatement().executeQuery();

            String modifiedApplicationName = null;
            String modifiedDescription = null;
            while (rs.next()) {
                String appName = rs.getString("APP_NAME");
                String description = rs.getString("DESCRIPTION");
                modifiedApplicationName = appName
                        .replaceAll("(^" + username + "_)", userSQLQuery.getUserIdentifier().getPseudonym() + "_");
                modifiedDescription = description.replaceAll("(\\s" + username + "_)",
                        " " + userSQLQuery.getUserIdentifier().getPseudonym() + "_");
            }

            if (modifiedApplicationName != null && modifiedDescription != null) {
                NamedPreparedStatement namedPreparedStatement2 = new NamedPreparedStatement(connection, query);
                namedPreparedStatement2.setString(MODIFIED_APP_NAME, modifiedApplicationName);
                namedPreparedStatement2.setString(MODIFIED_DESCRIPTION, modifiedDescription);
                namedPreparedStatement2.setString(PSEUDONYM, userSQLQuery.getUserIdentifier().getPseudonym());
                namedPreparedStatement2.setString(USERNAME, username);
                namedPreparedStatement2
                        .setString(USER_STORE_DOMAIN, userSQLQuery.getUserIdentifier().getUserStoreDomain());
                namedPreparedStatement2.setInt(TENANT_ID, userSQLQuery.getUserIdentifier().getTenantId());
                namedPreparedStatement2.getPreparedStatement().executeUpdate();
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }
}
