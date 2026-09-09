# 🔒 Firebase Production Security Rules for AIZAYA

## ⚠️ WHY THIS IS NEEDED
Firebase test mode rules expire after **30 days**. After that, ALL reads/writes will be **DENIED** and your app will stop working. Follow the steps below to switch to production mode.

---

## 📌 STEP 1: Firestore Security Rules

1. Go to **[Firebase Console](https://console.firebase.google.com/)**
2. Open project **techeagle-5f574**
3. Go to **Firestore Database** → **Rules** tab
4. **Delete everything** and paste the following rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {

    // ═══════════════════════════════════════
    // HELPER FUNCTIONS
    // ═══════════════════════════════════════
    
    // Check if user is logged in
    function isAuthenticated() {
      return request.auth != null;
    }
    
    // Check if user is accessing their own data
    function isOwner(userId) {
      return isAuthenticated() && request.auth.uid == userId;
    }
    
    // Check if user is an admin
    function isAdmin() {
      return isAuthenticated() && 
        get(/databases/$(database)/documents/users/$(request.auth.uid)).data.role == "admin";
    }

    // ═══════════════════════════════════════
    // USERS COLLECTION
    // ═══════════════════════════════════════
    match /users/{userId} {
      // Users can read their own profile
      // Admin can read any user profile
      allow read: if isOwner(userId) || isAdmin();
      
      // Users can create their own profile on signup
      allow create: if isAuthenticated() && request.auth.uid == userId;
      
      // Users can update their own profile, admin can update any
      allow update: if isOwner(userId) || isAdmin();
      
      // Only admin can delete users
      allow delete: if isAdmin();
      
      // ─── User's Conversations (subcollection) ───
      match /conversations/{conversationId} {
        allow read, write: if isOwner(userId);
        
        // ─── Messages inside conversations ───
        match /messages/{messageId} {
          allow read, write: if isOwner(userId);
        }
      }
    }

    // ═══════════════════════════════════════
    // QUIZZES COLLECTION (Admin manages, all authenticated users can read)
    // ═══════════════════════════════════════
    match /quizzes/{quizId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // FORMULAS COLLECTION
    // ═══════════════════════════════════════
    match /formulas/{formulaId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // ANNOUNCEMENTS COLLECTION
    // ═══════════════════════════════════════
    match /announcements/{announcementId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // ARTICLES COLLECTION
    // ═══════════════════════════════════════
    match /articles/{articleId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // STUDY MATERIALS COLLECTION
    // ═══════════════════════════════════════
    match /study_materials/{materialId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // GENERATED QUESTIONS COLLECTION
    // ═══════════════════════════════════════
    match /generated_questions/{questionId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // AI QUIZ QUESTIONS COLLECTION
    // ═══════════════════════════════════════
    match /ai_quiz_questions/{questionId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // AI FLASHCARDS COLLECTION
    // ═══════════════════════════════════════
    match /ai_flashcards/{flashcardId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // STUDY SUMMARIES COLLECTION
    // ═══════════════════════════════════════
    match /study_summaries/{summaryId} {
      allow read: if isAuthenticated();
      allow create, update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // USER PERFORMANCE COLLECTION
    // ═══════════════════════════════════════
    match /user_performance/{performanceId} {
      allow read: if isAuthenticated();
      allow create: if isAuthenticated();
      allow update, delete: if isAdmin();
    }

    // ═══════════════════════════════════════
    // DENY EVERYTHING ELSE
    // ═══════════════════════════════════════
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

5. Click **Publish**

---

## 📌 STEP 2: Realtime Database Security Rules

1. In Firebase Console, go to **Realtime Database** → **Rules** tab
2. **Delete everything** and paste:

```json
{
  "rules": {
    "users": {
      "$uid": {
        ".read": "$uid === auth.uid",
        ".write": "$uid === auth.uid"
      }
    },
    ".read": false,
    ".write": false
  }
}
```

3. Click **Publish**

---

## 📌 STEP 3: Firebase Storage Security Rules (if used later)

1. In Firebase Console, go to **Storage** → **Rules** tab
2. **Delete everything** and paste:

```
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    // Only authenticated users can read
    match /{allPaths=**} {
      allow read: if request.auth != null;
      allow write: if request.auth != null 
                   && request.resource.size < 10 * 1024 * 1024;
    }
  }
}
```

3. Click **Publish**

---

## ✅ SUMMARY OF WHAT THESE RULES DO

| Collection | Who Can READ | Who Can WRITE |
|---|---|---|
| `users/{uid}` | Owner + Admin | Owner (create/update) + Admin |
| `users/{uid}/conversations/**` | Owner only | Owner only |
| `quizzes` | All authenticated | Admin only |
| `formulas` | All authenticated | Admin only |
| `announcements` | All authenticated | Admin only |
| `articles` | All authenticated | Admin only |
| `study_materials` | All authenticated | Admin only |
| `generated_questions` | All authenticated | Admin only |
| `ai_quiz_questions` | All authenticated | Admin only |
| `ai_flashcards` | All authenticated | Admin only |
| `study_summaries` | All authenticated | Admin only |
| `user_performance` | All authenticated | Create: any user, Edit/Delete: Admin |
| Everything else | ❌ DENIED | ❌ DENIED |

---

## ⚠️ IMPORTANT NOTES

1. **You MUST have at least one admin user** in Firestore with `role: "admin"` for admin operations to work.
2. **Firestore-only users** (non-Firebase Auth users who login via email/password stored in Firestore) — these users **won't have `request.auth`**, so they will be denied by these rules. If you need them to work, you should migrate them to Firebase Auth.
3. **Test your app** after publishing rules to make sure everything works correctly.

