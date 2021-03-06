/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.underlying.common.yaml.swapper;

import org.apache.shardingsphere.sharding.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.sharding.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.config.YamlRuleConfiguration;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.stream.Collectors;

/**
 * YAML rule configuration swapper engine.
 */
public final class YamlRuleConfigurationSwapperEngine {
    
    static {
        ShardingSphereServiceLoader.register(YamlRuleConfigurationSwapper.class);
    }
    
    /**
     * Swap to YAML rule configurations.
     * 
     * @param ruleConfigurations rule configurations
     * @return YAML rule configurations
     */
    @SuppressWarnings("unchecked")
    public Collection<YamlRuleConfiguration> swapToYAMLConfigurations(final Collection<RuleConfiguration> ruleConfigurations) {
        Collection<YamlRuleConfiguration> result = new LinkedList<>();
        for (Entry<RuleConfiguration, YamlRuleConfigurationSwapper> entry : OrderedSPIRegistry.getRegisteredServices(ruleConfigurations, YamlRuleConfigurationSwapper.class).entrySet()) {
            result.add((YamlRuleConfiguration) entry.getValue().swap(entry.getKey()));
        }
        return result;
    }
    
    /**
     * Swap from YAML rule configurations to rule configurations.
     *
     * @param yamlRuleConfigurations YAML rule configurations
     * @return rule configurations
     */
    public Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<YamlRuleConfiguration> yamlRuleConfigurations) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        Collection<Class<?>> ruleConfigurationTypes = yamlRuleConfigurations.stream().map(YamlRuleConfiguration::getRuleConfigurationType).collect(Collectors.toList());
        for (Entry<Class<?>, YamlRuleConfigurationSwapper> entry : OrderedSPIRegistry.getRegisteredServicesByClass(ruleConfigurationTypes, YamlRuleConfigurationSwapper.class).entrySet()) {
            result.addAll(swapToRuleConfigurations(yamlRuleConfigurations, entry.getKey(), entry.getValue()));
        }
        return result;
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> swapToRuleConfigurations(final Collection<YamlRuleConfiguration> yamlRuleConfigurations, 
                                                                   final Class<?> ruleConfigurationType, final YamlRuleConfigurationSwapper swapper) {
        Collection<RuleConfiguration> result = new LinkedList<>();
        for (YamlRuleConfiguration each : yamlRuleConfigurations) {
            if (each.getRuleConfigurationType().equals(ruleConfigurationType)) {
                result.add((RuleConfiguration) swapper.swap(each));
            }
        }
        return result;
    }
}
