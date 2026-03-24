# Order App - Android Telegram Integration

A complete Android application for submitting order data to a Telegram bot.

## Features

✅ **User-Friendly Form Interface**
- Material Design 3 components
- Real-time form validation
- Auto-calculated total amount
- Date picker for order date
- All required and optional fields

✅ **Field Validation**
- Shop Name: Required, max 100 chars
- Order Date: Required, valid date format (dd/MM/yyyy)
- Salesman Name: Required, max 100 chars
- Number of Boxes: Required, positive integer
- Specification: Optional, max 500 chars
- Amount per Box: Required, positive decimal
- **Total Amount**: Auto-calculated (read-only) = Boxes × Amount
- Location: Required, max 100 chars
- Phone Number: Required, valid Indian 10-digit format

✅ **Telegram Integration**
- Direct API integration with Telegram Bot
- JSON formatted data transmission
- Beautiful HTML formatted messages in Telegram
- Error handling with retry mechanism
- Loading states

✅ **Error Handling**
- Field-level validation with user-friendly messages
- Network error detection and retry option
- API failure handling

## Project Structure

```
OrderApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/orderapp/
│   │   │   │   ├── MainActivity.java          # Main form activity
│   │   │   │   ├── api/
│   │   │   │   │   ├── TelegramBotAPIClient.java    # API configuration
│   │   │   │   │   ├── TelegramBotService.java      # Retrofit interface
│   │   │   │   │   └── TelegramResponse.java        # Response model
│   │   │   │   ├── model/
│   │   │   │   │   └── OrderData.java        # Order data model
│   │   │   │   └── validator/
│   │   │   │       └── FormDataValidator.java # Field validation logic
│   │   │   ├── res/
│   │   │   │   ├── layout/
│   │   │   │   │   └── activity_main.xml     # Form UI layout
│   │   │   │   ├── values/
│   │   │   │   │   ├── colors.xml
│   │   │   │   │   ├── strings.xml
│   │   │   │   │   └── styles.xml
│   │   │   └── AndroidManifest.xml
│   │   └── test/
│   └── build.gradle
├── build.gradle
└── settings.gradle
```

## Required Dependencies

- Android SDK 28+
- Android Studio Giraffe or latest
- Java 17+

## Setup Instructions

### 1. Get Telegram Bot Token
1. Open Telegram and find **@BotFather**
2. Send `/start` command
3. Send `/newbot` to create a new bot
4. Follow the prompts and save your **Bot Token**

### 2. Get Your Chat ID
1. Create a private group or use a personal chat
2. Add your bot to the group/chat
3. Send a message
4. Open browser: `https://api.telegram.org/bot<BOT_TOKEN>/getUpdates`
5. Find `chat.id` value

### 3. Update Telegram Credentials
Edit [app/src/main/java/com/orderapp/api/TelegramBotAPIClient.java](app/src/main/java/com/orderapp/api/TelegramBotAPIClient.java):

```java
private static final String BOT_TOKEN = "YOUR_BOT_TOKEN_HERE";  // Replace here
private static final String CHAT_ID = "YOUR_CHAT_ID_HERE";      // Replace here
```

### 4. Build and Run

```bash
# Build the project
./gradlew build

# Run on emulator or device
./gradlew installDebug
```

Or use Android Studio:
1. Open project
2. Click `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
3. Run on emulator/device

## How It Works

### Form Submission Flow
1. User fills all form fields
2. User clicks "Submit" button
3. App validates all fields
4. Auto-calculates total amount
5. Converts data to JSON
6. Sends to Telegram bot via API
7. Shows success/error dialog
8. Clears form on success

### Data Format (Telegram)
Data is sent as a formatted HTML message:

```
📦 NEW ORDER RECEIVED 📦

Shop Name: ABC Electronics
Order Date: 22/03/2026
Salesman: John Doe
Number of Boxes: 5
Specification: High quality electronics
Amount per Box: ₹ 250.00
Total Amount: ₹ 1250.00
Location: New Delhi
Phone: 9876543210
Submitted at: 2026-03-22 14:30:45
```

## Validation Rules

| Field | Min | Max | Format |
|-------|-----|-----|--------|
| Shop Name | 1 | 100 | Text |
| Order Date | - | - | dd/MM/yyyy |
| Salesman Name | 1 | 100 | Text |
| Boxes | 1 | - | Integer > 0 |
| Specification | 0 | 500 | Text (optional) |
| Amount/Box | - | - | Decimal > 0 |
| Location | 1 | 100 | Text |
| Phone | 10 | 10 | Indian format |

## Troubleshooting

### Bot Token Error
- Ensure token is copied correctly from BotFather
- No extra spaces or characters

### Chat ID Error
- Verify chat ID is correct from getUpdates
- Chat ID should be a number (or negative for groups)
- Ensure bot has permission to post in the chat

### Network Issues
- Check internet connection
- Verify API endpoint is accessible
- App will show retry dialog on network failure

### Build Errors
- Ensure Java 17+ is installed
- Update Android SDK to API 35+
- Run `./gradlew clean` before building

## UI Components Used

- **TextInputLayout** (Material Design 3)
- **MaterialButton**
- **DatePickerDialog**
- **AlertDialog**
- **EditText** (various input types)

## Permissions Required

- `android.permission.INTERNET` - For Telegram API calls

## Testing

1. Fill all required fields correctly
2. Verify total amount auto-calculates
3. Submit and check Telegram bot receives message
4. Try invalid inputs to verify validation
5. Test network error with retry

## Notes

- ✅ Placeholder tokens/chat IDs are ready to be replaced
- ✅ All fields include proper validation
- ✅ Currency is set to Indian Rupee (₹)
- ✅ Phone validation for Indian numbers (6-9 as first digit)
- ✅ Total amount is auto-calculated and read-only
- ✅ Material Design 3 with modern UI
- ✅ Error handling with user-friendly messages

## Future Enhancements

- Local SQLite database for offline submissions
- Order history and edit capability
- Multiple telegram group support
- Image attachments
- Signature capture
- Barcode scanning

## License

MIT License - Free to modify and distribute
