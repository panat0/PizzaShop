package com.pizzashop.controller;

import com.pizzashop.model.*;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controller สำหรับหน้าจอหลัก
 */
public class MainController implements Initializable {

    // === FXML Components ===

    @FXML private TableView<Item> itemTableView;
    @FXML private TableColumn<Item, String> nameColumn;
    @FXML private TableColumn<Item, String> categoryColumn;
    @FXML private TableColumn<Item, Double> priceColumn;

    @FXML private ComboBox<String> categoryFilter;
    @FXML private TextField searchField;

    @FXML private TableView<OrderItem> cartTableView;
    @FXML private TableColumn<OrderItem, String> cartItemColumn;
    @FXML private TableColumn<OrderItem, Integer> cartQuantityColumn;
    @FXML private TableColumn<OrderItem, Double> cartPriceColumn;

    // Member search components
    @FXML private TextField PhoneTextField;
    @FXML private Button checkMemberButton;
    @FXML private Text memberInfoLabel;
    @FXML private CheckBox dineInCheckBox;

    @FXML private Label totalLabel;
    @FXML private Label discountLabel;
    @FXML private Label promotionLabel;
    @FXML private TextArea orderSummaryArea;

    @FXML private Button addToCartButton;
    @FXML private Button removeFromCartButton;
    @FXML private Button clearCartButton;
    @FXML private Button checkoutButton;
    @FXML private Button registerMemberButton;

    // === Instance Variables ===

    private DataManager dataManager;
    private ObservableList<Item> allItems;
    private ObservableList<Item> filteredItems;
    private Order currentOrder;
    private Member currentMember; // เก็บสมาชิกปัจจุบัน

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // เริ่มต้น DataManager ก่อน
        dataManager = DataManager.getInstance();

