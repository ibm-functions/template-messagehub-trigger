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

package packages


import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import common.{TestHelpers, Wsk, WskProps, WskTestHelpers}
import java.io._
import spray.json.{JsArray, JsObject, JsString}

@RunWith(classOf[JUnitRunner])
class MessageHubTests extends TestHelpers
    with WskTestHelpers
    with BeforeAndAfterAll {

    implicit val wskprops = WskProps()
    val wsk = new Wsk()

    //set parameters for deploy tests
    val nodejsfolder = "../runtimes/nodejs/actions";
    val phpfolder = "../runtimes/php/actions";
    val pythonfolder = "../runtimes/python/actions";
    val swiftfolder = "../runtimes/swift/actions";

    behavior of "MessageHub Blueprint"
    val catsArray = Map("cats" -> JsArray(JsObject(
      "name" -> JsString("Kat"),
      "color" -> JsString("Red"))))
    val finalParam = Map("messages"->JsArray(JsObject("value" -> JsObject(catsArray))))


  /**
    * Test the nodejs "messageHub trigger" blueprint
    */
  it should "invoke process-message.js and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubNode"
    val file = Some(new File(nodejsfolder, "process-message.js").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }

  it should "invoke process-message.js without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubNode"
    val file = Some(new File(nodejsfolder, "process-message.js").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  /**
    * Test the python "messageHub trigger" blueprint
    */
  it should "invoke process-message.py and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubPython"
    val file = Some(new File(pythonfolder, "process-message.py").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }
  it should "invoke process-message.py without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubPython"
    val file = Some(new File(pythonfolder, "process-message.py").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  /**
    * Test the php "messageHub trigger" blueprint
    */
  it should "invoke process-message.php and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubPhp"
    val file = Some(new File(phpfolder, "process-message.php").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }
  it should "invoke process-message.php without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubPhp"
    val file = Some(new File(phpfolder, "process-message.php").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  /**
    * Test the swift "messageHub trigger" blueprint
    */
  it should "invoke process-message.swift and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubSwift"
    val file = Some(new File(swiftfolder, "process-message.swift").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }
  it should "invoke process-message.swift without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubSwift"
    val file = Some(new File(swiftfolder, "process-message.swift").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }
}
