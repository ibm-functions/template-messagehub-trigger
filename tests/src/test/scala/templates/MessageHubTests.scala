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

@RunWith(classOf[JUnitRunner])
class MessageHubTests extends TestHelpers
    with WskTestHelpers
    with BeforeAndAfterAll {

    implicit val wskprops = WskProps()
    val wsk = new Wsk()

    // statuses for deployWeb
    val successStatus = """"status":"success""""

    val deployTestRepo = "https://github.com/ibm-functions/template-messagehub-trigger"
    val messagehubActionPackage = "myPackage/process-message"
    val fakeMessageHubAction = "openwhisk-messagehub/messageHubFeed"
    val deployAction = "/whisk.system/deployWeb/wskdeploy"
    val deployActionURL = s"https://${wskprops.apihost}/api/v1/web${deployAction}.http"

    //set parameters for deploy tests
    val node8RuntimePath = "runtimes/nodejs"
    val nodejs8folder = "../runtimes/nodejs/actions";
    val nodejs8kind = JsString("nodejs:8")
    val node6RuntimePath = "runtimes/nodejs-6"
    val nodejs6folder = "../runtimes/nodejs-6/actions";
    val nodejs6kind = JsString("nodejs:6")
    val phpRuntimePath = "runtimes/php"
    val phpfolder = "../runtimes/php/actions";
    val phpkind = JsString("php:7.1")
    val pythonRuntimePath = "runtimes/python"
    val pythonfolder = "../runtimes/python/actions";
    val pythonkind = JsString("python:2")
    val swiftRuntimePath = "runtimes/swift"
    val swiftfolder = "../runtimes/swift/actions";
    val swiftkind = JsString("swift:3.1.1")

    def makePostCallWithExpectedResult(params: JsObject, expectedResult: String, expectedCode: Int) = {
      val response = RestAssured.given()
          .contentType("application/json\r\n")
          .config(RestAssured.config().sslConfig(new SSLConfig().relaxedHTTPSValidation()))
          .body(params.toString())
          .post(deployActionURL)
      assert(response.statusCode() == expectedCode)
      response.body.asString should include(expectedResult)
      response.body.asString.parseJson.asJsObject.getFields("activationId") should have length 1
    }

    def verifyAction(action: RunResult, name: String, kindValue: JsString): Unit = {
      val stdout = action.stdout
      assert(stdout.startsWith(s"ok: got action $name\n"))
      wsk.parseJsonString(stdout).fields("exec").asJsObject.fields("kind") shouldBe kindValue
    }

    behavior of "MessageHub Template"

    // test to create the nodejs 8 messagehub trigger template from github url.  Will use preinstalled folder.
    it should "create the nodejs 8 messagehub trigger action from github url" in {
      makePostCallWithExpectedResult(JsObject(
        "gitUrl" -> JsString(deployTestRepo),
        "manifestPath" -> JsString(node8RuntimePath),
        "envData" -> JsObject(
            "PACKAGE_NAME" -> JsString("myPackage"),
            "KAFKA_BROKERS" -> JsString("brokers,list"),
            "MESSAGEHUB_USER" -> JsString("username"),
            "MESSAGEHUB_PASS" -> JsString("password"),
            "KAFKA_ADMIN_URL" -> JsString("admin_url"),
            "KAFKA_TOPIC" -> JsString("topic"),
            "TRIGGER_NAME" -> JsString("myTrigger"),
            "RULE_NAME" -> JsString("myRule")
        ),
        "wskApiHost" -> JsString(wskprops.apihost),
        "wskAuth" -> JsString(wskprops.authKey)
      ), successStatus, 200);

      withActivation(wsk.activation, wsk.action.invoke(messagehubActionPackage)) {
        _.response.result.get.toString should include("Please make sure name and color are passed in as params.")
      }

      withActivation(wsk.activation, wsk.action.invoke(fakeMessageHubAction, Map("message" -> "echo".toJson))) {
        _.response.result.get.toString should include("echo")
      }

      val action = wsk.action.get("myPackage/process-change")
      verifyAction(action, messagehubActionPackage, nodejs8kind)

      // clean up after test
      wsk.action.delete(fakeMessageHubAction)
    }

    val catsArray = Map("cats" -> JsArray(JsObject(
      "name" -> JsString("Kat"),
      "color" -> JsString("Red"))))
    val finalParam = Map("messages"->JsArray(JsObject("value" -> JsObject(catsArray))))

  /**
    * Test the nodejs 6 "messageHub trigger" template
    */
  it should "invoke nodejs 6 process-message.js and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubNode"
    val file = Some(new File(nodejs6folder, "process-message.js").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file)
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }

  it should "invoke nodejs 6 process-message.js without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubNode"
    val file = Some(new File(nodejs6folder, "process-message.js").toString());

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
    * Test the nodejs 8 "messageHub trigger" template
    */
  it should "invoke nodejs 8 process-message.js and get the result" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubNode"
    val kind = Option("nodejs:8")
    val file = Some(new File(nodejs8folder, "process-message.js").toString());
    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind)
    }

    withActivation(wsk.activation, wsk.action.invoke(name, finalParam)) {
      _.response.result.get.toString should include regex """Red.*Kat"""
    }
  }

  it should "invoke nodejs 8 process-message.js without parameters and get an error" in withAssetCleaner(wskprops) { (wp, assetHelper) =>

    val name = "messageHubNode"
    val file = Some(new File(nodejs8folder, "process-message.js").toString());
    val kind = Option("nodejs:8")

    assetHelper.withCleaner(wsk.action, name) { (action, _) =>
      action.create(name, file, kind)
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
    * Test the php "messageHub trigger" template
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
    * Test the swift "messageHub trigger" template
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
