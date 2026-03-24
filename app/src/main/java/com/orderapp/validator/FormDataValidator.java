package com.orderapp.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * Validator for form data
 */
public class FormDataValidator {
    
    private static final int SHOP_NAME_MAX = 100;
    private static final int SALESMAN_NAME_MAX = 100;
    private static final int SPECIFICATION_MAX = 500;
    private static final int LOCATION_MAX = 100;
    private static final Pattern INDIAN_PHONE_PATTERN = Pattern.compile("^[6-9]\\d{9}$|^\\+91[6-9]\\d{9}$");
    
    public static class ValidationError {
        public String field;
        public String message;
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
    }
    
    public static ValidationError validateShopName(String shopName) {
        if (shopName == null || shopName.trim().isEmpty()) {
            return new ValidationError("Shop Name", "Shop name is required");
        }
        if (shopName.length() > SHOP_NAME_MAX) {
            return new ValidationError("Shop Name", "Shop name cannot exceed 100 characters");
        }
        return null;
    }
    
    public static ValidationError validateOrderDate(String date) {
        if (date == null || date.trim().isEmpty()) {
            return new ValidationError("Date", "Order date is required");
        }
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            sdf.setLenient(false);
            sdf.parse(date);
            return null;
        } catch (ParseException e) {
            return new ValidationError("Date", "Invalid date format (use dd/MM/yyyy)");
        }
    }
    
    public static ValidationError validateSalesmanName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return new ValidationError("Salesman Name", "Salesman name is required");
        }
        if (name.length() > SALESMAN_NAME_MAX) {
            return new ValidationError("Salesman Name", "Salesman name cannot exceed 100 characters");
        }
        return null;
    }
    
    public static ValidationError validateNumberOfBoxes(String boxes) {
        if (boxes == null || boxes.trim().isEmpty()) {
            return new ValidationError("Number of Boxes", "Number of boxes is required");
        }
        try {
            int numBoxes = Integer.parseInt(boxes.trim());
            if (numBoxes <= 0) {
                return new ValidationError("Number of Boxes", "Number of boxes must be greater than 0");
            }
            return null;
        } catch (NumberFormatException e) {
            return new ValidationError("Number of Boxes", "Number of boxes must be a valid integer");
        }
    }
    
    public static ValidationError validateSpecification(String spec) {
        if (spec != null && spec.length() > SPECIFICATION_MAX) {
            return new ValidationError("Specification", "Specification cannot exceed 500 characters");
        }
        return null;
    }
    
    public static ValidationError validateAmountPerBox(String amount) {
        if (amount == null || amount.trim().isEmpty()) {
            return new ValidationError("Amount per Box", "Amount per box is required");
        }
        try {
            double amountValue = Double.parseDouble(amount.trim());
            if (amountValue <= 0) {
                return new ValidationError("Amount per Box", "Amount per box must be greater than 0");
            }
            return null;
        } catch (NumberFormatException e) {
            return new ValidationError("Amount per Box", "Amount per box must be a valid number");
        }
    }
    
    public static ValidationError validateLocation(String location) {
        if (location == null || location.trim().isEmpty()) {
            return new ValidationError("Location", "Location is required");
        }
        if (location.length() > LOCATION_MAX) {
            return new ValidationError("Location", "Location cannot exceed 100 characters");
        }
        return null;
    }
    
    public static ValidationError validatePhoneNumber(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return new ValidationError("Phone Number", "Phone number is required");
        }
        
        String cleanPhone = phone.replaceAll("[\\s-]", "");
        
        if (!INDIAN_PHONE_PATTERN.matcher(cleanPhone).matches()) {
            return new ValidationError("Phone Number", "Enter a valid Indian phone number (10 digits)");
        }
        
        return null;
    }
    
    public static ValidationError validateAllFields(String shopName, String orderDate, 
                                                    String salesmanName, String numberOfBoxes,
                                                    String specification, String amountPerBox,
                                                    String location, String phoneNumber) {
        
        ValidationError error;
        
        error = validateShopName(shopName);
        if (error != null) return error;
        
        error = validateOrderDate(orderDate);
        if (error != null) return error;
        
        error = validateSalesmanName(salesmanName);
        if (error != null) return error;
        
        error = validateNumberOfBoxes(numberOfBoxes);
        if (error != null) return error;
        
        error = validateSpecification(specification);
        if (error != null) return error;
        
        error = validateAmountPerBox(amountPerBox);
        if (error != null) return error;
        
        error = validateLocation(location);
        if (error != null) return error;
        
        error = validatePhoneNumber(phoneNumber);
        if (error != null) return error;
        
        return null;
    }
}
