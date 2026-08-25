# Postman Scenario Tests

Local scenario tests live in:

- `postman/p3-api.local.postman_collection.json`
- `postman/p3-api.local.postman_environment.json`

The collection covers these API flows:

- local reset before and after the run
- seller/buyer registration with the provided test account variables
- seller onboarding creation and local-only approval
- store, asset upload, representative image, gallery, and asset variant flows
- order form success and main failure cases
- draft creation, draft consume, inquiry list/detail/timeline/read flows
- order confirmation send, viewed, revision request, replacement flows

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
