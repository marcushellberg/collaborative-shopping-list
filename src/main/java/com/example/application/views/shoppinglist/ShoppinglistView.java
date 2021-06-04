package com.example.application.views.shoppinglist;

import com.example.application.data.entity.Category;
import com.example.application.data.entity.ShoppingListItem;
import com.example.application.data.service.ShoppingService;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;

@Route(value = "")
@PageTitle("Shopping list")
public class ShoppinglistView extends Div {

  private final VerticalLayout shoppingList = new VerticalLayout();
  private ShoppingService shoppingService;
  private final List<Category> categories;

  public ShoppinglistView(ShoppingService shoppingService) {
    addClassName("shoppinglist-view");
    this.shoppingService = shoppingService;
    categories = shoppingService.findAllCategories();

    var newItemForm = new ItemForm(new ShoppingListItem(), categories);
    newItemForm.setSaveHandler(item -> {
      shoppingService.saveItem(item);
      newItemForm.setItem(new ShoppingListItem());
      updateList();
    });

    add(
        new H1("Shopping list"),
        newItemForm,
        shoppingList
    );

    updateList();
  }


  void updateList(){
    shoppingList.removeAll();

    shoppingService.getShoppingList().forEach((category, items) -> {
      shoppingList.add(new H2(category.getName()));
      items.forEach(item -> {
        var form = new ItemForm(item, categories);
        form.setSaveHandler(updated -> {
          saveItem(updated);
          updateList();
        });
        shoppingList.add(form);
      });
    });
  }

  void saveItem(ShoppingListItem updated){
    try {
      shoppingService.saveItem(updated);
    } catch (ObjectOptimisticLockingFailureException e){
      var notification = new Notification("Save conflict. Please try again.");
      notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
      notification.setPosition(Notification.Position.MIDDLE);
      notification.open();
    }
  }
}
