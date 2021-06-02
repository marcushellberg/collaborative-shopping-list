package com.example.application.views.shoppinglist;

import com.vaadin.flow.component.Text;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.PageTitle;

@Route(value = "")
@PageTitle("Shopping list")
public class ShoppinglistView extends Div {

    public ShoppinglistView() {
        addClassName("shoppinglist-view");
        add(new Text("Content placeholder"));
    }

}
