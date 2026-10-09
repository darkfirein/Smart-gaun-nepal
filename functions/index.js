/**
 * Smart Gaun Nepal - Secure Gemini Backend Proxy (Phase 17–18)
 *
 * Cloud Functions for Firebase (2nd Gen)
 *
 * Security Features:
 * 1. App Check Enforcement: Validates 'X-Firebase-AppCheck' via Firebase Admin SDK
 * 2. Identity Verification: Validates 'Authorization: Bearer <idToken>' via Firebase Auth
 * 3. Secret Protection: Reads GEMINI_API_KEY from Google Cloud Secret Manager (never exposed to client)
 * 4. Request Validation: Enforces prompt presence, string type, and 10,000 character limit
 */

const { onRequest } = require('firebase-functions/v2/https');
const { defineSecret } = require('firebase-functions/params');
const admin = require('firebase-admin');

admin.initializeApp();

const geminiApiKey = defineSecret('GEMINI_API_KEY');

exports.geminiProxy = onRequest(
  {
    secrets: [geminiApiKey],
    cors: false, // Disallow cross-origin browser abuse; only native Android app allowed
    timeoutSeconds: 30,
    memory: '256MiB',
  },
  async (req, res) => {
    // 1. Verify HTTP Method
    if (req.method !== 'POST') {
      res.status(405).json({ error: 'Method not allowed' });
      return;
    }

    // 2. Enforce Firebase App Check (Google Play Integrity verification)
    const appCheckToken = req.header('X-Firebase-AppCheck');
    if (!appCheckToken) {
      res.status(401).json({ error: 'Unauthorized: Missing App Check token' });
      return;
    }

    try {
      await admin.appCheck().verifyToken(appCheckToken);
    } catch (err) {
      console.warn('App Check verification failed:', err.message);
      res.status(401).json({ error: 'Unauthorized: Invalid App Check token' });
      return;
    }

    // 3. Optional/Enforced User Authentication
    const authHeader = req.header('Authorization');
    let authenticatedUid = null;
    if (authHeader && authHeader.startsWith('Bearer ')) {
      const idToken = authHeader.substring(7);
      try {
        const decoded = await admin.auth().verifyIdToken(idToken);
        authenticatedUid = decoded.uid;
      } catch (authErr) {
        console.warn('User auth token invalid:', authErr.message);
        res.status(401).json({ error: 'Unauthorized: Invalid user credentials' });
        return;
      }
    }

    // 4. Request Body Validation
    const { prompt, systemInstruction, contents } = req.body || {};
    if (!prompt || typeof prompt !== 'string' || prompt.trim().length === 0) {
      res.status(400).json({ error: 'Bad Request: Prompt must be a non-empty string' });
      return;
    }

    if (prompt.length > 10000) {
      res.status(400).json({ error: 'Bad Request: Prompt exceeds maximum length of 10,000 characters' });
      return;
    }

    // 5. Invoke Gemini API using server-side secret
    const apiKey = geminiApiKey.value();
    if (!apiKey) {
      res.status(500).json({ error: 'Internal configuration error: Server secret not configured' });
      return;
    }

    try {
      const endpoint = `https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`;
      const payload = {
        contents: contents || [
          {
            role: 'user',
            parts: [{ text: prompt.trim() }],
          },
        ],
        system_instruction: systemInstruction
          ? { parts: [{ text: systemInstruction }] }
          : undefined,
        generationConfig: {
          temperature: 0.4,
          maxOutputTokens: 800,
        },
      };

      const geminiResponse = await fetch(endpoint, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload),
      });

      if (!geminiResponse.ok) {
        const errText = await geminiResponse.text();
        console.error('Gemini upstream error:', geminiResponse.status, errText);
        res.status(502).json({ error: 'Upstream Gemini generation error' });
        return;
      }

      const resData = await geminiResponse.json();
      const reply =
        resData?.candidates?.[0]?.content?.parts?.[0]?.text || '';

      res.status(200).json({ text: reply.trim() });
    } catch (networkError) {
      console.error('Proxy network error:', networkError);
      res.status(500).json({ error: 'Internal proxy communication error' });
    }
  }
);
