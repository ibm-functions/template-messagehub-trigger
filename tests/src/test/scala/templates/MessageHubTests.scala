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

import java.io._

import common.TestUtils.RunResult
import common.{TestHelpers, Wsk, WskProps, WskTestHelpers}
import io.restassured.RestAssured
import io.restassured.config.SSLConfig
import org.junit.runner.RunWith
import org.scalatest.BeforeAndAfterAll
import org.scalatest.junit.JUnitRunner
import spray.json.DefaultJsonProtocol._
import spray.json._

@RunWith(classOf[JUnitRunner])
class MessageHubTests extends TestHelpers with WskTestHelpers with BeforeAndAfterAll {

  implicit val wskprops = WskProps()
  val wsk = new Wsk()

  // statuses for deployWeb
  val successStatus =
    """"status": "success""""

  val deployTestRepo = "https://github.com/ibm-functions/template-messagehub-trigger"
  val messagehubAction = "process-message"
  val fakeMessageHubAction = "openwhisk-messagehub/messageHubFeed"
  val deployAction = "/whisk.system/deployWeb/wskdeploy"
  val deployActionURL = s"https://${wskprops.apihost}/api/v1/web${deployAction}.http"
  val triggerName = "myTrigger"
  val packageName = "myPackage"
  val ruleName = "myRule"
  val binding = "openwhisk-messagehub"

  //set parameters for deploy tests
  val nodejsRuntimePath = "runtimes/nodejs"
  val nodejsfolder = "runtimes/nodejs/actions";
  val nodejskind = "nodejs:10"
  val phpRuntimePath = "runtimes/php"
  val phpfolder = "runtimes/php/actions";
  val phpkind = "php:7.3"
  val pythonRuntimePath = "runtimes/python"
  val pythonfolder = "runtimes/python/actions";
  val pythonkind = "python:3.7"
  val swiftRuntimePath = "runtimes/swift"
  val swiftfolder = "runtimes/swift/actions";
  val swiftkind = "swift:4.2"

  // params for messagehub actions
  val catsArray = Map("cats" -> JsArray(JsObject("name" -> JsString("Kat"), "color" -> JsString("Red"))))
  val finalParam = Map("messages" -> JsArray(JsObject("value" -> JsObject(catsArray))))

  behavior of "MessageHub Template"

  // test to create the nodejs 10 messagehub trigger template from github url.  Will use preinstalled folder.
  it should "create the nodejs 10 messagehub trigger action from github url" in {
    // create unique asset names
    val timestamp: String = System.currentTimeMillis.toString
    val nodejsPackage = packageName + timestamp
    val nodejsTrigger = triggerName + timestamp
    val nodejsRule = ruleName + timestamp
    val nodejsMessagehubAction = nodejsPackage + "/" + messagehubAction

    makePostCallWithExpectedResult(
      JsObject(
        "gitUrl" -> JsString(deployTestRepo),
        "manifestPath" -> JsString(nodejsRuntimePath),
        "envData" -> JsObject(
          "PACKAGE_NAME" -> JsString(nodejsPackage),
          "KAFKA_BROKERS" -> JsString("brokers,list"),
          "MESSAGEHUB_USER" -> JsString("username"),
          "MESSAGEHUB_PASS" -> JsString("password"),
          "KAFKA_ADMIN_URL" -> JsString("admin_url"),
          "KAFKA_TOPIC" -> JsString("topic"),
          "TRIGGER_NAME" -> JsString(nodejsTrigger),
          "RULE_NAME" -> JsString(nodejsRule)),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)),
      successStatus,
      200);

    // check that the actions were created and can be invoked with expected results
    withActivation(wsk.activation, wsk.action.invoke(fakeMessageHubAction, Map("message" -> "echo".toJson))) {
      _.response.result.get.toString should include("echo")
    }

