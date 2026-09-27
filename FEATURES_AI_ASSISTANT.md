# AI Assistant Feature

This version adds a Support Assistant to the RIASEC results flow.

## Results screen

- Displays all six RIASEC scores
- Displays the Holland Code
- Displays the Top-3 broad domains returned by the trained CatBoost/ONNX model
- Adds an **Ask Way...** button that opens the Support Assistant

## Support Assistant

Suggested questions:

- What is the RIASEC test?
- Why did you choose these domains for me?
- What can I do with these domains?

The assistant has two modes:

1. **Offline guide** — always available and gives deterministic explanations based on the saved result.
2. **LLM mode** — enabled when `OPENROUTER_API_KEY` is present in `local.properties`. The current model name is configurable with `OPENROUTER_MODEL`.

If the LLM service is unavailable, the app automatically falls back to the offline guide instead of failing.
