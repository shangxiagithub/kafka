/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.kafka.streams.processor.internals;

import org.apache.kafka.common.config.TopicConfig;
import org.apache.kafka.common.errors.InvalidTopicException;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class InternalTopicConfigTest {

    @Test
    public void shouldThrowNpeIfTopicConfigIsNull() {
        assertThrows(NullPointerException.class, () -> new RepartitionTopicConfig("topic", null));
    }

    @Test
    public void shouldThrowIfNameIsNull() {
        assertThrows(NullPointerException.class, () -> new RepartitionTopicConfig(null, Collections.emptyMap()));
    }

    @Test
    public void shouldThrowIfNameIsInvalid() {
        assertThrows(InvalidTopicException.class, () -> new RepartitionTopicConfig("foo bar baz", Collections.emptyMap()));
    }

    @Test
    public void shouldSetCreateTimeByDefaultForWindowedChangelog() {
        final WindowedChangelogTopicConfig topicConfig = new WindowedChangelogTopicConfig("name", Collections.emptyMap(), 10);

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("CreateTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }

    @Test
    public void shouldSetCreateTimeByDefaultForUnwindowedUnversionedChangelog() {
        final UnwindowedUnversionedChangelogTopicConfig topicConfig = new UnwindowedUnversionedChangelogTopicConfig("name", Collections.emptyMap());

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("CreateTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }

    @Test
    public void shouldSetCreateTimeByDefaultForVersionedChangelog() {
        final VersionedChangelogTopicConfig topicConfig = new VersionedChangelogTopicConfig("name", Collections.emptyMap(), 12);

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("CreateTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }

    @Test
    public void shouldSetCreateTimeByDefaultForRepartitionTopic() {
        final RepartitionTopicConfig topicConfig = new RepartitionTopicConfig("name", Collections.emptyMap());

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("CreateTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }

    @Test
    public void shouldAugmentRetentionMsWithWindowedChangelog() {
        final WindowedChangelogTopicConfig topicConfig = new WindowedChangelogTopicConfig("name", Collections.emptyMap(), 10);
        assertEquals("30", topicConfig.getProperties(Collections.emptyMap(), 20).get(TopicConfig.RETENTION_MS_CONFIG));
    }

    @Test
    public void shouldAugmentCompactionLagMsWithVersionedChangelog() {
        final VersionedChangelogTopicConfig topicConfig = new VersionedChangelogTopicConfig("name", Collections.emptyMap(), 12);
        assertEquals(Long.toString(12 + 24 * 60 * 60 * 1000L), topicConfig.getProperties(Collections.emptyMap(), 20).get(TopicConfig.MIN_COMPACTION_LAG_MS_CONFIG));
    }

    @Test
    public void shouldUseSuppliedConfigsForWindowedChangelogConfig() {
        final Map<String, String> configs = new HashMap<>();
        configs.put("message.timestamp.type", "LogAppendTime");

        final WindowedChangelogTopicConfig topicConfig = new WindowedChangelogTopicConfig("name", configs, 10);

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("LogAppendTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }

    @Test
    public void shouldUseSuppliedConfigsForVersionedChangelogConfig() {
        final Map<String, String> configs = new HashMap<>();
        configs.put("message.timestamp.type", "LogAppendTime");

        final VersionedChangelogTopicConfig topicConfig = new VersionedChangelogTopicConfig("name", configs, 12);

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("LogAppendTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }

    @Test
    public void shouldUseSuppliedConfigsForUnwindowedUnversionedChangelogConfig() {
        final Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", "1000");
        configs.put("retention.bytes", "10000");
        configs.put("message.timestamp.type", "LogAppendTime");

        final UnwindowedUnversionedChangelogTopicConfig topicConfig = new UnwindowedUnversionedChangelogTopicConfig("name", configs);

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("1000", properties.get(TopicConfig.RETENTION_MS_CONFIG));
        assertEquals("10000", properties.get(TopicConfig.RETENTION_BYTES_CONFIG));
        assertEquals("LogAppendTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }

    @Test
    public void shouldUseSuppliedConfigsForRepartitionConfig() {
        final Map<String, String> configs = new HashMap<>();
        configs.put("retention.ms", "1000");
        configs.put("message.timestamp.type", "LogAppendTime");

        final RepartitionTopicConfig topicConfig = new RepartitionTopicConfig("name", configs);

        final Map<String, String> properties = topicConfig.getProperties(Collections.emptyMap(), 0);
        assertEquals("1000", properties.get(TopicConfig.RETENTION_MS_CONFIG));
        assertEquals("LogAppendTime", properties.get(TopicConfig.MESSAGE_TIMESTAMP_TYPE_CONFIG));
    }
}