// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.api.tasks;

import com.google.common.collect.ImmutableList;
import java.util.Arrays;
import java.util.List;
import com.google.api.services.tasks.model.Task;
import org.mockito.internal.util.reflection.FieldInitializationReport;
import sun.jvm.hotspot.debugger.linux.x86.LinuxX86CFrame;

public class TasksProvider {
  public static final ImmutableList<String> TASKS_SAMPLE = ImmutableList.of(
      "Review code", "Write the design doc", "Talk to PM", "Investigate report", "Prepare slides");

  private static final Task FIRST = new Task();
  private static final Task SECOND = new Task();
  private static final Task ANOTHER = new Task();
  private static final Task YET_ANOTHER = new Task();
  private static final Task LAST = new Task();
  public static final List<Task> TASK_MODEL_LIST = Arrays.asList(FIRST, SECOND, ANOTHER, YET_ANOTHER, LAST);

  public TasksProvider() {
    int index = 0;
    for(Task task: TASK_MODEL_LIST){
      task.setId(Integer.toString(index));
      task.setTitle(Integer.toString(index));
      index++;
    }

  }

  public static List<String> getSampleAsString() {
    return TASKS_SAMPLE;
  }

  public List<Task> getTasks(){
    return TASK_MODEL_LIST;
  }
}
