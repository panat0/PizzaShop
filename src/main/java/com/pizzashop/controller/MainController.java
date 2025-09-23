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

    @FXML private ComboBox<Member> memberComboBox;
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

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // เริ่มต้น DataManager ก่อน
        dataManager = DataManager.getInstance();

        // สร้าง Order ใหม่ก่อน (ป้องกัน NullPointerException)
        createNewOrder();

        // จากนั้นค่อยตั้งค่าส่วนอื่นๆ
        setupTableViews();
        setupControls();  // ตอนนี้ currentOrder ไม่เป็น null แล้ว
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

        // Member combo box
        loadMembers();
        memberComboBox.setOnAction(e -> updateOrderMember());

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

    /**
     * โหลดข้อมูลทั้งหมด
     */
    private void loadData() {
        allItems = FXCollections.observableArrayList(dataManager.getAllItems());
        filteredItems = FXCollections.observableArrayList(allItems);
        itemTableView.setItems(filteredItems);
    }

    /**
     * โหลดสมาชิก
     */
    private void loadMembers() {
        memberComboBox.getItems().clear();
        memberComboBox.getItems().add(null); // ไม่เป็นสมาชิก
        memberComboBox.getItems().addAll(dataManager.getActiveMembers());

        // Custom string converter
        memberComboBox.setConverter(new javafx.util.StringConverter<Member>() {
            @Override
            public String toString(Member member) {
                return member == null ? "ไม่เป็นสมาชิก" : member.toString();
            }

            @Override
            public Member fromString(String string) {
                return null; // ไม่ใช้
            }
        });

        memberComboBox.setValue(null);
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

        // สร้างออเดอร์ใหม่
        createNewOrder();
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

            // รีโหลดสมาชิกหลังปิดหน้าต่าง
            loadMembers();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("ไม่สามารถเปิดหน้าสมัครสมาชิกได้", "ข้อผิดพลาด");
        }
    }

    /**
     * อัพเดทสมาชิกในออเดอร์
     */
    private void updateOrderMember() {
        Member selectedMember = memberComboBox.getValue();
        currentOrder.setMember(selectedMember);
        updateOrderSummary();
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

    /**
     * อัพเดทสรุปออเดอร์
     */
    private void updateOrderSummary() {
        // ราคารวม
        totalLabel.setText(String.format("%.2f บาท", currentOrder.getTotalPrice()));

        // ส่วนลด
        double savings = currentOrder.getTotalSavings();
        if (savings > 0) {
            discountLabel.setText(String.format("ประหยัดได้: %.2f บาท", savings));
            discountLabel.setVisible(true);
        } else {
            discountLabel.setVisible(false);
        }

        // โปรโมชั่น
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
     * สร้างข้อความแสดงตัวอย่างออเดอร์
     */
    private String generateOrderPreview() {
        StringBuilder sb = new StringBuilder();

        // ข้อมูลออเดอร์
        sb.append("=== ข้อมูลออเดอร์ ===\n");
        sb.append("เลขที่: ").append(currentOrder.getOrderId()).append("\n");
        sb.append("วันที่: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        sb.append("ประเภท: ").append(currentOrder.isDineIn() ? "ทานที่ร้าน" : "ซื้อกลับ").append("\n");

        // ข้อมูลสมาชิก
        if (currentOrder.getMember() != null) {
            Member member = currentOrder.getMember();
            sb.append("สมาชิก: ").append(member.getName()).append("\n");
            if (member.isBirthday()) {
                sb.append("🎂 วันเกิด - ได้ส่วนลดพิเศษ 15%!\n");
            }
        } else {
            sb.append("สมาชิก: ไม่เป็นสมาชิก\n");
        }

        sb.append("\n=== รายการสินค้า ===\n");

        if (currentOrder.isEmpty()) {
            sb.append("(ยังไม่มีรายการ)\n");
        } else {
            for (OrderItem item : currentOrder.getOrderItems()) {
                sb.append("• ").append(item.toString()).append("\n");
            }

            if (currentOrder.hasFreeWednesdayPizza()) {
                sb.append("• พิซซ่าฟรี x 1 (โปรวันพุธ)\n");
            }
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
        loadMembers();
    }
}