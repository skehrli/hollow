/*
 *  Copyright 2021 Netflix, Inc.
 *
 *     Licensed under the Apache License, Version 2.0 (the "License");
 *     you may not use this file except in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *     Unless required by applicable law or agreed to in writing, software
 *     distributed under the License is distributed on an "AS IS" BASIS,
 *     WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *     See the License for the specific language governing permissions and
 *     limitations under the License.
 *
 */
package com.netflix.hollow.api.codegen.perfapi;

import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.dataflow.qual.Pure;
import com.netflix.hollow.core.HollowDataset;
import com.netflix.hollow.core.schema.HollowObjectSchema;
import com.netflix.hollow.core.schema.HollowSchema;
import com.netflix.hollow.core.schema.HollowSchema.SchemaType;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class HollowPerformanceAPIGenerator {

    private HollowDataset dataset;
    private String apiClassname;
    private String packageName;
    private Path destinationPath;
    private Set<String> checkFieldExistsMethods = new HashSet<>();
    
    @Impure
    public static Builder newBuilder() {
        HollowPerformanceAPIGenerator gen = new HollowPerformanceAPIGenerator();
        return gen.theBuilder();
    }
    
    @Impure
    private Builder theBuilder() {
        return new Builder();
    }

    public class Builder {
        @Impure
        public Builder withDataset(HollowDataset dataset) {
            HollowPerformanceAPIGenerator.this.dataset = dataset;
            return this;
        }
        
        @Impure
        public Builder withAPIClassname(String apiClassname) {
            HollowPerformanceAPIGenerator.this.apiClassname = apiClassname;
            return this;
        }
        
        @Impure
        public Builder withPackageName(String packageName) {
            HollowPerformanceAPIGenerator.this.packageName = packageName;
            return this;
        }
        
        @Impure
        public Builder withDestination(String destinationPath) {
            return withDestination(Paths.get(destinationPath));
        }

        @Impure
        public Builder withDestination(Path destinationPath) {
            HollowPerformanceAPIGenerator.this.destinationPath = destinationPath;
            return this;
        }
        
        @Impure
        public Builder withCheckFieldExistsMethods(Set<String> checkFieldExistsMethods) {
            HollowPerformanceAPIGenerator.this.checkFieldExistsMethods.addAll(checkFieldExistsMethods);
            return this;
        }
        
        @Impure
        public Builder withCheckFieldExistsMethods(String... checkFieldExistsMethods) {
            HollowPerformanceAPIGenerator.this.checkFieldExistsMethods.addAll(Arrays.asList(checkFieldExistsMethods));
            return this;
        }
        
        @Pure
        public HollowPerformanceAPIGenerator build() {
            return HollowPerformanceAPIGenerator.this;
        }
    }
    
    @Impure
    public void generateSourceFiles() throws IOException {
        generate(dataset, packageName, apiClassname, destinationPath, checkFieldExistsMethods);
    }
    
    @Impure
    private void generate(HollowDataset dataset, String packageName, String apiClassName, Path destination, Set<String> checkFieldExistsMethods) throws IOException {
        Path packagePath = Paths.get(packageName.replace(".", File.separator));
        if (!destination.toAbsolutePath().endsWith(packagePath)) {
            destination = destination.resolve(packagePath);
        }
        Path apiClassDestination = destination.resolve(apiClassName + ".java");
        if (!Files.exists(apiClassDestination)) {
            Files.createDirectories(destination);
        }

        String apiClassContent = new HollowPerformanceAPIClassGenerator(dataset, apiClassName, packageName).generate();
        try (FileWriter writer = new FileWriter(apiClassDestination.toFile())) {
            writer.write(apiClassContent);
        }

        for (HollowSchema schema : dataset.getSchemas()) {
            if (schema.getSchemaType() == SchemaType.OBJECT) {
                Path objClassDestination = destination.resolve(schema.getName() + "PerfAPI.java");
                String objClassContent = new HollowObjectTypePerfAPIClassGenerator((HollowObjectSchema) schema, packageName, checkFieldExistsMethods).generate();
                try (FileWriter writer = new FileWriter(objClassDestination.toFile())) {
                    writer.write(objClassContent);
                }
            }
        }
    }

}