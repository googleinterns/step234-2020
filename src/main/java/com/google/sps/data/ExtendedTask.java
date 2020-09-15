/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.sps.data;

import com.google.api.client.util.GenericData;
import com.google.api.client.util.Key;
import com.google.api.services.tasks.model.Task;
import java.util.Comparator;

// Override equals and hashcode by extending GenericData
// Two tasks are equal if all of their properties match (It makes testing easier)
public class ExtendedTask extends GenericData {
  public static final Comparator<ExtendedTask> BY_DURATION = Comparator.comparing(ExtendedTask::getDuration);
  @Key
  private Task task;
  @Key
  private long duration;

  public ExtendedTask(Task task, Long duration) {
    this.task = task;
    this.duration = duration;
  }

  public static ExtendedTask getExtendedTaskWithDuration(long duration) {
    return new ExtendedTask(new Task(), duration);
  }

  public Task getTask() {
    return task;
  }

  public long getDuration() {
    return duration;
  }

  public void setDuration(long duration) {
    this.duration = duration;
  }

  public String getId() {
    return task.getId();
  }

  public String getTitle() {
    return task.getTitle();
  }
}


