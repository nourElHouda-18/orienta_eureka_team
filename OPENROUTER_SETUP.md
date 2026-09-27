# OpenRouter Free AI Integration

This project version uses OpenRouter instead of the paid OpenAI API integration.

Add to `local.properties`:

```properties
OPENROUTER_API_KEY=sk-or-v1-your-real-key-here
OPENROUTER_MODEL=openrouter/free
```

Then sync Gradle and run the app.

The Android client calls:

`https://openrouter.ai/api/v1/chat/completions`

The default `openrouter/free` router selects an available free model. Free-model availability and rate limits are controlled by OpenRouter and may change.
