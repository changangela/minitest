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

import minitest.api._
import minitest.platform.{Await, loadModule}
import sbt.testing.{Task => BaseTask, _}
import scala.compat.Platform.EOL
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try

final class Task(task: TaskDef, opts: Options, cl: ClassLoader) extends BaseTask {
  implicit val ec: ExecutionContext = DefaultExecutionContext
  private[this] val console = if (opts.useSbtLogging) None else Some(Array(new ConsoleLogger))

  def tags(): Array[String | Null] = Array.empty
  def taskDef(): TaskDef = task

  def reportStart(name: String, loggers: Array[Logger]): Unit = {
    for (logger <- console.getOrElse(loggers)) {
      val withColors = logger.ansiCodesSupported()
      val color = if (withColors) Console.GREEN else ""
      val reset = if (withColors) Console.RESET else ""
      logger.info(color + name + reset + EOL)
    }
  }

  def report(name: String, r: Result[_], loggers: Array[Logger]): Unit = {
    for (logger <- console.getOrElse(loggers)) {
      logger.info(r.formatted(name, logger.ansiCodesSupported()))
    }
  }

  def execute(eventHandler: EventHandler, _loggers: Array[Logger | Null],
    continuation: Array[BaseTask] => Unit): Unit = {
    val loggers = _loggers.map(x => x.nn)

    def loop(props: Iterator[TestSpec[Unit, Unit]]): Future[Unit] = {
      if (!props.hasNext) unit else {
        val property = props.next()
        val startTS = System.currentTimeMillis()
        val futureResult = property(())

        futureResult.flatMap { result =>
          val endTS = System.currentTimeMillis()

          report(property.name, result, loggers)
          eventHandler.handle(event(result, endTS - startTS))
          loop(props)
        }
      }
    }

    val future = loadSuite(task.fullyQualifiedName().nn, cl).fold(unit) { suite =>
      reportStart(task.fullyQualifiedName().nn, loggers)
      suite.properties.setupSuite()
      loop(suite.properties.iterator).map { _ =>
        suite.properties.tearDownSuite()
      }
    }

    future.onComplete(_ => continuation(Array.empty))
  }

  def execute(eventHandler: EventHandler | Null, loggers: Array[Logger | Null] | Null): Array[BaseTask | Null] = {
    val p = Promise[Unit]()
    execute(eventHandler.nn, loggers.nn, _ => p.success(()))
    Await.result(p.future, Duration.Inf)
    Array.empty
  }

  def loadSuite(name: String, loader: ClassLoader): Option[AbstractTestSuite] = {
    Try {
      // Manually inlined from TestUtils.loadModule(name, loader) Scala 2 macro
      val clazz = loader.loadClass(name + "$")
      clazz.getField("MODULE$").get(null)
    }.toOption
      .collect { case ref: AbstractTestSuite => ref }
  }

  def event(result: Result[Unit], durationMillis: Long): Event = new Event {
    def fullyQualifiedName(): String =
      task.fullyQualifiedName().nn

    def throwable(): OptionalThrowable =
      result match {
        case Result.Exception(source, _) =>
          new OptionalThrowable(source)
        case Result.Failure(_, Some(source), _) =>
          new OptionalThrowable(source)
        case _ =>
          new OptionalThrowable()
      }

    def status(): Status =
      result match {
        case Result.Exception(_,_) =>
          Status.Error
        case Result.Failure(_,_,_) =>
          Status.Failure
        case Result.Success(_) =>
          Status.Success
        case Result.Ignored(_,_) =>
          Status.Ignored
        case Result.Canceled(_,_) =>
          Status.Canceled
      }

    def selector(): Selector = {
      task.selectors().nn.head.nn
    }

    def fingerprint(): Fingerprint =
      task.fingerprint().nn

    def duration(): Long =
      durationMillis
  }

  private[this] val unit = Future.successful(())
}
