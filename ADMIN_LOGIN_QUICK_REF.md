# Admin Login Quick Reference

## 🔑 Default Admin Credentials

```
Email:    admin@zaya.com
Password: password123
```

## 📱 How to Login to Admin Panel

1. Open the AIZAYA app
2. Navigate to the **Admin Panel** section
3. Enter the credentials above
4. Click **"Login as Admin"**

## 🛠️ How to Create the Admin Account in Firebase

1. Open [Firebase Console](https://console.firebase.google.com/)
2. Select your AIZAYA project
3. Go to **Authentication** → **Users** tab
4. Click **"Create User"** button
5. Enter:
   - **Email:** `admin@zaya.com`
   - **Password:** `password123`
6. Click **"Create User"**

## ✅ Verify Login Works

- The app should display "Admin Portal" screen
- After successful login, you'll access the Admin Dashboard
- You can manage quizzes, formulas, announcements, and articles

## 🔐 Security Tips

- **Change the default password** immediately after first login
- Only add trusted individuals as admins
- Use strong passwords (at least 12 characters)
- Never share admin credentials

## 🔄 Add More Admins

Edit this file: `AdminViewModel.kt`

Find this section:
```kotlin
private val ADMIN_EMAILS = listOf(
    "zayacodehub@gmail.com",
    "admin@zaya.com"
)
```

Add your email:
```kotlin
private val ADMIN_EMAILS = listOf(
    "zayacodehub@gmail.com",
    "admin@zaya.com",
    "your-email@gmail.com"  // ← Add here
)
```

Then create the user in Firebase with the same email.

## 🆘 Troubleshooting

| Problem | Solution |
|---------|----------|
| Login fails | Verify email/password in Firebase Console |
| "Access Denied: Not an admin" | Check email is in ADMIN_EMAILS list |
| Can't access Admin Panel | Log out regular account first, then try admin login |
| Forgot password | Reset in Firebase Console (Authentication → Users → Change password) |

## 📋 Supported Admins

Currently configured admins:
1. `zayacodehub@gmail.com`
2. `admin@zaya.com` ← Default

To modify, edit `AdminViewModel.kt` and add/remove emails from `ADMIN_EMAILS` list.

---

**Ready to login?** Use the default credentials above! 🚀

