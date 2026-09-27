from catboost import CatBoostClassifier

model = CatBoostClassifier()
model.load_model("riasec_catboost_final.cbm")
model.save_model(
    "riasec_catboost_final.onnx",
    format="onnx",
    export_parameters={
        "onnx_domain": "ai.catboost",
        "onnx_model_version": 1,
        "onnx_doc_string": "User-trained final RIASEC CatBoost model",
    },
)
print("Exported exact CBM model to ONNX")
