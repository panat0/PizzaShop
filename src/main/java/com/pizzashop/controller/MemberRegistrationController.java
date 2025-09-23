package com.pizzashop.controller;

import com.pizzashop.model.DataManager;
import com.pizzashop.model.Member;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้าสมัครสมาชิก
 */
public class MemberRegistrationController implements Initializable {

    @FXML private TextField nameField;
    @FXML private TextField phoneField;
    @FXML private DatePicker birthDatePicker;
    @FXML private DatePicker joinDatePicker;
    @FXML private TextField expireDateField;
    @FXML private Button submitButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    private DataManager dataManager;
    private MainController mainController;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dataManager = DataManager.getInstance();
        setupEventHandlers();
        setDefaultValues();
        setupValidation();
    }

    /**
     * ตั้งค่า Event Handlers
     */
    private void setupEventHandlers() {
        submitButton.setOnAction(e -> handleSubmit());
        cancelButton.setOnAction(e -> handleCancel());

        // อัพเดทวันหมดอายุเมื่อวันที่สมัครเปลี่ยน
        joinDatePicker.setOnAction(e -> updateExpireDate());
    }

    /**
     * ตั้งค่าค่าเริ่มต้น
     */
    private void setDefaultValues() {
        LocalDate today = LocalDate.now();
        joinDatePicker.setValue(today);
        expireDateField.setEditable(false);
        updateExpireDate();
    }

    /**
     * ตั้งค่า validation
     */
    private void setupValidation() {
        // เพิ่ม listeners สำหรับ real-time validation
        nameField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        birthDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> clearError());
        joinDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> clearError());
    }

    /**
     * อัพเดทวันหมดอายุ
     */
    private void updateExpireDate() {
        LocalDate joinDate = joinDatePicker.getValue();
        if (joinDate != null) {
            LocalDate expireDate = joinDate.plusYears(1).minusDays(1);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            expireDateField.setText(expireDate.format(formatter));
        }
    }

    /**
     * ตรวจสอบความถูกต้องของข้อมูล
     */
    private boolean validateForm() {
        // ตรวจสอบชื่อ
        if (nameField.getText() == null || nameField.getText().trim().isEmpty()) {
            showError("กรุณากรอกชื่อ-นามสกุล");
            nameField.requestFocus();
            return false;
        }

        if (nameField.getText().trim().length() < 2) {
            showError("ชื่อต้องมีอย่างน้อย 2 ตัวอักษร");
            nameField.requestFocus();
            return false;
        }

        // ตรวจสอบเบอร์โทรศัพท์
        if (phoneField.getText() == null || phoneField.getText().trim().isEmpty()) {
            showError("กรุณากรอกเบอร์โทรศัพท์");
            phoneField.requestFocus();
            return false;
        }

        String cleanPhone = phoneField.getText().replaceAll("[^0-9]", "");
        if (cleanPhone.length() < 10 || cleanPhone.length() > 12) {
            showError("เบอร์โทรต้องมี 10-12 หลัก");
            phoneField.requestFocus();
            return false;
        }

        // ตรวจสอบว่าเบอร์นี้มีสมาชิกใช้แล้วหรือไม่
        Optional<Member> existingMember = dataManager.findMemberByPhone(cleanPhone);
        if (existingMember.isPresent()) {
            showError("เบอร์โทรนี้มีสมาชิกใช้แล้ว");
            phoneField.requestFocus();
            return false;
        }

        // ตรวจสอบวันเกิด
        if (birthDatePicker.getValue() == null) {
            showError("กรุณาเลือกวันเกิด");
            birthDatePicker.requestFocus();
            return false;
        }

        LocalDate birthDate = birthDatePicker.getValue();
        if (birthDate.isAfter(LocalDate.now())) {
            showError("วันเกิดไม่สามารถเกินวันปัจจุบันได้");
            birthDatePicker.requestFocus();
            return false;
        }

        if (birthDate.isBefore(LocalDate.now().minusYears(150))) {
            showError("วันเกิดไม่ถูกต้อง");
            birthDatePicker.requestFocus();
            return false;
        }

        // ตรวจสอบวันที่สมัคร
        if (joinDatePicker.getValue() == null) {
            showError("กรุณาเลือกวันที่สมัคร");
            joinDatePicker.requestFocus();
            return false;
        }

        LocalDate joinDate = joinDatePicker.getValue();
        if (joinDate.isAfter(LocalDate.now())) {
            showError("วันที่สมัครไม่สามารถเกินวันปัจจุบันได้");
            joinDatePicker.requestFocus();
            return false;
        }

        if (joinDate.isBefore(LocalDate.now().minusYears(1))) {
            showError("วันที่สมัครไม่ควรเก่าเกิน 1 ปี");
            joinDatePicker.requestFocus();
            return false;
        }

        return true;
    }

    /**
     * จัดการการกดปุ่มสมัคร
     */
    private void handleSubmit() {
        if (!validateForm()) {
            return;
        }

        try {
            // เตรียมข้อมูล
            String name = nameField.getText().trim();
            String phone = phoneField.getText().replaceAll("[^0-9]", ""); // เก็บเฉพาะตัวเลข
            LocalDate birthDate = birthDatePicker.getValue();
            LocalDate joinDate = joinDatePicker.getValue();

            // สร้างสมาชิกใหม่
            Member newMember = dataManager.addMember(name, phone, birthDate, joinDate);



            // แสดงข้อความยืนยัน
            Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
            successAlert.setTitle("สมัครสมาชิกสำเร็จ");
            successAlert.setHeaderText("ยินดีต้อนรับสมาชิกใหม่!");

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            successAlert.setContentText(String.format(
                    "รหัสสมาชิก: %s\nชื่อ: %s\nเบอร์: %s\nวันหมดอายุ: %s\n\nค่าสมัคร: 100 บาท",
                    getMemberId(newMember),
                    getMemberName(newMember),
                    formatPhoneNumber(getMemberPhone(newMember)),
                    getMemberExpireDate(newMember).format(formatter)
            ));
            successAlert.showAndWait();

            // ปิดหน้าต่าง
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            showError("เกิดข้อผิดพลาดในการบันทึกข้อมูล");
        }
    }

    /**
     * จัดการการกดปุ่มยกเลิก
     */
    private void handleCancel() {
        // ตรวจสอบว่ามีการกรอกข้อมูลแล้วหรือไม่
        boolean hasData = !nameField.getText().trim().isEmpty() ||
                !phoneField.getText().trim().isEmpty() ||
                birthDatePicker.getValue() != null ||
                (joinDatePicker.getValue() != null && !joinDatePicker.getValue().equals(LocalDate.now()));

        if (hasData) {
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("ยืนยันการยกเลิก");
            confirmAlert.setHeaderText("ต้องการยกเลิกการสมัครสมาชิก?");
            confirmAlert.setContentText("ข้อมูลที่กรอกจะหายไป");

            Optional<ButtonType> result = confirmAlert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                closeWindow();
            }
        } else {
            closeWindow();
        }
    }

    // === Helper Methods ===

    /**
     * แสดงข้อความ error
     */
    private void showError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
        }
    }

    /**
     * ล้างข้อความ error
     */
    private void clearError() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
        }
    }

    /**
     * ปิดหน้าต่าง
     */
    private void closeWindow() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    /**
     * จัดรูปแบบเบอร์โทรศัพท์สำหรับแสดงผล
     */
    private String formatPhoneNumber(String phone) {
        if (phone.length() == 10) {
            return phone.substring(0, 3) + "-" + phone.substring(3, 6) + "-" + phone.substring(6);
        } else if (phone.length() == 11) {
            return phone.substring(0, 3) + "-" + phone.substring(3, 7) + "-" + phone.substring(7);
        }
        return phone;
    }

    // === Member Helper Methods (เพื่อแก้ปัญหา method ที่หาไม่เจอใน Member class) ===

    /**
     * ได้ ID ของสมาชิก
     */
    private String getMemberId(Member member) {
        try {
            return (String) member.getClass().getMethod("getMemberId").invoke(member);
        } catch (Exception e) {
            return "N/A"; // fallback value
        }
    }

    /**
     * ได้ชื่อของสมาชิก
     */
    private String getMemberName(Member member) {
        try {
            return (String) member.getClass().getMethod("getName").invoke(member);
        } catch (Exception e) {
            return "N/A"; // fallback value
        }
    }

    /**
     * ได้เบอร์โทรของสมาชิก
     */
    private String getMemberPhone(Member member) {
        try {
            return (String) member.getClass().getMethod("getPhone").invoke(member);
        } catch (Exception e) {
            return "N/A"; // fallback value
        }
    }

    /**
     * ได้วันหมดอายุของสมาชิก
     */
    private LocalDate getMemberExpireDate(Member member) {
        try {
            return (LocalDate) member.getClass().getMethod("getExpireDate").invoke(member);
        } catch (Exception e) {
            return LocalDate.now().plusYears(1); // fallback value
        }
    }

    // === Setter ===

    /**
     * ตั้งค่า MainController สำหรับ callback
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }
}