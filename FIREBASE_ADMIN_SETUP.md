# Firebase Admin Account Setup Instructions

## Overview
This guide will help you set up the default admin account in Firebase Console so you can access the admin panel in the AIZAYA app.

## Required Information
- **Email:** `admin@zaya.com`
- **Password:** `password123`

## Step-by-Step Setup

### 1. Access Firebase Console
```
1. Go to https://console.firebase.google.com/
2. Sign in with your Google account
3. Click on "AIZAYA" project (or your project name)
```

### 2. Navigate to Authentication
```
1. In the left sidebar, find "Build" section
2. Click on "Authentication"
3. Click the "Users" tab
```

### 3. Create New User
```
1. Click the "Create user" button (usually on top right)
2. A dialog will appear
```

### 4. Enter Admin Credentials
```
Email Address:    admin@zaya.com
Password:         password123
```

### 5. Create the User
```
Click the "Create user" button in the dialog
Wait for confirmation message
```

### 6. Verify User Was Created
```
1. You should see "admin@zaya.com" in the users list
2. Note the User ID (UID) shown next to the email
3. This UID will be used for Firestore (optional)
```

## Optional: Set Admin Role in Firestore

This adds an extra security layer:

### 1. Go to Firestore Database
```
1. In left sidebar, find "Build" section
2. Click on "Firestore Database"
```

### 2. Create Users Collection
```
1. Click "Create collection" if it doesn't exist
2. Name it: "users"
3. Click "Next"
```

### 3. Create Admin Document
```
1. Click "Add document"
2. Document ID: Use the UID from step 6 above
3. Click "Add field":
   - Field: "role"
   - Type: "String"
   - Value: "admin"
4. Click "Save"
```

## Testing the Login

### 1. Build and Run the App
```
1. Open Android Studio
2. Build the app
3. Deploy to emulator or device
```

### 2. Access Admin Panel
```
1. Open the AIZAYA app
2. Navigate to Admin Panel
3. Enter:
   Email: admin@zaya.com
   Password: password123
4. Click "Login as Admin"
```

### 3. Verify Success
```
You should see:
- Admin Dashboard
- Options to manage quizzes, formulas, announcements, articles
```

## Changing the Admin Password Later

### 1. In Firebase Console
```
1. Go to Authentication → Users
2. Find "admin@zaya.com"
3. Click the three-dot menu (⋮)
4. Select "Change password"
5. Enter new password
6. Click "Update password"
```

### 2. Update App Login
```
Use the new password next time you login to admin panel
```

## Adding More Admin Accounts

### Method 1: Through Firebase Console
```
1. Repeat steps 1-5 above with new email
2. Create multiple admins this way
```

### Method 2: Update AdminViewModel.kt
```
1. Open: AdminViewModel.kt
2. Find: private val ADMIN_EMAILS
3. Add new email to the list:
   private val ADMIN_EMAILS = listOf(
       "zayacodehub@gmail.com",
       "admin@zaya.com",
       "newadmin@gmail.com"  ← Add here
   )
4. Save and rebuild app
5. Create the user in Firebase Console with the same email
```

## Troubleshooting

### Error: "User already exists"
- The email is already created in Firebase
- Use a different email or reset the password of existing user

### Error: "Invalid email"
- Make sure email format is correct (admin@zaya.com)
- Check for extra spaces

### Can't Login to Admin Panel
1. Verify email exists in Firebase Console
2. Check password is correct
3. Make sure email matches one in ADMIN_EMAILS list
4. Clear app cache and try again

### "Access Denied: Not an admin"
- Email must be in ADMIN_EMAILS list in AdminViewModel.kt
- User must exist in Firebase Authentication
- If using Firestore role field, it must be set to "admin"

## Security Best Practices

⚠️ **Important:**
1. ✅ Change the default password immediately
2. ✅ Only add trusted users as admins
3. ✅ Use unique, strong passwords
4. ✅ Never commit real passwords to version control
5. ✅ Regularly audit who has admin access
6. ✅ Enable 2-factor authentication on admin accounts (if available)

## Files Modified

- `AdminViewModel.kt` - Updated to support multiple admin emails
- `AdminLoginScreen.kt` - Added helpful hint with default credentials

## Contact

For issues with this setup:
1. Check that your Firebase project is properly configured
2. Verify google-services.json is in the correct location
3. Ensure Firebase dependencies are up to date
4. Review Firebase Console for any error messages

---

**Date:** March 17, 2026
**Admin System Version:** 1.0
**Firebase Support:** https://firebase.google.com/support

