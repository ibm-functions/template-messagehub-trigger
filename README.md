# template-messagehub-trigger

### Overview
You can use this template to deploy some IBM Cloud Functions assets for you.  The assets created by this template are described in the manifest.yaml file, which can be found at `template-messagehub-trigger/runtimes/your_language_choice/manifest.yaml`

The assets described by this template are a trigger to fire events when a message is received on the messagehub topic, an action to process the message and print it out, and a rule to tie the trigger and action together.

When this template is deployed and associated with a messagehub instance, it will print out any new messages on the topic.  It is expecting a stream of messages that contain a cat object with name and color fields.

You can use the wskdeploy tool to deploy this asset yourself using the manifest and available code.

### Available Languages
This template is available in node.js, php, python & swift.
