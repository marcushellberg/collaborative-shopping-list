package com.example.application.views.shoppinglist;

import com.example.application.data.entity.ShoppingListItem;
import com.example.application.data.service.ShoppingService;
import com.vaadin.collaborationengine.*;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Route(value = "")
@PageTitle("Shopping list")
public class ShoppinglistView extends VerticalLayout {

    private final VerticalLayout shoppingList = new VerticalLayout();
    private final ShoppingService shoppingService;
    private final Map<Integer, ItemForm> forms = new HashMap<>();

    public ShoppinglistView(ShoppingService shoppingService) {
        addClassName("shoppinglist-view");
        this.shoppingService = shoppingService;

        var content = new HorizontalLayout(getShoppingListLayout());
        content.setSizeFull();

        setWidth(null);
        setHeightFull();
        add(getHeader(), content);
        expand(content);

        shoppingService.getShoppingList().forEach(this::addItem);
    }

    private Component getHeader() {
        var header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.BASELINE);

        var h1 = new H1("Shopping list");
        header.add(h1);
        header.expand(h1);
        return header;
    }

    private Component getShoppingListLayout() {
        var newItemForm = new ItemForm(new ShoppingListItem());
        newItemForm.setSaveHandler(item -> {
            saveItem(item);
            newItemForm.setItem(new ShoppingListItem());
        });
        newItemForm.addClassName("spacing-b-xl");

        var shoppingListLayout = new VerticalLayout(
            newItemForm,
            shoppingList
        );
        shoppingList.setPadding(false);
        shoppingListLayout.setHeightFull();
        return shoppingListLayout;
    }

    void addItem(ShoppingListItem item) {
        var form = new ItemForm(item);
        form.setSaveHandler(this::saveItem);

        if (item.getId() != null) {
            form.setDeleteHandler(this::deleteItem);
        }
        forms.put(item.getId(), form);
        shoppingList.add(form);
    }

    void saveItem(ShoppingListItem updated) {
        try {
            var newItem = updated.getId() == null;
            var saved = shoppingService.saveItem(updated);

            if (newItem) {
                addItem(saved);
            } else {
                forms.get(saved.getId()).setItem(saved);
            }
        } catch (ObjectOptimisticLockingFailureException e) {
            showSaveError();
        }
    }

    void deleteItem(ShoppingListItem item) {
        try {
            shoppingService.deleteItem(item);
            shoppingList.remove(forms.get(item.getId()));
            forms.remove(item.getId());
        } catch (ObjectOptimisticLockingFailureException e) {
            showSaveError();
        }
    }

    private void showSaveError() {
        var notification = new Notification("Save conflict. Please try again.");
        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
        notification.setPosition(Notification.Position.MIDDLE);
        notification.open();
    }

}
