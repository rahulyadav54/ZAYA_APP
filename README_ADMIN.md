# 🔐 AIZAYA Admin Panel - Setup & Login Guide

## 👋 Welcome!

This document will help you set up and use the admin panel in the AIZAYA app with Gmail authentication.

---

## ⚡ Quick Start (5 Minutes)

### Step 1: Create Admin Account in Firebase
```
1. Go to: https://console.firebase.google.com/
2. Select: AIZAYA project
3. Click: Authentication → Users
4. Click: Create user button
5. Enter:
   Email: admin@zaya.com
   Password: password123
6. Click: Create user
```

### Step 2: Build and Run App
```
1. Open Android Studio
2. Build the project
3. Deploy to device/emulator
```

### Step 3: Login to Admin Panel
```
1. Open the AIZAYA app
2. Go to: Admin Panel
3. Enter:
   Email: admin@zaya.com
   Password: password123
4. Click: Login as Admin
5. ✅ You're now in the Admin Dashboard!
```

---

## 📚 Full Documentation

Choose what you need:

| Document | What It Contains | Time |
|----------|-----------------|------|
| **CODE_CHANGES.md** | Exact code modifications made | 5 min |
| **ADMIN_LOGIN_QUICK_REF.md** | Quick reference with credentials | 2 min |
| **ADMIN_LOGIN_SETUP.md** | Detailed setup guide | 15 min |
| **FIREBASE_ADMIN_SETUP.md** | Step-by-step Firebase console | 20 min |
| **ADMIN_VISUAL_GUIDE.md** | Diagrams and flowcharts | 10 min |
| **ADMIN_SETUP_SUMMARY.md** | Complete summary | 10 min |

---

## 🔑 Default Credentials

```
📧 Email:    admin@zaya.com
🔐 Password: password123
```

**⚠️ Important:** Change this password immediately after first login for security!

---

## 🎯 What You Can Do in Admin Panel

After logging in successfully:

✅ **Manage Quizzes**
- Create new quizzes
- Add questions and answers
- Edit existing quizzes
- Delete quizzes

✅ **Manage Formulas**
- Add mathematical/scientific formulas
- Organize by subject
- Edit formula details
- Delete formulas

✅ **Post Announcements**
- Share important updates
- Visible to all students
- Easy management

✅ **Manage Articles**
- Upload study materials
- Categorize articles
- Edit/delete articles
- Help students learn

---

## 🔧 Code Changes Made

### 1. **AdminViewModel.kt**
- ✅ Changed single admin email to list of emails
- ✅ Now supports multiple admins
- ✅ Default admin: `admin@zaya.com`

### 2. **AdminLoginScreen.kt**
- ✅ Added helpful hint showing default credentials
- ✅ Users can see credentials right on login screen

**For details:** See `CODE_CHANGES.md`

---

## 👥 Adding More Admins

### Method 1: Edit Code (Recommended)
```kotlin
// File: AdminViewModel.kt

private val ADMIN_EMAILS = listOf(
    "zayacodehub@gmail.com",
    "admin@zaya.com",
    "teacher@school.edu",      ← Add here
    "principal@school.edu"      ← Add here
)
```

Then:
1. Create these users in Firebase Console
2. Rebuild the app
3. They can now login as admins

### Method 2: Only Firebase (Advanced)
1. Email must already be in ADMIN_EMAILS list
2. Create user in Firebase Console
3. Optional: Set role="admin" in Firestore users collection

---

## 🔐 Security Best Practices

⚠️ **MUST DO:**
- [ ] Change default password immediately
- [ ] Use strong passwords (12+ characters)
- [ ] Only add trusted users
- [ ] Never share admin credentials
- [ ] Review admin access regularly

✅ **GOOD PRACTICES:**
- [ ] Use unique passwords for each admin
- [ ] Enable 2FA if available
- [ ] Keep ADMIN_EMAILS list private
- [ ] Audit admin activity
- [ ] Remove admins when they leave

---

## 🆘 Troubleshooting

### "Access Denied: Not an admin"
**Cause:** Email not in ADMIN_EMAILS list
**Solution:** 
- Check AdminViewModel.kt ADMIN_EMAILS list
- Add your email if missing
- Rebuild app

### "Login Failed"
**Cause:** Wrong email/password or user doesn't exist
**Solution:**
- Verify email exists in Firebase Console
- Check password is correct
- Reset password in Firebase if forgotten

### "Can't access Admin Panel"
**Cause:** User account not created in Firebase
**Solution:**
- Go to Firebase Console
- Create user with email: admin@zaya.com
- Use password: password123
- Try login again

