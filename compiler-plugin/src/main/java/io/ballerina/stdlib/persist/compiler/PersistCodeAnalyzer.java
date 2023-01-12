/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.stdlib.persist.compiler;

import io.ballerina.projects.plugins.CodeAnalysisContext;
import io.ballerina.projects.plugins.CodeAnalyzer;

/**
 * Persist Code Analyzer.
 */
public class PersistCodeAnalyzer extends CodeAnalyzer {

    @Override
    public void init(CodeAnalysisContext codeAnalysisContext) {
//        codeAnalysisContext.addSyntaxNodeAnalysisTask(new PersistRecordValidator(),
//                List.of(SyntaxKind.TYPE_DEFINITION, SyntaxKind.ENUM_DECLARATION, SyntaxKind.TYPE_CAST_EXPRESSION));
    }
}
