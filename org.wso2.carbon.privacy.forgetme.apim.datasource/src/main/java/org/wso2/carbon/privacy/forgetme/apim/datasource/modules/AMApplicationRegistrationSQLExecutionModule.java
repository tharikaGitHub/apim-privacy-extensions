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

import org.apache.commons.lang.StringUtils;
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

public class AMApplicationRegistrationSQLExecutionModule implements Module<UserSQLQuery> {

    private static final Logger log = LoggerFactory.getLogger(AMApplicationRegistrationSQLExecutionModule.class);

    private static final String TENANT_ID = "tenant_id";
    private static final String MODIFIED_STRING = "modifiedString";
    private static final String SUBSCRIBER_ID = "subscriberId";
    private static final String SUPER_TENANT = "carbon.super";
    private static final String TENANT_DOMAIN_SEPARATOR = "@";

    private DataSource dataSource;

    public AMApplicationRegistrationSQLExecutionModule(DataSourceConfig dataSourceConfig) throws SQLModuleException {

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

        String query = "UPDATE AM_APPLICATION_REGISTRATION SET INPUTS = `modifiedString` WHERE SUBSCRIBER_ID = `subscriberId`";

        // If this user belongs to a tenant we should append the tenant domain name to username in certain queries.
        if (!StringUtils.equals(userSQLQuery.getUserIdentifier().getTenantDomain(), SUPER_TENANT)) {
            username = username + TENANT_DOMAIN_SEPARATOR + userSQLQuery.getUserIdentifier().getTenantDomain();
        }

        try (Connection connection = dataSource.getConnection()) {

            NamedPreparedStatement namedPreparedStatement = new NamedPreparedStatement(connection,
                    userSQLQuery.getSqlQuery().toString());
            namedPreparedStatement.setInt(TENANT_ID, userSQLQuery.getUserIdentifier().getTenantId());

            ResultSet rs = namedPreparedStatement.getPreparedStatement().executeQuery();

            String modifiedInputString = null;
            int subscriberId = -1;
            while (rs.next()) {
                String inputs = rs.getString("INPUTS");
                subscriberId = rs.getInt("SUBSCRIBER_ID");
                modifiedInputString = inputs.replaceAll("sername\":\"" + username + "\"",
                        "sername\":\"" + userSQLQuery.getUserIdentifier().getPseudonym() + "\"");
            }

            if (modifiedInputString != null && subscriberId != -1) {
                NamedPreparedStatement namedPreparedStatement2 = new NamedPreparedStatement(connection, query);
                namedPreparedStatement2.setString(MODIFIED_STRING, modifiedInputString);
                namedPreparedStatement2.setInt(SUBSCRIBER_ID, subscriberId);
                namedPreparedStatement2.getPreparedStatement().executeUpdate();
            }
        } catch (SQLException e) {
            throw new SQLModuleException(e);
        }
    }
}
