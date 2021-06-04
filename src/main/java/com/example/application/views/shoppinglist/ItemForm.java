package com.example.application.views.shoppinglist;

import com.example.application.data.entity.Category;
import com.example.application.data.entity.ShoppingListItem;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.IntegerField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;

import java.util.List;

public class ItemForm extends HorizontalLayout {

    private final IntegerField amount = new IntegerField("Amount");
    private final TextField name = new TextField("Item");
    private final ComboBox<Category> category = new ComboBox<>("Category");
    private final Binder<ShoppingListItem> binder = new Binder<>(ShoppingListItem.class);
    private final Button button = new Button("Add");
    private ShoppingListItem item;

    @FunctionalInterface
    public interface SaveHandler {
        void itemSaved(ShoppingListItem item);
    }

    public ItemForm(ShoppingListItem item, List<Category> categories) {
        setAlignItems(Alignment.BASELINE);
        category.setItems(categories);
        category.setItemLabelGenerator(Category::getName);
        add(amount, name, category, button);
        binder.bindInstanceFields(this);
        setItem(item);
    }

    public void setSaveHandler(SaveHandler saveHandler){
        button.addClickListener(click -> {
            if(binder.writeBeanIfValid(item)){
                saveHandler.itemSaved(item);
            }
        });
    }

    public void setItem(ShoppingListItem item) {
        this.item = item;
        binder.readBean(item);
        if(item.getId() != null) {
            button.setText("Update");
        }
    }
}
