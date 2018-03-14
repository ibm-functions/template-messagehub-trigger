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
import common.TestUtils.RunResult
import com.jayway.restassured.RestAssured
import com.jayway.restassured.config.SSLConfig
import spray.json._
import spray.json.DefaultJsonProtocol._

@RunWith(classOf[JUnitRunner])
class MessageHubBlueTests extends TestHelpers
  with WskTestHelpers
  with BeforeAndAfterAll {

  implicit val wskprops = WskProps()
  val wsk = new Wsk()

  //set parameters for deploy tests
  val nodejs8folder = "../runtimes/nodejs/actions";
  val nodejs8kind = "nodejs:8"
  val nodejs6folder = "../runtimes/nodejs-6/actions";
  val nodejs6kind = "nodejs:6"
  val phpfolder = "../runtimes/php/actions";
  val phpkind = "php:7.1"
  val pythonfolder = "../runtimes/python/actions";
  val pythonkind = "python-jessie:3"
  val swiftfolder = "../runtimes/swift/actions";
  val swiftkind = "swift:4.1"

  // params for messagehub actions
  val catsArray = Map("cats" -> JsArray(JsObject(
    "name" -> JsString("Kat"),
    "color" -> JsString("Red"))))
  val finalParam = Map("messages" -> JsArray(JsObject("value" -> JsObject(catsArray))))

  behavior of "MessageHub Template"

  /**
    * Test the nodejs 6 "messageHub trigger" template
    */
  it should "invoke nodejs 6 process-message.js and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubNode6" + timestamp
    val file = Some(new File(nodejs6folder, "process-message.js").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(nodejs6kind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }

  it should "invoke nodejs 6 process-message.js without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubNode6" + timestamp
    val file = Some(new File(nodejs6folder, "process-message.js").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(nodejs6kind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  /**
    * Test the nodejs 8 "messageHub trigger" template
    */
  it should "invoke nodejs 8 process-message.js and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubNode8" + timestamp
    val file = Some(new File(nodejs8folder, "process-message.js").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(nodejs8kind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }

  it should "invoke nodejs 8 process-message.js without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubNode8" + timestamp
    val file = Some(new File(nodejs8folder, "process-message.js").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(nodejs8kind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  /**
    * Test the python "messageHub trigger" template
    */
  it should "invoke process-message.py and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubPython" + timestamp
    val file = Some(new File(pythonfolder, "process-message.py").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(pythonkind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }
  it should "invoke process-message.py without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubPython" + timestamp
    val file = Some(new File(pythonfolder, "process-message.py").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(pythonkind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  /**
    * Test the php "messageHub trigger" template
    */
  it should "invoke process-message.php and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubPhp" + timestamp
    val file = Some(new File(phpfolder, "process-message.php").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(phpkind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }
  it should "invoke process-message.php without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubPhp" + timestamp
    val file = Some(new File(phpfolder, "process-message.php").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(phpkind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  /**
    * Test the swift "messageHub trigger" template
    */
  it should "invoke process-message.swift and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubSwift" + timestamp
    val file = Some(new File(swiftfolder, "process-message.swift").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(swiftkind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }

  it should "invoke process-message.swift without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>
    val timestamp: String = System.currentTimeMillis.toString
    val name = "messageHubSwift" + timestamp
    val file = Some(new File(swiftfolder, "process-message.swift").toString());

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind = Some(swiftkind))
    }

    withActivation(wsk.activation, wsk.action.invoke(name)) {
      activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include("Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }
  }

  private def makePostCallWithExpectedResult(params: JsObject, expectedResult: String, expectedCode: Int) = {
    val response = RestAssured.given()
      .contentType("application/json\r\n")
      .config(RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation()))
      .body(params.toString())
      .post(deployActionURL)
    assert(response.statusCode() == expectedCode)
    response.body.asString should include(expectedResult)
    response.body.asString.parseJson.asJsObject.getFields("activationId") should have length 1
  }

  private def verifyRuleList(ruleListResult: RunResult, ruleName: String) = {
    val ruleList = ruleListResult.stdout
    val listOutput = ruleList.lines
    listOutput.find(_.contains(ruleName)).get should (include(ruleName) and include("active"))
  }

  private def verifyTriggerList(triggerListResult: RunResult, triggerName: String) = {
    val triggerList = triggerListResult.stdout
    val listOutput = triggerList.lines
    listOutput.find(_.contains(triggerName)).get should include(triggerName)
  }

  private def verifyAction(action: RunResult, name: String, kindValue: JsString): Unit = {
    val stdout = action.stdout
    assert(stdout.startsWith(s"ok: got action $name\n"))
    wsk.parseJsonString(stdout).fields("exec").asJsObject.fields("kind") shouldBe kindValue
  }
}
