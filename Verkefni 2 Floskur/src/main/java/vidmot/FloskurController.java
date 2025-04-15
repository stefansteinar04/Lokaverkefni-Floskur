package vidmot;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import vinnsla.Floskur;
import java.net.URL;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class FloskurController implements Initializable {
    private static final String RANGT = "rangt-inntak";

    @FXML private TextField fxFloskur;
    @FXML private TextField fxDosir;
    @FXML private Label fxISKFloskur;
    @FXML private Label fxISKDosir;
    @FXML private Label fxSamtalsFjoldi;
    @FXML private Label fxSamtalsVirdi;
    @FXML private Label fxHeildFjoldi;
    @FXML private Label fxHeildVirdi;
    @FXML private Button fxLanguageToggle;
    @FXML private ChoiceBox<String> fxBottleType;
    @FXML private Label fxCansLabel;
    @FXML private Label fxBottlesLabel;
    @FXML private Label fxTotalLabel;
    @FXML private Button fxPayButton;
    @FXML private Button fxClearButton;
    @FXML private Label fxTotalCountLabel;
    @FXML private Label fxTotalValueLabel;

    private final Floskur floskur = new Floskur();
    private ResourceBundle messages;
    private boolean isIcelandic = true;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        messages = ResourceBundle.getBundle("messages", new Locale("is"));
        updateBottleTypes();
        fxBottleType.setValue(messages.getString("bottle.type.plastic"));
        updateLanguage();

        // Bæta við listener á fxDosir og fxFloskur til að uppfæra í rauntíma
        fxDosir.textProperty().addListener((obs, oldValue, newValue) -> updateSamtals());
        fxFloskur.textProperty().addListener((obs, oldValue, newValue) -> updateSamtals());
        fxBottleType.getSelectionModel().selectedItemProperty().addListener((obs, oldValue, newValue) -> updateSamtals());
    }

    @FXML
    protected void onToggleLanguage(ActionEvent event) {
        isIcelandic = !isIcelandic;
        messages = ResourceBundle.getBundle("messages", isIcelandic ? new Locale("is") : new Locale("en"));
        fxLanguageToggle.setText(isIcelandic ? "EN" : "IS");
        updateLanguage();
        updateBottleTypes();
    }

    private void updateLanguage() {
        fxDosir.setPromptText(messages.getString("enter.number.prompt"));
        fxFloskur.setPromptText(messages.getString("enter.number.prompt"));
        fxCansLabel.setText(messages.getString("cans.label"));
        fxBottlesLabel.setText(messages.getString("bottles.label"));
        fxTotalLabel.setText(messages.getString("total.label"));
        fxPayButton.setText(messages.getString("pay.button"));
        fxClearButton.setText(messages.getString("clear.button"));
        fxTotalCountLabel.setText(messages.getString("total.count.label"));
        fxTotalValueLabel.setText(messages.getString("total.value.label"));
        updateSamtals();
    }

    private void updateBottleTypes() {
        String selected = fxBottleType.getValue();
        fxBottleType.getItems().clear();
        fxBottleType.getItems().addAll(
                messages.getString("bottle.type.plastic"),
                messages.getString("bottle.type.glass")
        );
        if (selected != null) {
            String newSelected = selected.equals("Plastic") || selected.equals(messages.getString("bottle.type.plastic"))
                    ? messages.getString("bottle.type.plastic")
                    : messages.getString("bottle.type.glass");
            fxBottleType.setValue(newSelected);
        }
    }

    @FXML
    protected void onHreinsa(ActionEvent actionEvent) {
        floskur.hreinsa();
        fxDosir.setText("");
        eydaRanga(fxDosir.getStyleClass());
        fxFloskur.setText("");
        eydaRanga(fxFloskur.getStyleClass());
        updateSamtals();
    }

    @FXML
    protected void onGreida(ActionEvent actionEvent) {
        updateSamtals();
        floskur.greida();
        Platform.runLater(() -> {
            fxHeildFjoldi.setText(floskur.getHeildFjoldi() + "");
            fxHeildVirdi.setText(floskur.getHeildVirdi() + "");
        });
        onHreinsa(actionEvent);
    }

    public void onStafur(KeyEvent keyEvent) {
        TextField textField = ((TextField) keyEvent.getSource());
        List<String> styleClasses = textField.getStyleClass();
        keyEydaRanga(keyEvent, styleClasses);
    }

    private static void keyEydaRanga(KeyEvent keyEvent, List<String> styleClasses) {
        if (styleClasses == null || styleClasses.isEmpty()) {
            return;
        }
        if (keyEvent.getCode() != KeyCode.ENTER) {
            eydaRanga(styleClasses);
        }
    }

    private static void eydaRanga(List<String> styleClasses) {
        styleClasses.remove(RANGT);
    }

    private void updateSamtals() {
        try {
            // Lesa fjölda dósa
            int dosir = fxDosir.getText().isEmpty() ? 0 : Integer.parseInt(fxDosir.getText());
            // Lesa fjölda flöskna
            int floskurCount = fxFloskur.getText().isEmpty() ? 0 : Integer.parseInt(fxFloskur.getText());

            // Athuga hvort inntak sé neikvætt
            if (dosir < 0) {
                fxDosir.getStyleClass().add(RANGT);
                dosir = 0;
            } else {
                eydaRanga(fxDosir.getStyleClass());
            }
            if (floskurCount < 0) {
                fxFloskur.getStyleClass().add(RANGT);
                floskurCount = 0;
            } else {
                eydaRanga(fxFloskur.getStyleClass());
            }

            // Map the localized string back to the model’s expected value
            String bottleType = fxBottleType.getValue().equals(messages.getString("bottle.type.plastic")) ? "Plastic" : "Glass";

            // Uppfæra fjölda og virði
            floskur.setDosir(dosir);
            floskur.setFjoldiFloskur(floskurCount, bottleType);

            // Uppfæra UI
            fxISKDosir.setText(floskur.getISKDosir() + "");
            fxISKFloskur.setText(floskur.getISKFloskur() + "");
            fxSamtalsFjoldi.setText(floskur.getSamtalsFjoldi() + "");
            fxSamtalsVirdi.setText(floskur.getSamtalsVirdi() + "");
            fxHeildFjoldi.setText(floskur.getHeildFjoldi() + "");
            fxHeildVirdi.setText(floskur.getHeildVirdi() + "");
        } catch (NumberFormatException e) {
            // Ef inntak er ógilt, setja núll sem sjálfgefið
            fxSamtalsFjoldi.setText("0");
            fxSamtalsVirdi.setText("0");
            fxISKDosir.setText("0");
            fxISKFloskur.setText("0");
            if (!fxDosir.getText().isEmpty()) {
                fxDosir.getStyleClass().add(RANGT);
            }
            if (!fxFloskur.getText().isEmpty()) {
                fxFloskur.getStyleClass().add(RANGT);
            }
        }
    }
}