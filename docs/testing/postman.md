# Postman Scenario Tests

Local scenario tests live in:

- `postman/p3-api.local.postman_collection.json`
- `postman/p3-api.local.postman_environment.json`

The collection covers these API flows:

- local reset before and after the run
- seller/buyer registration with the provided test account variables
- current user profile query and update flows
- seller onboarding creation and local-only approval
- store, asset upload, representative image, gallery, and asset variant flows
- order form option group success and main failure cases
- draft creation, start reference asset validation, draft consume, inquiry list/detail/timeline/read flows
- order confirmation preview, send, viewed, payment CTA, revision request, replacement flows
- seller dashboard and revenue response contract flows

Run the API with the local scenario profile:

```bash
SPRING_PROFILES_ACTIVE=local-scenario ./gradlew bootRun
```

Then run the collection from the repository root so Postman CLI can resolve the upload fixture path:

```bash
postman collection run postman/p3-api.local.postman_collection.json \
  -e postman/p3-api.local.postman_environment.json \
  --working-dir .
```

`local-scenario` deliberately replaces Cognito JWT verification, S3 asset storage, Redis draft
storage, and Redis chat publish/subscribe with local implementations. Test account identifiers are
kept in the Postman environment and profile configuration, not hardcoded in production flow code.

The collection intentionally excludes Point3 payment preparation and approval calls. Payment-related
automated tests must not call real Point3 APIs.
