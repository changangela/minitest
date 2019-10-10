/*
 * Copyright (c) 2014-2019 by The Minitest Project Developers.
 * Some rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package minitest.runner

import sbt.testing.{Runner => BaseRunner, Task => BaseTask, _}

final class Runner(
  // error overiding Array[T | JavaNull] | JavaNull with Array[T]
  val args: Array[String | Null] | Null,
  val remoteArgs: Array[String | Null] | Null,
  val options: Options,
  classLoader: ClassLoader)
  extends BaseRunner {

  def done(): String = ""

  def tasks(list: Array[TaskDef | Null] | Null): Array[BaseTask | Null] = {
    list.nn.map(t => new Task(t.nn, options, classLoader))
  }

  def receiveMessage(msg: String): Option[String] = {
    None
  }

  def serializeTask(task: BaseTask, serializer: TaskDef | Null => String): String =
    serializer(task.taskDef().nn)

  def deserializeTask(task: String, deserializer: String => TaskDef): BaseTask =
    new Task(deserializer(task), options, classLoader)
}
