
package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@Scope("prototype")
public class NovaReceitaController {

    @FXML private TextField txtDesc;
    @FXML private TextField txtValor;
    @FXML private TextField txtCategoria;
    @FXML private DatePicker dpData;
    @FXML private ComboBox<SituacaoMovimentacaoFinanceira> cbSituacao;
    @FXML private TextField txtObs;

    @FXML
    public void initialize() {
        dpData.setValue(LocalDate.now());

        cbSituacao.setItems(FXCollections.observableArrayList(
                SituacaoMovimentacaoFinanceira.values()
        ));

        cbSituacao.setValue(SituacaoMovimentacaoFinanceira.PAGO);
    }

    public String getDescricao() {
        return txtDesc.getText();
    }

    public String getValor() {
        return txtValor.getText();
    }

    public String getCategoria() {
        return txtCategoria.getText();
    }

    public SituacaoMovimentacaoFinanceira getSituacao() {
        return cbSituacao.getValue();
    }

    public LocalDate getData() {
        return dpData.getValue();
    }

    public String getObservacoes() {
        return txtObs.getText();
    }
}
