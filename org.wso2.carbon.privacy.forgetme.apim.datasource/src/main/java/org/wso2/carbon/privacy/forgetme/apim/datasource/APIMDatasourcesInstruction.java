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

package org.wso2.carbon.privacy.forgetme.apim.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.privacy.forgetme.api.report.ReportAppender;
import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeResult;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionExecutionException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;
import org.wso2.carbon.privacy.forgetme.api.runtime.ProcessorConfig;
import org.wso2.carbon.privacy.forgetme.api.user.UserIdentifier;
import org.wso2.carbon.privacy.forgetme.apim.datasource.modules.AMApplicationRegistrationSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.apim.datasource.modules.IDNOauthConsumerAppsSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.apim.datasource.modules.SPAppSQLExecutionModule;
import org.wso2.carbon.privacy.forgetme.sql.config.DataSourceConfig;
import org.wso2.carbon.privacy.forgetme.sql.exception.SQLModuleException;
import org.wso2.carbon.privacy.forgetme.sql.instructions.DatasourceProcessorConfig;
import org.wso2.carbon.privacy.forgetme.sql.module.Module;
import org.wso2.carbon.privacy.forgetme.sql.sql.SQLQuery;
import org.wso2.carbon.privacy.forgetme.sql.sql.UserSQLQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * Forget-Me instruction which processes a table in RDBMS.
 * The data-source is passed as a processor config or environment.
 */
public class APIMDatasourcesInstruction implements ForgetMeInstruction {

    private static final Logger log = LoggerFactory.getLogger(APIMDatasourcesInstruction.class);

    @Override
    public ForgetMeResult execute(UserIdentifier userIdentifier, ProcessorConfig processorConfig,
            Environment environment, ReportAppender reportAppender) throws InstructionExecutionException {

        String datasourceName = APIMDatasourceConstants.WSO2AM_DB;

        //Map of SQL Queries for data retrieval for each table of concern stored against the table name
        Map<String, SQLQuery> queryList = new HashMap<>();
        queryList.put(APIMDatasourceConstants.AM_APPLICATION_REGISTRATION_TABLE,
                new SQLQuery(APIMDatasourceConstants.AM_APPLICATION_REGISTRATION_QUERY));
        queryList.put(APIMDatasourceConstants.IDN_OAUTH_CONSUMER_APPS_TABLE,
                new SQLQuery(APIMDatasourceConstants.IDN_OAUTH_CONSUMER_APPS_QUERY));
        queryList.put(APIMDatasourceConstants.SP_APP_TABLE, new SQLQuery(APIMDatasourceConstants.SP_APP_QUERY));

        reportAppender.appendSection("Processing queries in APIM datasource Extensions");

        try {
            for (Map.Entry<String, SQLQuery> query : queryList.entrySet()) {

                UserSQLQuery userSQLQuery = new UserSQLQuery();
                userSQLQuery.setSqlQuery(query.getValue());
                userSQLQuery.setUserIdentifier(userIdentifier);

                DataSourceConfig dataSourceConfig = ((DatasourceProcessorConfig) processorConfig)
                        .getDataSourceConfig(datasourceName);

                String tableName = query.getKey();

                Module<UserSQLQuery> sqlExecutionModule;

                if (APIMDatasourceConstants.AM_APPLICATION_REGISTRATION_TABLE.equals(tableName)) {
                    sqlExecutionModule = new AMApplicationRegistrationSQLExecutionModule(dataSourceConfig);
                } else if (APIMDatasourceConstants.IDN_OAUTH_CONSUMER_APPS_TABLE.equals(tableName)) {
                    sqlExecutionModule = new IDNOauthConsumerAppsSQLExecutionModule(dataSourceConfig);
                } else if (APIMDatasourceConstants.SP_APP_TABLE.equals(tableName)) {
                    sqlExecutionModule = new SPAppSQLExecutionModule(dataSourceConfig);
                } else {
                    throw new SQLModuleException("Cannot find a suitable execution module.");
                }

                sqlExecutionModule.execute(userSQLQuery);
                reportAppender.append("Executed query %s", userSQLQuery);
            }
        } catch (ModuleException e) {
            throw new InstructionExecutionException("Error occurred while executing sql query", e);
        }
        reportAppender.appendSection("Completed all sql queries under APIM Datasources Extensions");
        return new ForgetMeResult();
    }
}
