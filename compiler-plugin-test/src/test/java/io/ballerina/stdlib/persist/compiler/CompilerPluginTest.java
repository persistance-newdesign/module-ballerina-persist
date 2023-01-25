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
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_201;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_202;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_203;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_204;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_205;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_206;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_301;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_302;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_303;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_304;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_305;
import static io.ballerina.stdlib.persist.compiler.DiagnosticsCodes.PERSIST_306;

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
                new String[]{"persist model definition only supports record definitions"},
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
                        PERSIST_202.getCode(),
                        PERSIST_203.getCode(),
                        PERSIST_204.getCode(),
                        PERSIST_201.getCode()
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
                        "array of boolean-typed field is not supported in an entity",
                        "json-typed field is not supported in an entity",
                        "json[]-typed field is not supported in an entity",
                        "union-typed field is not supported in an entity"
                },
                new String[]{
                        PERSIST_206.getCode(),
                        PERSIST_205.getCode(),
                        PERSIST_205.getCode(),
                        PERSIST_205.getCode()
                },
                new String[]{
                        "(12:4,12:11)",
                        "(14:4,14:8)",
                        "(15:4,15:10)",
                        "(18:4,18:21)"
                }
        );
    }

    @Test
    public void validateReadonlyFieldCount() {
        List<Diagnostic> diagnostics = getDiagnostic("readonly-field.bal", 1, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "entity 'MedicalNeed' must have at least one identifier readonly field"
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
                        PERSIST_301.getCode()
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
                        PERSIST_305.getCode()
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
                        "the associated entity 'Workspace' does not have the associated Building-typed field",
                        "the associated entity 'Building1' does not have the associated Workspace2-typed field"
                },
                new String[]{
                        PERSIST_302.getCode(),
                        PERSIST_302.getCode()
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
                        PERSIST_303.getCode(),
                        PERSIST_303.getCode()
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
                        "entity should not contain foreign key field 'buildingBuildingCode' for relation 'Building'",
                        "entity should not contain foreign key field 'building2BuildingCode' for relation 'Building2'",
                        "entity should not contain foreign key field 'workspace3WorkspaceId' for relation 'Workspace3'",
                        "entity should not contain foreign key field 'building4BuildingCode' for relation 'Building4'"
                },
                new String[]{
                        PERSIST_304.getCode(),
                        PERSIST_304.getCode(),
                        PERSIST_304.getCode(),
                        PERSIST_304.getCode()
                },
                new String[]{
                        "(15:4,15:32)",
                        "(22:4,22:33)",
                        "(42:4,42:33)",
                        "(56:4,56:33)"
                }
        );
    }

    @Test
    public void validateInvalidRelations() {
        List<Diagnostic> diagnostics = getDiagnostic("invalid-relation.bal", 2, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "persist model definition only supports record definitions",
                        "Integer[]-typed field is not supported in an entity"
                },
                new String[]{
                        PERSIST_101.getCode(),
                        PERSIST_205.getCode()
                },
                new String[]{
                        "(2:0,2:17)",
                        "(10:4,10:13)"
                }
        );
    }

    @Test
    public void validateOptionalAssociation() {
        List<Diagnostic> diagnostics = getDiagnostic("optional-association.bal", 1, DiagnosticSeverity.ERROR);
        testDiagnostic(
                diagnostics,
                new String[]{
                        "entity does not support nillable associations"
                },
                new String[]{
                        PERSIST_306.getCode()
                },
                new String[]{
                        "(14:4,14:13)"
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
