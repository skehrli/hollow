/*
 *  Copyright 2016-2019 Netflix, Inc.
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
 */
package com.netflix.hollow.api.codegen;

import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.Impure;
import org.checkerframework.checker.mustcall.qual.NotOwning;
import org.checkerframework.dataflow.qual.SideEffectFree;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ArgumentParser<T extends Enum> {
    public class ParsedArgument {
        private final T key;
        private final String value;

        @SideEffectFree
        public ParsedArgument(T key, String value) {
            this.key = key;
            this.value = value;
        }

        @NotOwning
        @Pure
        public T getKey() {
            return key;
        }

        @Pure
        public String getValue() {
            return value;
        }
    }
    private static final Pattern COMMAND_LINE_ARG_PATTERN = Pattern.compile("--(\\w+)=([\\w, ./-]+)");

    private final List<ParsedArgument> parsedArguments = new ArrayList<>();

    @Impure
    public ArgumentParser(Class<T> validArguments, String[] args) {
        for (String arg : args) {
            Matcher matcher = COMMAND_LINE_ARG_PATTERN.matcher(arg);
            if (!matcher.matches()) {
                throw new IllegalArgumentException("Invalid argument " + arg);
            }
            String argK = matcher.group(1);
            String argV = matcher.group(2);
            try {
                T key = (T) Enum.valueOf(validArguments, argK);
                parsedArguments.add(new ParsedArgument(key, argV));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid argument " + arg);
            }
        }
    }

    @Pure
    public List<ParsedArgument> getParsedArguments() {
        return this.parsedArguments;
    }
}