### "Fields cannot be empty"
**Cause:** Email or password field is empty
**Solution:**
- Enter email: admin@zaya.com
- Enter password: password123
- Click Login

---

## 📋 Setup Checklist

Use this to verify everything is working:

- [ ] Code files updated (AdminViewModel.kt, AdminLoginScreen.kt)
- [ ] Firebase project selected
- [ ] User created in Firebase Authentication
- [ ] App built successfully
- [ ] App installed on device/emulator
- [ ] Can access Admin Panel from app
- [ ] Can login with admin@zaya.com
- [ ] Admin Dashboard appears
- [ ] Can see quiz management options
- [ ] Can see formula management options
- [ ] Can see announcement options
- [ ] Can see article management options

---

## 🚀 Next Steps

### Immediate (First Time)
1. Create admin account in Firebase ← **START HERE**
2. Build and run the app
3. Test login with default credentials
4. Verify Admin Dashboard loads

### Short Term (First Week)
1. Change default password
2. Add more admin users if needed
3. Create some sample quizzes/formulas
4. Test all admin features

### Long Term (Ongoing)
1. Maintain list of admin users
2. Add content regularly
3. Monitor student usage
4. Update as needed

---

## 📞 Support

### Quick Links
- Firebase Console: https://console.firebase.google.com/
- Firebase Docs: https://firebase.google.com/docs
- Android Dev: https://developer.android.com/

### Common Issues
- See ADMIN_LOGIN_SETUP.md → Troubleshooting section
- See ADMIN_VISUAL_GUIDE.md → Need Help table
- Check Firebase Console for any errors

---

## 📖 File Structure

```
AIZAYA/
├── ADMIN_LOGIN_QUICK_REF.md       ← Quick reference (read first!)
├── ADMIN_LOGIN_SETUP.md            ← Complete setup guide
├── FIREBASE_ADMIN_SETUP.md         ← Firebase console steps
├── ADMIN_VISUAL_GUIDE.md           ← Diagrams & flowcharts
├── ADMIN_SETUP_SUMMARY.md          ← What was done summary
├── CODE_CHANGES.md                 ← Exact code modifications
│
└── app/
    └── src/main/java/com/example/aizaya/
        ├── AdminViewModel.kt       ← ✅ UPDATED (multiple emails)
        ├── AdminLoginScreen.kt     ← ✅ UPDATED (hint added)
        ├── AdminDashboardScreen.kt
        └── ... other files
```

---

## ✨ Features Overview

| Feature | Status | Admin Can | Notes |
|---------|--------|-----------|-------|
| Admin Login | ✅ Active | Login with Gmail | Default: admin@zaya.com |
| Multiple Admins | ✅ Active | Add multiple users | Edit ADMIN_EMAILS list |
| Quiz Management | ✅ Active | CRUD quizzes | Full control |
| Formula Management | ✅ Active | CRUD formulas | Full control |
| Announcements | ✅ Active | Post/edit | Visible to all |
| Articles | ✅ Active | Manage content | Study materials |
| Role-Based Access | ✅ Active | Admin-only | Non-admins denied |
| Password Security | ✅ Active | Change in Firebase | Use strong passwords |

---

## 🎓 Learning Resources

- 📖 See FIREBASE_ADMIN_SETUP.md for Firebase tutorial
- 📊 See ADMIN_VISUAL_GUIDE.md for architecture diagrams
- 🔧 See CODE_CHANGES.md for technical details
- ⚡ See ADMIN_LOGIN_QUICK_REF.md for quick answers

---

## 📝 Version Info

```
App Version: AIZAYA v1.0
Admin System: v1.0
Setup Date: March 17, 2026
Status: ✅ Production Ready
```

---

## 💡 Pro Tips

💡 **Tip 1:** The hint text on login screen shows default credentials
💡 **Tip 2:** You can add multiple admins by editing one file
💡 **Tip 3:** Password can be changed in Firebase anytime
💡 **Tip 4:** Always backup admin credentials somewhere safe
💡 **Tip 5:** Test admin features thoroughly before giving to others

---

## 🎉 Ready to Go!

You're all set to use the admin panel. Follow the **Quick Start** section above and you'll have it working in 5 minutes!

### TL;DR
1. Create `admin@zaya.com` / `password123` in Firebase
2. Build app
3. Login to Admin Panel
4. Done! 🚀

---

**Questions?** Check the documentation files or the Firebase Console for errors.

**Happy administrating!** 🎓

