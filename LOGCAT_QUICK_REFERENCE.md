# Logcat Logging - Quick Reference Card

## 🎯 One-Liner Commands

### View Submission Logs Only
```bash
adb logcat 'SubmitOrder:D *:S'
```

### View API Logs Only  
```bash
adb logcat 'TelegramAPI:D *:S'
```

### View Both (Recommended)
```bash
adb logcat 'SubmitOrder:D TelegramAPI:D *:S'
```

### View with Timestamps
```bash
adb logcat -v threadtime 'SubmitOrder:D TelegramAPI:D *:S'
```

### Save to File
```bash
adb logcat -d 'SubmitOrder:D TelegramAPI:D *:S' > ~/order_logs.txt
```

---

## 📱 What Gets Logged

### When Submit Button Pressed ✅

```
SubmitOrder: 🔵 SUBMIT BUTTON PRESSED
SubmitOrder: 📝 STEP 1: Reading form field values...
SubmitOrder:   - shopName: 'ABC Shop' (length: 8)
SubmitOrder:   - orderDate: '24/03/2026'
SubmitOrder:   - salesmanName: 'John' (length: 4)
SubmitOrder:   - numberOfBoxes: '5'
SubmitOrder:   - specification: '...' (length: 25)
SubmitOrder:   - amountPerBox: '250.50'
SubmitOrder:   - location: 'Delhi' (length: 5)
SubmitOrder:   - phoneNumber: '9876543210'
```

### Field Validation ✅

```
SubmitOrder: ✅ STEP 3: Validating all form fields...
SubmitOrder: ✅ All fields validated successfully
```

### Numeric Parsing ✅

```
SubmitOrder: 🔢 STEP 4: Parsing numeric values...
SubmitOrder:   - Boxes: 5 (type: int)
SubmitOrder:   - Amount per box: 250.5 (type: double)
SubmitOrder:   - Calculated total: 1252.5 (5 × 250.5)
```

### API Request ✅

```
TelegramAPI: 🌐 TELEGRAM API REQUEST INITIATED
TelegramAPI: 📌 Submit button disabled
TelegramAPI: 📦 STEP 1: Order Data Details
TelegramAPI:   - Shop: ABC Shop
TelegramAPI:   - Date: 24/03/2026
TelegramAPI:   - Boxes: 5, Amount: ₹250.5, Total: ₹1252.5
TelegramAPI: 📝 STEP 2: Converting to JSON...
TelegramAPI: ✏️ STEP 3: Formatting message for Telegram...
TelegramAPI:   - Message length: 350 characters
TelegramAPI: 🔧 STEP 4: Getting API configuration...
TelegramAPI:   - Bot Token: 8725***x8g (masked)
TelegramAPI:   - Chat ID: 8353059211
TelegramAPI: 🚀 STEP 6: Executing API call...
```

### API Response ✅

```
TelegramAPI: ✅ API RESPONSE RECEIVED
TelegramAPI: ⏱️  Response time: 1250ms
TelegramAPI: 📍 Response Details:
TelegramAPI:   - HTTP Status Code: 200
TelegramAPI:   - Is Successful: true
TelegramAPI: ✅ SUCCESS: Order submitted to Telegram successfully!
TelegramAPI: ⏱️  Total API time: 1401ms
```

### Error Handling ❌

```
TelegramAPI: ❌ API REQUEST FAILED
TelegramAPI: 🚨 Error Details:
TelegramAPI:   - Exception Type: IOException
TelegramAPI:   - Exception Message: Unable to resolve host
TelegramAPI: 💡 Common Causes:
TelegramAPI:   1. No internet connection
TelegramAPI:   2. Telegram API is unreachable
```

---

## 🎯 Testing Workflow

```bash
# 1. Clear logs
adb logcat -c

# 2. Start monitoring (note: quote the filter string for zsh)
adb logcat 'SubmitOrder:D TelegramAPI:D *:S'

# 3. Press Submit button in app

# 4. Watch the logs appear in real-time
```

---

## 🔍 Searching Logs

### Find Success Messages
```bash
adb logcat | grep "SUCCESS\|COMPLETE"
```

### Find All Errors
```bash
adb logcat | grep "ERROR\|FAILED\|❌"
```

### Find API Responses
```bash
adb logcat | grep "Response\|HTTP Status"
```

### Find Timing Info
```bash
adb logcat | grep "⏱️"
```

---

## 📋 Log Tags Available

| Tag | Purpose | When Used |
|-----|---------|-----------|
| **SubmitOrder** | Form submission & validation | When Submit button pressed |
| **TelegramAPI** | API request/response | API communication |

---

## ✨ Log Info Included

- ✅ **Field Values** - All 9 form fields logged
- ✅ **Validation Details** - Each field validated
- ✅ **Numeric Calculations** - Box count, amounts, totals
- ✅ **JSON Representation** - Full data as JSON
- ✅ **API Credentials** - Bot token, Chat ID (masked)
- ✅ **Request Details** - URL, method, headers, parameters
- ✅ **Response Status** - HTTP code, success/failure
- ✅ **Response Body** - Full response JSON
- ✅ **Error Details** - Exception types, messages, stack traces
- ✅ **Performance Metrics** - Execution times in milliseconds
- ✅ **Timestamps** - HH:mm:ss.SSS format

---

## 🚀 Quick Test

```bash
# Terminal 1: Monitor logs (use quotes for zsh)
adb logcat 'SubmitOrder:D TelegramAPI:D *:S'

# Terminal 2: Open app and press Submit button
# You should see ~50+ detailed log lines appear instantly
```

