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

public class APIMDatasourceConstants {

    //SQL Queries

    public static final String AM_APPLICATION_REGISTRATION_QUERY = "SELECT INPUTS, SUBSCRIBER_ID FROM "
            + "AM_APPLICATION_REGISTRATION AM WHERE (SELECT TENANT_ID FROM AM_SUBSCRIBER SUB WHERE SUB.SUBSCRIBER_ID = "
            + "AM.SUBSCRIBER_ID) = `tenant_id`";
    public static final String IDN_OAUTH_CONSUMER_APPS_QUERY = "SELECT APP_NAME FROM IDN_OAUTH_CONSUMER_APPS WHERE "
            + "USERNAME = `username` AND TENANT_ID = `tenant_id` AND USER_DOMAIN = `user_store_domain`";
    public static final String SP_APP_QUERY = "SELECT APP_NAME, DESCRIPTION FROM SP_APP WHERE USERNAME = `username` "
            + "AND TENANT_ID = `tenant_id` AND USER_STORE = `user_store_domain`";

    //Table names

    public static final String AM_APPLICATION_REGISTRATION_TABLE = "AM_APPLICATION_REGISTRATION";
    public static final String IDN_OAUTH_CONSUMER_APPS_TABLE = "IDN_OAUTH_CONSUMER_APPS";
    public static final String SP_APP_TABLE = "SP_APP";

    //Datasources

    public static final String WSO2AM_DB = "WSO2AM_DB";
}
