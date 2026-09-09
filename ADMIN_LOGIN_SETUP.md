# Admin Panel Login Setup Guide

## Overview
The AIZAYA app now supports admin access through the Admin Panel. This guide will help you set up admin credentials for logging into the admin panel.

## Default Admin Account
A default admin account has been configured for easy access:

```
Email: admin@zaya.com
Password: password123
```

## Setup Steps

### Step 1: Create the Admin Account in Firebase

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your AIZAYA project
3. Navigate to **Authentication** > **Users**
4. Click **Create User** button
5. Fill in the details:
   - Email: `admin@zaya.com`
   - Password: `password123` (You can change this later)
6. Click **Create User**

### Step 2: Set Admin Role in Firestore (Optional but Recommended)

1. In Firebase Console, go to **Firestore Database**
2. Create a new document in the `users` collection
3. Set the document ID to the User ID from the created user
4. Add the following field:
   ```
   role: "admin"
   ```

### Step 3: Test Admin Login

1. Open the AIZAYA app
2. Navigate to the Admin Panel
3. Enter credentials:
   - Email: `admin@zaya.com`
   - Password: `password123`
4. Click "Login as Admin"

## Adding More Admin Accounts

To add additional admin accounts, edit `AdminViewModel.kt`:

```kotlin
private val ADMIN_EMAILS = listOf(
    "zayacodehub@gmail.com",
    "admin@zaya.com",
    "youremail@gmail.com"  // Add new email here
)
```

Then create the corresponding user in Firebase Authentication.

## Security Notes

⚠️ **Important:**
- Change the default password immediately in Firebase Console
- Use strong, unique passwords for admin accounts
- Only add trusted users as admins
- For production, consider using a more secure password management system
- Never commit real passwords to version control

## Troubleshooting

### "Access Denied: Not an admin" error
- Verify the email exists in your Firebase Authentication
- Check that the email matches one in the `ADMIN_EMAILS` list in AdminViewModel.kt
- Ensure the user was created in Firebase Console

### "Login Failed" error
- Verify the email and password are correct
- Check your Firebase project credentials in `google-services.json`
- Ensure Firebase is properly initialized

### Can't access Admin Panel
- Make sure you're logged out of any regular user account first
- Clear app cache and try again
- Verify Firebase connectivity

## Custom Password (Optional)

Instead of the default password, you can set your own:

1. In Firebase Console, go to **Authentication** > **Users**
2. Find the admin user
3. Click the three-dot menu
4. Select **Change password**
5. Enter new password
6. Click **Update password**

## Multiple Admin Support

The system now supports multiple admin accounts. To add more:

1. Add email to `ADMIN_EMAILS` in `AdminViewModel.kt`
2. Create the user in Firebase Authentication
3. Rebuild and deploy the app

Example with multiple admins:
```kotlin
private val ADMIN_EMAILS = listOf(
    "zayacodehub@gmail.com",
    "admin@zaya.com",
    "principal@school.edu",
    "coordinator@school.edu"
)
```

---

**Last Updated:** March 17, 2026
**Admin Portal Version:** 1.0

