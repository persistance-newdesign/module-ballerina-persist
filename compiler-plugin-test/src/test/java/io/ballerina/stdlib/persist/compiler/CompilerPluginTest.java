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

import io.ballerina.projects.DiagnosticResult;
import io.ballerina.projects.Package;
import io.ballerina.projects.ProjectEnvironmentBuilder;
import io.ballerina.projects.directory.BuildProject;
import io.ballerina.projects.directory.SingleFileProject;
import io.ballerina.projects.environment.Environment;
import io.ballerina.projects.environment.EnvironmentBuilder;
import io.ballerina.tools.diagnostics.Diagnostic;
import io.ballerina.tools.diagnostics.DiagnosticInfo;
import io.ballerina.tools.diagnostics.DiagnosticSeverity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_101;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_102;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_103;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_110;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_111;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_112;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_113;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_114;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_115;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_121;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_122;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_123;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_124;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_129;

/**
 * Tests persist compiler plugin.
 */
public class CompilerPluginTest {

    private static ProjectEnvironmentBuilder getEnvironmentBuilder() {
        Path distributionPath = Paths.get("../", "target", "ballerina-runtime").toAbsolutePath();
        Environment environment = EnvironmentBuilder.getBuilder().setBallerinaHome(distributionPath).build();
        return ProjectEnvironmentBuilder.getBuilder(environment);
    }

    private Package loadPersistModelFile(String name) {
        Path projectDirPath = Paths.get("src", "test", "resources", "test-src", "project_2", "persist").
                toAbsolutePath().resolve(name);
        SingleFileProject project = SingleFileProject.load(getEnvironmentBuilder(), projectDirPath);
        return project.currentPackage();
    }

