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

package com.google.sps.servlets;

import com.google.api.services.tasks.model.Task;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RunWith(JUnit4.class)
public final class ScheduleServletTest {

  private static final Set<String> SOME_SELECTED_IDS;
  private static final Set<String> EMPTY_IDS = new HashSet<>(Collections.emptyList());

  private static final Task ZERO = new Task();
  private static final Task ONE = new Task();
  private static final Task TWO = new Task();
  private static final Task THREE = new Task();
  private static final Task FOUR = new Task();
  private static final Task FIVE = new Task();
  private static final Task SIX = new Task();
  private static final Task SEVEN = new Task();

  static {
    Set<String> idList = Stream.of("1", "0", "4", "6").collect(Collectors.toCollection(HashSet::new));
    SOME_SELECTED_IDS = Collections.unmodifiableSet(idList);
  }

  private List<Task> allTasks = Arrays.asList(ZERO, ONE, TWO, THREE, FOUR, FIVE, SIX, SEVEN);
  private List<Task> someTasks = Arrays.asList(ZERO, FOUR, FIVE, SEVEN);
  private List<Task> emptyTasks = Collections.emptyList();

  @Before
  public void setTaskIds() {
    for (int i = 0; i < allTasks.size(); i++) {
      allTasks.get(i).setId(Integer.toString(i));
      allTasks.get(i).setTitle("Title of task with id " + i);
    }
  }


  @Test
  public void filterSelectedTaskTitles_allSelectedExist_returnsIntersection() throws IOException {
    ScheduleServlet servlet = new ScheduleServlet();
    List<String> intersection = Stream.of(ZERO, ONE, FOUR, SIX).map(Task::getTitle).collect(Collectors.toList());
    List<String> result = servlet.filterSelectedTaskTitles(SOME_SELECTED_IDS, allTasks);
    Assert.assertEquals(intersection, result);
  }

  @Test
  public void filterSelectedTaskTitles_selectionHasIntersection_returnsIntersection() throws IOException {
    ScheduleServlet servlet = new ScheduleServlet();
    List<String> intersection = Stream.of(ZERO, FOUR).map(Task::getTitle).collect(Collectors.toList());
    List<String> result = servlet.filterSelectedTaskTitles(SOME_SELECTED_IDS, someTasks);
    Assert.assertEquals(intersection, result);
  }

  @Test
  public void filterSelectedTaskTitiles_emptyTaskList_returnsEmptyList() {
    ScheduleServlet servlet = new ScheduleServlet();
    List<String> result = servlet.filterSelectedTaskTitles(SOME_SELECTED_IDS, emptyTasks);
    Assert.assertEquals(Collections.emptyList(), result);
  }

  @Test
  public void filterSelectedTaskTitiles_emptyIdList_returnsEmptyList() {
    ScheduleServlet servlet = new ScheduleServlet();
    List<String> result = servlet.filterSelectedTaskTitles(EMPTY_IDS, someTasks);
    Assert.assertEquals(Collections.emptyList(), result);
  }

}
