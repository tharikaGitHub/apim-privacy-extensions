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

import org.wso2.carbon.privacy.forgetme.api.runtime.Environment;
import org.wso2.carbon.privacy.forgetme.api.runtime.ForgetMeInstruction;
import org.wso2.carbon.privacy.forgetme.api.runtime.InstructionReader;
import org.wso2.carbon.privacy.forgetme.api.runtime.ModuleException;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Implements Instruction generation for apim specific datasources processing.
 */
public class APIMDatasourcesInstructionReader implements InstructionReader {

    private static final String APIM_DATASOURCES_EXTENTION = "apim-datasources";

    @Override
    public String getType() {
        return APIM_DATASOURCES_EXTENTION;
    }

    @Override
    public List<ForgetMeInstruction> read(Path contentDir, Properties properties, Environment environment)
            throws ModuleException {
        List<ForgetMeInstruction> result = new ArrayList<>();
        APIMDatasourcesInstruction forgetMeInstruction = new APIMDatasourcesInstruction();
        result.add(forgetMeInstruction);
        return result;
    }
}