    @Test
    public void identifyModelFileFailure1() {
        Path projectDirPath = Paths.get("src", "test", "resources", "test-src", "persist").
                toAbsolutePath().resolve("single-bal.bal");
        SingleFileProject project = SingleFileProject.load(getEnvironmentBuilder(), projectDirPath);
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0);
    }

    @Test
    public void identifyModelFileFailure2() {
        Path projectDirPath = Paths.get("src", "test", "resources", "test-src", "project_1", "resources").
                toAbsolutePath().resolve("single-bal.bal");
        SingleFileProject project = SingleFileProject.load(getEnvironmentBuilder(), projectDirPath);
        DiagnosticResult diagnosticResult = project.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0);
    }

    @Test
    public void skipValidationsForBalProjectFiles() {
        Path projectDirPath = Paths.get("src", "test", "resources", "test-src", "project_1").
                toAbsolutePath();
        BuildProject project2 = BuildProject.load(getEnvironmentBuilder(), projectDirPath);
        DiagnosticResult diagnosticResult = project2.currentPackage().getCompilation().diagnosticResult();
        Assert.assertEquals(diagnosticResult.diagnosticCount(), 0);
    }

    @Test
    public void identifyModelFileSuccess() {
        List<Diagnostic> diagnostics = getDiagnostic("valid-persist-model-path.bal", 1, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{"persist model definition only supports enum and record declarations"},
                new String[]{PERSIST_101.getCode()},
                new String[]{"(2:0,3:1)"}
        );
    }

    @Test
    public void validateEntityRecordProperties() {
        List<Diagnostic> diagnostics = getDiagnostic("record-properties.bal", 1, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "an entity should be a closed record"
                },
                new String[]{
                        PERSIST_102.getCode()
                },
                new String[]{
                        "(11:25,17:1)"
                }
        );
    }

    @Test
    public void validateEntityFieldProperties() {
        List<Diagnostic> diagnostics = getDiagnostic("field-properties.bal", 4, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "an entity does not support defaultable field",
                        "an entity does not support inherited field",
                        "an entity does not support optional field",
                        "an entity does not support rest descriptor field"
                },
                new String[]{
                        PERSIST_111.getCode(),
                        PERSIST_112.getCode(),
                        PERSIST_113.getCode(),
                        PERSIST_110.getCode()
                },
                new String[]{
                        "(4:4,4:28)",
                        "(12:4,12:17)",
                        "(13:4,13:35)",
                        "(22:4,22:11)"
                }
        );
    }

    @Test
    public void validateEntityFieldType() {
        List<Diagnostic> diagnostics = getDiagnostic("field-types.bal", 4, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "an entity field of array type is not supported",
                        "an entity field of 'json' type is not supported",
                        "an entity field of 'json[]' type is not supported",
                        "an entity field of array type is not supported"
                },
                new String[]{
                        PERSIST_115.getCode(),
                        PERSIST_114.getCode(),
                        PERSIST_114.getCode(),
                        PERSIST_115.getCode()
                },
                new String[]{
                        "(12:4,12:11)",
                        "(14:4,14:8)",
                        "(15:4,15:10)",
                        "(18:4,18:16)"
                }
        );
    }

    @Test
    public void validateReadonlyFieldCount() {
        List<Diagnostic> diagnostics = getDiagnostic("readonly-field.bal", 1, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "entity 'MedicalNeed' must have an identifier readonly field"
                },
                new String[]{
                        PERSIST_103.getCode()
                },
                new String[]{
                        "(3:12,3:23)"
                }
        );
    }

    @Test
    public void validateSelfReferencedEntity() {
        List<Diagnostic> diagnostics = getDiagnostic("self-referenced-entity.bal", 1, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "an entity cannot reference itself in association"
                },
                new String[]{
                        PERSIST_121.getCode()
                },
                new String[]{
                        "(8:4,8:26)"
                }
        );
    }

    @Test
    public void validateManyToManyRelationship() {
        List<Diagnostic> diagnostics = getDiagnostic("many-to-many.bal", 1, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "n:m association is not supported yet"
                },
                new String[]{
                        PERSIST_129.getCode()
                },
                new String[]{
                        "(14:4,14:24)"
                }
        );
    }

    @Test
    public void validateMandatoryRelationField() {
        List<Diagnostic> diagnostics = getDiagnostic("mandatory-relation-field.bal", 2, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "the associated entity 'Workspace' does not have the field with the relationship type",
                        "the associated entity 'Building1' does not have the field with the relationship type"
                },
                new String[]{
                        PERSIST_122.getCode(),
                        PERSIST_122.getCode()
                },
                new String[]{
                        "(8:4,8:27)",
                        "(27:4,27:23)"
                }
        );
    }

    @Test
    public void validateDuplicatedRelationField() {
        List<Diagnostic> diagnostics = getDiagnostic("duplicated-relations-field.bal", 2, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "entity does not support duplicated relations to an associated entity",
                        "entity does not support duplicated relations to an associated entity"
                },
                new String[]{
                        PERSIST_123.getCode(),
                        PERSIST_123.getCode()
                },
                new String[]{
                        "(9:4,9:28)",
                        "(31:4,31:24)"
                }
        );
    }

    @Test
    public void validatePresenceOfForeignKeyField() {
        List<Diagnostic> diagnostics = getDiagnostic("foreign-key-present.bal", 4, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "entity should not contain foreign key field for relation 'Building'",
                        "entity should not contain foreign key field for relation 'Building2'",
                        "entity should not contain foreign key field for relation 'Workspace3'",
                        "entity should not contain foreign key field for relation 'Building4'"
                },
                new String[]{
                        PERSIST_124.getCode(),
                        PERSIST_124.getCode(),
                        PERSIST_124.getCode(),
                        PERSIST_124.getCode()
                },
                new String[]{
                        "(15:4,15:32)",
                        "(22:4,22:33)",
                        "(42:4,42:33)",
                        "(56:4,56:33)"
                }
        );
    }

    private List<Diagnostic> getDiagnostic(String modelFileName, int count, DiagnosticSeverity diagnosticSeverity) {
        DiagnosticResult diagnosticResult = loadPersistModelFile(modelFileName).getCompilation().diagnosticResult();
        List<Diagnostic> errorDiagnosticsList = diagnosticResult.diagnostics().stream().filter
                (r -> r.diagnosticInfo().severity().equals(diagnosticSeverity)).collect(Collectors.toList());
        Assert.assertEquals(errorDiagnosticsList.size(), count);
        return errorDiagnosticsList;
    }

    private void testDiagnostic(List<Diagnostic> errorDiagnosticsList, String[] messages, String[] codes,
                                String[] locations) {
        for (int index = 0; index < errorDiagnosticsList.size(); index++) {
            Diagnostic diagnostic = errorDiagnosticsList.get(index);
            String location = diagnostic.location().lineRange().toString();
            Assert.assertEquals(location, locations[index]);
            DiagnosticInfo diagnosticInfo = diagnostic.diagnosticInfo();
            Assert.assertEquals(diagnosticInfo.code(), codes[index]);
            Assert.assertTrue(diagnosticInfo.messageFormat().startsWith(messages[index]));
        }
    }
}