        // สร้าง Order ใหม่ก่อน (ป้องกัน NullPointerException)
        createNewOrder();
        setupTableViews();
        setupControls();
        loadData();
    }

    /**
     * ตั้งค่า TableViews
     */
    private void setupTableViews() {
        // Item Table
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        priceColumn.setCellValueFactory(new PropertyValueFactory<>("price"));

        // Format price column
        priceColumn.setCellFactory(column -> new TableCell<Item, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f บาท", price));
                }
            }
        });

        // Cart Table
        cartItemColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getItem().getName()));
        cartQuantityColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getQuantity()).asObject());
        cartPriceColumn.setCellValueFactory(cellData ->
                new SimpleDoubleProperty(cellData.getValue().getTotal()).asObject());

        // Format cart price column
        cartPriceColumn.setCellFactory(column -> new TableCell<OrderItem, Double>() {
            @Override
            protected void updateItem(Double price, boolean empty) {
                super.updateItem(price, empty);
                if (empty || price == null) {
                    setText(null);
                } else {
                    setText(String.format("%.2f บาท", price));
                }
            }
        });
    }

    /**
     * ตั้งค่า Controls
     */
    private void setupControls() {

        // Category filter
        categoryFilter.getItems().add("ทั้งหมด");
        categoryFilter.getItems().addAll(dataManager.getAllCategories());
        categoryFilter.setValue("ทั้งหมด");
        categoryFilter.setOnAction(e -> filterItems());

        // Search field
        searchField.textProperty().addListener((obs, oldVal, newVal) -> filterItems());

        // Phone search setup
        setupPhoneSearch();

        // Dine in checkbox
        dineInCheckBox.setOnAction(e -> updateOrderDineIn());

        // Double click to add item
        itemTableView.setRowFactory(tv -> {
            TableRow<Item> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    addToCart();
                }
            });
            return row;
        });

        // Enable/disable buttons
        updateButtonStates();
    }


     //ตั้งค่าระบบค้นหาสมาชิกด้วยเบอร์โทร
    private void setupPhoneSearch() {
        memberInfoLabel.setText("");
        PhoneTextField.setOnAction(e -> searchMemberByPhone());
        checkMemberButton.setOnAction(e -> searchMemberByPhone());
        PhoneTextField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!oldVal.equals(newVal)) {
                resetMemberInfo();
            }
        });
    }

    // ค้นหาสมาชิกด้วยเบอร์โทร
    @FXML
    private void searchMemberByPhone() {
        String phoneNumber = PhoneTextField.getText().trim();

        if (phoneNumber.isEmpty()) {
            showAlert("กรุณาใส่เบอร์โทรศัพท์", "ไม่ได้ใส่เบอร์โทร");
            return;
        }

        // ค้นหาสมาชิกจาก DataManager (คืนค่าเป็น Optional<Member>)
        Optional<Member> memberOptional = dataManager.findMemberByPhone(phoneNumber);

        if (memberOptional.isPresent()) {
            Member foundMember = memberOptional.get();

            // ตรวจสอบว่าสมาชิกยังใช้งานได้หรือไม่
            if (!foundMember.isActive()) {
                memberInfoLabel.setText("❌ สมาชิกนี้ถูกยกเลิกแล้ว");
                memberInfoLabel.setStyle("-fx-fill: red;");
                currentMember = null;
                currentOrder.setMember(null);
            } else if (foundMember.isExpired()) {
                memberInfoLabel.setText("⚠️ สมาชิกนี้หมดอายุแล้ว (" + foundMember.getName() + ")");
                memberInfoLabel.setStyle("-fx-fill: orange;");
                currentMember = null;
                currentOrder.setMember(null);
            } else {
                // สมาชิกใช้งานได้ปกติ
                currentMember = foundMember;
                currentOrder.setMember(foundMember);
                displayMemberInfo(foundMember);
            }
        } else {
            // ไม่พบสมาชิก
            memberInfoLabel.setText("❌ ไม่พบสมาชิกที่ใช้เบอร์ " + phoneNumber);
            memberInfoLabel.setStyle("-fx-fill: red;");
            currentMember = null;
            currentOrder.setMember(null);
        }

        updateOrderSummary();
    }

    /**
     * แสดงข้อมูลสมาชิก
     */
    private void displayMemberInfo(Member member) {
        StringBuilder info = new StringBuilder();
        info.append("✅ สมาชิก: ").append(member.getName());

        if (member.isBirthday()) {
            info.append("\n🎂 วันเกิดวันนี้! ได้รับส่วนลดพิเศษ 15%");
            memberInfoLabel.setStyle("-fx-fill: #ff6b35;");
        } else {
            memberInfoLabel.setStyle("-fx-fill: green;");
        }

        memberInfoLabel.setText(info.toString());
    }

    /**
     * รีเซ็ตข้อมูลสมาชิก
     */
    private void resetMemberInfo() {
        if (currentMember != null) {
            currentMember = null;
            currentOrder.setMember(null);
            memberInfoLabel.setText("กรุณาใส่เบอร์โทรเพื่อค้นหาสมาชิก");
            memberInfoLabel.setStyle("-fx-fill: black;");
            updateOrderSummary();
        }
    }

    /**
     * ล้างข้อมูลการค้นหาสมาชิก
     */
    @FXML
    private void clearMemberSearch() {
        PhoneTextField.clear();
        resetMemberInfo();
    }

    /**
     * โหลดข้อมูลทั้งหมด
     */
    private void loadData() {
        allItems = FXCollections.observableArrayList(dataManager.getAllItems());
        filteredItems = FXCollections.observableArrayList(allItems);
        itemTableView.setItems(filteredItems);
    }

    /**
     * สร้างออเดอร์ใหม่
     */
    private void createNewOrder() {
        currentOrder = dataManager.createOrder(false);
        updateCartDisplay();
        updateOrderSummary();
    }

    /**
     * กรองรายการสินค้า
     */
    private void filterItems() {
        String selectedCategory = categoryFilter.getValue();
        String searchText = searchField.getText().toLowerCase().trim();

        filteredItems.clear();

        for (Item item : allItems) {
            boolean matchCategory = "ทั้งหมด".equals(selectedCategory) ||
                    item.getCategory().equals(selectedCategory);
            boolean matchSearch = searchText.isEmpty() ||
                    item.getName().toLowerCase().contains(searchText);

            if (matchCategory && matchSearch) {
                filteredItems.add(item);
            }
        }
    }

    // === Event Handlers ===

    @FXML
    private void addToCart() {
        Item selectedItem = itemTableView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            showAlert("กรุณาเลือกสินค้าก่อน", "ไม่ได้เลือกสินค้า");
            return;
        }

        // ถามจำนวน
        TextInputDialog dialog = new TextInputDialog("1");
        dialog.setTitle("เพิ่มสินค้า");
        dialog.setHeaderText("เพิ่ม: " + selectedItem.getName());
        dialog.setContentText("จำนวน:");

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            try {
                int quantity = Integer.parseInt(result.get());
                if (quantity > 0) {
                    currentOrder.addItem(selectedItem, quantity);
                    updateCartDisplay();
                    updateOrderSummary();
                    updateButtonStates();
                } else {
                    showAlert("จำนวนต้องมากกว่า 0", "จำนวนไม่ถูกต้อง");
                }
            } catch (NumberFormatException e) {
                showAlert("กรุณาใส่ตัวเลขเท่านั้น", "จำนวนไม่ถูกต้อง");
            }
        }
    }

    @FXML
    private void removeFromCart() {
        OrderItem selected = cartTableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("กรุณาเลือกรายการที่ต้องการลบ", "ไม่ได้เลือกรายการ");
            return;
        }

        currentOrder.removeItem(selected.getItem());
        updateCartDisplay();
        updateOrderSummary();
        updateButtonStates();
    }

    @FXML
    private void clearCart() {
        if (currentOrder.isEmpty()) return;

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("ยืนยันการล้างตะกร้า");
        alert.setHeaderText("ต้องการล้างตะกร้าสินค้าทั้งหมด?");
        alert.setContentText("การดำเนินการนี้ไม่สามารถยกเลิกได้");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            currentOrder.clear();
            updateCartDisplay();
            updateOrderSummary();
            updateButtonStates();
        }
    }

    @FXML
    private void checkout() {
        if (currentOrder.isEmpty()) {
            showAlert("ตะกร้าสินค้าว่าง", "ไม่มีรายการสินค้า");
            return;
        }

        // แสดงใบเสร็จ
        Alert receipt = new Alert(Alert.AlertType.INFORMATION);
        receipt.setTitle("ใบเสร็จ");
        receipt.setHeaderText("การชำระเงินสำเร็จ");
        receipt.setContentText(currentOrder.getOrderSummary());
        receipt.getDialogPane().setPrefWidth(400);
        receipt.showAndWait();

        // บันทึกออเดอร์
        dataManager.saveOrder(currentOrder);

        // สร้างออเดอร์ใหม่และล้างข้อมูลสมาชิก
        createNewOrder();
        clearMemberSearch();
        updateButtonStates();
    }

    @FXML
    private void openMemberRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/pizza/view/MemberRegistrationView.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("สมัครสมาชิก");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.WINDOW_MODAL);
            stage.initOwner(registerMemberButton.getScene().getWindow());

            MemberRegistrationController controller = loader.getController();
            controller.setMainController(this);

            stage.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("ไม่สามารถเปิดหน้าสมัครสมาชิกได้", "ข้อผิดพลาด");
        }
    }

    /**
     * อัพเดทประเภทการสั่ง
     */
    private void updateOrderDineIn() {
        currentOrder.setDineIn(dineInCheckBox.isSelected());
        updateOrderSummary();
    }

    // === UI Update Methods ===

    /**
     * อัพเดทการแสดงผลตะกร้า
     */
    private void updateCartDisplay() {
        ObservableList<OrderItem> cartItems = FXCollections.observableArrayList(currentOrder.getOrderItems());
        cartTableView.setItems(cartItems);
    }


    //สรุปออเดอร์
    private void updateOrderSummary() {
        // คำนวณราคาโดยตรงในตัว Controller
        double originalPrice = calculateOriginalPrice();
        double finalPrice = calculateFinalPrice();
        double totalSavings = originalPrice - finalPrice;

        // แสดงราคาสุดท้าย
        totalLabel.setText(String.format("%.2f บาท", finalPrice));

        // แสดงส่วนลดใน discountLabel (ไม่ใช่ในสรุป)
        if (totalSavings > 0) {
            discountLabel.setText(String.format("ลดราคาทั้งหมด: %.2f บาท", totalSavings));
            discountLabel.setVisible(true);
            discountLabel.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
        } else {
            discountLabel.setVisible(false);
        }

        // แสดงโปรโมชั่น
        if (currentOrder.hasFreeWednesdayPizza()) {
            promotionLabel.setText("🍕 ได้พิซซ่าฟรี 1 ถาด! (โปรวันพุธ)");
            promotionLabel.setVisible(true);
        } else {
            promotionLabel.setVisible(false);
        }

        // สรุปรายละเอียด
        orderSummaryArea.setText(generateOrderPreview());

    }

    /**
     * คำนวณราคาเต็มก่อนลด
     */
    private double calculateOriginalPrice() {
        if (currentOrder.isEmpty()) return 0.0;

        double total = 0.0;
        for (OrderItem item : currentOrder.getOrderItems()) {
            total += item.getItem().getPrice() * item.getQuantity();
        }
        return total;
    }

    /**
     * คำนวณราคาสุดท้ายหลังลด
     */
    private double calculateFinalPrice() {
        return currentOrder.getTotalPrice();
    }

    /**
     * สร้างข้อความแสดงตัวอย่างออเดอร์
     */
    private String generateOrderPreview() {
        StringBuilder sb = new StringBuilder();
        double finalPrice = calculateFinalPrice();

        // ข้อมูลออเดอร์
        sb.append("=== ข้อมูลออเดอร์ ===\n");
        sb.append("เลขที่: ").append(currentOrder.getOrderId()).append("\n");
        sb.append("วันที่: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        sb.append("ประเภท: ").append(currentOrder.isDineIn() ? "ทานที่ร้าน" : "ซื้อกลับ").append("\n");

        // ข้อมูลสมาชิก
        if (currentOrder.getMember() != null) {
            Member member = currentOrder.getMember();
            sb.append("สมาชิก: ").append(member.getName()).append(" (").append(member.getMemberId()).append(")\n");
            if (member.isBirthday()) {
                sb.append("🎂 วันเกิด - ได้ส่วนลดพิเศษ 15%!\n");
            } else {
                sb.append("💳 ส่วนลดสมาชิก 10%\n");
            }
        } else {
            sb.append("สมาชิก: ไม่เป็นสมาชิก\n");
        }

        sb.append("\n=== รายการสินค้า ===\n");

        if (currentOrder.isEmpty()) {
            sb.append("(ยังไม่มีรายการ)\n");
        } else {
            double originalTotal = 0;
            double discountAmount = 0.0;
            double freePizza = 128;
            for (OrderItem item : currentOrder.getOrderItems()) {
                double itemTotal = item.getItem().getPrice() * item.getQuantity();
                originalTotal += itemTotal;
                sb.append("• ").append(item.getItem().getName())
                        .append(" x ").append(item.getQuantity())
                        .append(" = ").append(String.format("%.2f บาท", itemTotal)).append("\n");

            }

            if (currentOrder.hasFreeWednesdayPizza()){
                originalTotal-=freePizza;
            }

            sb.append("\n=== สรุปราคา ===\n");
            sb.append("ราคาเต็ม: ").append(String.format("%.2f บาท", originalTotal)).append("\n");

            if (currentOrder.getMember() != null) {

                double discountRate = currentOrder.getMember().isBirthday() ? 0.15 : 0.10;
                discountAmount = originalTotal * discountRate;
                sb.append("ส่วนลด (").append((int)(discountRate * 100)).append("%): -")
                        .append(String.format("%.2f บาท", discountAmount)).append("\n");
            }

            if (currentOrder.hasFreeWednesdayPizza()) {
                sb.append("ส่วนลด พิซซ่าฟรี: -128.00 บาท\n");
            }

            sb.append("ราคาสุทธิ: ").append(String.format("%.2f บาท", finalPrice )).append("\n");

        }

        return sb.toString();
    }

    /**
     * อัพเดทสถานะปุ่ม
     */
    private void updateButtonStates() {
        boolean hasItems = !currentOrder.isEmpty();
        boolean hasSelectedCartItem = cartTableView.getSelectionModel().getSelectedItem() != null;
        boolean hasSelectedItem = itemTableView.getSelectionModel().getSelectedItem() != null;

        addToCartButton.setDisable(!hasSelectedItem);
        removeFromCartButton.setDisable(!hasSelectedCartItem);
        clearCartButton.setDisable(!hasItems);
        checkoutButton.setDisable(!hasItems);
    }

    /**
     * แสดง Alert
     */
    private void showAlert(String message, String title) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // === Selection Handlers ===

    @FXML
    private void handleItemSelection() {
        updateButtonStates();
    }

    @FXML
    private void handleCartSelection() {
        updateButtonStates();
    }

    /**
     * รีเฟรชข้อมูลหลังสมัครสมาชิกใหม่
     */
    public void refreshMemberData() {
        if (!PhoneTextField.getText().trim().isEmpty()) {
            searchMemberByPhone();
        }
    }
}