    withActivation(wsk.activation, wsk.action.invoke(nodejsMessagehubAction)) {
      _.response.result.get.toString should include(
        "Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }

    // confirm trigger exists
    val triggers = wsk.trigger.list()
    verifyTriggerList(triggers, nodejsTrigger);
    val triggerRun = wsk.trigger.fire(nodejsTrigger, finalParam)

    // confirm trigger will fire action with expected result
    withActivation(wsk.activation, triggerRun) { activation =>
      val logEntry = activation.logs.get(0).parseJson.asJsObject
      val triggerActivationId: String = logEntry.getFields("activationId")(0).convertTo[String]
      withActivation(wsk.activation, triggerActivationId) { triggerActivation =>
        triggerActivation.response.result.get.toString should include regex """Red.*Kat"""
      }
    }

    // confirm rule exists
    val rules = wsk.rule.list()
    verifyRuleList(rules, nodejsRule)

    val action = wsk.action.get(nodejsMessagehubAction)
    verifyAction(action, nodejsMessagehubAction, JsString(nodejskind))

    // clean up after test
    wsk.action.delete(nodejsMessagehubAction)
    wsk.pkg.delete(nodejsPackage)
    wsk.pkg.delete(binding)
    wsk.trigger.delete(nodejsTrigger)
    wsk.rule.delete(nodejsRule)
  }

  // test to create the php messagehub trigger template from github url.  Will use preinstalled folder.
  it should "create the php messagehub trigger action from github url" in {
    // create unique asset names
    val timestamp: String = System.currentTimeMillis.toString
    val phpPackage = packageName + timestamp
    val phpTrigger = triggerName + timestamp
    val phpRule = ruleName + timestamp
    val phpMessagehubAction = phpPackage + "/" + messagehubAction

    makePostCallWithExpectedResult(
      JsObject(
        "gitUrl" -> JsString(deployTestRepo),
        "manifestPath" -> JsString(phpRuntimePath),
        "envData" -> JsObject(
          "PACKAGE_NAME" -> JsString(phpPackage),
          "KAFKA_BROKERS" -> JsString("brokers,list"),
          "MESSAGEHUB_USER" -> JsString("username"),
          "MESSAGEHUB_PASS" -> JsString("password"),
          "KAFKA_ADMIN_URL" -> JsString("admin_url"),
          "KAFKA_TOPIC" -> JsString("topic"),
          "TRIGGER_NAME" -> JsString(phpTrigger),
          "RULE_NAME" -> JsString(phpRule)),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)),
      successStatus,
      200);

    // check that the actions were created and can be invoked with expected results
    withActivation(wsk.activation, wsk.action.invoke(fakeMessageHubAction, Map("message" -> "echo".toJson))) {
      _.response.result.get.toString should include("echo")
    }

    withActivation(wsk.activation, wsk.action.invoke(phpMessagehubAction)) {
      _.response.result.get.toString should include(
        "Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }

    // confirm trigger exists
    val triggers = wsk.trigger.list()
    verifyTriggerList(triggers, phpTrigger);
    val triggerRun = wsk.trigger.fire(phpTrigger, finalParam)

    // confirm trigger will fire action with expected result
    withActivation(wsk.activation, triggerRun) { activation =>
      val logEntry = activation.logs.get(0).parseJson.asJsObject
      val triggerActivationId: String = logEntry.getFields("activationId")(0).convertTo[String]
      withActivation(wsk.activation, triggerActivationId) { triggerActivation =>
        triggerActivation.response.result.get.toString should include regex """Red.*Kat"""
      }
    }

    // confirm rule exists
    val rules = wsk.rule.list()
    verifyRuleList(rules, phpRule)

    val action = wsk.action.get(phpMessagehubAction)
    verifyAction(action, phpMessagehubAction, JsString(phpkind))

    // clean up after test
    wsk.action.delete(phpMessagehubAction)
    wsk.pkg.delete(phpPackage)
    wsk.pkg.delete(binding)
    wsk.trigger.delete(phpTrigger)
    wsk.rule.delete(phpRule)
  }

  // test to create the python messagehub trigger template from github url.  Will use preinstalled folder.
  it should "create the python messagehub trigger action from github url" in {
    // create unique asset names
    val timestamp: String = System.currentTimeMillis.toString
    val pythonPackage = packageName + timestamp
    val pythonTrigger = triggerName + timestamp
    val pythonRule = ruleName + timestamp
    val pythonMessagehubAction = pythonPackage + "/" + messagehubAction

    makePostCallWithExpectedResult(
      JsObject(
        "gitUrl" -> JsString(deployTestRepo),
        "manifestPath" -> JsString(pythonRuntimePath),
        "envData" -> JsObject(
          "PACKAGE_NAME" -> JsString(pythonPackage),
          "KAFKA_BROKERS" -> JsString("brokers,list"),
          "MESSAGEHUB_USER" -> JsString("username"),
          "MESSAGEHUB_PASS" -> JsString("password"),
          "KAFKA_ADMIN_URL" -> JsString("admin_url"),
          "KAFKA_TOPIC" -> JsString("topic"),
          "TRIGGER_NAME" -> JsString(pythonTrigger),
          "RULE_NAME" -> JsString(pythonRule)),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)),
      successStatus,
      200);

    // check that the actions were created and can be invoked with expected results
    withActivation(wsk.activation, wsk.action.invoke(fakeMessageHubAction, Map("message" -> "echo".toJson))) {
      _.response.result.get.toString should include("echo")
    }

    withActivation(wsk.activation, wsk.action.invoke(pythonMessagehubAction)) {
      _.response.result.get.toString should include(
        "Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }

    // confirm trigger exists
    val triggers = wsk.trigger.list()
    verifyTriggerList(triggers, pythonTrigger);
    val triggerRun = wsk.trigger.fire(pythonTrigger, finalParam)

    // confirm trigger will fire action with expected result
    withActivation(wsk.activation, triggerRun) { activation =>
      val logEntry = activation.logs.get(0).parseJson.asJsObject
      val triggerActivationId: String = logEntry.getFields("activationId")(0).convertTo[String]
      withActivation(wsk.activation, triggerActivationId) { triggerActivation =>
        triggerActivation.response.result.get.toString should include regex """Red.*Kat"""
      }
    }

    // confirm rule exists
    val rules = wsk.rule.list()
    verifyRuleList(rules, pythonRule)

    val action = wsk.action.get(pythonMessagehubAction)
    verifyAction(action, pythonMessagehubAction, JsString(pythonkind))

    // clean up after test
    wsk.action.delete(pythonMessagehubAction)
    wsk.pkg.delete(pythonPackage)
    wsk.pkg.delete(binding)
    wsk.trigger.delete(pythonTrigger)
    wsk.rule.delete(pythonRule)
  }

  // test to create the swift messagehub trigger template from github url.  Will use preinstalled folder.
  it should "create the swift messagehub trigger action from github url" in {
    // create unique asset names
    val timestamp: String = System.currentTimeMillis.toString
    val swiftPackage = packageName + timestamp
    val swiftTrigger = triggerName + timestamp
    val swiftRule = ruleName + timestamp
    val swiftMessagehubAction = swiftPackage + "/" + messagehubAction

    makePostCallWithExpectedResult(
      JsObject(
        "gitUrl" -> JsString(deployTestRepo),
        "manifestPath" -> JsString(swiftRuntimePath),
        "envData" -> JsObject(
          "PACKAGE_NAME" -> JsString(swiftPackage),
          "KAFKA_BROKERS" -> JsString("brokers,list"),
          "MESSAGEHUB_USER" -> JsString("username"),
          "MESSAGEHUB_PASS" -> JsString("password"),
          "KAFKA_ADMIN_URL" -> JsString("admin_url"),
          "KAFKA_TOPIC" -> JsString("topic"),
          "TRIGGER_NAME" -> JsString(swiftTrigger),
          "RULE_NAME" -> JsString(swiftRule)),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)),
      successStatus,
      200);

    // check that the actions were created and can be invoked with expected results
    withActivation(wsk.activation, wsk.action.invoke(fakeMessageHubAction, Map("message" -> "echo".toJson))) {
      _.response.result.get.toString should include("echo")
    }

    withActivation(wsk.activation, wsk.action.invoke(swiftMessagehubAction)) {
      _.response.result.get.toString should include(
        "Invalid arguments. Must include 'messages' JSON array with 'value' field")
    }

    // confirm trigger exists
    val triggers = wsk.trigger.list()
    verifyTriggerList(triggers, swiftTrigger);
    val triggerRun = wsk.trigger.fire(swiftTrigger, finalParam)

    // confirm trigger will fire action with expected result
    withActivation(wsk.activation, triggerRun) { activation =>
      val logEntry = activation.logs.get(0).parseJson.asJsObject
      val triggerActivationId: String = logEntry.getFields("activationId")(0).convertTo[String]
      withActivation(wsk.activation, triggerActivationId) { triggerActivation =>
        triggerActivation.response.result.get.toString should include regex """Red.*Kat"""
      }
    }

    // confirm rule exists
    val rules = wsk.rule.list()
    verifyRuleList(rules, swiftRule)

    val action = wsk.action.get(swiftMessagehubAction)
    verifyAction(action, swiftMessagehubAction, JsString(swiftkind))

    // clean up after test
    wsk.action.delete(swiftMessagehubAction)
    wsk.pkg.delete(swiftPackage)
    wsk.pkg.delete(binding)
    wsk.trigger.delete(swiftTrigger)
    wsk.rule.delete(swiftRule)
  }

  /**
   * Test the nodejs 10 "messageHub trigger" template
   */
  it should "invoke nodejs 10 process-message.js and get the result" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "messageHubNodeJS" + timestamp
      val file = Some(new File(nodejsfolder, "process-message.js").toString());
      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(nodejskind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
        _.response.result.get.toString should include regex """Red.*Kat"""
      }
  }

  it should "invoke nodejs 10 process-message.js without parameters and get an error" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "messageHubNodeJS" + timestamp
      val file = Some(new File(nodejsfolder, "process-message.js").toString());

      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(nodejskind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name)) { activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include(
          "Invalid arguments. Must include 'messages' JSON array with 'value' field")
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
  it should "invoke process-message.py without parameters and get an error" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "messageHubPython" + timestamp
      val file = Some(new File(pythonfolder, "process-message.py").toString());

      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(pythonkind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name)) { activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include(
          "Invalid arguments. Must include 'messages' JSON array with 'value' field")
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
  it should "invoke process-message.php without parameters and get an error" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "messageHubPhp" + timestamp
      val file = Some(new File(phpfolder, "process-message.php").toString());

      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(phpkind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name)) { activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include(
          "Invalid arguments. Must include 'messages' JSON array with 'value' field")
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

  it should "invoke process-message.swift without parameters and get an error" in withAssetCleaner(wskprops) {
    (wp, assetHelper) =>
      val timestamp: String = System.currentTimeMillis.toString
      val name = "messageHubSwift" + timestamp
      val file = Some(new File(swiftfolder, "process-message.swift").toString());

      assetHelper.withCleaner(wsk.action, name) { (action, _) =>
        action.create(name, file, kind = Some(swiftkind))
      }

      withActivation(wsk.activation, wsk.action.invoke(name)) { activation =>
        activation.response.success shouldBe false
        activation.response.result.get.toString should include(
          "Invalid arguments. Must include 'messages' JSON array with 'value' field")
      }
  }

  private def makePostCallWithExpectedResult(params: JsObject, expectedResult: String, expectedCode: Int) = {
    val response = RestAssured
      .given()
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
