# RIASEC Explorer
An AI-powered interest discovery application developed for the **GOMYCODE Hackathon**.

The application helps users better understand their vocational interests through a RIASEC assessment and uses machine learning to recommend compatible career and study domains.

---

## About the Project

Choosing a study field or career path can be difficult, especially when people are unsure about their interests.

This project uses the **RIASEC vocational interest model** to build an individual interest profile and recommend career domains that may align with the user's preferences.

The application includes:

- User registration and login
- Personal profile
- 48-question RIASEC assessment
- Automatic calculation of RIASEC scores
- Holland Code generation
- Machine-learning-based recommendations
- Top 3 compatible career domains
- Saved assessment results

---

## What is RIASEC?

The **RIASEC model**, also known as **Holland's Occupational Themes**, is a theory of vocational interests developed by **John L. Holland**.

It categorizes vocational interests into six dimensions:

| Code | Type | Description |
|------|------|-------------|
| **R** | Realistic | Practical, technical, and hands-on activities |
| **I** | Investigative | Analytical, scientific, and problem-solving activities |
| **A** | Artistic | Creative and expressive activities |
| **S** | Social | Helping, teaching, and interacting with others |
| **E** | Enterprising | Leadership, persuasion, and entrepreneurial activities |
| **C** | Conventional | Organized, structured, and detail-oriented activities |

RIASEC is widely used in career guidance to help individuals understand their interests and identify educational and occupational environments that may align with them.

In our system, the RIASEC assessment generates an individual interest profile that is used to recommend relevant career and study domains.

---

## RIASEC Assessment

The assessment contains **48 questions**, with 8 questions for each RIASEC dimension:

- R1–R8
- I1–I8
- A1–A8
- S1–S8
- E1–E8
- C1–C8

Each activity is rated from **1 to 5**.

The application then calculates six RIASEC scores:

```text
R_score
I_score
A_score
S_score
E_score
C_score
```

The three strongest dimensions are used to generate the user's **Holland Code**.

For example:

```text
I-A-S
```

---

## Machine Learning

The application uses a **CatBoost multiclass classification model** to recommend compatible career and study domains.

The model uses:

```text
48 individual RIASEC responses
+
6 calculated RIASEC scores
=
54 input features
```

Instead of predicting only one domain, the system returns the **Top 3 most compatible domains**.

The seven domains are:

- Arts & Design
- Business & Management
- Education
- Health
- Humanities & Communication
- STEM
- Social & Public Sciences

---

## Model Performance

The final model achieved the following results on the test set:

| Metric | Score |
|--------|------:|
| Accuracy | 44.65% |
| Balanced Accuracy | 38.62% |
| Macro F1 | 38.71% |
| Top-2 Accuracy | 67.31% |
| Top-3 Accuracy | **80.71%** |

Because RIASEC measures vocational interests rather than determining one single correct career, the application focuses on **Top 3 recommendations** to support exploration and decision-making.

---

## Dataset

The dataset used for the RIASEC model comes from **OpenPsychometrics**.

The RIASEC Markers from the Interest Item Pool, developed by **Liao, Armstrong, and Rounds (2008)**, provide a public-domain alternative to commercially available interest assessments and have been used in psychological research.

**Dataset source:**  
https://openpsychometrics.org/_rawdata/

---

## Figma Prototype

The application interface was first designed using Figma.

**Prototype:**  
https://www.figma.com/proto/TPjGrSNY4iDw7rmIpM9phi/Sans-titre?node-id=2-10&t=6s4CjUCGIWJ61nVK-0&scaling=min-zoom&content-scaling=fixed&page-id=0%3A1

---

## Technologies Used

- Android
- Java
- XML
- Python
- Pandas
- Scikit-learn
- CatBoost
- ONNX
- ONNX Runtime
- Google Colab
- Figma
- Git
- GitHub

---

## Application Flow

```text
Registration / Login
        |
        v
       Home
        |
        v
RIASEC Assessment
   48 Questions
        |
        v
RIASEC Interest Profile
        |
        v
Holland Code + 6 Scores
        |
        v
Machine Learning Model
        |
        v
Top 3 Compatible Domains
        |
        v
      Results
```

---

## Running the Project

1. Clone the repository:

```bash
git clone YOUR_REPOSITORY_URL
```

2. Open the project in **Android Studio**.
3. Wait for Gradle synchronization to finish.
4. Connect an Android device or launch an emulator.
5. Run the application.

---

## Disclaimer

The recommendations provided by this application are intended to support **self-discovery and career exploration**.

RIASEC interests do not determine one perfect career or study field. Other factors such as personality, abilities, values, education, and personal circumstances may also influence career decisions.

---

## GOMYCODE Hackathon

This project was developed as part of the **GOMYCODE Hackathon**.

The goal of the project is to combine vocational psychology, machine learning, and mobile development to create an accessible interest discovery tool.
