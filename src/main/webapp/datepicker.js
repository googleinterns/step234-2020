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

$('input[name="daterange"]').daterangepicker({
  ranges: {
    'Tomorrow': [moment().add(1, 'days'), moment().add(1, 'days')],
    'Next week': [moment().startOf('week').add(1, 'weeks').add(1, 'days'), moment().startOf('week').add(2, 'weeks')],
    'Weekend': [moment().startOf('week').add(6, 'days'), moment().startOf('week').add(7, 'days')],
    'Next 7 Days': [moment(), moment().add(7, 'days')],
    'Until end of this month': [moment(), moment().endOf('month')],
    'Next Month': [moment().add(1, 'month').startOf('month').add(), moment().add(1, 'month').endOf('month')]
  },
  "locale": {
    "format": "YYYY-MM-DD",
    "separator": " - "
  },
  "linkedCalendars": false,
  "alwaysShowCalendars": true,
  "startDate": moment().add(1, 'days'),
  "endDate": moment().add(1, 'days'),
  "minDate": moment().add(1, 'days'),
  "maxDate": moment().add(200, 'days')
}, function(start, end, label) {
  console.log('New date range selected: ' + start.format('YYYY-MM-DD') + ' to ' + end.format('YYYY-MM-DD') + ' (predefined range: ' + label + ')');
});
