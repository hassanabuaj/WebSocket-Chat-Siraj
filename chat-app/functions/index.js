// Firebase Functions v2 API (works with v5 package)
const { onRequest } = require("firebase-functions/v2/https");
const { setGlobalOptions } = require("firebase-functions/v2");

// set default region (same as functions.region("us-central1") in v1)
setGlobalOptions({ region: "us-central1" });

exports.notifyMessage = onRequest((req, res) => {
    console.log("notifyMessage payload:", req.body);
    res.status(200).send({ ok: true });
});
