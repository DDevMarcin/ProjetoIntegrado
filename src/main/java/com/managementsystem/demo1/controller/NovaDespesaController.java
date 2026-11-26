
package com.managementsystem.demo1.controller;

import com.managementsystem.demo1.model.CategoriaDespesa;
import com.managementsystem.demo1.model.SituacaoMovimentacaoFinanceira;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class NovaDespesaController {

    @FXML private TextField txtDesc;
    @FXML private TextField txtValor;
    @FXML private ComboBox<CategoriaDespesa> cbCategoria;
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

    public void carregarCategorias(List<CategoriaDespesa> categorias) {
        cbCategoria.setItems(
                FXCollections.observableArrayList(categorias)
        );

        cbCategoria.setConverter(new StringConverter<>() {
            @Override
            public String toString(CategoriaDespesa categoria) {
                return categoria != null ? categoria.getNome() : "";
            }

            @Override
            public CategoriaDespesa fromString(String texto) {
                return null;
            }
        });

        if (!cbCategoria.getItems().isEmpty()) {
            cbCategoria.getSelectionModel().selectFirst();
        }
    }

    public String getDescricao() {
        return txtDesc.getText();
    }

    public String getValor() {
        return txtValor.getText();
    }

    public CategoriaDespesa getCategoria() {
        return cbCategoria.getValue();
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
