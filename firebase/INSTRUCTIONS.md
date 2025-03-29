# Firebase Setup Instructions

## Updating Firestore Security Rules

To allow your app to save user data to Firestore, you need to update the Firestore security rules in the Firebase Console:

1. Go to the [Firebase Console](https://console.firebase.google.com/) and select your project
2. In the left sidebar, click on **Firestore Database**
3. Click on the **Rules** tab
4. Replace the existing rules with the content from the `firestore.rules` file in this directory
5. Click **Publish** to save the rules

Example rules:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow authenticated users to read and write their own user data
    match /users/{userId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && request.auth.uid == userId;
    }
    
    // Default deny
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

These rules allow:
- Authenticated users to read and write only their own user documents
- No access to any other documents by default

## Troubleshooting User Data Persistence

If users are authenticating successfully but no data is being written to Firestore:

1. Check the Logcat for errors in `AuthViewModel` or `UserRepository`
2. Verify that the Firestore security rules allow writes to the `users` collection
3. Ensure you have the correct Firebase configuration in your app
4. Check that the Firebase SDK dependencies are correctly included in your app's build.gradle
5. Verify network connectivity when testing
6. Check your Firebase project billing status if applicable

## User Data Structure

The app creates the following structure in Firestore:

```
users/
  ├── {userId}/
      ├── userId: string
      ├── name: string
      ├── email: string
      ├── photoUrl: string
      ├── currency: string
      ├── joinDate: timestamp (Firestore Timestamp)
      └── lastLogin: timestamp (Firestore Timestamp)
```

With this structure:
- Currency preference is preserved between logins
- Join date records when the user first created their account using a Firestore Timestamp
- Last login is updated every time the user logs in using a Firestore Timestamp
- Timestamp fields are automatically formatted into readable dates in the UI 