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

import minitest.runner.Framework.ModuleFingerprint
import sbt.testing.{Framework => BaseFramework, Runner => _, _}

class Framework extends BaseFramework {
  def name(): String = "minitest"

  def options: Options = Options()

  // overriding method of type (): Array[sbt.testing.Fingerprint | JavaNull] | JavaNull
  def fingerprints(): Array[Fingerprint | Null] =
    Array(ModuleFingerprint)

  def runner(args: Array[String | Null] | Null, remoteArgs: Array[String | Null] | Null, testClassLoader: ClassLoader | Null): Runner =
    new minitest.runner.Runner(args, remoteArgs, options, testClassLoader.nn)

  // overriding method of type Array[something | JavaNull] | JavaNull
  def slaveRunner(args: Array[String | Null], remoteArgs: Array[String | Null], testClassLoader: ClassLoader, send: String => Unit): Runner =
    runner(args, remoteArgs, testClassLoader)
}

object Framework {
  /**
   * A fingerprint that searches only for singleton objects
   * of type [[minitest.api.AbstractTestSuite]].
   */
  object ModuleFingerprint extends SubclassFingerprint {
    val isModule = true
    def requireNoArgConstructor(): Boolean = true
    def superclassName(): String = "minitest.api.AbstractTestSuite"
  }
}
