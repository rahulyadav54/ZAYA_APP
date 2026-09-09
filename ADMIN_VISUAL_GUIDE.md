# Admin Panel Login - Visual Guide

## 📊 System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    AIZAYA App (Android)                     │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           Admin Login Screen                         │   │
│  │                                                      │   │
│  │  Default: admin@zaya.com / password123              │   │
│  │                                                      │   │
│  │  [Email Input Box]                                  │   │
│  │  [Password Input Box]                               │   │
│  │  [Login as Admin Button]                            │   │
│  │                                                      │   │
│  └─────────┬────────────────────────────────────────────┘   │
│            │                                                  │
│            │ Credentials sent                                │
│            ▼                                                  │
│  ┌──────────────────────────────────────────────────────┐   │
│  │     Firebase Authentication Check                   │   │
│  │     verify email & password                         │   │
│  └─────────┬────────────────────────────────────────────┘   │
│            │                                                  │
│            ├─ If email NOT in ADMIN_EMAILS:                  │
│            │  └─> ❌ "Access Denied: Not an admin"           │
│            │                                                  │
│            └─ If email IS in ADMIN_EMAILS:                   │
│               └─> ✅ Proceed to Admin Dashboard              │
│                                                               │
│  ┌──────────────────────────────────────────────────────┐   │
│  │           Admin Dashboard                           │   │
│  │  ┌────────────┬────────────┬────────────┐           │   │
│  │  │  Quizzes   │  Formulas  │   Notes    │           │   │
│  │  └────────────┴────────────┴────────────┘           │   │
│  │  ┌────────────┬────────────┐                         │   │
│  │  │Announce    │  Articles  │                         │   │
│  │  └────────────┴────────────┘                         │   │
│  │                                                      │   │
│  └──────────────────────────────────────────────────────┘   │
│                                                               │
└─────────────────────────────────────────────────────────────┘
         │
         │ Firebase API Call
         ▼
┌─────────────────────────────────────────────────────────────┐
│               Firebase Console                              │
├─────────────────────────────────────────────────────────────┤
│                                                               │
│  Authentication (Users)                                     │
│  ┌───────────────────────────────────────────────────┐      │
│  │ Email: admin@zaya.com                             │      │
│  │ Status: ✅ Active                                  │      │
│  │ UID: xyz123abc...                                 │      │
│  └───────────────────────────────────────────────────┘      │
│                                                               │
│  Firestore (Database)                                       │
│  ┌───────────────────────────────────────────────────┐      │
│  │ Collection: users                                 │      │
│  │   └─ Document: xyz123abc... (UID)                │      │
│  │      └─ Field: role = "admin"                    │      │
│  └───────────────────────────────────────────────────┘      │
│                                                               │
└─────────────────────────────────────────────────────────────┘
```

## 🔐 Admin Authorization Flow

```
User Opens App
    │
    ▼
Goes to Admin Panel
    │
    ▼
Enters Email & Password
    │
    ▼
AdminLoginScreen.kt
    │
    ├─ Read email input: "admin@zaya.com"
    ├─ Read password input: "password123"
    │
    ▼
Firebase.auth.signInWithEmailAndPassword(email, password)
    │
    ├─ Valid credentials? ────┐
    │                         │
    │                      ┌──┴──┐
    │                      │ YES │ ┌─ Invalid
    │                      └──┬──┘ │
    │                         │    │
    │                         ▼    ▼
    │            checkAdminRole()  ❌ Error Message
    │                    │
    │                    ▼
    │       Check: ADMIN_EMAILS.contains(email)?
    │                    │
    │          ┌─────────┴──────────┐
    │          │                    │
    │      ✅ YES                  ❌ NO
    │          │                    │
    │          ▼                    ▼
    │   Check Firestore        ❌ Access Denied
    │   role = "admin"
    │          │
    │    ┌─────┴──────┐
    │    │            │
    │   ✅ YES     ❌ NO
    │    │            │
    │    ▼            ▼
    │  ✅ Allow    ✅ Allow*
    │    │            │
    │    └─────┬──────┘
    │          │
    │          ▼
    │  Open Admin Dashboard
    │
    └─> Store session token
```

*Allows admin if email matches, even without Firestore role field

## 📝 Admin Emails Configuration

```
File: AdminViewModel.kt
Location: app/src/main/java/com/example/aizaya/AdminViewModel.kt

BEFORE:
private val ADMIN_EMAIL = "zayacodehub@gmail.com"
    └─ Only one admin!

AFTER:
private val ADMIN_EMAILS = listOf(
    "zayacodehub@gmail.com",
    "admin@zaya.com"
)
    └─ Multiple admins supported!

TO ADD MORE:
private val ADMIN_EMAILS = listOf(
    "zayacodehub@gmail.com",
    "admin@zaya.com",
    "principal@school.edu",        ← Add here
    "coordinator@school.edu",       ← Add here
    "teacher1@school.edu"           ← Add here
)
```

## 🚀 Setup Timeline

```
Day 1: Setup Firebase
├─ Go to Firebase Console
├─ Create user: admin@zaya.com / password123
└─ (Optional) Set role in Firestore

Day 1: Build and Test App
├─ Build app with updated code
├─ Deploy to device/emulator
├─ Open Admin Panel
├─ Login with admin credentials
└─ ✅ Success!

Day 2+: Add More Admins
├─ Edit AdminViewModel.kt
├─ Add new emails to list
├─ Create users in Firebase
└─ Users can now login as admin
```

## 🔑 Credentials Quick Lookup

### Default Admin
```
Email:    admin@zaya.com
Password: password123
Status:   Ready to use
```

### Additional Admins (if added)
```
Email:    (your email here)
Password: (set in Firebase)
Status:   (will be active after adding to list)
```

## ⚡ Quick Checklist

- [ ] Read AdminViewModel.kt changes
- [ ] Read AdminLoginScreen.kt changes
- [ ] Create user in Firebase Console
  - [ ] Email: admin@zaya.com
  - [ ] Password: password123
- [ ] (Optional) Set role in Firestore
- [ ] Build app
- [ ] Test admin login
- [ ] Change default password if production
- [ ] Add more admins if needed
- [ ] Test each admin account

## 📞 Need Help?

| Issue | Check |
|-------|-------|
| Can't login | Is user created in Firebase? |
| "Access Denied" | Is email in ADMIN_EMAILS list? |
| Forgot password | Reset in Firebase Console |
| Add new admin | Edit ADMIN_EMAILS & create user |
| Change password | Firebase Console → Users → Change |

---

**Remember:** 
- 🔐 Change default password in production
- 👤 Only add trusted admins
- 📋 Keep ADMIN_EMAILS updated
- 🔑 Secure all admin credentials

Happy administrating! 🎉

