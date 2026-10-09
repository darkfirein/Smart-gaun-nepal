# Smart Gaun Nepal — GitHub & Firebase Mobile Setup Guide

This guide is designed for beginners using an **Android phone and Google Chrome browser**. No PC is required.

---

## 📋 Overview of Deployment Flow

```
[AI Studio Build / Android App]
          ↓ (Export to GitHub)
     [GitHub Repo]
          ↓ (GitHub Actions Workflow)
[Firebase Cloud Functions (2nd Gen)]
          ↓ (Secret Manager: GEMINI_API_KEY)
   [Google Gemini API]
```

---

## Step 1: Export or Push the Project to GitHub from Chrome

1. In **AI Studio Build** on your phone, tap the **Settings / Export** menu in the top right.
2. Select **Push to GitHub** (or **Export to ZIP** if uploading manually to a new GitHub repository).
3. If connecting for the first time, authorize GitHub in your browser.
4. Choose your GitHub repository (e.g., `username/smart-gaun-nepal`) and push the `main` branch.
5. All prepared files will now be in your repository:
   - `functions/index.js` (Secure Gemini Backend Proxy)
   - `functions/package.json`
   - `firebase.json`
   - `.firebaserc`
   - `firestore.rules`
   - `.github/workflows/deploy-firebase-functions.yml`

---

## Step 2: Set Your Firebase Project ID in `.firebaserc`

1. Open Chrome and go to **[Firebase Console](https://console.firebase.google.com/)**.
2. Tap on your project (or tap **Add project** to create one named `smart-gaun-nepal`).
3. Tap the **Gear Icon (⚙️)** next to *Project Overview* → **Project settings**.
4. Note your **Project ID** (e.g., `smart-gaun-nepal-12345`).
5. In Chrome, open your GitHub repository:
   - Navigate to `.firebaserc`.
   - Tap the **Pencil Icon (✏️)** to edit.
   - Replace `"YOUR_FIREBASE_PROJECT_ID"` with your actual Project ID.
   - Scroll down and tap **Commit changes**.

---

## Step 3: Securely Store the Gemini API Key in Secret Manager

The client application **never** holds the Gemini API key. It lives exclusively in Google Cloud Secret Manager for your Cloud Function.

1. Open Chrome and go to the **[Google Cloud Console Secret Manager](https://console.cloud.google.com/security/secret-manager)**.
2. Ensure your Firebase project is selected in the top dropdown.
3. Tap **+ CREATE SECRET** (or **New Secret**).
4. Enter the details:
   - **Name**: `GEMINI_API_KEY` *(exact name required)*
   - **Secret value**: Paste your Gemini API key from AI Studio
5. Tap **Create secret**.
6. **Grant Access to Cloud Functions**:
   - Tap on `GEMINI_API_KEY` → tap **Permissions** tab → tap **Grant Access**.
   - **New principals**: Select your project's App Engine or Compute Engine default service account:
     `YOUR_PROJECT_ID@appspot.gserviceaccount.com` (or the Cloud Functions 2nd Gen runtime service account).
   - **Role**: `Secret Manager Secret Accessor`.
   - Tap **Save**.

---

## Step 4: Configure GitHub Secrets for Deployment

To allow GitHub Actions to deploy to Firebase on your behalf without exposing passwords:

### Option A: Firebase Token (Simplest for Beginners)
1. In Firebase Console or Cloud Shell (available in Chrome at [shell.cloud.google.com](https://shell.cloud.google.com)):
   - Run: `curl -sL https://firebase.tools | bash` (or if using Google Cloud Shell, Firebase is preinstalled).
   - Run: `firebase login:ci`
   - Follow the browser link to authorize and copy the generated token string.
2. In Chrome, open your **GitHub repository**:
   - Tap **Settings** tab.
   - In the sidebar, tap **Secrets and variables** → **Actions**.
   - Tap **New repository secret**.
   - **Name**: `FIREBASE_TOKEN`
   - **Value**: Paste the token generated above.
   - Tap **Add secret**.

### Option B: Workload Identity Federation (Recommended Enterprise Method)
1. Set up a Workload Identity Pool in Google Cloud Console IAM.
2. Add secrets in GitHub:
   - `GCP_WORKLOAD_IDENTITY_PROVIDER`: `projects/PROJECT_NUMBER/locations/global/workloadIdentityPools/POOL/providers/PROVIDER`
   - `GCP_SERVICE_ACCOUNT`: `github-actions@YOUR_PROJECT_ID.iam.gserviceaccount.com`

---

## Step 5: Run the Deployment via GitHub Actions in Chrome

1. On your phone in Chrome, open your GitHub repository.
2. Tap the **Actions** tab at the top.
3. In the left workflow list, tap **Deploy Firebase Functions & Rules**.
4. You will see a banner: *"This workflow has a workflow_dispatch event trigger."*
5. Tap the **Run workflow** dropdown button:
   - **Branch**: `main`
   - **Type DEPLOY to confirm deployment**: Leave as `DEPLOY`
6. Tap the green **Run workflow** button.
7. Tap the running job to watch real-time deployment logs.
8. Once completed (green checkmark ✅), both your `geminiProxy` function and `firestore.rules` are live!

---

## Step 6: Verify the Deployed Cloud Function

1. Open Chrome and go to **[Firebase Console](https://console.firebase.google.com/)**.
2. In the left menu, tap **Build** → **Functions**.
3. You will see:
   - **Function name**: `geminiProxy`
   - **Region**: e.g., `us-central1`
   - **Trigger**: HTTPS
   - **Runtime**: Node.js 20
4. Note the function's HTTPS URL (e.g. `https://geminiproxy-xxxx-uc.a.run.app` or `https://us-central1-YOUR_PROJECT.cloudfunctions.net/geminiProxy`).
5. Tap **Logs** to inspect real-time execution logs and error diagnostics if needed.

---

## Step 7: Configure Firebase App Check with Play Integrity

To protect your backend so only your verified Android app can call it:

1. In Firebase Console, tap **Build** → **App Check**.
2. In the **Apps** tab, tap your registered Android app (`com.aistudio.smartgaun.nepal`).
3. Select **Play Integrity**.
4. Enter your SHA-256 certificate fingerprint:
   - If publishing to Google Play: Go to **Google Play Console** → Your App → **Release** → **Setup** → **App signing** → Copy the **App signing key certificate SHA-256 fingerprint**.
   - Paste the SHA-256 fingerprint into Firebase App Check.
5. In App Check, tap the **APIs** tab:
   - Enable App Check enforcement for **Cloud Firestore** and **Cloud Functions**.

---

## ⚠️ Actions That Cannot Be Done by Editing Repository Files

The following steps require manual actions in your browser (Chrome) and cannot be automated solely inside Git repository code:
1. **Enabling Firebase Services**: Creating the Firebase Project in Firebase Console.
2. **Secret Manager Secret Creation**: Creating `GEMINI_API_KEY` in Google Cloud Secret Manager and granting Secret Accessor permission.
3. **Google Play Integrity Registration**: Registering your Google Play Console SHA-256 fingerprint inside Firebase App Check.
4. **Triggering First Deployment**: Tapping **Run workflow** under the GitHub Actions tab.
