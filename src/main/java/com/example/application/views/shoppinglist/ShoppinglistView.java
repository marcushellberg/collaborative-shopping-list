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
public class ShoppinglistView extends Div {

    private final VerticalLayout shoppingList = new VerticalLayout() {{
        setPadding(false);
    }};
    private final ShoppingService shoppingService;
    private final UserInfo userInfo;
    private final Map<Integer, ItemForm> forms = new HashMap<>();

    public ShoppinglistView(ShoppingService shoppingService) {
        addClassName("shoppinglist-view");
        this.shoppingService = shoppingService;

        var name = SecurityContextHolder.getContext().getAuthentication().getName();
        userInfo = new UserInfo(name, name);

        var newItemForm = new ItemForm(new ShoppingListItem(), userInfo);
        newItemForm.setSaveHandler(item -> {
            saveItem(item);
            newItemForm.setItem(new ShoppingListItem());
        });

        var messageList = new CollaborationMessageList(userInfo, "chat");
        var messageInput = new CollaborationMessageInput(messageList);

        add(
            getHeader(),
            new HorizontalLayout(
                new VerticalLayout(newItemForm, shoppingList),
                new VerticalLayout(messageList, messageInput)
            )
        );

        setupCollaborationEngine(shoppingService);
    }

    Component getHeader() {
        var header = new HorizontalLayout();
        header.setWidthFull();
        header.setAlignItems(FlexComponent.Alignment.BASELINE);

        var avatars = new CollaborationAvatarGroup(userInfo, "users");
        avatars.getStyle().set("width", "unset"); // Why does not setSizeUndefined work?
        var h1 = new H1("Shopping list");
        header.add(h1, avatars);
        header.expand(h1);
        return header;
    }

    private void setupCollaborationEngine(ShoppingService shoppingService) {
        CollaborationEngine.getInstance().openTopicConnection(this, "list", userInfo, topicConnection -> {
            var items = topicConnection.getNamedMap("items");

            items.subscribe(e -> {
                if (e.getOldValue(ShoppingListItem.class) == null) { // new
                    addItem(e.getValue(ShoppingListItem.class));
                } else {
                    if (e.getValue(ShoppingListItem.class) == null) { // deleted
                        deleteItem(e.getOldValue(ShoppingListItem.class));
                    } else { // updated
                        var updated = e.getValue(ShoppingListItem.class);
                        forms.get(updated.getId()).setItem(updated);
                    }
                }
            });

            // Init the map if it is empty
            if (items.getKeys().count() == 0) {
                shoppingService.getShoppingList().forEach(this::addItem);
            }

            return null;
        });
    }

    void addItem(ShoppingListItem item) {
        var form = new ItemForm(item, userInfo);
        form.setSaveHandler(this::saveItem);

        if (item.getId() != null) { // Only for saved items
            form.setDeleteHandler(this::deleteItem);
        }
        forms.put(item.getId(), form);
        shoppingList.add(form);
    }

    void saveItem(ShoppingListItem updated) {
        try {
            var saved = shoppingService.saveItem(updated);

            CollaborationEngine.getInstance().openTopicConnection(this, "list", userInfo, topicConnection -> {
                var items = topicConnection.getNamedMap("items");
                items.put(saved.getId().toString(), saved);

                return null;
            });

        } catch (ObjectOptimisticLockingFailureException e) {
            showSaveError();
        }
    }

    void deleteItem(ShoppingListItem item) {
        try {
            shoppingService.deleteItem(item);
            shoppingList.remove(forms.get(item.getId()));
            forms.remove(item.getId());

            // Update the shared
            CollaborationEngine.getInstance().openTopicConnection(this, "list", userInfo, topicConnection -> {
                var items = topicConnection.getNamedMap("items");
                items.put(item.getId().toString(), null); // no delete API?

                return null;
            });

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
